#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "RaidAccessInterface.h"
#ifdef _DEBUG
    #define DEBUGPRINT(...)     printf(__VA_ARGS__);
#else
    #define DEBUGPRINT(...)
#endif

unsigned short EXPONENTS_LONG_FIRST[] = {
    0x8000,         0x2000,
    0x0800,         0x0200,
    0x0080,         0x0020,
    0x0008,         0x0002};

unsigned short EXPONENTS_LONG_SECOND[] = {
            0x4000,         0x1000,
            0x0400,         0x0100,
            0x0040,         0x0010,
            0x0004,         0x0001};

unsigned short EXPONENTS_SHORT[] = {
    0x80, 0x40, 0x20, 0x10,
    0x08, 0x04, 0x02, 0x01};

void split(FILE* in, FILE* devices[]) {
    unsigned char* chars, a, b, p;
    unsigned char parity_pos = 2, index, i;
    size_t rlen;
    unsigned short c;

    /* allocate memory that contains the two characters we read at once */
    chars = (unsigned char*) malloc(sizeof(char) * 2);
    if (chars == NULL) {
        printf("Memory error\n");
        exit(0x11);
    }

    rlen = fread(chars, 1, sizeof(char) * 2, in);
    DEBUGPRINT("rlen = %d\n", rlen);
    DEBUGPRINT("BEGIN WHILE\n");
    while (rlen > 0) {
        DEBUGPRINT("NEXT ITERATION\n");
        a = 0;
        b = 0;
        if (rlen == 2) {
            index = 7;
            DEBUGPRINT("chars[0] = %u\n", chars[0]);
            DEBUGPRINT("chars[1] = %u\n", chars[1]);
            c = chars[0];
            DEBUGPRINT("c = %u\n", c);
            c <<= 8;
            DEBUGPRINT("c = %u\n", c);
            c |= chars[1];
            DEBUGPRINT("c = %u\n", c);
            /* We only need every second element from this range */
            DEBUGPRINT("BEGIN FOR\n");
            for (i = 0; i <= 7; i++) {
                DEBUGPRINT("i = %u\n", i);
                DEBUGPRINT("EXPONENTS_LONG_FIRST[i] = %u\n", EXPONENTS_LONG_FIRST[i]);
                if (c & EXPONENTS_LONG_FIRST[i]) {
                    DEBUGPRINT("a = %u\n", a);
                    a |= (1 << index);
                    DEBUGPRINT("a = %u\n", a);
                }
                DEBUGPRINT("EXPONENTS_LONG_SECOND[i + 1] = %u\n", EXPONENTS_LONG_SECOND[i]);
                if (c & EXPONENTS_LONG_SECOND[i]) {
                    DEBUGPRINT("b = %u\n", b);
                    b |= (1 << index);
                    DEBUGPRINT("b = %u\n", b);
                }
                index--;
            }
            p = a ^ b;
            DEBUGPRINT("WRITING a = %u\n", a);
            DEBUGPRINT("WRITING b = %u\n", b);
            DEBUGPRINT("WRITING p = %u\n", p);
            fwrite(&a, 1, sizeof(char), devices[(parity_pos + 1) % 3]);
            fwrite(&b, 1, sizeof(char), devices[(parity_pos + 2) % 3]);
            fwrite(&p, 1, sizeof(char), devices[parity_pos]);
        } else if (rlen == 1) {
            a = chars[0];
            p = (~a) + 256;
            DEBUGPRINT("WRITING a = %u\n", a);
            DEBUGPRINT("WRITING p = %u\n", p);
            fwrite(&a, 1, sizeof(char), devices[(parity_pos + 1) % 3]);
            fwrite(&p, 1, sizeof(char), devices[parity_pos]);
        } else {
            printf("Read error\n");
            exit(0x12);
        }
        parity_pos = (parity_pos + 1) % 3;
        DEBUGPRINT("parity_pos = %u\n", parity_pos);
        rlen = fread(chars, 1, sizeof(char) * 2, in);
        DEBUGPRINT("\n--------------------------------------------------\n\n");
    }
    free(chars);
}

