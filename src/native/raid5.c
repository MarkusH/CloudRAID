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
#include "de_dhbw_mannheim_cloudraid_jni_RaidAccessInterface.h"

#include <stdlib.h>
#include <string.h>

#define SUCCESS_MERGE_BIT  0x01
#define SUCCESS_MERGE_BYTE 0x02
#define SUCCESS_SPLIT_BIT  0x03
#define SUCCESS_SPLIT_BYTE 0x04

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
        printf ( "Read error\n" );
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

int split_byte ( FILE *in, FILE *devices[] )
{
    unsigned char *chars, *out, parity_pos = 2, *hash;
    size_t rlen, *out_len;

    /* sha context
       the last element [3] is for the input file */
    struct sha256_ctx sha256_ctx[4];
    size_t sha256_len[4];
    char *sha256_buf[4];
    void *sha256_resblock[4];
    int i, j;

    chars = ( unsigned char* ) malloc ( sizeof ( unsigned char ) * 2 * RAID5_BLOCKSIZE );
    out = ( unsigned char* ) malloc ( sizeof ( unsigned char ) * RAID5_BLOCKSIZE * 3 );
    out_len = ( size_t* ) malloc ( sizeof ( size_t ) * 3 );
    if ( chars == NULL )
    {
        return MEMERR_BUF;
    }
    if ( out == NULL )
    {
        free ( chars ); /* Already allocated */
        return MEMERR_DEV;
    }
    if ( out_len == NULL )
    {
        free ( out ); /* Already allocated */
        free ( chars ); /* Already allocated */
        return MEMERR_DEV;
    }

    /* create the sha256 context */
    for ( i = 0; i < 4; i++ )
    {
        sha256_resblock[i] = malloc ( 32 );
        if ( sha256_resblock[i] == NULL )
        {
            for ( j = 0; j < i - 1; j++ )
            {
                free ( sha256_buf[j] ); /* Already allocated */
            }
            for ( j = 0; j < i; j++ )
            {
                free ( sha256_resblock[j] ); /* Already allocated */
            }
            free ( out_len ); /* Already allocated */
            free ( out ); /* Already allocated */
            free ( chars ); /* Already allocated */
            return MEMERR_SHA;
        }

        sha256_buf[i] = ( char* ) malloc ( SHA256_BLOCKSIZE + 72 );
        if ( sha256_buf[i] == NULL )
        {
            free ( sha256_resblock[i] ); /* Already allocated */
            for ( j = 0; j < i; j++ )
            {
                free ( sha256_buf[j] ); /* Already allocated */
                free ( sha256_resblock[j] ); /* Already allocated */
            }
            free ( out_len ); /* Already allocated */
            free ( out ); /* Already allocated */
            free ( chars ); /* Already allocated */
            return MEMERR_SHA;
        }
        sha256_init_ctx ( &sha256_ctx[i] );
        sha256_len[i] = 0;
    }

    rlen = fread ( chars, sizeof ( unsigned char ), 2 * RAID5_BLOCKSIZE, in );
    while ( rlen > 0 )
    {
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
        for ( i = 0; i < 4; i++ )
        {
            free ( sha256_buf[i] );
        }
        free ( out_len ); /* Already allocated */
        free ( out ); /* Already allocated */
        free ( chars ); /* Already allocated */
        return READERR_IN;
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
        printf ( "\n\n\t\t%s\n\n", hash );
    }


    free ( hash );
    for ( i = 0; i < 4; i++ )
    {
        free ( sha256_buf[i] );
        free ( sha256_resblock[i] );
    }
    free ( out_len );
    free ( out );
    free ( chars );
    return SUCCESS_SPLIT_BYTE;
}

int merge_byte ( FILE *out, FILE *devices[] )
{
    unsigned char *in, *buf, parity_pos = 2;
    size_t *in_len, out_len;

    in = ( unsigned char* ) malloc ( sizeof ( unsigned char ) * RAID5_BLOCKSIZE * 3 );
    in_len = ( size_t* ) malloc ( sizeof ( size_t ) * 3 );
    buf = ( unsigned char* ) malloc ( sizeof ( unsigned char ) * 2 * RAID5_BLOCKSIZE );
    if ( in == NULL )
    {
        return MEMERR_DEV;
    }
    if ( in_len == NULL )
    {
        free ( in ); /* Already allocated */
        return MEMERR_DEV;
    }
    if ( buf == NULL )
    {
        free ( in ); /* Already allocated */
        free ( in_len ); /* Already allocated */
        return MEMERR_BUF;
    }

    in_len[0] = fread ( &in[0], sizeof ( char ), RAID5_BLOCKSIZE, devices[ ( parity_pos + 1 ) % 3] );
    in_len[1] = fread ( &in[RAID5_BLOCKSIZE], sizeof ( char ), RAID5_BLOCKSIZE, devices[ ( parity_pos + 2 ) % 3] );
    in_len[2] = fread ( &in[2 * RAID5_BLOCKSIZE], sizeof ( char ), RAID5_BLOCKSIZE, devices[parity_pos] );

    while ( in_len[0] > 0 || in_len[1] > 0 || in_len[2] > 0 )
    {
        merge_byte_block ( in, in_len, buf, &out_len );
        fwrite ( buf, sizeof ( unsigned char ), out_len, out );

        parity_pos = ( parity_pos + 1 ) % 3;
        in_len[0] = fread ( &in[0], sizeof ( char ), RAID5_BLOCKSIZE, devices[ ( parity_pos + 1 ) % 3] );
        in_len[1] = fread ( &in[RAID5_BLOCKSIZE], sizeof ( char ), RAID5_BLOCKSIZE, devices[ ( parity_pos + 2 ) % 3] );
        in_len[2] = fread ( &in[2 * RAID5_BLOCKSIZE], sizeof ( char ), RAID5_BLOCKSIZE, devices[parity_pos] );
    }
    if ( ferror ( devices[0] ) )
    {
        return READERR_DEV0;
    }
    if ( ferror ( devices[1] ) )
    {
        return READERR_DEV1;
    }
    if ( ferror ( devices[2] ) )
    {
        return READERR_DEV2;
    }
    free ( buf );
    free ( in_len );
    free ( in );
    return SUCCESS_MERGE_BYTE;
}

