public class RaidAccessInterface {
    private native void splitInterface(String in, String out0, String out1, String out2);

    private native void mergeInterface(String out, String in0, String in1, String in2);

    public static void main(String[] args) {
        if (args.length != 5 && args.length != 6) {
            System.out.println("You have to specify <split|merge> <infile|outfile> <dev0> <dev1> <dev2> [--bits|--bytes]");
            System.exit(1);
        }

        /*
         * Depending on what the user passed as the last argument, we will
         * either split and merge the input at bit or byte level.
         */
        if (args.length == 6 && args[5].equals("--bytes")) {
            System.loadLibrary("raid5bytes");
        } else {
            System.loadLibrary("raid5bits");
        }

        long startTime = System.currentTimeMillis();
        if (args[0].toLowerCase().equals("split"))
            new RaidAccessInterface().splitInterface(args[1], args[2], args[3], args[4]);
        else if (args[0].toLowerCase().equals("merge"))
            new RaidAccessInterface().mergeInterface(args[1], args[2], args[3], args[4]);
        else {
            System.out.println("Unknown mode! Use either \"split\" or \"merge\"");
            System.exit(2);
        }
        long endTime = System.currentTimeMillis();
        System.out.println((endTime - startTime) / 1000 + " s.");
    }
}
