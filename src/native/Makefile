DFLAG = -Wall -pedantic -g
OFLAG = -O3
JAVASOURCE = ./../../src
JAVABIN = ./../../bin

all: jcompile compile

jcompile:
	mkdir -p ${JAVABIN}/de/dhbw/mannheim/cloudraid/jni/
	javac -sourcepath ${JAVASOURCE}/de/dhbw/mannheim/cloudraid/jni/ -classpath ${JAVASOURCE} -d ${JAVABIN} ${JAVASOURCE}/de/dhbw/mannheim/cloudraid/jni/RaidAccessInterface.java
	javah -classpath ${JAVABIN}:. -d . de.dhbw.mannheim.cloudraid.jni.RaidAccessInterface

compile:
	mkdir -p ./build/usr/lib
ifdef DEBUG
	gcc -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -fPIC ${DFLAG} -shared -o ./build/usr/lib/libcloudraid.so raid5.c
else
	gcc -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -fPIC ${OFLAG} -shared -o ./build/usr/lib/libcloudraid.so raid5.c
endif

clangcompile:
	mkdir -p ./build/usr/lib
	clang -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -fPIC -O3 -shared -o ./build/usr/lib/libcloudraid.so raid5.c

install:
	cp -rvf ./build/* /

clean:
	rm -rf ./build
	rm -f de_dhbw_mannheim_cloudraid_jni_RaidAccessInterface.h
