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

#ifndef RAID5_H
#define RAID5_H 1

#include "defines.h"
#include "rc4.h"
#include "utils.h"
#include "sha256.h"

#include <stdio.h>
#include <stddef.h>

#ifndef ENCRYPT_DATA
#define ENCRYPT_DATA 1
#endif

#ifndef RAID5BLOCKSIZE
#define RAID5BLOCKSIZE 1024
#endif

#if SHA256_BLOCKSIZE % (RAID5BLOCKSIZE * 2) != 0
#error "invalid RAID5BLOCKSIZE. Twice the RAID5 BLOCKSIZE must be a factor of SHA256_BLOCKSIZE"
#endif

#define _VERSION_ "0.0.2prealpha"
#define _NAME_ "CloudRAID-RAID5"
#define _VENDOR_ "cloudraid"

#define SUCCESS_MERGE  0x0001
#define MEMERR_BUF     0x0002
#define MEMERR_SHA     0x0004
#define OPENERR_DEV0   0x0008
#define OPENERR_DEV1   0x0010
#define OPENERR_DEV2   0x0020
#define OPENERR_IN     0x0040
#define OPENERR_OUT    0x0080
#define METADATA_ERROR 0x0100
#define SUCCESS_SPLIT  0x0200

#define METADATA_MISS_DEV0    0x01
#define METADATA_MISS_DEV1    0x02
#define METADATA_MISS_DEV2    0x04
#define METADATA_MISS_IN      0x08
#define METADATA_MISS_VERSION 0x10
#define METADATA_MISS_MISSING 0x20
#define METADATA_MEMORY_ERROR 0x80


#ifdef __cplusplus
extern "C"
{
#endif

    typedef struct raid5md {
        unsigned char version;
        unsigned char hash_dev0[65];
        unsigned char hash_dev1[65];
        unsigned char hash_dev2[65];
        unsigned char hash_in[65];
        unsigned char salt[ENCRYPTION_SALT_BYTES];
        unsigned int missing;
    } raid5md;

    static const unsigned char RAID5_METADATA_VERSION = 2;
    static const unsigned int RAID5_METADATA_BYTES = 2 + 4*64 + ENCRYPTION_SALT_BYTES + 4;

    void merge_byte_block(const unsigned char *in, const size_t in_len[], const unsigned int parity_pos, const unsigned int dead_device, const unsigned int missing, unsigned char *out, size_t *out_len);
    void split_byte_block(const unsigned char *in, const size_t in_len, unsigned char *out, size_t out_len[]);

    LIBEXPORT int merge_file(FILE *out, FILE *devices[], FILE *meta, const unsigned char *key, const int keylen);
    LIBEXPORT int split_file(FILE *in, FILE *devices[], FILE *meta, const unsigned char *key, const int keylen);

    LIBEXPORT int cmp_metadata(raid5md *md1, raid5md *md2);
    LIBEXPORT int cmp_metadata_hash(raid5md *md1, raid5md *md2, const int idx);
    LIBEXPORT int create_metadata(FILE *devices[], raid5md *md);
    LIBEXPORT void new_metadata(raid5md *md);
    LIBEXPORT void print_metadata(raid5md *md);
    LIBEXPORT int read_metadata(FILE *fp, raid5md *md);
    LIBEXPORT void set_metadata_hash(raid5md *md, const int idx, const unsigned char hash[65]);
    LIBEXPORT int write_metadata(FILE *fp, raid5md *md);

#ifdef __cplusplus
}
#endif

#endif
