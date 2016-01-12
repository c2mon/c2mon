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

import java.sql.Timestamp;

/**
 * This class is passed as an parameter when requesting data from the daily
 * snapshot table.
 * 
 * @author vdeila
 * 
 */
public class DailySnapshotRequestBean {

  /** The tag ids to request */
  private Long[] tagIds;

  /** From which date to have the daily snapshot data (the time is ignored) */
  private Timestamp fromTime = null;

  /** To which date to have the daily snapshot data (the time is ignored) */
  private Timestamp toTime = null;

  /**
   * 
   * @param tagIds
   *          The tag ids to request
   * @param fromTime
   *          From which date to have the daily snapshot data (the time is
   *          ignored)
   * @param toTime
   *          To which date to have the daily snapshot data (the time is
   *          ignored)
   */
  public DailySnapshotRequestBean(final Long[] tagIds, final Timestamp fromTime, final Timestamp toTime) {
    this.tagIds = tagIds;
    this.fromTime = fromTime;
    this.toTime = toTime;
  }

  /**
   * @return The tag ids to request
   */
  public Long[] getTagIds() {
    return tagIds;
  }

  /**
   * @return From which date to have the daily snapshot data (the time is
   *         ignored)
   */
  public Timestamp getFromTime() {
    return fromTime;
  }

  /**
   * @return To which date to have the daily snapshot data (the time is ignored)
   */
  public Timestamp getToTime() {
    return toTime;
  }

}