/**
 * Implements the mergeInterface method defined in the Java RaidAccessInterface
 * class.
 */
JNIEXPORT jint JNICALL Java_de_dhbw_mannheim_cloudraid_jni_RaidAccessInterface_mergeInterface
( JNIEnv *env, jobject obj, jstring str1, jstring str2, jstring str3, jstring str4 )
{
    /* Convert the Java Strings to char arrays for usage in the C program. */
    const char *out = ( *env )->GetStringUTFChars ( env, str1, 0 );
    const char *in1 = ( *env )->GetStringUTFChars ( env, str2, 0 );
    const char *in2 = ( *env )->GetStringUTFChars ( env, str3, 0 );
    const char *in3 = ( *env )->GetStringUTFChars ( env, str4, 0 );
    int status;

    /* Generate file pointers. */
    FILE *fp;
    FILE *devices[3];

    devices[0] = fopen ( in1, "rb" );
    devices[1] = fopen ( in2, "rb" );
    devices[2] = fopen ( in3, "rb" );
    if ( devices[0] == NULL )
    {
        printf ( "File not found!\n" );
        return OPENERR_DEV0;
    }
    if ( devices[1] == NULL )
    {
        printf ( "File not found!\n" );
        return OPENERR_DEV1;
    }
    if ( devices[2] == NULL )
    {
        printf ( "File not found!\n" );
        return OPENERR_DEV2;
    }

    fp = fopen ( out, "wb" );
    if ( fp == NULL )
    {
        printf ( "File could not be created!\n" );
        return OPENERR_OUT;
    }

    /* Invoke the native merge method. */
    status = merge_byte ( fp, devices );

    /* Close the files. */
    fclose ( fp );
    fclose ( devices[0] );
    fclose ( devices[1] );
    fclose ( devices[2] );

    /* Clean the memory. / Release the char arrays. */
    ( *env )->ReleaseStringUTFChars ( env, str1, out );
    ( *env )->ReleaseStringUTFChars ( env, str2, in1 );
    ( *env )->ReleaseStringUTFChars ( env, str3, in2 );
    ( *env )->ReleaseStringUTFChars ( env, str4, in3 );
    return status;
}

/**
 * Implements the splitInterface method defined in the Java RaidAccessInterface
 * class.
 */
JNIEXPORT jint JNICALL Java_de_dhbw_mannheim_cloudraid_jni_RaidAccessInterface_splitInterface
( JNIEnv *env, jobject obj, jstring str1, jstring str2, jstring str3, jstring str4 )
{
    /* Convert the Java Strings to char arrays for usage in this C program. */
    const char *in = ( *env )->GetStringUTFChars ( env, str1, 0 );
    const char *out1 = ( *env )->GetStringUTFChars ( env, str2, 0 );
    const char *out2 = ( *env )->GetStringUTFChars ( env, str3, 0 );
    const char *out3 = ( *env )->GetStringUTFChars ( env, str4, 0 );
    int status;

    /* Generate file pointers. */
    FILE *fp;
    FILE *devices[3];

    fp = fopen ( in, "rb" );
    if ( fp == NULL )
    {
        printf ( "File not found!\n" );
        return OPENERR_OUT;
    }

    devices[0] = fopen ( out1, "wb" );
    devices[1] = fopen ( out2, "wb" );
    devices[2] = fopen ( out3, "wb" );
    if ( devices[0] == NULL )
    {
        printf ( "File could not be created!\n" );
        return OPENERR_DEV0;
    }
    if ( devices[1] == NULL )
    {
        printf ( "File could not be created!\n" );
        return OPENERR_DEV1;
    }
    if ( devices[2] == NULL )
    {
        printf ( "File could not be created!\n" );
        return OPENERR_DEV2;
    }

    /* Invoke the native split method. */
    status = split_byte ( fp, devices );

    /* Close the files. */
    fclose ( fp );
    fclose ( devices[0] );
    fclose ( devices[1] );
    fclose ( devices[2] );

    /* Clean the memory. / Release the char arrays. */
    ( *env )->ReleaseStringUTFChars ( env, str1, in );
    ( *env )->ReleaseStringUTFChars ( env, str2, out1 );
    ( *env )->ReleaseStringUTFChars ( env, str3, out2 );
    ( *env )->ReleaseStringUTFChars ( env, str4, out3 );
    return status;
}
