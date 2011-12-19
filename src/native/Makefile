ifdef DEBUG
CFLAGS = -Wall -pedantic -g
else
CFLAGS = -O3
endif

CC = gcc

JAVASOURCE = ./../../src
JAVABIN = ./../../bin
SRC = raid5.c sha256.c
OBJ = raid5.o sha256.o
LIB = ./build/usr/lib/libcloudraid.so

all: compile

jcompile:
	mkdir -p ${JAVABIN}/de/dhbw/mannheim/cloudraid/jni/
	javac -sourcepath ${JAVASOURCE}/de/dhbw/mannheim/cloudraid/jni/ -classpath ${JAVASOURCE} -d ${JAVABIN} ${JAVASOURCE}/de/dhbw/mannheim/cloudraid/jni/RaidAccessInterface.java
	javah -classpath ${JAVABIN}:. -d . de.dhbw.mannheim.cloudraid.jni.RaidAccessInterface

compile: jcompile ${OBJ}
	mkdir -p ./build/usr/lib
	${CC} ${CFLAGS} -shared -o ${LIB} ${OBJ}

${OBJ}:
	${CC} ${SRC} -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -fPIC ${CFLAGS} -c

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
	${CC} test_raid5.c ${CFLAGS} ${LIB} -o ./test/test_raid5