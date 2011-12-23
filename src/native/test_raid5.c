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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main ( void )
{
    int i;
    int status;
    unsigned char *ascii;
    FILE *fp[4];
    char *filename[] = {"test_raid5.dat",
                        "test_raid5.dev0.dat",
                        "test_raid5.dev1.dat",
                        "test_raid5.dev2.dat",
                        "test_raid5.out.dat"
                       };

    char *assumed[] = {"3b6f5cf4c8c3e8b6c6894da81c1fcea588db14d088c5970c1b98faed940b2ce4",
                                "ae3956a5b5c993b66312a187aa89f935c984f516426ee5ec6fbf028518f04875",
                                "a71d099882bc3f1d0a6e44589804a075d774998ec7ee03941da4ad9168a630f6",
                                "52854ea24eb536f1bc17ca9dc36828dacdbc34f077540067cede2100ec43e058",
                                "3b6f5cf4c8c3e8b6c6894da81c1fcea588db14d088c5970c1b98faed940b2ce4"
                               };

    printf ( "Running test for RAID5:\n\n" );

    /* Create test file */
    fp[0] = fopen ( filename[0], "wb" );
    if ( !fp[0] )
    {
        printf ( "Cannot write test file!\n" );
        return 1;
    }
    for ( i = 0; i < 13056; i++ )
    {
        /* Every device becomes the parity twice. dev2 three
           times but the third time only 512+256 Bytes
           (3/4 BLOCKSIZE).
           2 * BLOCKSIZE * 6 + 3/4 BLOCKSIZE = 13056 */
        fprintf ( fp[0], "%c", ( i*i + i ) % 256 );
    }
    fclose ( fp[0] );

    /* Open test file for split */
    fp[0] = fopen ( filename[0], "rb" );
    if ( !fp[0] )
    {
        printf ( "Cannot read test file!\n" );
        return 1;
    }

    /* Create device files */
    for ( i = 1; i <= 3; i++ )
    {
        fp[i] = fopen ( filename[i], "wb" );
        if ( !fp[i] )
        {
            printf ( "Cannot create device file %d!\n", i - 1 );
            return 1;
        }
    }

    /* perform the split */
    split_byte ( fp[0], &fp[1] );

    /* Close the input file */
    fclose ( fp[0] );

    /* Close and reopen device files for merge */
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

    /* Open output file for merge */
    fp[0] = fopen ( filename[4], "wb" );
    if ( !fp[0] )
    {
        printf ( "Cannot write output file!\n" );
        return 1;
    }

    /* perform the merge */
    merge_byte ( fp[0], &fp[1] );

    /* Close ALL files */
    for ( i = 0; i <= 3; i++ )
    {
        fclose ( fp[i] );
    }

    status = 0;
    for ( i = 0; i <= 4; i++ )
    {
        printf ( "Checking file %s ... ", filename[i] );
        ascii = check_sha256_sum ( filename[i], (unsigned char*) assumed[i] );

        if ( ascii == NULL )
        {
            printf ( "CORRECT!\n\n" );
            printf ( "%-12s%s\n%-12s%s\n\n", "Calculated:" , ascii, "Assumed:", assumed[i] );
        }
        else
        {
            if ( memcmp ( ascii, assumed[i], 64 ) == 0 )
            {
                printf ( "Memory Error!\n\n" );
            }
            else
            {
                printf ( "FALSE!\n" );
                printf ( "%-12s%s\n%-12s%s\n\n", "Calculated:" , ascii, "Assumed:", assumed[i] );
                free ( ascii );
            }
            status++;
        }
        //remove ( filename[i] );
    }

    return status;
}

