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
package cern.c2mon.client.ext.history.dbaccess.beans;

/**
 * When rows from the saved history events table are retrieved from the
 * database, it is converted into this object.
 * 
 * @author vdeila
 */
public class SavedHistoryEventRecordBean extends HistoryRecordBean {

  /** The event id */
  private final long eventId;

  /**
   * 
   * @param eventId
   *          The event id
   * @param tagId
   *          the tag id
   */
  public SavedHistoryEventRecordBean(final Long eventId, final Long tagId) {
    super(tagId);
    this.eventId = eventId;
  }

  /**
   * @return the eventId
   */
  public long getEventId() {
    return eventId;
  }
}
