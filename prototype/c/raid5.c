#include <stdio.h>
#include <stdlib.h>
#include <string.h>

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
    #ifdef _DEBUG
    printf("rlen = %d\n", rlen);
    printf("BEGIN WHILE\n");
    #endif
    while (rlen > 0) {
        #ifdef _DEBUG
        printf("NEXT ITERATION\n");
        #endif
        a = 0;
        b = 0;
        if (rlen == 2) {
            index = 7;
            #ifdef _DEBUG
            printf("chars[0] = %u\n", chars[0]);
            printf("chars[1] = %u\n", chars[1]);
            #endif
            c = chars[0];
            #ifdef _DEBUG
            printf("c = %u\n", c);
            #endif
            c <<= 8;
            #ifdef _DEBUG
            printf("c = %u\n", c);
            #endif
            c |= chars[1];
            #ifdef _DEBUG
            printf("c = %u\n", c);
            #endif
            /* We only need every second element from this range */
            #ifdef _DEBUG
            printf("BEGIN FOR\n");
            #endif
            for (i = 0; i <= 7; i++) {
                #ifdef _DEBUG
                printf("i = %u\n", i);
                printf("EXPONENTS_LONG_FIRST[i] = %u\n", EXPONENTS_LONG_FIRST[i]);
                #endif
                if (c & EXPONENTS_LONG_FIRST[i]) {
                    #ifdef _DEBUG
                    printf("a = %u\n", a);
                    #endif
                    a |= (1 << index);
                    #ifdef _DEBUG
                    printf("a = %u\n", a);
                    #endif
                }
                #ifdef _DEBUG
                printf("EXPONENTS_LONG_SECOND[i + 1] = %u\n", EXPONENTS_LONG_SECOND[i]);
                #endif
                if (c & EXPONENTS_LONG_SECOND[i]) {
                    #ifdef _DEBUG
                    printf("b = %u\n", b);
                    #endif
                    b |= (1 << index);
                    #ifdef _DEBUG
                    printf("b = %u\n", b);
                    #endif
                }
                index--;
            }
            p = a ^ b;
            #ifdef _DEBUG
            printf("WRITING a = %u\n", a);
            printf("WRITING b = %u\n", b);
            printf("WRITING p = %u\n", p);
            #endif
            fwrite(&a, 1, sizeof(char), devices[(parity_pos + 1) % 3]);
            fwrite(&b, 1, sizeof(char), devices[(parity_pos + 2) % 3]);
            fwrite(&p, 1, sizeof(char), devices[parity_pos]);
        } else if (rlen == 1) {
            a = chars[0];
            p = (~a) + 256;
            #ifdef _DEBUG
            printf("WRITING a = %u\n", a);
            printf("WRITING p = %u\n", p);
            #endif
            fwrite(&a, 1, sizeof(char), devices[(parity_pos + 1) % 3]);
            fwrite(&p, 1, sizeof(char), devices[parity_pos]);
        } else {
            printf("Read error\n");
            exit(0x12);
        }
        parity_pos = (parity_pos + 1) % 3;
        #ifdef _DEBUG
        printf("parity_pos = %u\n", parity_pos);
        #endif
        rlen = fread(chars, 1, sizeof(char) * 2, in);
        #ifdef _DEBUG
        printf("\n--------------------------------------------------\n\n");
        #endif
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
    #ifdef _DEBUG
    printf("alen = %d\n", alen);
    printf("blen = %d\n", blen);
    printf("plen = %d\n", plen);
    printf("BEGIN WHILE\n");
    #endif
    while (alen > 0 && blen > 0 && plen > 0) {
        #ifdef _DEBUG
        printf("NEXT ITERATION\n");
        printf("a = %d\n", a);
        printf("b = %d\n", b);
        printf("p = %d\n", p);
        #endif
        if ((a ^ b) != p) {
            printf("[WARNING] Parity does not match!");
        }
        c = 0;
        for (i = 0; i <= 7; i++) {
            #ifdef _DEBUG
            printf("i = %u\n", i);
            printf("EXPONENTS_SHORT[i] = %u\n", EXPONENTS_SHORT[i]);
            #endif
            if (EXPONENTS_SHORT[i] == 0x08) {
                #ifdef _DEBUG
                printf("WRITING c = %u\n", c);
                #endif
                fwrite(&c, 1, sizeof(char), out);
                c = 0;
                #ifdef _DEBUG
                printf("RESET c = %u\n", c);
                #endif
            }
            #ifdef _DEBUG
            printf("c = %u\n", c);
            #endif
            c <<= 1;
            #ifdef _DEBUG
            printf("c = %u\n", c);
            #endif
            if ((a & EXPONENTS_SHORT[i]) > 0) {
                c |= 1;
                #ifdef _DEBUG
                printf("UPDATE by primary device   c = %u\n", c);
                #endif
            }
            c <<= 1;
            #ifdef _DEBUG
            printf("c = %u\n", c);
            #endif
            if ((b & EXPONENTS_SHORT[i]) > 0) {
                c |= 1;
                #ifdef _DEBUG
                printf("UPDATE by secondary device   c = %u\n", c);
                #endif
            }
        }
        parity_pos = (parity_pos + 1) % 3;
        #ifdef _DEBUG
        printf("parity_pos = %u\n", parity_pos);
        printf("WRITING c = %u\n", c);
        #endif
        fwrite(&c, 1, sizeof(char), out);
        alen = fread(&a, 1, sizeof(char), devices[(parity_pos + 1) % 3]);
        blen = fread(&b, 1, sizeof(char), devices[(parity_pos + 2) % 3]);
        plen = fread(&p, 1, sizeof(char), devices[parity_pos]);
        #ifdef _DEBUG
        printf("alen = %d\n", alen);
        printf("blen = %d\n", blen);
        printf("plen = %d\n", plen);
        printf("\n--------------------------------------------------\n\n");
        #endif
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
        #ifdef _DEBUG
        printf("BEGIN open files\n");
        #endif
        fp = fopen(argv[2], "rb");
        if (fp == NULL) {
            printf("Read error\n");
            exit(0x03);
        }
        devices[0] = fopen(argv[3], "wb");
        devices[1] = fopen(argv[4], "wb");
        devices[2] = fopen(argv[5], "wb");
        #ifdef _DEBUG
        printf("END open files\n");
        printf("BEGIN split\n");
        #endif
        split(fp, devices);
        #ifdef _DEBUG
        printf("END split\n");
        #endif
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
    #ifdef _DEBUG
    printf("BEGIN close files\n");
    #endif
    fclose(fp);
    fclose(devices[0]);
    fclose(devices[1]);
    fclose(devices[2]);
    #ifdef _DEBUG
    printf("END close files\n");
    #endif
    return 0;
}
