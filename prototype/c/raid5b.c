#include <stdio.h>
#include <stdlib.h>

int main(int argc, const char* argv[]) {
  FILE * pFile;
  FILE * outFile0;
  FILE * outFile1;
  FILE * outFile2;
  long lSize;
  unsigned char * buffer;
  size_t result;
  unsigned char outBuf[1];

  if (argc == 1) {
    printf("Please insert a path to a file.");
    exit (4);
  }

  pFile = fopen ( argv[1] , "rb" );
  outFile0 = fopen("/tmp/a.txt", "wb");
  outFile1 = fopen("/tmp/b.txt", "wb");
  outFile2 = fopen("/tmp/c.txt", "wb");
  if (pFile==NULL) {fputs ("File error",stderr); exit (1);}
 
  // obtain file size:
  fseek (pFile , 0 , SEEK_END);
  lSize = ftell (pFile);
  rewind (pFile);

  // allocate memory to contain the whole file:
  buffer = (unsigned char*) malloc (sizeof(char)*lSize);
  if (buffer == NULL) {fputs ("Memory error",stderr); exit (2);}

  // copy the file into the buffer:
  result = fread (buffer,1,lSize,pFile);
  if (result != lSize) {fputs ("Reading error",stderr); exit (3);}

  /* the whole file is now loaded in the memory buffer. */

  unsigned int pot[] = {1,1<<2,1<<3,1<<4,1<<5,1<<6,1<<7,1<<8,1<<9,1<<10,1<<11,1<<12,1<<13,1<<14,1<<15};

  int i;
  int j;
  int count = 0;
  int doubleByte;
  int theBit;
  unsigned char byte0;
  unsigned char byte1;
  unsigned char byte2;
  int theOldBit = 0;
  for (i = 0; i < lSize; i += 2) {
    byte0 = 0;
    byte1 = 0;
    byte2 = 0;
    int temp = buffer[i];
    doubleByte = (temp << 8) + buffer[i + 1];
    for (j = 15; j >= 0; j--) {
	theBit = (doubleByte & pot[j]) >> j;
	switch (count % 3) {
	case 0:
	  if ((j % 2) == 1) {
	    byte0 = (byte0 << 1) + theBit;
	  } else {
	    byte1 = (byte1 << 1) + theBit;
	    byte2 = (byte2 << 1) + (theBit ^ theOldBit);
	  }
	  break;
	case 1:
	  if ((j % 2) == 1) {
	    byte1 = (byte1 << 1) + theBit;
	  } else {
	    byte2 = (byte2 << 1) + theBit;
	    byte0 = (byte0 << 1) + (theBit ^ theOldBit);
	  }
	  break;
	case 2:
	  if ((j % 2) == 1) {
	    byte2 = (byte2 << 1) + theBit;
	  } else {
	    byte0 = (byte0 << 1) + theBit;
	    byte1 = (byte1 << 1) + (theBit ^ theOldBit);
	  }
	  break;
	}
	theOldBit = theBit;
    }
    count++;
    outBuf[0] = byte0;
    fwrite (outBuf, 1, sizeof(outBuf), outFile0);
    outBuf[0] = byte1;
    fwrite (outBuf, 1, sizeof(outBuf), outFile1);
    outBuf[0] = byte2;
    fwrite (outBuf, 1, sizeof(outBuf), outFile2);
  }

  // terminate
  fclose (pFile);
  fclose (outFile0);
  fclose (outFile1);
  fclose (outFile2);
  free (buffer);
  return 0;
}