void merge(FILE* out, FILE* devices[]) {
    unsigned char a, b, c, p;
    unsigned char parity_pos = 2, i;
    size_t alen, blen, plen;

    alen = fread(&a, 1, sizeof(char), devices[(parity_pos + 1) % 3]);
    blen = fread(&b, 1, sizeof(char), devices[(parity_pos + 2) % 3]);
    plen = fread(&p, 1, sizeof(char), devices[parity_pos]);
    DEBUGPRINT("alen = %d\n", alen);
    DEBUGPRINT("blen = %d\n", blen);
    DEBUGPRINT("plen = %d\n", plen);
    DEBUGPRINT("BEGIN WHILE\n");
    while (alen > 0 && blen > 0 && plen > 0) {
        DEBUGPRINT("NEXT ITERATION\n");
        DEBUGPRINT("a = %d\n", a);
        DEBUGPRINT("b = %d\n", b);
        DEBUGPRINT("p = %d\n", p);
        if ((a ^ b) != p) {
            printf("[WARNING] Parity does not match!");
        }
        c = 0;
        for (i = 0; i <= 7; i++) {
            DEBUGPRINT("i = %u\n", i);
            DEBUGPRINT("EXPONENTS_SHORT[i] = %u\n", EXPONENTS_SHORT[i]);
            if (EXPONENTS_SHORT[i] == 0x08) {
                DEBUGPRINT("WRITING c = %u\n", c);
                fwrite(&c, 1, sizeof(char), out);
                c = 0;
                DEBUGPRINT("RESET c = %u\n", c);
            }
            DEBUGPRINT("c = %u\n", c);
            c <<= 1;
            DEBUGPRINT("c = %u\n", c);
            if ((a & EXPONENTS_SHORT[i]) > 0) {
                c |= 1;
                DEBUGPRINT("UPDATE by primary device   c = %u\n", c);
            }
            c <<= 1;
            DEBUGPRINT("c = %u\n", c);
            if ((b & EXPONENTS_SHORT[i]) > 0) {
                c |= 1;
                DEBUGPRINT("UPDATE by secondary device   c = %u\n", c);
            }
        }
        parity_pos = (parity_pos + 1) % 3;
        DEBUGPRINT("parity_pos = %u\n", parity_pos);
        DEBUGPRINT("WRITING c = %u\n", c);
        fwrite(&c, 1, sizeof(char), out);
        alen = fread(&a, 1, sizeof(char), devices[(parity_pos + 1) % 3]);
        blen = fread(&b, 1, sizeof(char), devices[(parity_pos + 2) % 3]);
        plen = fread(&p, 1, sizeof(char), devices[parity_pos]);
        DEBUGPRINT("alen = %d\n", alen);
        DEBUGPRINT("blen = %d\n", blen);
        DEBUGPRINT("plen = %d\n", plen);
        DEBUGPRINT("\n--------------------------------------------------\n\n");
    }

    if (alen > 0 && plen > 0) {
        if ((a ^ p) != 0xFF) {
            printf("[WARNING] Parity does not match!");
        }
        fwrite(&a, 1, sizeof(char), out);
    }
}

