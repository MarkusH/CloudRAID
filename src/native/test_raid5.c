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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/time.h>
#ifndef BENCHMARK
#define BENCHSIZE 13056
#endif

int main ( void )
{
    int i;
    int status;
#ifdef CHECKING
    unsigned char *ascii;
#endif
    FILE *fp[5];
    char *filename[] = {"test_raid5.dat",
                        "test_raid5.dev0.dat",
                        "test_raid5.dev1.dat",
                        "test_raid5.dev2.dat",
                        "test_raid5.out.dat",
                        "test_raid5.meta.dat"
                       };

#ifdef CHECKING
    char *assumed[] = {"3b6f5cf4c8c3e8b6c6894da81c1fcea588db14d088c5970c1b98faed940b2ce4",
                       "5a672ad40199303d6bc2d550a9e099cefdb5a5958c76bd2e5ef31e910b623680",
                       "51213026e91f4ca01a4522f55ae523f4c6a0d2247662db569846cf2226caceb3",
                       "0c3c738a3ca13e0c68c06fb448d095a28cbb7b548d9f790759752212ee1ccf25",
                       "3b6f5cf4c8c3e8b6c6894da81c1fcea588db14d088c5970c1b98faed940b2ce4",
                       "1544a46302662591dec6a58cce795d552e7f7a011dac0bc219659aab6fd32808"
                      };
#endif
#ifdef BENCHMARK
    struct timeval start, end;
    float elapsed;
#endif
    rc4_key rc4key;

    printf ( "Running test for RAID5:\n\n" );

    /** Create test file **/
    fp[0] = fopen ( filename[0], "wb" );
    if ( !fp[0] )
    {
        printf ( "Cannot write test file!\n" );
        return 1;
    }
    for ( i = 0; i < BENCHSIZE; i++ )
    {
        /* Every device becomes the parity twice. dev2 three
           times but the third time only 512+256 Bytes
           (3/4 BLOCKSIZE).
           2 * BLOCKSIZE * 6 + 3/4 BLOCKSIZE = 13056 */
        fprintf ( fp[0], "%c", ( i*i + i ) % 256 );
    }
    fclose ( fp[0] );

    /** Open test file for split **/
    fp[0] = fopen ( filename[0], "rb" );
    if ( !fp[0] )
    {
        printf ( "Cannot read test file!\n" );
        return 1;
    }

    /** Create device and metadata files **/
    for ( i = 1; i <= 3; i++ )
    {
        fp[i] = fopen ( filename[i], "wb" );
        if ( !fp[i] )
        {
            printf ( "Cannot create device file %d!\n", i - 1 );
            return 1;
        }
    }

    fp[4] = fopen ( filename[5], "wb" );
    if ( !fp[4] )
    {
        printf ( "Cannot create metadata file!\n" );
        return 1;
    }

    prepare_key ( ( unsigned char * ) "password", 8, &rc4key );

    /** perform the split **/
    printf ( "Start split ... " );
    fflush ( stdout );
#ifdef BENCHMARK
    gettimeofday ( &start, NULL );
#endif
    split_file ( fp[0], &fp[1], fp[4], &rc4key );
#ifdef BENCHMARK
    gettimeofday ( &end, NULL );
#endif
    printf ( "Done\n" );
    fflush ( stdout );
#ifdef BENCHMARK
    elapsed = ( ( end.tv_sec-start.tv_sec ) * 1000000.0f + end.tv_usec - start.tv_usec ) / 1000.0f;
    printf ( "split time: %.3f ms\n\n", elapsed );
#endif

    /** Close the input file **/
    fclose ( fp[0] );

    /** Close and reopen device and metadata files for merge **/
    for ( i = 1; i <= 3; i++ )
    {
        fclose ( fp[i] );
        fp[i] = fopen ( filename[i], "rb" );
        if ( !fp[i] )
        {
            printf ( "Cannot open device file %d!\n", i - 1 );
            return 1;
        }
    }

    fclose ( fp[4] );
    fp[4] = fopen ( filename[5], "rb" );
    printf ( "\n%s\n", filename[5] );
    if ( !fp[4] )
    {
        printf ( "Cannot open metadata file!\n" );
        return 1;
    }

    /** Open output file for merge **/
    fp[0] = fopen ( filename[4], "wb" );
    if ( !fp[0] )
    {
        printf ( "Cannot write output file!\n" );
        return 1;
    }

    prepare_key ( ( unsigned char * ) "password", 8, &rc4key );
    /** perform the merge **/
    printf ( "Start merge ... " );
    fflush ( stdout );
#ifdef BENCHMARK
    gettimeofday ( &start, NULL );
#endif
    merge_file ( fp[0], &fp[1], fp[4], &rc4key );
#ifdef BENCHMARK
    gettimeofday ( &end, NULL );
#endif
    printf ( "Done\n" );
    fflush ( stdout );
#ifdef BENCHMARK
    elapsed = ( ( end.tv_sec-start.tv_sec ) * 1000000.0f + end.tv_usec - start.tv_usec ) / 1000.0f;
    printf ( "merge time: %.3f ms\n\n", elapsed );
#endif

    /** Close ALL files **/
    for ( i = 0; i <= 3; i++ )
    {
        fclose ( fp[i] );
    }

    status = 0;
    for ( i = 0; i <= 5; i++ )
    {
#ifdef CHECKING
        printf ( "Checking file %s ... ", filename[i] );
        ascii = check_sha256_sum ( filename[i], ( unsigned char* ) assumed[i] );

        if ( ascii == NULL )
        {
            printf ( "CORRECT!\n" );
        }
        else
        {
            if ( memcmp ( ascii, assumed[i], 64 ) == 0 )
            {
                printf ( "Memory Error!\n" );
            }
            else
            {
                printf ( "FALSE!\n" );
                printf ( "%-12s%s\n%-12s%s\n", "Calculated:" , ascii, "Assumed:", assumed[i] );
                free ( ascii );
            }
            status++;
        }
#endif
        remove ( filename[i] );
    }
    return status;
}

