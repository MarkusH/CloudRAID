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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main ( void )
{
    short i;
    void *resblock;
    char *ascii;
    FILE *fp;
    char *filename = "test_sha256.dat";
    char *assumed = "40aff2e9d2d8922e47afd4648e6967497158785fbd1da870e7110266bf944880\0";

    printf ( "Running test for SHA256:\n\n");

    fp = fopen(filename, "wb");
    if ( !fp )
    {
        printf ( "Cannot write test file!\n");
        return 1;
    }
    for (i = 0x00; i <= 0xFF; i++) {
        fprintf(fp, "%c", i);
    }
    fclose(fp);

    resblock = malloc ( 32 );
    ascii = ( char * ) malloc ( 65 );
    if (!resblock || !ascii) {
        printf("Cannot allocate memory");
        return 1;
    }

    fp = fopen ( filename, "rb" );
    if ( !fp )
    {
        printf ( "Cannot read test file!\n");
        return 1;
    }

    sha256_stream ( fp, resblock );
    ascii_from_resbuf ( ascii, resblock );

    printf ( "%-12s%s\n%-12s%s\n", "Calculated:" , ascii, "Assumed:", assumed);
    if (memcmp(ascii, assumed, 64) == 0) {
        printf( "CORRECT!\n\n" );
    }
    else {
        printf( "FALSE!\n\n" );
    }

    fclose ( fp );

    remove( filename );

    free ( resblock );
    free ( ascii );
    return 0;
}

