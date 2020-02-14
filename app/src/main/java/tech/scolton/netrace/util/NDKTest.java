package tech.scolton.netrace.util;

public class NDKTest {
  public static native String test();

  static {
    System.loadLibrary("test-lib");
  }
}
