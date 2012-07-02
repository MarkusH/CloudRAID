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

#include <stdio.h>
#include <stddef.h>
#ifndef RAID5BLOCKSIZE
#define RAID5BLOCKSIZE 1024
#endif

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
        unsigned int missing;
    } raid5md;

    static const unsigned int RAID5_BLOCKSIZE = RAID5BLOCKSIZE;
    static const unsigned char RAID5_METADATA_VERSION = 1;

    void merge_byte_block(const unsigned char *in, const size_t in_len[], const unsigned int parity_pos, const unsigned int dead_device, const unsigned int missing, unsigned char *out, size_t *out_len);
    void split_byte_block(const unsigned char *in, const size_t in_len, unsigned char *out, size_t out_len[]);

    DLLEXPORT int merge_file(FILE *out, FILE *devices[], FILE *meta, rc4_key *key);
    DLLEXPORT int split_file(FILE *in, FILE *devices[], FILE *meta, rc4_key *key);

    DLLEXPORT int cmp_metadata(raid5md *md1, raid5md *md2);
    DLLEXPORT int cmp_metadata_hash(raid5md *md1, raid5md *md2, const int idx);
    DLLEXPORT int create_metadata(FILE *devices[], raid5md *md);
    DLLEXPORT void new_metadata(raid5md *md);
    DLLEXPORT void print_metadata(raid5md *md);
    DLLEXPORT int read_metadata(FILE *fp, raid5md *md);
    DLLEXPORT void set_metadata_hash(raid5md *md, const int idx, const unsigned char hash[65]);
    DLLEXPORT int write_metadata(FILE *fp, raid5md *md);

#ifdef __cplusplus
}
#endif

#endif
