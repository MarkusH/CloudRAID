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
    unsigned char key[] = "no$xEe!1'-%FAn:z";
    unsigned char salt[] = "Kpq,@M&[/&C16>|LBA%m)ri=ExtRIGkDSjM$tdDSfP5v-Dp}g#3$`aPiO?J&#}I3e@`N+sSCm[-^q8;!hmUE1a-Yjr7)!CJ%4eA/?Fk1NXXwE^7I?2u9bxtylb?2}8,.52zl8!2vi^u#zrSbsl:;%Z%qiA(l6'OAc&}LpFZnkqW|',y,q_I|$Zm@/jYod)+?eV>_@yaTqHgb$sPJ+drvhmrTsl1%'E=leg[4[=Gga,Vyge6]\bU+<k#Dd?8P.aI&";
    unsigned char salted_key[ENCRYPTION_SALT_BYTES];
    int ret = 0;
    unsigned char expected[] = "no$xEe!1'-%FAn:zBA%m)ri=ExtRIGkDSjM$tdDSfP5v-Dp}g#3$`aPiO?J&#}I3e@`N+sSCm[-^q8;!hmUE1a-Yjr7)!CJ%4eA/?Fk1NXXwE^7I?2u9bxtylb?2}8,.52zl8!2vi^u#zrSbsl:;%Z%qiA(l6'OAc&}LpFZnkqW|',y,q_I|$Zm@/jYod)+?eV>_@yaTqHgb$sPJ+drvhmrTsl1%'E=leg[4[=Gga,Vyge6]no$xEe!1'-%FAn:z";

    printf("Running test for hmac:\n\n");
    printf("Salt: ");
    print_salt(stdout, salt);
    printf("\n");

    ret = hmac(key, 16, salt, salted_key);
    if(ret == 0) {
        printf("Salted Key: ");
        print_salt(stdout, salted_key);
        printf("\n");
        if(memcmp(salted_key, expected, ENCRYPTION_SALT_BYTES) == 0) {
            printf("Salted Key correct\n");
        } else {
            printf("Salted Key failed:");
        }
    } else {
        printf("Got result %d from hmac instead of 0\n", ret);
        return 2;
    }

    return 0;
}

