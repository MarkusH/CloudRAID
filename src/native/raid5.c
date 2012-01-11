/*
 * Copyright 2011 by the CloudRAID Team, see AUTHORS for more details.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

#include "raid5.h"
#include "sha256.h"
#include "rc4.h"
#include "de_dhbw_mannheim_cloudraid_jni_RaidAccessInterface.h"

#include <stdlib.h>
#include <string.h>

#define SUCCESS_MERGE 0x02
#define SUCCESS_SPLIT 0x04

#define MEMERR_DEV 0x10
#define MEMERR_SHA 0x16
#define MEMERR_BUF 0x17

#define READERR_DEV0 0x20
#define READERR_DEV1 0x21
#define READERR_DEV2 0x22
#define READERR_IN   0x29

#define OPENERR_DEV0 0x30
#define OPENERR_DEV1 0x31
#define OPENERR_DEV2 0x32
#define OPENERR_OUT  0x38
#define OPENERR_IN   0x39

#define METADATAERR 0x40

void merge_byte_block ( const unsigned char *in, const size_t in_len[], unsigned char *out, size_t *out_len )
{
    int i;
    if ( in_len[0] > 0 && in_len[2] == in_len[0] )
    {
        for ( i = 0; i < in_len[1]; i++ )
        {
            if ( ( in[i] ^ in[RAID5_BLOCKSIZE + i] ) != in[2 * RAID5_BLOCKSIZE + i] ) /* Parity does not match */
            {
                printf ( "[WARNING] Parity does not match!\n" );
            }
        }
        for ( i = in_len[1]; i < in_len[0]; i++ )
        {
            if ( ( in[i] ^ in[2 * RAID5_BLOCKSIZE + i] ) != 0xFF ) /* Parity does not match */
            {
                printf ( "[WARNING] Parity does not match!\n" );
            }
        }
        memcpy ( &out[0], &in[0], in_len[0] ); /* Copy the first part of the read bytes */
        *out_len = in_len[0];
        if ( in_len[1] > 0 )
        {
            memcpy ( &out[RAID5_BLOCKSIZE], &in[RAID5_BLOCKSIZE], in_len[1] ); /* Copy the second part of the read bytes */
            *out_len += in_len[1];
        }
    }
    else
    {
        *out_len = -1;
    }
}

void split_byte_block ( const unsigned char *in, const size_t in_len, unsigned char *out, size_t out_len[] )
{
    int i, partial;
    if ( in_len > RAID5_BLOCKSIZE )
    {
        partial = in_len - RAID5_BLOCKSIZE; /* in case of in in_len == 2 * RAID5_BLOCKSIZE, partial == RAID5_BLOCKSIZE */
        memcpy ( &out[0], &in[0], RAID5_BLOCKSIZE ); /* Copy the first part of the read bytes */
        memcpy ( &out[RAID5_BLOCKSIZE], &in[RAID5_BLOCKSIZE], partial ); /* Copy the second part of the read bytes */
        for ( i = 0; i < partial; i++ )
        {
            out[2 * RAID5_BLOCKSIZE + i] = out[i] ^ out[RAID5_BLOCKSIZE + i]; /* Bytewise calculation of the parity */
        }
        for ( i = partial; i < RAID5_BLOCKSIZE; i++ ) /* no effect for in_len == 2 * RAID5_BLOCKSIZE */
        {
            out[2 * RAID5_BLOCKSIZE + i] = ~out[i]; /* Parity of the overflowing bytes */
        }
        out_len[0] = RAID5_BLOCKSIZE;
        out_len[1] = partial;
        out_len[2] = RAID5_BLOCKSIZE;
    }
    else
    {
        memcpy ( &out[0], &in[0], in_len ); /* Copy the first part of the read bytes */
        for ( i = 0; i < in_len; i++ )
        {
            out[2 * RAID5_BLOCKSIZE + i] = ~out[i]; /* Parity of the overflowing bytes */
        }
        out_len[0] = in_len;
        out_len[1] = 0;
        out_len[2] = in_len;
    }
}

