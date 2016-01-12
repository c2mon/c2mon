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
 * This class is passed as a parameter when requesting the initial record for a
 * tag
 * 
 * @author vdeila
 * 
 */
public class InitialRecordHistoryRequestBean {

  /** The tag id for this request */
  private final long tagId;

  /** The requested record will be having the value that was on this time */
  private Timestamp beforeTime;

  /**
   * 
   * @param tagId
   *          The tag id to get the initial record for
   * @param beforeTime
   *          The requested record will be having the value that it had on this
   *          time
   */
  public InitialRecordHistoryRequestBean(final long tagId, final Timestamp beforeTime) {
    this.tagId = tagId;
    this.beforeTime = beforeTime;
  }

  /*
   * Getters and setters
   */

  /**
   * @return the beforeTime
   */
  public Timestamp getBeforeTime() {
    return beforeTime;
  }

  /**
   * @param beforeTime
   *          the beforeTime to set
   */
  public void setBeforeTime(final Timestamp beforeTime) {
    this.beforeTime = beforeTime;
  }

  /**
   * @return the tagId
   */
  public long getTagId() {
    return tagId;
  }
}
