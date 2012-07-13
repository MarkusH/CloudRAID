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
#include <string.h>

int main(void)
{
    char *key = "no$xEe!1'-%FAn:z";
    unsigned char *salt;
    unsigned char hash[65];
    int ret = 0;

    char *expected = "5c6d12e948525a8bd91b07b617b14314316812fe2943d269a97d90930429f33c";

    salt = (unsigned char *) calloc(ENCRYPTION_SALT_BYTES + 1, sizeof(unsigned char));
    if(salt == NULL) {
        printf("Cannot get memory for salt");
        return 1;
    }

    printf("Running test for hmac:\n\n");

    if(ret == 0) {
        printf("Salt is: ");
        print_salt(stdout, salt);
        printf("\n");

        ret = hmac(key, 16, salt, hash);
        if(ret == 0) {
            if(hash == NULL) {
                if(hash == NULL) {
                    printf("Error: Hash is NULL\n");
                }
                return 2;
            } else {
                printf("Hash is: %s\n", hash);
                memcpy(salt, key, 16);
                memcpy(&salt[ENCRYPTION_SALT_BYTES - 16], key, 16);
                if(memcmp(hash, expected, 64) == 0) {
                    printf("Hash Correct\n");
                } else {
                    printf("Hash test failed: expected:\n%s\n", expected);
                }
            }
        } else {
            printf("Got result %d from hmac instead of 0\n", ret);
            return 1;
        }
    } else {
        printf("Got result %d during salt creation\n", ret);
        return 1;
    }

    return 0;
}

