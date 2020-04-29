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
package cern.c2mon.shared.daq.republisher;

import cern.c2mon.shared.daq.lifecycle.Lifecycle;

/**
 * A Republisher can be used for managing re-publication of
 * any events in case of failure. 
 * 
 * <p> Instantiate one of these through the RepublisherFactory.
 * 
 * @author Mark Brightwell
 *
 * @param <T> the type of event the publisher publishes
 */
public interface Republisher<T> extends Lifecycle {

  /**
   * Call this method to indicate that the publication of this
   * event failed and should be re-attempted. 
   * @param event publication failed for this event
   */
  void publicationFailed(T event);

  /**
   * Override republication delay (default is 10s)
   * @param republicationDelay in milliseconds
   */
  void setRepublicationDelay(int republicationDelay);
  
  /**
   * @return returns the total number of failed publication attempts since the
   * application started.
   */
  long getNumberFailedPublications();
  
  /**
   * @return returns the current number of events waiting for re-publication
   */
  int getSizeUnpublishedList();
}
