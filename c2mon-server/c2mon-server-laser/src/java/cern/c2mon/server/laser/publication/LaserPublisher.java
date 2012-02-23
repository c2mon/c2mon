package cern.c2mon.server.laser.publication;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Interface to LaserPublisher. Only used by backup publisher so far.
 * 
 * @author Mark Brightwell
 *
 */
public interface LaserPublisher {

  /**
   * The lock synchronizing publications to LASER.
   * @return lock
   */
  ReentrantReadWriteLock getBackupLock();

  /**
   * Returns the LASER source name used.
   * @return source name
   */
  String getSourceName();

}
