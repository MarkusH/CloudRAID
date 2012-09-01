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

#ifndef UTILS_H
#define UTILS_H 1

#include "defines.h"

#include <stdio.h>

#define ENCRYPTION_SALT_BYTES 256

#ifdef __cplusplus
extern "C"
{
#endif

    LIBEXPORT int create_salt(unsigned char *salt);
    LIBEXPORT void print_salt(FILE *__stream, unsigned char *salt, const unsigned int saltlen);
    LIBEXPORT unsigned long hmac(const unsigned char *key, const unsigned int keylen, const unsigned char *salt, const unsigned int saltlen, unsigned char *hash);
    LIBEXPORT unsigned long hmac_hex(const unsigned char *key, const unsigned int keylen, const unsigned char *salt, const unsigned int saltlen, unsigned char *ascii);

#ifdef __cplusplus
}
#endif

#endif
