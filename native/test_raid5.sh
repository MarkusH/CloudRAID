#!/bin/bash
TOTERR=0
for fileid in 1 2 3 ; do
    for i in {0..28} ; do
        BS=$(($i*256))
        gcc test_raid5.c -DCHECKING=0 -DBENCHSIZE=${BS} -DENCRYPT_DATA=1 -DFILEID=${fileid}\
            -Wall -pedantic -g "./build/usr/lib/libcloudraid.so" \
            -o "./testing/test_raid5_${BS}"

        ./testing/test_raid5_${BS} > /dev/null
        RC=$?
        if [ $RC -ne 0 ] ; then
            TOTERR=$(($TOTERR+1))
        fi

        in=$(sha256sum test_raid5.dat)
        out=$(sha256sum test_raid5.out.dat)
        in=${in:0:64}
        out=${out:0:64}
        if [ "${in}" == "${out}" ] ; then
            echo "${BS} CORRECT"
        else
            echo "${BS} FALSE"
            if [ $RC -eq 0 ] ; then
                TOTERR=$(($TOTERR+1))
            fi
        fi
    done
    rm test_raid5.*dat testing/test_raid5_*
done
exit $TOTERR
