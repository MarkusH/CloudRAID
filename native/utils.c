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
#include "sha2.h"

#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <string.h>

#if defined(_WIN32) || defined(_WIN64)
#include <Windows.h>
#endif

LIBEXPORT int create_salt(unsigned char *salt)
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

LIBEXPORT void print_salt(FILE *__stream, unsigned char *salt, const unsigned int saltlen)
{
    int i;
    for(i = 0; i < saltlen; i++) {
        fprintf(__stream, "%02x", salt[i]);
    }
}

/**
 * Compute the HMAC for the given key and salt. The HMAC will be stored in `*hash`
 * as 32 byte digest.
 */
LIBEXPORT unsigned long hmac(const unsigned char *key, const unsigned int keylen, const unsigned char *salt, const unsigned int saltlen, unsigned char *hash)
{
#define B SHA256_BLOCK_LENGTH
#define L SHA256_DIGEST_LENGTH
#define IPAD 0x36
#define OPAD 0x5c
    unsigned char *k0 = NULL, *k0_ipad = NULL, *k0_ipad_text = NULL, *h_k0_ipad_text = NULL,
                   *k0_opad = NULL, *k0_opad_h_k0_ipad_text = NULL, *h_k0_opad_h_k0_ipad_text = NULL;
    unsigned long status = 0;
    unsigned int itr = 0;
    struct sha256_ctx sha256_ctx;
    if(key == NULL) {
        return 0x0001;
    }

    if(salt == NULL) {
        return 0x0002;
    }

    if(hash == NULL) {
        return 0x0004;
    }

    k0 = (unsigned char *) calloc(B, sizeof(unsigned char));
    if(k0 == NULL) {
        status = 0x0008;
        goto end;
    }
    if(keylen <= B) {  /* STEP 1 and STEP 3*/
        memcpy(k0, key, keylen);
        /* FOR STEP 3: No need to append zeros here, since calloc already filles the memory with zeros */
    } else if(keylen > B) {  /* STEP 2 */
        /* The key is larger than the hash internal block size, thus hash it */
        sha256_init(&sha256_ctx);
        sha256_update(key, keylen, &sha256_ctx);
        sha256_final(k0, &sha256_ctx);
        /* No need to append zeros here, since calloc already filles the memory with zeros */
    }

    /* STEP 4: byte-wise XOR the key with the IPAD */
    k0_ipad = (unsigned char *) calloc(B, sizeof(unsigned char));
    if(k0_ipad == NULL) {
        status = 0x0010;
        goto end;
    }
    for(itr = 0; itr < B; itr++) {
        k0_ipad[itr] = k0[itr] ^ IPAD;
    }

    /* STEP 5: Appending the text to the ipadded key */
    k0_ipad_text = (unsigned char *) calloc(B + saltlen, sizeof(unsigned char));
    if(k0_ipad_text == NULL) {
        status = 0x0020;
        goto end;
    }
    memcpy(k0_ipad_text, k0_ipad, B);
    memcpy(&k0_ipad_text[B], salt, saltlen);

    /* STEP 6: hash the output of step 5 */
    h_k0_ipad_text = (unsigned char *) calloc(L, sizeof(unsigned char));
    if(h_k0_ipad_text == NULL) {
        status = 0x0040;
        goto end;
    }
    memset(&sha256_ctx, 0, sizeof(struct sha256_ctx));
    sha256_init(&sha256_ctx);
    sha256_update(k0_ipad_text, B + saltlen, &sha256_ctx);
    sha256_final(h_k0_ipad_text, &sha256_ctx);

    /* STEP 7: byte-wise XOR the key with the OPAD */
    k0_opad = (unsigned char *) calloc(B, sizeof(unsigned char));
    if(k0_opad == NULL) {
        status = 0x0080;
        goto end;
    }
    for(itr = 0; itr < B; itr++) {
        k0_opad[itr] = k0[itr] ^ OPAD;
    }

    /* STEP 8: append the data from step 6 to the opadded key */
    k0_opad_h_k0_ipad_text = (unsigned char *) calloc(B + L, sizeof(unsigned char));
    if(k0_opad_h_k0_ipad_text == NULL) {
        status = 0x0100;
        goto end;
    }
    memcpy(k0_opad_h_k0_ipad_text, k0_opad, B);
    memcpy(&k0_opad_h_k0_ipad_text[B], h_k0_ipad_text, L);

    /* STEP 9: hash the output from step 8 */
    h_k0_opad_h_k0_ipad_text = (unsigned char *) calloc(L, sizeof(unsigned char));
    if(h_k0_opad_h_k0_ipad_text == NULL) {
        status = 0x0200;
        goto end;
    }
    memset(&sha256_ctx, 0, sizeof(struct sha256_ctx));
    sha256_init(&sha256_ctx);
    sha256_update(k0_opad_h_k0_ipad_text, B + L, &sha256_ctx);
    sha256_final(h_k0_opad_h_k0_ipad_text, &sha256_ctx);

    /* STEP 10: copy the final hash to the call-by-reference hash */
    memcpy(hash, h_k0_opad_h_k0_ipad_text, L);

end:
    if(h_k0_opad_h_k0_ipad_text != NULL) {
        free(h_k0_opad_h_k0_ipad_text);
    }
    if(k0_opad_h_k0_ipad_text != NULL) {
        free(k0_opad_h_k0_ipad_text);
    }
    if(k0_opad != NULL) {
        free(k0_opad);
    }
    if(h_k0_ipad_text != NULL) {
        free(h_k0_ipad_text);
    }
    if(k0_ipad_text != NULL) {
        free(k0_ipad_text);
    }
    if(k0_ipad != NULL) {
        free(k0_ipad);
    }
    if(k0 != NULL) {
        free(k0);
    }

    return status;
}

/**
 * Compute the HMAC for the given key and salt. The HMAC will be stored in `*ascii`
 * as the well-known 64 byte character array. This is the printable representation
 *
 * See hmac()
 */
LIBEXPORT unsigned long hmac_hex(const unsigned char *key, const unsigned int keylen, const unsigned char *salt, const unsigned int saltlen, unsigned char *ascii)
{
    unsigned char hash[SHA256_DIGEST_LENGTH] = {0x00};
    int i = 0;
    unsigned long ret = hmac(key, keylen, salt, saltlen, hash);
    for(i = 0; i < SHA256_DIGEST_LENGTH; i++) {
        sprintf(((char *) ascii) + i * 2, "%02x", ((unsigned char *) hash) [i]);
    }
    return ret;
}
