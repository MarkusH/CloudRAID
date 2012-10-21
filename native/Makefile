FEATURES =

ifdef DEBUG
FEATURES += -DDEBUG=${DEBUG}
CFLAGS = -Wall -pedantic -g ${FEATURES}
else
CFLAGS = -O3 ${FEATURES}
endif

ifdef EMPTY_SALT
FEATURES += -DEMPTY_SALT=1
endif

ifndef DESTDIR
DESTDIR = /
endif

ifeq ($(ARCH),-m64)
    TARGET = 64
else
    ifeq ($(ARCH),-m32)
        TARGET =
    else
        TARGET =
        ARCH =
    endif
endif

CC = gcc
ifdef C
CC = $(C)
endif

ifndef JAVA_HOME
    exit 1
endif

JAVASOURCE = ./../core/src
JAVABIN = ./../core/bin
SRC = raid5.c sha2.c rc4.c utils.c
OBJ = $(SRC:.c=.o)
LIB = ./build/usr/lib/libcloudraid.so
ifneq ($(ARCH),)
    LIB = ./build/lib/linux$(TARGET)/libcloudraid.so
endif
CFLAGS += $(ARCH)

all: compile

jcompile:
	mkdir -p ${JAVABIN}/de/dhbw_mannheim/cloudraid/core/impl/jni/
	javac -sourcepath ${JAVASOURCE}/de/dhbw_mannheim/cloudraid/core/impl/jni/ -classpath ${JAVASOURCE} -d ${JAVABIN} ${JAVASOURCE}/de/dhbw_mannheim/cloudraid/core/impl/jni/RaidAccessInterface.java
	javah -classpath ${JAVABIN}:. -d . de.dhbw_mannheim.cloudraid.core.impl.jni.RaidAccessInterface

compile: ${OBJ}
	mkdir -p ./build/usr/lib
	${CC} ${CFLAGS} -shared -o ${LIB} ${OBJ}

bundlecompile: ${OBJ}
ifeq ($(ARCH),)
	exit 1
endif
	mkdir -p ./build/lib/linux${TARGET}
	${CC} ${CFLAGS} -shared -o ${LIB} ${OBJ}

crosscompile:
	make bundlecompile ARCH=-m32
	make bundleinstall
	make clean
	make bundlecompile ARCH=-m64
	make bundleinstall

.c.o:
	${CC} -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -fPIC ${CFLAGS} -c $<

install:
	cp -rvf ./build/* ${DESTDIR}

bundleinstall:
	mkdir -p ../core/lib
	cp -rvf ./build/lib/* ../core/lib/

clean:
	rm -rf ./build
	rm -rf ./testing
	rm -f ${OBJ}
	rm -f *.dat

test:
	make clean
	make DEBUG=${DEBUG} EMPTY_SALT=1
	mkdir -p ./testing/
	${CC} test_sha256.c ${CFLAGS} ${LIB} -o ./testing/test_sha256
	${CC} test_utils.c ${CFLAGS} ${LIB} -o ./testing/test_utils
	${CC} test_raid5.c -DCHECKING=1 -DFILEID=1 ${CFLAGS} ${LIB} -o ./testing/test_raid5

run-test:
	./testing/test_sha256
	./testing/test_utils
	./testing/test_raid5
	./test_raid5.sh

benchmark:
	${CC} test_raid5.c -DBENCHSIZE=1073741824 -DBENCHMARK=1 -DFILEID=1 ${CFLAGS} ${LIB} -o ./testing/test_raid5_1gib
	${CC} test_raid5.c -DBENCHSIZE=524288000 -DBENCHMARK=1 -DFILEID=1 ${CFLAGS} ${LIB} -o ./testing/test_raid5_500mib
	${CC} test_raid5.c -DBENCHSIZE=262144000 -DBENCHMARK=1 -DFILEID=1 ${CFLAGS} ${LIB} -o ./testing/test_raid5_250mib
	${CC} test_raid5.c -DBENCHSIZE=104857600 -DBENCHMARK=1 -DFILEID=1 ${CFLAGS} ${LIB} -o ./testing/test_raid5_100mib
	${CC} test_raid5.c -DBENCHSIZE=52428800 -DBENCHMARK=1 -DFILEID=1 ${CFLAGS} ${LIB} -o ./testing/test_raid5_50mib
	${CC} test_raid5.c -DBENCHSIZE=20971520 -DBENCHMARK=1 -DFILEID=1 ${CFLAGS} ${LIB} -o ./testing/test_raid5_20mib
	${CC} test_raid5.c -DBENCHSIZE=1048576 -DBENCHMARK=1 -DFILEID=1 ${CFLAGS} ${LIB} -o ./testing/test_raid5_1mib
	${CC} test_raid5.c -DBENCHSIZE=102400 -DBENCHMARK=1 -DFILEID=1 ${CFLAGS} ${LIB} -o ./testing/test_raid5_100kib
	${CC} test_raid5.c -DBENCHSIZE=10240 -DBENCHMARK=1 -DFILEID=1 ${CFLAGS} ${LIB} -o ./testing/test_raid5_10kib
