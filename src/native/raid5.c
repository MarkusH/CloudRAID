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
#include <stddef.h>

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

#define METADATA_ERROR        0x40
#define METADATA_MISS_DEV0    0x01
#define METADATA_MISS_DEV1    0x02
#define METADATA_MISS_DEV2    0x04
#define METADATA_MISS_IN      0x08
#define METADATA_MISS_VERSION 0x10
#define METADATA_MISS_MISSING 0x20

void merge_byte_block ( const unsigned char *in, const size_t in_len[], const unsigned int parity_pos, const unsigned int dead_device, const unsigned int missing, unsigned char *out, size_t *out_len )
{
    int i, len;
    if ( parity_pos > 2 || dead_device > 2 ) /* just to assure */
    {
        *out_len = -1;
        return;
    }

    /*
     * \ Device 0
     * / Device 1
     * X Device 2
     *
     *  dead = 0           dead = 1           dead = 2
     * ___ \\\ ///        XXX ___ ///        XXX \\\ ___    parity_pos = 0
     * ___ XXX \\\        /// ___ \\\        /// XXX ___    parity_pos = 1
     * ___ /// XXX        \\\ ___ XXX        \\\ /// ___    parity_pos = 2
     * ___ \\\ ///        XXX ___ ///        XXX \\\ ___    parity_pos = 0
     * ___ XXX \\\        /// ___ \\\        /// XXX ___    parity_pos = 1
     * ___ /// XXX        \\\ ___ XXX        \\\ /// ___    parity_pos = 2
     */

    /*
     * *in always contains the primary device in [0], the secondary device in [1] and the parity in [2] except the file does not exist
     */
    if ( dead_device == parity_pos ) /* (0|0) (1|1) (2|2) */
    {
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
        if ( ( dead_device + 1 ) % 3 == parity_pos ) /* get the secondary device: (0|1) (1|2) (2|0) */
        {
            /*
             * in[0] is first part, in[2] is parity
             *
             * output is the content of in[0] plus the XOR of in[0] and in[2] for in_len[0] - missing characters
             *
             */
            memcpy ( &out[0], &in[0], in_len[0] ); /* Copy the first part of the read bytes */
            len = in_len[0] - missing; /* Set the expected length of the secondary device */
            *out_len = in_len[0] + len;
            for ( i = 0; i < len; i++ )
            {
                out[RAID5_BLOCKSIZE + i] = in[i] ^ in[2 * RAID5_BLOCKSIZE + i];
            }
        }

        else
        {
            if ( ( dead_device + 2 ) % 3 == parity_pos ) /* get the primary device: (0|2) (1|0) (2|1) */
            {
                /*
                 * in[1] is second part, in[2] is parity
                 *
                 * output is the content of XOR of in[1] and in[2] for in_len[1] characters plus
                 * the XOR of in[2] with 0xFF for the remaining characters up to in_len[2] plus in[1] for length in_len[1]
                 *
                 */
                *out_len = in_len[1] + in_len[2];
                for ( i = 0; i < in_len[1]; i++ )
                {
                    out[i] = in[RAID5_BLOCKSIZE + i] ^ in[2 * RAID5_BLOCKSIZE + i];
                }
                for ( i = in_len[1]; i < in_len[2]; i++ )
                {
                    out[i] = in[2 * RAID5_BLOCKSIZE + i] ^ 0xFF;
                }
                memcpy ( &out[RAID5_BLOCKSIZE], &in[RAID5_BLOCKSIZE], in_len[1] ); /* Copy the second part of the read bytes */
            }
            else
            {
                *out_len = -1;
            }
        }
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

int split_file ( FILE *in, FILE *devices[], FILE *meta, rc4_key *key )
{
    unsigned char *chars = NULL, *out = NULL, parity_pos = 2, *hash = NULL;
    size_t rlen, *out_len = NULL, l = 0, min = -1, max = 0;
    int status;

    /* sha context
       the last element [3] is for the input file */
    struct sha256_ctx sha256_ctx[4];
    size_t sha256_len[4];
    char *sha256_buf[4] = {NULL, NULL, NULL, NULL};
    void *sha256_resblock[4] = {NULL, NULL, NULL, NULL};
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
#if ENCRYPT_DATA == 1
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
            if ( sha256_len[i] == SHA256_BLOCKSIZE )
            {
                sha256_process_block ( sha256_buf[i], SHA256_BLOCKSIZE, &sha256_ctx[i] );
                sha256_len[i] = 0;
            }
        }
        /*
         * parity_pos = 2
         * i =          0       1       2
         * Begin        0       BS      2*BS
         *
         * parity_pos = 0
         * i =          0       1       2
         * Begin        2*BS    0       BS
         *
         * parity_pos = 1
         * i =          0       1       2
         * Begin        BS      2*BS    0
         */
        if ( sha256_len[0] < SHA256_BLOCKSIZE )
        {
            i = ( parity_pos == 2 ) ? 0 : ( parity_pos == 0 ) ? 2 : 1;
            memcpy ( sha256_buf[0] + sha256_len[0], &out[i * RAID5_BLOCKSIZE], out_len[i] );
            sha256_len[0] += out_len[i];
        }
        if ( sha256_len[1] < SHA256_BLOCKSIZE )
        {
            i = ( parity_pos == 2 ) ? 1 : ( parity_pos == 0 ) ? 0 : 2;
            memcpy ( sha256_buf[1] + sha256_len[1], &out[i * RAID5_BLOCKSIZE], out_len[i] );
            sha256_len[1] += out_len[i];
        }
        if ( sha256_len[2] < SHA256_BLOCKSIZE )
        {
            i = ( parity_pos == 2 ) ? 2 : ( parity_pos == 0 ) ? 1 : 0;
            memcpy ( sha256_buf[2] + sha256_len[2], &out[i * RAID5_BLOCKSIZE], out_len[i] );
            sha256_len[2] += out_len[i];
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
        if ( i < 3 )
        {
            l = ftell ( devices[i] );
            min = ( min <= l ) ? min : l;
            max = ( max >= l ) ? max : l;
        }
    }
    metadata.missing = max - min;
    status = write_metadata ( meta, &metadata );
    if ( status != 0 )
    {
        status = METADATA_ERROR;
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

int merge_file ( FILE *out, FILE *devices[], FILE *meta, rc4_key *key )
{
    unsigned char *in = NULL, *buf = NULL, parity_pos = 2, dead_device, i;
    size_t *in_len = NULL, out_len, l = 0;
    int status, mds;
    raid5md metadata, md_read;

    new_metadata ( &metadata );
    new_metadata ( &md_read );

    status = read_metadata ( meta, &metadata );

    create_metadata ( devices, &md_read );
    md_read.version = RAID5_METADATA_VERSION;

    mds = cmp_metadata ( &metadata, &md_read );

    if ( ( mds & METADATA_MISS_DEV0 ) == 0 && ( mds & METADATA_MISS_DEV1 ) == 0 )
    {
        dead_device = 2;
    }
    else
    {
        if ( ( mds & METADATA_MISS_DEV1 ) == 0 && ( mds & METADATA_MISS_DEV2 ) == 0 )
        {
            dead_device = 0;
        }
        else
        {
            if ( ( mds & METADATA_MISS_DEV2 ) == 0 && ( mds & METADATA_MISS_DEV0 ) == 0 )
            {
                dead_device = 1;
            }
            else
            {
                status = METADATA_ERROR;
                goto end;
            }

        }
    }

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

    in_len[0] = ( devices[ ( parity_pos + 1 ) % 3] ) ? fread ( &in[0], sizeof ( char ), RAID5_BLOCKSIZE, devices[ ( parity_pos + 1 ) % 3] ) : 0;
    in_len[1] = ( devices[ ( parity_pos + 2 ) % 3] ) ? fread ( &in[RAID5_BLOCKSIZE], sizeof ( char ), RAID5_BLOCKSIZE, devices[ ( parity_pos + 2 ) % 3] ) : 0;
    in_len[2] = ( devices[parity_pos] ) ? fread ( &in[2 * RAID5_BLOCKSIZE], sizeof ( char ), RAID5_BLOCKSIZE, devices[parity_pos] ) : 0;
    while ( in_len[0] > 0 || in_len[1] > 0 || in_len[2] > 0 )
    {
        /*
        * Detect end of file, since reading to the end but
        * not beyond does NOT set the EOF marker! Afterwards
        * the missing bytes can be set.
        */
        for ( i = 0; i < 3; i++ )
        {
            if ( parity_pos != i && devices[i] )
            {
                getc ( devices[i] );
            }
        }
        l = ( ( devices[0] && feof ( devices[0] ) ) || ( devices[1] && feof ( devices[1] ) ) || ( devices[2] && feof ( devices[2] ) ) ) ? metadata.missing : 0;
        for ( i = 0; i < 3; i++ )
        {
            if ( parity_pos != i && devices[i] && !feof ( devices[i] ) )
            {
                fseek ( devices[i], -1, SEEK_CUR );
            }
        }
        /* Call the merge */
        merge_byte_block ( in, in_len, parity_pos, dead_device, l, buf, &out_len );
        if ( out_len == -1 )
        {
            status = READERR_IN;
            goto end;
        }
#if ENCRYPT_DATA == 1
        /* encrypt the input file */
        rc4 ( buf, out_len, key );
#endif

        fwrite ( buf, sizeof ( unsigned char ), out_len, out );

        parity_pos = ( parity_pos + 1 ) % 3;
        in_len[0] = ( devices[ ( parity_pos + 1 ) % 3] ) ? fread ( &in[0], sizeof ( char ), RAID5_BLOCKSIZE, devices[ ( parity_pos + 1 ) % 3] ) : 0;
        in_len[1] = ( devices[ ( parity_pos + 2 ) % 3] ) ? fread ( &in[RAID5_BLOCKSIZE], sizeof ( char ), RAID5_BLOCKSIZE, devices[ ( parity_pos + 2 ) % 3] ) : 0;
        in_len[2] = ( devices[parity_pos] ) ? fread ( &in[2 * RAID5_BLOCKSIZE], sizeof ( char ), RAID5_BLOCKSIZE, devices[parity_pos] ) : 0;
    }
    if ( devices[0] && ferror ( devices[0] ) )
    {
        status = READERR_DEV0;
        goto end;
    }
    if ( devices[1] && ferror ( devices[1] ) )
    {
        status = READERR_DEV1;
        goto end;
    }
    if ( devices[2] && ferror ( devices[2] ) )
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

/**
 * result might be:
 * 0x00 - metadata matches
 *
 * or either any combination of the following:
 * 0x01 - hash_dev0 differs (METADATA_MISS_DEV0)
 * 0x02 - hash_dev1 differs (METADATA_MISS_DEV1)
 * 0x04 - hash_dev2 differs (METADATA_MISS_DEV2)
 * 0x08 - hash_in differs   (METADATA_MISS_IN)
 * 0x10 - version missmatch (METADATA_MISS_VERSION)
 *
 * 0xff - memory error in md1 or md2
 */
int cmp_metadata ( raid5md *md1, raid5md *md2 )
{
    int i, cmp = 0x00;

    if ( md1 == NULL || md2 == NULL )
    {
        return 0xff;
    }

    cmp |= ( md1->version == md2->version ) ? 0x00 : METADATA_MISS_VERSION;
    cmp |= ( md1->missing == md2->missing ) ? 0x00 : METADATA_MISS_MISSING;
    for ( i = 0; i < 4; i++ )
    {
        cmp |= ( cmp_metadata_hash ( md1, md2, i ) );
    }
    return cmp;
}

/**
 * Returns 0 iff the hash specified by idx matches md1 and md2
 * OR if the index is out of range [0..3].
 * Otherwize the error number (see cmp_metadata).
 */
int cmp_metadata_hash ( raid5md *md1, raid5md *md2, const int idx )
{
    if ( md1 == NULL || md2 == NULL )
    {
        return 0;
    }
    switch ( idx )
    {
    case 0:
        return ( memcmp ( md1->hash_dev0, md2->hash_dev0, 64 ) != 0 ) ? METADATA_MISS_DEV0 : 0;
    case 1:
        return ( memcmp ( md1->hash_dev1, md2->hash_dev1, 64 ) != 0 ) ? METADATA_MISS_DEV1 : 0;
    case 2:
        return ( memcmp ( md1->hash_dev2, md2->hash_dev2, 64 ) != 0 ) ? METADATA_MISS_DEV2 : 0;
    case 3:
        return ( memcmp ( md1->hash_in, md2->hash_in, 64 ) != 0 ) ? METADATA_MISS_IN : 0;
    }
    return 0;
}

/**
 * Takes 3 devices and calculates the sha256 sum of each
 * file. The checksums are stored into the given metadata.
 * Returns 0 on success.
 */
int create_metadata ( FILE *devices[], raid5md *md )
{
    int i;
    unsigned char *ascii = NULL;
    size_t fpos, l = 0, min = -1, max = 0;

    if ( md == NULL )
    {
        return 1;
    }

    ascii = ( unsigned char * ) malloc ( 65 );
    if ( ascii == NULL )
    {
        return 1;
    }

    new_metadata ( md ); /* clean the metadata */

    for ( i = 0; i < 3; i++ )
    {
        if ( devices[i] )
        {
            fpos = ftell ( devices[i] );
            rewind ( devices[i] );
            if ( build_sha256_sum_file ( devices[i], ascii ) == 0 )
            {
                set_metadata_hash ( md, i, ascii );
            }
            l = ftell ( devices[i] );
            fseek ( devices[i], fpos, SEEK_SET );
        }
        min = ( min < l ) ? min : l;
        max = ( max > l ) ? max : l;
    }
    md->missing = l;
    free ( ascii );
    return 0;
}

void new_metadata ( raid5md *md )
{
    if ( md )
    {
        memset ( md->hash_dev0, 0, 65 );
        memset ( md->hash_dev1, 0, 65 );
        memset ( md->hash_dev2, 0, 65 );
        memset ( md->hash_in, 0, 65 );
        md->version = 0;
        md->missing = 0;
    }
}

void print_metadata ( raid5md *md )
{
    if ( md )
    {
        printf ( "\nVersion: %02x\n", md->version );
        printf ( "Missing: %d\n", md->missing );
        printf ( "0: %64s\n", md->hash_dev0 );
        printf ( "1: %64s\n", md->hash_dev1 );
        printf ( "2: %64s\n", md->hash_dev2 );
        printf ( "I: %64s\n", md->hash_in );
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
            new_metadata ( md ); /* clean the metadata */
            fscanf ( fp, "%2hhu", & ( md->version ) );
            fscanf ( fp, "%64s", md->hash_dev0 );
            fscanf ( fp, "%64s", md->hash_dev1 );
            fscanf ( fp, "%64s", md->hash_dev2 );
            fscanf ( fp, "%64s", md->hash_in );
            fscanf ( fp, "%4x", & ( md->missing ) );
            return 0;
        }
        return 2;
    }
    return 1;
}

void set_metadata_hash ( raid5md *md, const int idx, const unsigned char hash[65] )
{
    if ( md )
    {
        switch ( idx )
        {
        case 0:
            memcpy ( md->hash_dev0, hash, 65 );
            break;
        case 1:
            memcpy ( md->hash_dev1, hash, 65 );
            break;
        case 2:
            memcpy ( md->hash_dev2, hash, 65 );
            break;
        case 3:
            memcpy ( md->hash_in, hash, 65 );
            break;
        }
    }
}

int write_metadata ( FILE *fp, raid5md *md )
{
    if ( fp )
    {
        if ( md )
        {
            fprintf ( fp, "%02x", md->version );
            fprintf ( fp, "%64s", md->hash_dev0 );
            fprintf ( fp, "%64s", md->hash_dev1 );
            fprintf ( fp, "%64s", md->hash_dev2 );
            fprintf ( fp, "%64s", md->hash_in );
            fprintf ( fp, "%04x", md->missing );
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
    FILE *fp = NULL;
    FILE *devices[3] = {NULL, NULL, NULL};
    FILE *meta = NULL;

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
        status = METADATA_ERROR;
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
    status = merge_file ( fp, devices, meta, &rc4key );

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

    void *resblock = NULL;
    char *outputBaseName = NULL, retvalue[65];
    int status;
    unsigned char i;
    const int tmpLength = strlen ( tempOutputDirPath );
    rc4_key rc4key;

    /* Generate file pointers. */
    FILE *fp = NULL;
    FILE *devices[3] = {NULL, NULL, NULL};
    FILE *meta = NULL;

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
        status = METADATA_ERROR;
        goto end;
    }

    /* construct the RC4 key */
    prepare_key ( ( unsigned char* ) key, keyLength, &rc4key );

    /* Invoke the native split method. */
    status = split_file ( fp, devices, meta, &rc4key );

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

