/*
 * Copyright 2011 - 2012 by the CloudRAID Team
 * see AUTHORS for more details
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

#include "utils.h"
#include "sha256.h"

#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <string.h>

#if defined(_WIN32) || defined(_WIN64)
#include <Windows.h>
#endif

int create_salt(unsigned char *salt)
{
#if defined(_WIN32) || defined(_WIN64)
    int i;
#else
    FILE *prd = NULL;
#endif
    if(salt == NULL) {
        return 1;
    }

    /* initialize random seed: */
#if defined(_WIN32) || defined(_WIN64)
    srand(time(NULL));
    /* create the salt */
    for(i = 0; i < ENCRYPTION_SALT_BYTES; i++) {
        salt[i] = (unsigned char) rand();
    }
#else
    prd = fopen("/dev/urandom", "rb");
    if(prd == NULL) {
        return 2;
    }
    fread(salt, sizeof(unsigned char), ENCRYPTION_SALT_BYTES, prd);
    fclose(prd);
#endif

    return 0;
}

void print_salt(FILE *__stream, unsigned char *salt)
{
    int i;
    for(i = 0; i < ENCRYPTION_SALT_BYTES; i++) {
        fprintf(__stream, "%02x", ((unsigned char *) salt) [i]);
    }
}

int gen_salted_key(const char *key, int keylen, unsigned char *salt, unsigned char *hash)
{
    int status = 0;

    if(key == NULL) {
        return 1;
    }

    if(keylen < MIN_KEY_LENGTH) {
        return 2;
    }

    if(keylen > ENCRYPTION_SALT_BYTES / 3) {
        return 3;
    }

    if(salt == NULL) {
        return 4;
    }

    if(hash == NULL) {
        return 5;
    }

    /* copy the given salt for further usage */
    memcpy(hash, salt, ENCRYPTION_SALT_BYTES);

    /* copy the key to the beginning and the end of the temp salt array */
    memcpy(hash, key, keylen);
    memcpy(&hash[ENCRYPTION_SALT_BYTES - keylen], key, keylen);

    return 0;
}
