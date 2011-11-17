#!/usr/bin/python


import sys


def main():
    print(sys.argv)
    if len(sys.argv) != 5:
        print("You have to specify <infile> <dev1> <dev2> <parity>")
        exit(1)
    split(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4])


def split(infile, dev1, dev2, parity):
    fp = open(infile, 'rb')
    fp_l = open(dev1, 'wb')
    fp_r = open(dev2, 'wb')
    fp_p = open(parity, 'wb')
    chars = fp.read(2)
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

            fp_l.write(bytes(chr(a), 'iso-8859-1'))
            fp_r.write(bytes(chr(b), 'iso-8859-1'))
            fp_p.write(bytes(chr(a ^ b), 'iso-8859-1'))

        else:
            p = 0
            for L in [0x80, 0x40, 0x20, 0x10, 0x8, 0x4, 0x2, 0x1]:
                a = char & L
                if a > 0:
                    p |= 1
                p <<= 1
            fp_l.write(bytes(chr(char), 'iso-8859-1'))
            fp_p.write(bytes(chr(p), 'iso-8859-1')) #

        chars = fp.read(2)
    fp.close()
    fp_l.close()
    fp_r.close()
    fp_p.close()


if __name__ == "__main__":
    main()
