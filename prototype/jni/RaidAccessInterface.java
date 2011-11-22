public class RaidAccessInterface {
  private native void splitInterface(String in, String out0, String out1, String out2);
  private native void mergeInterface(String out, String in0, String in1, String in2);
  public static void main(String[] args) {
    new RaidAccessInterface().splitInterface("/home/florian/Desktop/test.pdf", "/tmp/a.txt", "/tmp/b.txt", "/tmp/c.txt");
    new RaidAccessInterface().mergeInterface("/home/florian/Desktop/test1.pdf", "/tmp/a.txt", "/tmp/b.txt", "/tmp/c.txt");
  }
  static {
    System.loadLibrary("raid5");
  }
}