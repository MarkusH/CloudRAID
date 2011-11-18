#!/usr/bin/python


import sys


def main():
    if len(sys.argv) != 6:
        print('You have to specify <split|merge> <infile|outfile> <dev1> <dev2> <parity>')
        exit(1)
    if sys.argv[1].lower() == 'split':
        split(sys.argv[2], sys.argv[3], sys.argv[4], sys.argv[5])
    elif sys.argv[1].lower() == 'merge':
        merge(sys.argv[2], sys.argv[3], sys.argv[4], sys.argv[5])
    else:
        print('Unknown mode! Use either "split" or "merge"')
        exit(2)


def split(infile, dev1, dev2, parity):
    fp = open(infile, 'rb')
    fp_out = [open(dev1, 'wb'), open(dev2, 'wb'), open(parity, 'wb')]
    chars = fp.read(2)
    parity_pos = 2
    while chars:
        a = b = 0
        index = 7
        if len(chars) == 2:
            char = chars[0]
            for L, R in [(0x80, 0x40), (0x20, 0x10), (0x8, 0x4), (0x2, 0x1)]:
                set_left = char & L
                set_right = char & R

                if set_left > 0:
                    a = a | 2**index
                if set_right > 0:
                    b = b | 2**index

                index -= 1
            char = chars[1]
            for L, R in [(0x80, 0x40), (0x20, 0x10), (0x8, 0x4), (0x2, 0x1)]:
                set_left = char & L
                set_right = char & R

                if set_left > 0:
                    a = a | 2**index
                if set_right > 0:
                    b = b | 2**index

                index -= 1

            fp_out[(parity_pos + 1) % 3].write(bytes(chr(a), 'iso-8859-1'))
            fp_out[(parity_pos + 2) % 3].write(bytes(chr(b), 'iso-8859-1'))
            fp_out[parity_pos].write(bytes(chr(a ^ b), 'iso-8859-1'))

        else:
            char = chars[0]
            p = 0
            for L in [0x80, 0x40, 0x20, 0x10, 0x8, 0x4, 0x2, 0x1]:
                a = char & L
                p <<= 1
                if a == 0:
                    p |= 1
            fp_out[(parity_pos + 1) % 3].write(bytes(chr(char), 'iso-8859-1'))
            fp_out[parity_pos].write(bytes(chr(p), 'iso-8859-1'))

        parity_pos = (parity_pos + 1) % 3

        chars = fp.read(2)
    fp.close()
    fp_out[0].close()
    fp_out[1].close()
    fp_out[2].close()


def merge(outfile, dev1, dev2, parity):
    fp = open(outfile, 'wb')
    fp_in = [open(dev1, 'rb'), open(dev2, 'rb'), open(parity, 'rb')]

    parity_pos = 2

    l = fp_in[(parity_pos + 1) % 3].read(1)
    r = fp_in[(parity_pos + 2) % 3].read(1)
    p = fp_in[parity_pos].read(1)

    while l and r and p:
        l = ord(l)
        r = ord(r)
        p = ord(p)
        if (l ^ r) != p:
            print('[WARNING] parity does not match the values of device 1 and 2')
        out = 0
        index = 7
        for L in [0x80, 0x40, 0x20, 0x10, 0x8, 0x4, 0x2, 0x1]:
            if index == 3:
                fp.write(bytes(chr(out), 'iso-8859-1'))
                out = 0

            out <<= 1
            if (l & L) > 0:
                out |= 1
            out <<= 1
            if (r & L) > 0:
                out |= 1

            index -= 1

        parity_pos = (parity_pos + 1) % 3

        fp.write(bytes(chr(out), 'iso-8859-1'))
        l = fp_in[(parity_pos + 1) % 3].read(1)
        r = fp_in[(parity_pos + 2) % 3].read(1)
        p = fp_in[parity_pos].read(1)

    if l and p: # the original file was longer by 8 bit
        l = ord(l)
        p = ord(p)
        if l ^ p != 0xFF:
            print('[WARNING] parity does not match the values of device 1 and 2')
        fp.write(bytes(chr(l), 'iso-8859-1'))


if __name__ == "__main__":
    main()