int split_byte ( FILE *in, FILE *devices[], FILE *meta, rc4_key *key )
{
    unsigned char *chars, *out, parity_pos = 2, *hash;
    size_t rlen, *out_len;
    int status;

    /* sha context
       the last element [3] is for the input file */
    struct sha256_ctx sha256_ctx[4];
    size_t sha256_len[4];
    char *sha256_buf[4];
    void *sha256_resblock[4];
    int i;

    raid5md metadata;
    metadata.version = RAID5_METADATA_VERSION;

    chars = ( unsigned char* ) malloc ( sizeof ( unsigned char ) * 2 * RAID5_BLOCKSIZE );
    out = ( unsigned char* ) malloc ( sizeof ( unsigned char ) * RAID5_BLOCKSIZE * 3 );
    out_len = ( size_t* ) malloc ( sizeof ( size_t ) * 3 );
    if ( chars == NULL )
    {
        status = MEMERR_BUF;
        goto end;
    }
    if ( out == NULL )
    {
        status = MEMERR_DEV;
        goto end;
    }
    if ( out_len == NULL )
    {
        status = MEMERR_DEV;
        goto end;
    }

    /* create the sha256 context */
    for ( i = 0; i < 4; i++ )
    {
        sha256_resblock[i] = malloc ( 32 );
        if ( sha256_resblock[i] == NULL )
        {
            status = MEMERR_SHA;
            goto end;
        }

        sha256_buf[i] = ( char* ) malloc ( SHA256_BLOCKSIZE + 72 );
        if ( sha256_buf[i] == NULL )
        {
            status = MEMERR_SHA;
            goto end;
        }
        sha256_init_ctx ( &sha256_ctx[i] );
        sha256_len[i] = 0;
    }

    rlen = fread ( chars, sizeof ( unsigned char ), 2 * RAID5_BLOCKSIZE, in );
    while ( rlen > 0 )
    {
#ifdef ENCRYPT_DATA
        /* encrypt the input file */
        rc4 ( chars, rlen, key );
#endif
        if ( sha256_len[3] == SHA256_BLOCKSIZE )
        {
            sha256_process_block ( sha256_buf[3], SHA256_BLOCKSIZE, &sha256_ctx[3] );
            sha256_len[3] = 0;
        }
        if ( sha256_len[3] < SHA256_BLOCKSIZE )
        {
            memcpy ( sha256_buf[3] + sha256_len[3], chars, rlen );
            sha256_len[3] += rlen;
        }
        split_byte_block ( chars, rlen, out, out_len );
        fwrite ( &out[0], sizeof ( unsigned char ), out_len[0], devices[ ( parity_pos + 1 ) % 3] );
        if ( out_len[1] > 0 )
        {
            fwrite ( &out[RAID5_BLOCKSIZE], sizeof ( unsigned char ), out_len[1], devices[ ( parity_pos + 2 ) % 3] );
        }
        fwrite ( &out[2 * RAID5_BLOCKSIZE], sizeof ( unsigned char ), out_len[2], devices[parity_pos] );

        for ( i = 0; i < 3; i++ )
        {
            if ( sha256_len[ ( parity_pos + 1 + i ) % 3] == SHA256_BLOCKSIZE )
            {
                sha256_process_block ( sha256_buf[ ( parity_pos + 1 + i ) % 3], SHA256_BLOCKSIZE, &sha256_ctx[ ( parity_pos + 1 + i ) % 3] );
                sha256_len[ ( parity_pos + 1 + i ) % 3] = 0;
            }
            if ( sha256_len[ ( parity_pos + 1 + i ) % 3] < SHA256_BLOCKSIZE )
            {
                memcpy ( sha256_buf[ ( parity_pos + 1 + i ) % 3] + sha256_len[ ( parity_pos + 1 + i ) % 3], &out[i * RAID5_BLOCKSIZE], out_len[ ( parity_pos + 1 + i ) % 3] );
                sha256_len[ ( parity_pos + 1 + i ) % 3] += out_len[ ( parity_pos + 1 + i ) % 3];
            }
        }

        parity_pos = ( parity_pos + 1 ) % 3;
        rlen = fread ( chars, sizeof ( char ), 2 * RAID5_BLOCKSIZE, in );
    }
    if ( ferror ( in ) )
    {
        status = READERR_IN;
        goto end;
    }

    hash = ( unsigned char* ) malloc ( 65 );
    for ( i = 0; i < 4; i++ )
    {
        if ( sha256_len[i] == SHA256_BLOCKSIZE )
        {
            sha256_process_block ( sha256_buf[i], SHA256_BLOCKSIZE, &sha256_ctx[i] );
        }
        else
        {
            if ( sha256_len[i] > 0 )
            {
                sha256_process_bytes ( sha256_buf[i], sha256_len[i], &sha256_ctx[i] );
            }
        }
        sha256_finish_ctx ( &sha256_ctx[i], sha256_resblock[i] );
        ascii_from_resbuf ( hash, sha256_resblock[i] );
        set_metadata_hash ( &metadata, i, hash );
    }
    status = write_metadata ( meta, &metadata );
    if ( status != 0 )
    {
        status = METADATAERR;
        goto end;
    }

    status = SUCCESS_SPLIT;

end:

    if ( hash )
    {
        free ( hash );
    }

    for ( i = 0; i < 4; i++ )
    {
        if ( sha256_buf[i] )
        {
            free ( sha256_buf[i] );
        }
        if ( sha256_resblock[i] )
        {
            free ( sha256_resblock[i] );
        }
    }
    if ( out_len )
    {
        free ( out_len );
    }
    if ( out )
    {
        free ( out );
    }
    if ( chars )
    {
        free ( chars );
    }
    return status;
}