int main(int argc, const char* argv[]) {
    FILE* fp;
    FILE* devices[3];

    if (argc != 6) {
        printf("You have to specify <split|merge> <infile|outfile> \
                <dev0> <dev1> <dev2>\n");
        exit (0x01);
    }

    if (strcmp(argv[1], "split") == 0) {
        DEBUGPRINT("BEGIN open files\n");
        fp = fopen(argv[2], "rb");
        if (fp == NULL) {
            printf("Read error\n");
            exit(0x03);
        }
        devices[0] = fopen(argv[3], "wb");
        devices[1] = fopen(argv[4], "wb");
        devices[2] = fopen(argv[5], "wb");
        DEBUGPRINT("END open files\n");
        DEBUGPRINT("BEGIN split\n");
        split(fp, devices);
        DEBUGPRINT("END split\n");
    } else if (strcmp(argv[1], "merge") == 0) {
        fp = fopen(argv[2], "wb");
        devices[0] = fopen(argv[3], "rb");
        devices[1] = fopen(argv[4], "rb");
        devices[2] = fopen(argv[5], "rb");
        if (devices[0] == NULL || devices[1] == NULL || devices[2] == NULL) {
            printf("Read error\n");
            exit(0x04);
        }
        merge(fp, devices);
    } else {
        printf("Unknown mode! Use either 'split' or 'merge'\n");
        exit(0x02);
    }

    /* we can close all files here since we only reach this part of the code if
     * we have opened the files */
    DEBUGPRINT("BEGIN close files\n");
    fclose(fp);
    fclose(devices[0]);
    fclose(devices[1]);
    fclose(devices[2]);
    DEBUGPRINT("END close files\n");
    return 0;
}

/* 
 * Implements the splitInterface method defined in the Java RaidAccessInterface
 * class.
 */
JNIEXPORT void JNICALL Java_RaidAccessInterface_splitInterface
  (JNIEnv *env, jobject obj, jstring str1, jstring str2, jstring str3, jstring str4) {
    /* Convert the Java Strings to char arrays for usage in this C program. */
    const char *in = (*env)->GetStringUTFChars(env, str1, 0);
    const char *out1 = (*env)->GetStringUTFChars(env, str2, 0);
    const char *out2 = (*env)->GetStringUTFChars(env, str3, 0);
    const char *out3 = (*env)->GetStringUTFChars(env, str4, 0);

    /* Generate file pointers. */
    FILE *fp;
    FILE *devices[3];

    fp = fopen(in, "rb");
    devices[0] = fopen(out1, "wb");
    devices[1] = fopen(out2, "wb");
    devices[2] = fopen(out3, "wb");

    /* Invoke the native split method. */
    split(fp, devices);

    /* Close the files.  */
    fclose(fp);
    fclose(devices[0]);
    fclose(devices[1]);
    fclose(devices[2]);

    /* Clean the memory. / Release the char arrays.  */
    (*env)->ReleaseStringUTFChars(env, str1, in);
    (*env)->ReleaseStringUTFChars(env, str2, out1);
    (*env)->ReleaseStringUTFChars(env, str3, out2);
    (*env)->ReleaseStringUTFChars(env, str4, out3);
}

/*
 * Implements the mergeInterface method defined in the Java RaidAccessInterface
 * class.
 */
JNIEXPORT void JNICALL Java_RaidAccessInterface_mergeInterface
  (JNIEnv *env, jobject obj, jstring str1, jstring str2, jstring str3, jstring str4) {
    /* Convert the Java Strings to char arrays for usage in the C program.  */
    const char *out = (*env)->GetStringUTFChars(env, str1, 0);
    const char *in1 = (*env)->GetStringUTFChars(env, str2, 0);
    const char *in2 = (*env)->GetStringUTFChars(env, str3, 0);
    const char *in3 = (*env)->GetStringUTFChars(env, str4, 0);

    /* Generate file pointers. */
    FILE *fp;
    FILE *devices[3];

    fp = fopen(out, "wb");
    devices[0] = fopen(in1, "rb");
    devices[1] = fopen(in2, "rb");
    devices[2] = fopen(in3, "rb");

    /* Invoke the native merge method. */
    merge(fp, devices);

    /* Close the files. */
    fclose(fp);
    fclose(devices[0]);
    fclose(devices[1]);
    fclose(devices[2]);

    /* Clean the memory. / Release the char arrays. */
    (*env)->ReleaseStringUTFChars(env, str1, out);
    (*env)->ReleaseStringUTFChars(env, str2, in1);
    (*env)->ReleaseStringUTFChars(env, str3, in2);
    (*env)->ReleaseStringUTFChars(env, str4, in3);
}
