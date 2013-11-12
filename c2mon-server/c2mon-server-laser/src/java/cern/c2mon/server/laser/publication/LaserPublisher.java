package cern.c2mon.server.laser.publication;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import cern.laser.source.alarmsysteminterface.AlarmSystemInterface;

/**
 * Interface to LaserPublisher. Only used by backup publisher so far.
 * 
 * @author Mark Brightwell
 *
 */
public interface LaserPublisher {

  /**
   * Returns the LASER source name used.
   * @return source name
   */
  String getSourceName();

  /**
   * Are there unpublished alarms in the re-publication list.
   * @return true if there are
   */
  boolean hasUnpublishedAlarms();

  /**
   * Can be used to set the republish delay. Is only taken
   * into account when set before (re-)started. 
   * 
   * @param republishDelay new delay
   */
  void setRepublishDelay(long republishDelay);

  /**
   * For use by the backup publisher
   * @return LASER asi object
   */
  AlarmSystemInterface getAsi();

  /**
   * Lock string used to only allow one backup to run at any time across a server
   * cluster.
   */
  final static String backupLock = "c2mon.laser.backupLock";
}