int merge_byte ( FILE *out, FILE *devices[], FILE *meta, rc4_key *key )
{
    unsigned char *in, *buf, parity_pos = 2;
    size_t *in_len, out_len;
    int status;

    raid5md metadata;
    status = read_metadata ( meta, &metadata );

    in = ( unsigned char* ) malloc ( sizeof ( unsigned char ) * RAID5_BLOCKSIZE * 3 );
    in_len = ( size_t* ) malloc ( sizeof ( size_t ) * 3 );
    buf = ( unsigned char* ) malloc ( sizeof ( unsigned char ) * 2 * RAID5_BLOCKSIZE );
    if ( in == NULL )
    {
        status = MEMERR_DEV;
        goto end;
    }
    if ( in_len == NULL )
    {
        status = MEMERR_DEV;
        goto end;
    }
    if ( buf == NULL )
    {
        status = MEMERR_BUF;
        goto end;
    }

    in_len[0] = fread ( &in[0], sizeof ( char ), RAID5_BLOCKSIZE, devices[ ( parity_pos + 1 ) % 3] );
    in_len[1] = fread ( &in[RAID5_BLOCKSIZE], sizeof ( char ), RAID5_BLOCKSIZE, devices[ ( parity_pos + 2 ) % 3] );
    in_len[2] = fread ( &in[2 * RAID5_BLOCKSIZE], sizeof ( char ), RAID5_BLOCKSIZE, devices[parity_pos] );

    while ( in_len[0] > 0 || in_len[1] > 0 || in_len[2] > 0 )
    {
        merge_byte_block ( in, in_len, buf, &out_len );
        if ( out_len == -1 )
        {
            status = READERR_IN;
            goto end;
        }
#ifdef ENCRYPT_DATA
        /* encrypt the input file */
        rc4 ( buf, out_len, key );
#endif

        fwrite ( buf, sizeof ( unsigned char ), out_len, out );

        parity_pos = ( parity_pos + 1 ) % 3;
        in_len[0] = fread ( &in[0], sizeof ( char ), RAID5_BLOCKSIZE, devices[ ( parity_pos + 1 ) % 3] );
        in_len[1] = fread ( &in[RAID5_BLOCKSIZE], sizeof ( char ), RAID5_BLOCKSIZE, devices[ ( parity_pos + 2 ) % 3] );
        in_len[2] = fread ( &in[2 * RAID5_BLOCKSIZE], sizeof ( char ), RAID5_BLOCKSIZE, devices[parity_pos] );
    }
    if ( ferror ( devices[0] ) )
    {
        status = READERR_DEV0;
        goto end;
    }
    if ( ferror ( devices[1] ) )
    {
        status = READERR_DEV1;
        goto end;
    }
    if ( ferror ( devices[2] ) )
    {
        status = READERR_DEV2;
        goto end;
    }
    status = SUCCESS_MERGE;

end:
    if ( buf )
    {
        free ( buf );
    }
    if ( in_len )
    {
        free ( in_len );
    }
    if ( in )
    {
        free ( in );
    }
    return status;
}

void print_metadata ( raid5md *md )
{
    if ( md )
    {
        printf ( "\n\nVersion: %02x\n", md->version );
        printf ( "I: %64s\n", md->hash_in );
        printf ( "0: %64s\n", md->hash_dev0 );
        printf ( "1: %64s\n", md->hash_dev1 );
        printf ( "2: %64s\n\n", md->hash_dev2 );
    }
    else
    {
        printf ( "\nNo metadata given!\n" );
    }
}

int read_metadata ( FILE *fp, raid5md *md )
{
    if ( fp )
    {
        if ( md )
        {
            fscanf ( fp, "%2hhu", & ( md->version ) );
            fscanf ( fp, "%64s", md->hash_in );
            fscanf ( fp, "%64s", md->hash_dev0 );
            fscanf ( fp, "%64s", md->hash_dev1 );
            fscanf ( fp, "%64s", md->hash_dev2 );
            return 0;
        }
        return 2;
    }
    return 1;
}

