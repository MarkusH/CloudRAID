DFLAG = -Wall -pedantic -g -D_DEBUG
OFLAG = -O3
CBASE = ./../c/

all: jcompile compile

jcompile:
	javac ./RaidAccessInterface.java
	javah -classpath . -d ${CBASE} RaidAccessInterface

compile:
	mkdir -p ./build/usr/lib
ifdef DEBUG
	gcc -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -fPIC ${DFLAG} -shared -o ./build/usr/lib/libraid5bits.so -DUSE_BITS ${CBASE}raid5c.c
	gcc -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -fPIC ${DFLAG} -shared -o ./build/usr/lib/libraid5bytes.so -DUSE_BYTES ${CBASE}raid5c.c
else
	gcc -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -fPIC ${OFLAG} -shared -o ./build/usr/lib/libraid5bits.so -DUSE_BITS ${CBASE}raid5c.c
	gcc -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -fPIC ${OFLAG} -shared -o ./build/usr/lib/libraid5bytes.so -DUSE_BYTES ${CBASE}raid5c.c
endif

clangcompile:
	mkdir -p ./build/usr/lib
	clang -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -fPIC -O3 -shared -o ./build/usr/lib/libraid5bits.so -DUSE_BITS ${CBASE}raid5c.c
	clang -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -fPIC -O3 -shared -o ./build/usr/lib/libraid5bytes.so -DUSE_BYTES ${CBASE}raid5c.c

install:
	cp -r ./build/* /

clean:
	rm -rf ./build
	rm -f ${CBASE}RaidAccessInterface.h
	rm -f *.class
