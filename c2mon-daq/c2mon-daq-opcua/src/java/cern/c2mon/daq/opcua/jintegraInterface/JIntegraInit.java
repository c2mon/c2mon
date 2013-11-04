package cern.c2mon.daq.opcua.jintegraInterface;

public class JIntegraInit {
  static boolean initialised = false;
  public static void init() {
    if(initialised) return;
    initialised = true;
  }
}
