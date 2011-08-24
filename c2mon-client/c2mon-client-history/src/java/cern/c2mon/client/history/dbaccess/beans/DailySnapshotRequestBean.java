/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.client.history.dbaccess.beans;

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
