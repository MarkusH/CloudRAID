#ifndef RC4_H
#define RC4_H 1

#ifdef __cplusplus
extern "C"
{
#endif
    typedef struct rc4_key
    {
        unsigned char state[256];
        unsigned char x;
        unsigned char y;
    } rc4_key;

    extern void swap_byte ( unsigned char *a, unsigned char *b );
    extern void prepare_key ( const unsigned char *key_data_ptr, int key_data_len, rc4_key *key );
    extern void rc4 ( unsigned char *buffer_ptr, int buffer_len, rc4_key * key );

#ifdef __cplusplus
}
#endif

#endif

