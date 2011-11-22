all: jcompile compile

jcompile:
	-javac RaidAccessInterface.java
	-javah -classpath . RaidAccessInterface

compile:
	-mkdir -p build/usr/lib
	-gcc -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -fPIC -O3 -shared -o build/usr/lib/libraid5.so raid5.c

install:
	-cp -r build/* /

clean:
	-rm -rf build
	-rm -f *.class
