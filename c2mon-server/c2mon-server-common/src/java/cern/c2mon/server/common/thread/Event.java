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
package cern.c2mon.server.common.thread;

/**
 * Used for internal server synchronization.
 * Old events should be rejected by beans, that keep
 * track of the time of the latest event they processed.
 * 
 * @author Mark Brightwell
 *
 * @param <T> the return type of the event
 */
public class Event<T> {

  /**
   * The time of the event, as provided by the processing bean.
   */
  private long eventTime;
  
  /**
   * The return value of the event, if any (o.w. is null).
   */
  private T returnValue;
  
  /**
   * When no return value is passed.
   * @param eventTime time of the event
   */
  public Event(long eventTime) {
    super();
    this.eventTime = eventTime;
    validate();
  }
  
  /**
   * Constructor when return value is needed
   * @param eventTime time of the event
   * @param returnValue return value
   */
  public Event(long eventTime, T returnValue) {
    super();
    this.eventTime = eventTime;
    this.returnValue = returnValue;
    validate();
  }

  private void validate() {
    //no retrictions so far
  }

  /**
   * @return the eventTime
   */
  public long getEventTime() {
    return eventTime;
  }

  /**
   * @return the returnValue
   */
  public T getReturnValue() {
    return returnValue;
  }
 
  
  
}
