package cern.c2mon.shared.util.threadhandler;

public interface MockProcessor {

  /**
   * Random parameters to a random method (mocked in test class)
   * @param stringArg
   * @param longARg
   * @param intArg
   * @param objectArg
   */
  void processUpdate(String stringArg, Long longArg, int intArg, Object objectArg);
  
}
