#!/usr/bin/python2


import sys

# Precalculate the bits 15 to 0 and store them as a tuple [(15,14),(13,12),...]
EXPONENTS_TUPLES = [(2**(15 - x), 2**(15 - x - 1)) for x in range(0, 15, 2)]
# Precalculate the bits 7 to 0
EXPONENTS = [2**(7 - x) for x in range(0, 8)]


def main():
    if len(sys.argv) != 6:
        print('You have to specify <split|merge> <infile|outfile> '
              '<dev0> <dev1> <dev2>')
        exit(1)
    if sys.argv[1].lower() == 'split':
        split(sys.argv[2], sys.argv[3], sys.argv[4], sys.argv[5])
    elif sys.argv[1].lower() == 'merge':
        merge(sys.argv[2], sys.argv[3], sys.argv[4], sys.argv[5])
    else:
        print('Unknown mode! Use either "split" or "merge"')
        exit(2)


def split(infile, dev0, dev1, dev2):
    """
    This function splits the content of the given `infile` to the files that
    are specified by `dev0`, `dev1` and `dev2`. The parity moves after each
    byte. Its position for the first byte is `dev2`, for the second byte is
    `dev0`, for the third byte is `dev1` and for the forth byte `dev2` again.

    :param infile: The filepath to the input file that should be splittet for
    the RAID
    :param dev0: The filepath to the first device file
    :param dev1: The filepath to the second device file
    :param dev2: The filepath to the third device file

    :type infile: string
    :type dev0: string
    :type dev1: string
    :type dev2: string
    """

    fp = open(infile, 'rb')
    fp_out = [open(dev0, 'wb'), open(dev1, 'wb'), open(dev2, 'wb')]
    # we read 2 characters for easier working
    chars = fp.read(2)
    # the initial device for the parity is 2. We will increment it, so the next
    # device will be 0
    parity_pos = 2
    while chars:
        a = b = 0
        # we check whether we have read 1 or 2 bytes. And since we allways read
        # two bytes, we can decide here, if the input file has an odd file size
        if len(chars) == 2:
            index = 7
            # we take the ordinal value of both read bytes an put them into a
            # single integer, where the first byte uses the bits 8 to 15 and
            # the second byte uses 0 to 7
            char = ord(chars[0])
            char <<= 8
            char |= ord(chars[1])
            # iterating over the bit tuples to split the two characters into
            # two separat bytes
            for L, R in EXPONENTS_TUPLES:
                # if the according bit is set, we set the bit in our output
                # byte. This is the same as `a = a | 2**index`
                if char & L > 0:
                    a |= (1 << index)
                if char & R > 0:
                    b |= (1 << index)

                index -= 1
            # we now write the byte for each device. Because we use a list for
            # storing the devices, we can simply increment the parity position
            # and detect the new device for the parity
            fp_out[(parity_pos + 1) % 3].write(bytes(chr(a)))
            fp_out[(parity_pos + 2) % 3].write(bytes(chr(b)))
            # This writes the parity as `a xor b`
            fp_out[parity_pos].write(bytes(chr(a ^ b)))

        else:
            # this is called if the file size is odd
            char = ord(chars[0])
            # this calculates the 1st-complement
            p = (~char) + 256
            # and here we write the byte and the parity
            fp_out[(parity_pos + 1) % 3].write(bytes(chr(char)))
            fp_out[parity_pos].write(bytes(chr(p)))

        parity_pos = (parity_pos + 1) % 3

        # try to read the next 2 bytes
        chars = fp.read(2)
    fp.close()
    fp_out[0].close()
    fp_out[1].close()
    fp_out[2].close()


def merge(outfile, dev0, dev1, dev2):
    """
    This function merges the content of the given device files `dev0`, `dev1`
    and `dev2` to the `outfile`. The device for the first byte partiy must be
    `dev2`, for the second byte must be `dev0` and for the third byte must be
    `dev1`.

    :param infile: The filepath to the output file that should be splittet for
    the RAID
    :param dev0: The filepath to the first device file
    :param dev1: The filepath to the second device file
    :param dev2: The filepath to the third device file

    :type infile: string
    :type dev0: string
    :type dev1: string
    :type dev2: string
    """

    fp = open(outfile, 'wb')
    fp_in = [open(dev0, 'rb'), open(dev1, 'rb'), open(dev2, 'rb')]

    # this describes the position of the parity device. See
    # :py:func:`raid5.split()` for further information
    parity_pos = 2

    # we read the 3 separat bytes, that we combine to 2
    l = fp_in[(parity_pos + 1) % 3].read(1)
    r = fp_in[(parity_pos + 2) % 3].read(1)
    p = fp_in[parity_pos].read(1)

    # as long as we have a value for all 3 variables, we are neither at the
    # end of the files nor the file has an odd file size
    while l and r and p:
        l = ord(l)
        r = ord(r)
        p = ord(p)
        # check that the parity matches. If not - print a message
        if (l ^ r) != p:
            print('[WARNING] parity does not match the values of device 1 and 2')
        out = 0
        # iterate over the list of bits
        for L in EXPONENTS:
            # if the current bit is number 3, we write the first byte and
            # continue with the second one
            if L == 0x08:
                fp.write(bytes(chr(out)))
                out = 0

            # if the regarding bit is set in the first device, we set it in the
            # output byte as well. We then shift the output byte one left and
            # set the next bit in the output byte if the according bit is set
            # in the second device as well.
            out <<= 1
            if (l & L) > 0:
                out |= 1
            out <<= 1
            if (r & L) > 0:
                out |= 1

        # change the parity device
        parity_pos = (parity_pos + 1) % 3

        # write the output byte and read the next input bytes
        fp.write(bytes(chr(out)))
        l = fp_in[(parity_pos + 1) % 3].read(1)
        r = fp_in[(parity_pos + 2) % 3].read(1)
        p = fp_in[parity_pos].read(1)

    # This if statement only evalueates to true if the original file has an odd
    # file size
    if l and p:
        # So what we do here is to check if the *overhead* byte matches the
        # parity (and write a message if that isn't the case), and write it to
        # the output file
        l = ord(l)
        p = ord(p)
        if l ^ p != 0xFF:
            print('[WARNING] parity does not match the values of device 1 and 2')
        fp.write(bytes(chr(l)))


if __name__ == "__main__":
    main()
