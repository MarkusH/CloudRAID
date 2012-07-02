FEATURES = -DENCRYPT_DATA=1

ifdef DEBUG
FEATURES += -DDEBUG=${DEBUG}
CFLAGS = -Wall -pedantic -g ${FEATURES}
else
CFLAGS = -O3 ${FEATURES}
endif

ifndef DESTDIR
DESTDIR = /
endif

CC = gcc

JAVASOURCE = ./../core/src
JAVABIN = ./../core/bin
SRC = raid5.c sha256.c rc4.c
OBJ = $(SRC:.c=.o)
LIB = ./build/usr/lib/libcloudraid.so

all: compile

jcompile:
	mkdir -p ${JAVABIN}/de/dhbw_mannheim/cloudraid/core/impl/jni/
	javac -sourcepath ${JAVASOURCE}/de/dhbw_mannheim/cloudraid/core/impl/jni/ -classpath ${JAVASOURCE} -d ${JAVABIN} ${JAVASOURCE}/de/dhbw_mannheim/cloudraid/core/impl/jni/RaidAccessInterface.java
	javah -classpath ${JAVABIN}:. -d . de.dhbw_mannheim.cloudraid.core.impl.jni.RaidAccessInterface

compile: ${OBJ}
	mkdir -p ./build/usr/lib
	${CC} ${CFLAGS} -shared -o ${LIB} ${OBJ}

.c.o:
	${CC} -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -fPIC ${CFLAGS} -c $<

install:
	cp -rvf ./build/* ${DESTDIR}

clean:
	rm -rf ./build
	rm -rf ./testing
	rm -f ${OBJ}
	rm -f *.dat

test:
	mkdir -p ./testing/
	${CC} test_sha256.c ${CFLAGS} ${LIB} -o ./testing/test_sha256
	${CC} test_raid5.c -DCHECKING=1 -DFILEID=1 ${CFLAGS} ${LIB} -o ./testing/test_raid5

run-test:
	./testing/test_sha256
	./testing/test_raid5
	./test_raid5.sh

benchmark:
	${CC} test_raid5.c -DBENCHSIZE=1073741824 -DBENCHMARK=1 ${CFLAGS} ${LIB} -o ./testing/test_raid5_1gib
	${CC} test_raid5.c -DBENCHSIZE=524288000 -DBENCHMARK=1 ${CFLAGS} ${LIB} -o ./testing/test_raid5_500mib
	${CC} test_raid5.c -DBENCHSIZE=262144000 -DBENCHMARK=1 ${CFLAGS} ${LIB} -o ./testing/test_raid5_250mib
	${CC} test_raid5.c -DBENCHSIZE=104857600 -DBENCHMARK=1 ${CFLAGS} ${LIB} -o ./testing/test_raid5_100mib
	${CC} test_raid5.c -DBENCHSIZE=52428800 -DBENCHMARK=1 ${CFLAGS} ${LIB} -o ./testing/test_raid5_50mib
	${CC} test_raid5.c -DBENCHSIZE=20971520 -DBENCHMARK=1 ${CFLAGS} ${LIB} -o ./testing/test_raid5_20mib
	${CC} test_raid5.c -DBENCHSIZE=1048576 -DBENCHMARK=1 ${CFLAGS} ${LIB} -o ./testing/test_raid5_1mib
	${CC} test_raid5.c -DBENCHSIZE=102400 -DBENCHMARK=1 ${CFLAGS} ${LIB} -o ./testing/test_raid5_100kib
	${CC} test_raid5.c -DBENCHSIZE=10240 -DBENCHMARK=1 ${CFLAGS} ${LIB} -o ./testing/test_raid5_10kib
