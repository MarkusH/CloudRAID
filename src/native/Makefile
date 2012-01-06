FEATURES = -DENCRYPT_DATA=1 -DCALC_SHA256=1

ifdef DEBUG
CFLAGS = -Wall -pedantic -g ${FEATURES}
else
CFLAGS = -O3 ${FEATURES}
endif

CC = gcc

JAVASOURCE = ./../../src
JAVABIN = ./../../bin
SRC = raid5.c sha256.c rc4.c
OBJ = $(SRC:.c=.o)
LIB = ./build/usr/lib/libcloudraid.so

all: compile

jcompile:
	mkdir -p ${JAVABIN}/de/dhbw/mannheim/cloudraid/jni/
	javac -sourcepath ${JAVASOURCE}/de/dhbw/mannheim/cloudraid/jni/ -classpath ${JAVASOURCE} -d ${JAVABIN} ${JAVASOURCE}/de/dhbw/mannheim/cloudraid/jni/RaidAccessInterface.java
	javah -classpath ${JAVABIN}:. -d . de.dhbw.mannheim.cloudraid.jni.RaidAccessInterface

compile: jcompile ${OBJ}
	mkdir -p ./build/usr/lib
	${CC} ${CFLAGS} -shared -o ${LIB} ${OBJ}

#${OBJ}:
#	${CC} ${SRC} -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -fPIC ${CFLAGS} -c

.c.o:
	${CC} -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -fPIC ${CFLAGS} -c $<

install:
	cp -rvf ./build/* /

clean:
	rm -rf ./build
	rm -rf ./test
	rm -f ${OBJ}
	rm -f de_dhbw_mannheim_cloudraid_jni_RaidAccessInterface.h

test: clean compile
	mkdir -p ./test/
	${CC} test_sha256.c ${CFLAGS} ${LIB} -o ./test/test_sha256
	${CC} test_raid5.c -DCHECKING=1 ${CFLAGS} ${LIB} -o ./test/test_raid5
	${CC} test_raid5.c -DBENCHSIZE=1073741824 -DBENCHMARK=1 ${CFLAGS} ${LIB} -o ./test/test_raid5_1gib
	${CC} test_raid5.c -DBENCHSIZE=524288000 -DBENCHMARK=1 ${CFLAGS} ${LIB} -o ./test/test_raid5_500mib
	${CC} test_raid5.c -DBENCHSIZE=262144000 -DBENCHMARK=1 ${CFLAGS} ${LIB} -o ./test/test_raid5_250mib
	${CC} test_raid5.c -DBENCHSIZE=104857600 -DBENCHMARK=1 ${CFLAGS} ${LIB} -o ./test/test_raid5_100mib
	${CC} test_raid5.c -DBENCHSIZE=52428800 -DBENCHMARK=1 ${CFLAGS} ${LIB} -o ./test/test_raid5_50mib
	${CC} test_raid5.c -DBENCHSIZE=20971520 -DBENCHMARK=1 ${CFLAGS} ${LIB} -o ./test/test_raid5_20mib
	${CC} test_raid5.c -DBENCHSIZE=1048576 -DBENCHMARK=1 ${CFLAGS} ${LIB} -o ./test/test_raid5_1mib
	${CC} test_raid5.c -DBENCHSIZE=102400 -DBENCHMARK=1 ${CFLAGS} ${LIB} -o ./test/test_raid5_100kib
	${CC} test_raid5.c -DBENCHSIZE=10240 -DBENCHMARK=1 ${CFLAGS} ${LIB} -o ./test/test_raid5_10kib

run-test:
	./test/test_sha256
	./test/test_raid5
