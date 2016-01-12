/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
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
