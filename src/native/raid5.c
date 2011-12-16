/*
 * Copyright 2011 by the CloudRAID Team, see AUTHORS for more details.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "de_dhbw_mannheim_cloudraid_jni_RaidAccessInterface.h"

#define BLOCKSIZE 1024

#define SUCCESS_MERGE_BIT  0x01
#define SUCCESS_MERGE_BYTE 0x02
#define SUCCESS_SPLIT_BIT  0x03
#define SUCCESS_SPLIT_BYTE 0x04

#define MEMERR_DEV0 0x10
#define MEMERR_DEV1 0x11
#define MEMERR_DEV2 0x12
#define MEMERR_BUF  0x17

#define READERR_DEV0 0x20
#define READERR_DEV1 0x21
#define READERR_DEV2 0x22
#define READERR_IN   0x29

#define OPENERR_DEV0 0x30
#define OPENERR_DEV1 0x31
#define OPENERR_DEV2 0x32
#define OPENERR_OUT  0x38
#define OPENERR_IN   0x39

unsigned short EXPONENTS_LONG_FIRST[] =
{
    0x8000, 0x2000, 0x0800, 0x0200,
    0x0080, 0x0020, 0x0008, 0x0002
};

unsigned short EXPONENTS_LONG_SECOND[] =
{
    0x4000, 0x1000, 0x0400, 0x0100,
    0x0040, 0x0010, 0x0004, 0x0001
};

unsigned short EXPONENTS_SHORT[] =
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

    while ( rlen > 0 )   /* as long as we have read at least character */
    {
        a = 0;
        b = 0;
        if ( rlen == 2 )   /* we can do the bitwise split / file size is even */
        {
            index = 7;
            /* convert the 2 bytes to a short, the first byte is multiplied with 2^8 and hence uses the leftmost 8 bits */
            c = chars[0];
            c <<= 8;
            c |= chars[1];
            for ( i = 0; i <= 7; i++ )   /* bitwise check if the bit is set */
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
        else   /* odd file size take care of last byte */
        {
            a = chars[0];
            p = ( ~a ) + 256;
            fwrite ( &a, sizeof ( char ), 1, devices[ ( parity_pos + 1 ) % 3] );
            fwrite ( &p, sizeof ( char ), 1, devices[parity_pos] );
        }
        parity_pos = ( parity_pos + 1 ) % 3; /* rotate the devices */
        rlen = fread ( chars, sizeof ( char ), 2, in );
    }
    if ( ferror ( in ) )   /* read error */
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
 * and  1, 3, 5, 7, 9, 11, 13, 15 of *in
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
    while ( alen > 0 && blen > 0 && plen > 0 )   /* as long as we read from all 3 devices */
    {
        if ( ( a ^ b ) != p )   /* check parity validity */
        {
            printf ( "[WARNING] Parity does not match!" );
        }
        c = 0;
        for ( i = 0; i <= 7; i++ )
        {
            if ( i == 4 )   /* we read the first 2 nibbles => 1 byte */
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

int split_byte ( FILE *in, FILE *devices[] )
{
    unsigned char  *chars, *a, *b, *p, parity_pos = 2;
    size_t rlen, partial;
    unsigned int i;

    /* allocate memory that contains the two characters we read at once */
    chars = ( unsigned char* ) malloc ( sizeof ( char ) * 2 * BLOCKSIZE );
    a = ( unsigned char* ) malloc ( sizeof ( char ) * BLOCKSIZE );
    b = ( unsigned char* ) malloc ( sizeof ( char ) * BLOCKSIZE );
    p = ( unsigned char* ) malloc ( sizeof ( char ) * BLOCKSIZE );
    if ( chars == NULL )
    {
        printf ( "Memory error\n" );
        return MEMERR_BUF;
    }
    if ( a == NULL )
    {
        printf ( "Memory error\n" );
        return MEMERR_DEV0;
    }
    if ( b == NULL )
    {
        printf ( "Memory error\n" );
        return MEMERR_DEV1;
    }
    if ( p == NULL )
    {
        printf ( "Memory error\n" );
        return MEMERR_DEV2;
    }
    rlen = fread ( chars, sizeof ( unsigned char ), 2 * BLOCKSIZE, in );
    while ( rlen > 0 )
    {
        if ( rlen == 2 * BLOCKSIZE )   /* two complete input blocks */
        {
            memcpy ( a, &chars[0], BLOCKSIZE ); /* Copy the first part of the read bytes*/
            memcpy ( b, &chars[BLOCKSIZE], BLOCKSIZE ); /* Copy the second part of the read bytes*/
            for ( i = 0; i < BLOCKSIZE; i++ )
            {
                p[i] = a[i] ^ b[i]; /* Bytewise calculation of the parity */
            }
            fwrite ( a, sizeof ( unsigned char ), BLOCKSIZE, devices[ ( parity_pos + 1 ) % 3] );
            fwrite ( b, sizeof ( unsigned char ), BLOCKSIZE, devices[ ( parity_pos + 2 ) % 3] );
            fwrite ( p, sizeof ( unsigned char ), BLOCKSIZE, devices[parity_pos] );
        }
        else
        {
            if ( rlen > BLOCKSIZE )
            {
                partial = rlen - BLOCKSIZE;
                memcpy ( a, &chars[0], BLOCKSIZE ); /* Copy the first part of the read bytes */
                memcpy ( b, &chars[BLOCKSIZE], partial ); /* Copy the second part of the read bytes */
                for ( i = 0; i < partial; i++ )
                {
                    p[i] = a[i] ^ b[i]; /* Bytewise calculation of the parity */
                }
                for ( i = partial; i < BLOCKSIZE; i++ )
                {
                    p[i] = ( ~a[i] ) + 256; /* Parity of the overflowing bytes */
                }
                fwrite ( a, sizeof ( unsigned char ), BLOCKSIZE, devices[ ( parity_pos + 1 ) % 3] );
                fwrite ( b, sizeof ( unsigned char ), partial, devices[ ( parity_pos + 2 ) % 3] );
                fwrite ( p, sizeof ( unsigned char ), BLOCKSIZE, devices[parity_pos] );
            }
            else
            {
                memcpy ( a, &chars[0], rlen ); /* Copy the first part of the read bytes */
                for ( i = 0; i < rlen; i++ )
                {
                    p[i] = ( ~a[i] ) + 256; /* Parity of the overflowing bytes */
                }
                fwrite ( a, sizeof ( unsigned char ), rlen, devices[ ( parity_pos + 1 ) % 3] );
                fwrite ( p, sizeof ( unsigned char ), rlen, devices[parity_pos] );
            }
        }
        parity_pos = ( parity_pos + 1 ) % 3;
        rlen = fread ( chars, sizeof ( char ), 2 * BLOCKSIZE, in );
    }
    if ( ferror ( in ) )
    {
        printf ( "Read error\n" );
        return READERR_IN;
    }

    free ( p );
    free ( b );
    free ( a );
    free ( chars );
    return SUCCESS_SPLIT_BYTE;
}

int merge_byte ( FILE *out, FILE *devices[] )
{
    unsigned char *a, *b, *p, parity_pos = 2;
    size_t alen, blen, plen;
    unsigned int i;

    a = ( unsigned char* ) malloc ( sizeof ( char ) * BLOCKSIZE );
    b = ( unsigned char* ) malloc ( sizeof ( char ) * BLOCKSIZE );
    p = ( unsigned char* ) malloc ( sizeof ( char ) * BLOCKSIZE );

    alen = fread ( a, sizeof ( char ), BLOCKSIZE, devices[ ( parity_pos + 1 ) % 3] );
    blen = fread ( b, sizeof ( char ), BLOCKSIZE, devices[ ( parity_pos + 2 ) % 3] );
    plen = fread ( p, sizeof ( char ), BLOCKSIZE, devices[parity_pos] );

    while ( alen == BLOCKSIZE && blen == BLOCKSIZE && plen == BLOCKSIZE )
    {
        for ( i = 0; i < BLOCKSIZE; i++ )
        {
            if ( ( a[i] ^ b[i] ) != p[i] )   /* Parity does not match */
            {
                printf ( "[WARNING] Parity does not match!" );
            }
        }
        fwrite ( a, sizeof ( char ), BLOCKSIZE, out );
        fwrite ( b, sizeof ( char ), BLOCKSIZE, out );
        parity_pos = ( parity_pos + 1 ) % 3;
        alen = fread ( a, sizeof ( char ), BLOCKSIZE, devices[ ( parity_pos + 1 ) % 3] );
        blen = fread ( b, sizeof ( char ), BLOCKSIZE, devices[ ( parity_pos + 2 ) % 3] );
        plen = fread ( p, sizeof ( char ), BLOCKSIZE, devices[parity_pos] );
    }

    if ( alen == BLOCKSIZE && plen == BLOCKSIZE && blen > 0 )   /* Last block of output is != 2 * BLOCKSIZE and > BLOCKSIZE */
    {
        for ( i = 0; i < blen; i++ )
        {
            if ( ( a[i] ^ b[i] ) != p[i] )   /* Parity does not match */
            {
                printf ( "[WARNING] Parity does not match!" );
            }
        }
        for ( i = blen; i < BLOCKSIZE; i++ )
        {
            if ( ( a[i] ^ p[i] ) != 0xFF )   /* Parity does not match */
            {
                printf ( "[WARNING] Parity does not match!" );
            }
        }
        fwrite ( a, sizeof ( char ), BLOCKSIZE, out );
        fwrite ( b, sizeof ( char ), blen, out );
    }
    else
    {
        if ( alen > 0 && plen  > 0 && alen == plen )
        {
            for ( i = 0; i < alen; i++ )
            {
                if ( ( a[i] ^ p[i] ) != 0xFF )   /* Parity does not match */
                {
                    printf ( "[WARNING] Parity does not match!" );
                }
            }
            fwrite ( a, sizeof ( char ), BLOCKSIZE, out );
        }
        else
        {
            if ( ferror ( devices[0] ) )
            {
                printf ( "Read error\n" );
                return READERR_DEV0;
            }
            if ( ferror ( devices[1] ) )
            {
                printf ( "Read error\n" );
                return READERR_DEV1;
            }
            if ( ferror ( devices[2] ) )
            {
                printf ( "Read error\n" );
                return READERR_DEV2;
            }
        }
    }
    free ( p );
    free ( b );
    free ( a );
    return SUCCESS_MERGE_BYTE;
}


/**
 * Implements the mergeInterface method defined in the Java RaidAccessInterface
 * class.
 */
JNIEXPORT jint JNICALL Java_de_dhbw_mannheim_cloudraid_jni_RaidAccessInterface_mergeInterface
( JNIEnv *env, jobject obj, jstring str1, jstring str2, jstring str3, jstring str4, jboolean bits )
{
    /* Convert the Java Strings to char arrays for usage in the C program.  */
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

    /* Close the files.  */
    fclose ( fp );
    fclose ( devices[0] );
    fclose ( devices[1] );
    fclose ( devices[2] );

    /* Clean the memory. / Release the char arrays.  */
    ( *env )->ReleaseStringUTFChars ( env, str1, in );
    ( *env )->ReleaseStringUTFChars ( env, str2, out1 );
    ( *env )->ReleaseStringUTFChars ( env, str3, out2 );
    ( *env )->ReleaseStringUTFChars ( env, str4, out3 );
    return status;
}