void set_metadata_hash ( raid5md *md, const int idx, const unsigned char hash[65] )
{
    switch ( idx )
    {
    case 0:
        memcpy ( md->hash_in, hash, 65 );
        break;
    case 1:
        memcpy ( md->hash_dev0, hash, 65 );
        break;
    case 2:
        memcpy ( md->hash_dev1, hash, 65 );
        break;
    case 3:
        memcpy ( md->hash_dev2, hash, 65 );
        break;
    }
}

int write_metadata ( FILE *fp, raid5md *md )
{
    if ( fp )
    {
        if ( md )
        {
            fprintf ( fp, "%02x", md->version );
            fprintf ( fp, "%64s", md->hash_in );
            fprintf ( fp, "%64s", md->hash_dev0 );
            fprintf ( fp, "%64s", md->hash_dev1 );
            fprintf ( fp, "%64s", md->hash_dev2 );
            return 0;
        }
        return 2;
    }
    return 1;
}

/**
 * Implements the mergeInterface method defined in the Java RaidAccessInterface
 * class.
 */
JNIEXPORT jint JNICALL Java_de_dhbw_mannheim_cloudraid_jni_RaidAccessInterface_mergeInterface
( JNIEnv * env, jclass cls, jstring _tempInputDirPath, jstring _hash, jstring _outputFilePath, jstring _key, jint _keyLength )
{
    /* Convert the Java Strings to char arrays for usage in the C program. */
    const char *tempInputDirPath = ( *env )->GetStringUTFChars ( env, _tempInputDirPath, 0 );
    const char *hash = ( *env )->GetStringUTFChars ( env, _hash, 0 );
    const char *outputFilePath = ( *env )->GetStringUTFChars ( env, _outputFilePath, 0 );
    const char *key = ( *env )->GetStringUTFChars ( env, _key, 0 );
    const int keyLength = _keyLength;

    const int tmpLength = strlen ( ( char * ) tempInputDirPath );
    int status, i;
    char *inputBaseName;
    rc4_key rc4key;

    /* Generate file pointers. */
    FILE *fp;
    FILE *devices[3];
    FILE *meta;

    /* construct base output path:
     *  - tmpfolder: tmpLength bytes, including ending slash /
     *  - hash:      64 bytes
     *  - extension: 2 bytes for .i
     *  - \0:        1 byte
     */
    inputBaseName = ( char* ) malloc ( tmpLength + 64 + 2 + 1 );
    if ( inputBaseName == NULL )
    {
        status = OPENERR_IN;
        goto end;
    }
    memcpy ( inputBaseName, tempInputDirPath, tmpLength );
    memcpy ( &inputBaseName[tmpLength], hash, 64 );

    /* open the files */
    for ( i = 0; i < 3; i++ )
    {
        sprintf ( &inputBaseName[ tmpLength + 64 ], ".%c", i+0x30 );
        devices[i] = fopen ( inputBaseName, "rb" );
    }
    if ( devices[0] == NULL )
    {
        status = OPENERR_DEV0;
        goto end;
    }
    if ( devices[1] == NULL )
    {
        status = OPENERR_DEV1;
        goto end;
    }
    if ( devices[2] == NULL )
    {
        status = OPENERR_DEV2;
        goto end;
    }

    sprintf ( &inputBaseName[ tmpLength + 64 ], ".m" );
    meta = fopen ( inputBaseName, "rb" );
    if ( meta == NULL )
    {
        status = METADATAERR;
        goto end;
    }

    fp = fopen ( outputFilePath, "wb" );
    if ( fp == NULL )
    {
        status = OPENERR_OUT;
        goto end;
    }

    /* construct the RC4 key */
    prepare_key ( ( unsigned char* ) key, keyLength, &rc4key );

    /* Invoke the native merge method. */
    status = merge_byte ( fp, devices, meta, &rc4key );

end:
    /* Close the files. */
    if ( fp )
    {
        fclose ( fp );
    }
    for ( i=0; i < 3; i++ )
    {
        if ( devices[i] )
        {
            fclose ( devices[i] );
        }
    }
    if ( meta )
    {
        fclose ( meta );
    }

    if ( inputBaseName )
    {
        free ( inputBaseName );
    }

    /* Clean the memory. / Release the char arrays. */
    ( *env )->ReleaseStringUTFChars ( env, _tempInputDirPath, tempInputDirPath );
    ( *env )->ReleaseStringUTFChars ( env, _hash, hash );
    ( *env )->ReleaseStringUTFChars ( env, _outputFilePath, outputFilePath );
    ( *env )->ReleaseStringUTFChars ( env, _key, key );
    return status;
}

