#include "sha256.h"
#include <stdio.h>
#include <stdlib.h>

int main ( int argc, const char* argv[] )
{
    int i;
    void *resblock;
    char *ascii;

    if ( argc < 1 )
    {
        exit ( 1 );
    }
    else
    {
        if ( argc == 1 )
        {
            printf ( "\nRun with %s file [file [...]]\n\n", argv[0] );
            exit ( 1 );
        }
    }

    resblock = malloc ( 32 );
    ascii = ( char * ) malloc ( 65 );
    for ( i = 1; i < argc; i++ )
    {
        FILE *fp = fopen ( argv[i], "rb" );
        if ( !fp )
        {
            printf ( "Cannot read file %s!\n", argv[i] );
            continue;
        }
        sha256_stream ( fp, resblock );
        ascii_from_resbuf ( ascii, resblock );
        printf ( "%s  %s\n", ascii, argv[i] );
        fclose ( fp );
    }
    free ( resblock );
    free ( ascii );
    return 0;
}

