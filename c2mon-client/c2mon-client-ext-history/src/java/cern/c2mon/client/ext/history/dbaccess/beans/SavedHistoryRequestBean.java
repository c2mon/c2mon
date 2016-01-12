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

import java.util.Collection;

import cern.c2mon.client.ext.history.dbaccess.SavedHistoryMapper;

/**
 * Bean which is passed to the {@link SavedHistoryMapper} as a request.
 * 
 * @author vdeila
 * 
 */
public class SavedHistoryRequestBean {

  /** The event id of the data to get */
  private final long eventId;

  /** the tag ids which is requested */
  private final Collection<Long> tagIds;

  /**
   * 
   * @param eventId
   *          the event id of the data to get
   * @param tagIds
   *          the tag ids which is requested
   */
  public SavedHistoryRequestBean(final long eventId, final Collection<Long> tagIds) {
    this.eventId = eventId;
    this.tagIds = tagIds;
  }

  /**
   * @return the tag ids which is requested
   */
  public Collection<Long> getTagIds() {
    return tagIds;
  }

  /**
   * @return the event id of the data to get
   */
  public long getEventId() {
    return eventId;
  }

}
