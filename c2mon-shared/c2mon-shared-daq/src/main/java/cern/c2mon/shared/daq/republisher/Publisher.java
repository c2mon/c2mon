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
package cern.c2mon.server.common.republisher;

import org.springframework.jms.JmsException;

/**
 * Should be implemented by a service wishing to make use
 * of the Republisher functionalities.
 * 
 * @author Mark Brightwell
 *
 * @param <T> type of event that is published
 */
public interface Publisher<T> {

  /**
   * (Re-)publish the event. The calling class should throw a runtime {@link JmsException}
   * if the publication fails and should be re-attempted. Any other exception
   * thrown will result in the removal of the event from the re-publication list.
   * 
   * <p>IMPORTANT: this method should usually NOT call the Republisher publicationFailed
   * method. Rather, if the publication fails, throw an exception as described above. If
   * this publish method returns successully, the event will be removed from the re-publication
   * list!
   */
  void publish(T event);
  
}
