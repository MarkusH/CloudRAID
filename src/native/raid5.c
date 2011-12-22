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

#include "sha256.h"
#include "de_dhbw_mannheim_cloudraid_jni_RaidAccessInterface.h"

#include <stdlib.h>
#include <string.h>

#define SUCCESS_MERGE_BIT  0x01
#define SUCCESS_MERGE_BYTE 0x02
#define SUCCESS_SPLIT_BIT  0x03
#define SUCCESS_SPLIT_BYTE 0x04

#define MEMERR_DEV 0x10
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

static const unsigned int RAID5_BLOCKSIZE = 1024;

static const unsigned short EXPONENTS_LONG_FIRST[] =
{
    0x8000, 0x2000, 0x0800, 0x0200,
    0x0080, 0x0020, 0x0008, 0x0002
};

static const unsigned short EXPONENTS_LONG_SECOND[] =
{
    0x4000, 0x1000, 0x0400, 0x0100,
    0x0040, 0x0010, 0x0004, 0x0001
};

static const unsigned short EXPONENTS_SHORT[] =
{
    0x80, 0x40, 0x20, 0x10,
    0x08, 0x04, 0x02, 0x01
};

/**
 * Bitwise split the characters from *in and write them to the *devices files. E.g.: *in has
 * 10010010 11001001
 * Then the devices 0 and 1 become, where the bits of 0 are the bits 0, 2, 4, 6, 8, 10, 12, 14
 * and the bits of 1 are 1, 3, 5, 7, 9, 11, 13, 15 of *in
 * 0: 10011010
 * 1: 01001001
 *
 * The device 2 becomes the bitwise XOR of the devices 0 and 1
 * 2: 11010011
 *
 * If the input file has an odd file size, the parity of the last byte is a bitwise NOT.
 */
int split_bit ( FILE *in, FILE *devices[] )
{
    unsigned char *chars, a, b, p, parity_pos = 2;
    unsigned char index, i;
    unsigned short c;
    size_t rlen;

    /* allocate memory that contains the two characters we read at once */
    chars = ( unsigned char* ) malloc ( sizeof ( char ) * 2 );
    if ( chars == NULL )
    {
        printf ( "Memory error\n" );
        return MEMERR_BUF;
    }
    /* read the characters */
    rlen = fread ( chars, sizeof ( char ), 2, in );

    while ( rlen > 0 ) /* as long as we have read at least character */
    {
        a = 0;
        b = 0;
        if ( rlen == 2 ) /* we can do the bitwise split / file size is even */
        {
            index = 7;
            /* convert the 2 bytes to a short, the first byte is multiplied with 2^8 and hence uses the leftmost 8 bits */
            c = chars[0];
            c <<= 8;
            c |= chars[1];
            for ( i = 0; i <= 7; i++ ) /* bitwise check if the bit is set */
            {
                if ( c & EXPONENTS_LONG_FIRST[i] )
                {
                    a |= ( 1 << index );
                }
                if ( c & EXPONENTS_LONG_SECOND[i] )
                {
                    b |= ( 1 << index );
                }
                index--;
            }
            p = a ^ b; /* parity: xor of the two bytes */
            fwrite ( &a, sizeof ( char ), 1, devices[ ( parity_pos + 1 ) % 3] );
            fwrite ( &b, sizeof ( char ), 1, devices[ ( parity_pos + 2 ) % 3] );
            fwrite ( &p, sizeof ( char ), 1, devices[parity_pos] );
        }
        else /* odd file size take care of last byte */
        {
            a = chars[0];
            p = ~a;
            fwrite ( &a, sizeof ( char ), 1, devices[ ( parity_pos + 1 ) % 3] );
            fwrite ( &p, sizeof ( char ), 1, devices[parity_pos] );
        }
        parity_pos = ( parity_pos + 1 ) % 3; /* rotate the devices */
        rlen = fread ( chars, sizeof ( char ), 2, in );
    }
    if ( ferror ( in ) ) /* read error */
    {
        printf ( "Read error\n" );
        return READERR_IN;
    }
    free ( chars );
    return SUCCESS_SPLIT_BIT;
}

/**
 * Bitwise merge the characters from *devices and write it to *out. E.g.: *devices has
 * 0: 10011010
 * 1: 01001001
 * 2: 11010011
 *
 * Then *out alternating becomes the bits 0 to 7 from 0 and 1 as bits 0, 2, 4, 6, 8, 10, 12, 14
 * and 1, 3, 5, 7, 9, 11, 13, 15 of *in
 * *out: 10010010 11001001
 *
 * see split_bit for more information
 */
