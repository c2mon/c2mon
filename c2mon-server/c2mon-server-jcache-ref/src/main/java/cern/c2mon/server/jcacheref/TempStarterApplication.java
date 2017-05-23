package cern.c2mon.server.jcacheref;

import cern.c2mon.server.jcacheref.utils.FunctionalReadWriteLock;

/**
 * @author Szymon Halastra
 */
public class TempStarterApplication {

  public static void main(String[] args) {
    FunctionalReadWriteLock guard = new FunctionalReadWriteLock();

    guard.write(() -> {
      /* Cache operation with locking */
    });

    guard.read(() -> {
      /* getting stuff from cache safely */
    });

    /* It is also possible to wrap it in lambdas, of course not in static method */
    guard.write(this::unsafeCall);
  }

  private void unsafeCall() {

  }
}