/**
 * Implements the splitInterface method defined in the Java RaidAccessInterface
 * class.
 */
JNIEXPORT jstring JNICALL Java_de_dhbw_mannheim_cloudraid_jni_RaidAccessInterface_splitInterface
( JNIEnv *env, jclass cls, jstring _inputFilePath, jstring _tempOutputDirPath, jstring _key, jint _keyLength )
{
    /* Convert the Java Strings to char arrays for usage in this C program. */
    const char *inputFilePath = ( *env )->GetStringUTFChars ( env, _inputFilePath, 0 );
    const char *tempOutputDirPath = ( *env )->GetStringUTFChars ( env, _tempOutputDirPath, 0 );
    const char *key = ( *env )->GetStringUTFChars ( env, _key, 0 );
    const int keyLength = _keyLength;

    void *resblock;
    char *outputBaseName, retvalue[65];
    int status;
    unsigned char i;
    const int tmpLength = strlen ( tempOutputDirPath );
    rc4_key rc4key;

    /* Generate file pointers. */
    FILE *fp;
    FILE *devices[3];
    FILE *meta;

    /* open input file */
    fp = fopen ( inputFilePath, "rb" );
    if ( fp == NULL )
    {
        status = OPENERR_IN;
        goto end;
    }

    /* construct base output path:
     *  - tmpfolder: tmpLength bytes, including ending slash /
     *  - hash:      64 bytes
     *  - extension: 2 bytes for .i
     *  - \0:        1 byte
     */
    outputBaseName = ( char* ) malloc ( tmpLength + 64 + 2 + 1 );
    resblock = malloc ( 32 );
    if ( outputBaseName == NULL || resblock == NULL )
    {
        status = OPENERR_IN;
        goto end;
    }
    memcpy ( outputBaseName, tempOutputDirPath, tmpLength );
    /* build the hash */
    sha256_buffer ( inputFilePath, strlen ( inputFilePath ), resblock );
    ascii_from_resbuf ( ( unsigned char* ) &outputBaseName[ tmpLength ] , resblock );

    /* open the files */
    for ( i = 0; i < 3; i++ )
    {
        sprintf ( &outputBaseName[ tmpLength + 64 ], ".%c", i+0x30 );
        devices[i] = fopen ( outputBaseName, "wb" );
    }
    if ( devices[0] == NULL )
    {
        status = OPENERR_DEV0;
        goto end;
    }
    if ( devices[1] == NULL )
    {
        status = OPENERR_DEV1;
        goto end;
    }
    if ( devices[2] == NULL )
    {
        status = OPENERR_DEV2;
        goto end;
    }

    sprintf ( &outputBaseName[ tmpLength + 64 ], ".m" );
    meta = fopen ( outputBaseName, "wb" );
    if ( meta == NULL )
    {
        status = METADATAERR;
        goto end;
    }

    /* construct the RC4 key */
    prepare_key ( ( unsigned char* ) key, keyLength, &rc4key );

    /* Invoke the native split method. */
    status = split_byte ( fp, devices, meta, &rc4key );

end:
    if ( status == SUCCESS_SPLIT )
    {
        memcpy ( retvalue, &outputBaseName[ tmpLength ], 64 );
        retvalue[64] = '\0';
    }
    else
    {
        retvalue[0] = status;
        retvalue[1] = '\0';
    }
    /* Close the files. */
    if ( fp )
    {
        fclose ( fp );
    }
    for ( i=0; i < 3; i++ )
    {
        if ( devices[i] )
        {
            fclose ( devices[i] );
        }
    }
    if ( meta )
    {
        fclose ( meta );
    }

    if ( resblock )
    {
        free ( resblock );
    }
    if ( outputBaseName )
    {
        free ( outputBaseName );
    }

    /* Clean the memory. / Release the char arrays. */
    ( *env )->ReleaseStringUTFChars ( env, _inputFilePath, inputFilePath );
    ( *env )->ReleaseStringUTFChars ( env, _tempOutputDirPath, tempOutputDirPath );
    ( *env )->ReleaseStringUTFChars ( env, _key, key );
    return ( *env )->NewStringUTF ( env, retvalue );
}