int merge_bit ( FILE *out, FILE *devices[] )
{
    unsigned char a, b, p, parity_pos = 2;
    unsigned char c, i;
    size_t alen, blen, plen;

    /* read the devices */
    alen = fread ( &a, sizeof ( char ), 1, devices[ ( parity_pos + 1 ) % 3] );
    blen = fread ( &b, sizeof ( char ), 1, devices[ ( parity_pos + 2 ) % 3] );
    plen = fread ( &p, sizeof ( char ), 1, devices[parity_pos] );
    while ( alen > 0 && blen > 0 && plen > 0 ) /* as long as we read from all 3 devices */
    {
        if ( ( a ^ b ) != p ) /* check parity validity */
        {
            printf ( "[WARNING] Parity does not match!" );
        }
        c = 0;
        for ( i = 0; i <= 7; i++ )
        {
            if ( i == 4 ) /* we read the first 2 nibbles => 1 byte */
            {
                fwrite ( &c, sizeof ( char ), 1, out );
                c = 0;
            }
            c <<= 1;
            if ( ( a & EXPONENTS_SHORT[i] ) > 0 )
            {
                c |= 1;
            }
            c <<= 1;
            if ( ( b & EXPONENTS_SHORT[i] ) > 0 )
            {
                c |= 1;
            }
        }
        fwrite ( &c, sizeof ( char ), 1, out );
        parity_pos = ( parity_pos + 1 ) % 3; /* rotate the devices */

        /* read the next bytes */
        alen = fread ( &a, sizeof ( char ), 1, devices[ ( parity_pos + 1 ) % 3] );
        blen = fread ( &b, sizeof ( char ), 1, devices[ ( parity_pos + 2 ) % 3] );
        plen = fread ( &p, sizeof ( char ), 1, devices[parity_pos] );
    }
    if ( alen > 0 && plen > 0 )
    {
        if ( ( a ^ p ) != 0xFF )
        {
            printf ( "[WARNING] Parity does not match!" );
        }
        fwrite ( &a, sizeof ( char ), 1, out );
    }
    return SUCCESS_MERGE_BIT;
}

void merge_byte_block (const unsigned char *in, const size_t in_len[], unsigned char *out, size_t *out_len)
{
    int i, partial;
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
        if (in_len[1] > 0)
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

void split_byte_block (const unsigned char *in, const size_t in_len, unsigned char *out, size_t out_len[])
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
    unsigned char *chars, *out, parity_pos = 2;
    size_t rlen, *out_len;

    chars = ( unsigned char* ) malloc ( sizeof ( unsigned char ) * 2 * RAID5_BLOCKSIZE );
    out = ( unsigned char* ) malloc ( sizeof ( unsigned char ) * RAID5_BLOCKSIZE * 3 );
    out_len = ( size_t* ) malloc ( sizeof ( size_t ) * 3 );
    if ( chars == NULL )
    {
        return MEMERR_BUF;
    }
    if ( out == NULL )
    {
        free(chars); /* Already allocated */
        return MEMERR_DEV;
    }
    rlen = fread ( chars, sizeof ( unsigned char ), 2 * RAID5_BLOCKSIZE, in );
    while ( rlen > 0 )
    {
        split_byte_block(chars, rlen, out, out_len);
        fwrite ( &out[0], sizeof ( unsigned char ), out_len[0], devices[ ( parity_pos + 1 ) % 3] );
        if (out_len[1] > 0)
        {
            fwrite ( &out[RAID5_BLOCKSIZE], sizeof ( unsigned char ), out_len[1], devices[ ( parity_pos + 2 ) % 3] );
        }
        fwrite ( &out[2 * RAID5_BLOCKSIZE], sizeof ( unsigned char ), out_len[2], devices[parity_pos] );
        parity_pos = ( parity_pos + 1 ) % 3;
        rlen = fread ( chars, sizeof ( char ), 2 * RAID5_BLOCKSIZE, in );
    }
    if ( ferror ( in ) )
    {
        return READERR_IN;
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

    in = ( unsigned char* ) malloc ( sizeof ( unsigned char ) * RAID5_BLOCKSIZE * 3);
    in_len = ( size_t* ) malloc ( sizeof ( size_t ) * 3 );
    buf = ( unsigned char* ) malloc ( sizeof ( unsigned char ) * 2 * RAID5_BLOCKSIZE);

    in_len[0] = fread ( &in[0], sizeof ( char ), RAID5_BLOCKSIZE, devices[ ( parity_pos + 1 ) % 3] );
    in_len[1] = fread ( &in[RAID5_BLOCKSIZE], sizeof ( char ), RAID5_BLOCKSIZE, devices[ ( parity_pos + 2 ) % 3] );
    in_len[2] = fread ( &in[2 * RAID5_BLOCKSIZE], sizeof ( char ), RAID5_BLOCKSIZE, devices[parity_pos] );

    while ( in_len[0] > 0 || in_len[1] > 0 || in_len[2] > 0)
    {
        merge_byte_block(in, in_len, buf, &out_len);
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
( JNIEnv *env, jobject obj, jstring str1, jstring str2, jstring str3, jstring str4, jboolean bits )
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
    if ( bits )
    {
        status = merge_bit ( fp, devices );
    }
    else
    {
        status = merge_byte ( fp, devices );
    }

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
( JNIEnv *env, jobject obj, jstring str1, jstring str2, jstring str3, jstring str4, jboolean bits )
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
    if ( bits )
    {
        status = split_bit ( fp, devices );
    }
    else
    {
        status = split_byte ( fp, devices );
    }

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
