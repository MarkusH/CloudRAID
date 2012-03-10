#!/bin/bash

for i in {0..28} ; do
    BS=$(($i*256))
    gcc test_raid5.c -DCHECKING=0 -DBENCHSIZE=${BS} -DENCRYPT_DATA=1 \
        -Wall -pedantic -g "./build/usr/lib/libcloudraid.so" \
        -o "./testing/test_raid5_${BS}"

    ./testing/test_raid5_${BS} > /dev/null

    in=$(sha256sum test_raid5.dat)
    out=$(sha256sum test_raid5.out.dat)
    in=${in:0:64}
    out=${out:0:64}
    if [ "${in}" == "${out}" ] ; then
        echo "${BS} CORRECT"
    else
        echo "${BS} FALSE"
    fi
done
