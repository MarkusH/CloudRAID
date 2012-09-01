/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * FILE: sha2.h
 * AUTHOR: Aaron D. Gifford <me@aarongifford.com>
 *
 * A licence was granted to the ASF by Aaron on 4 November 2003.
 */

#ifndef __SHA2_H__
#define __SHA2_H__

#include "defines.h"
#include <stdio.h>
#include <stddef.h>

#ifdef __cplusplus

extern "C" {
#endif

    /*** SHA-256 defines ***/
#define SHA256_BLOCK_LENGTH 64
#define SHA256_DIGEST_LENGTH 32
#define SHA256_DIGEST_STRING_LENGTH (SHA256_DIGEST_LENGTH * 2 + 1)

    /*** SHA-256 context ***/
    typedef struct sha256_ctx {
        unsigned int state[8];
        unsigned long bitcount;
        unsigned char buffer[SHA256_BLOCK_LENGTH];
    } sha256_ctx;

    /*** SHA-256 functions ***/
    void sha256_init(sha256_ctx *context);
    void sha256_update(const unsigned char *data, size_t len, sha256_ctx *context);
    void sha256_final(unsigned char digest[SHA256_DIGEST_LENGTH], sha256_ctx *context);
    char *sha256_end(char buffer[], sha256_ctx *context);
    char *sha256_data(const unsigned char *data, size_t len, char digest[SHA256_DIGEST_STRING_LENGTH]);

    LIBEXPORT int build_sha256_sum_file(FILE *filename, unsigned char *hash);
    LIBEXPORT int build_sha256_sum(char *filename, unsigned char *hash);
    LIBEXPORT unsigned char *check_sha256_sum(char *filename, unsigned char *hash);
#ifdef __cplusplus
}
#endif /* __cplusplus */

#endif /* __SHA2_H__ */

