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
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import cern.c2mon.client.history.dbaccess.HistoryMapper;

/**
 * This class is passed as an parameter when requesting history data for tags.<br/>
 * The max records is the total amount for all the tags, NOT per tag. If per tag
 * is desired, you must ask only for one tag at a time.
 * 
 * @see HistoryMapper
 * 
 * @author vdeila
 * 
 */
public class ShortTermLogHistoryRequestBean {

  /** The format of a string with a date and time */
  private static final String DATE_TIME_FORMAT = "dd.MM.yyyy HH:mm:ss";

  /** The tag ids to request */
  private Long[] tagIds;

  /** From time */
  private Timestamp fromTime = null;

  /** To time */
  private Timestamp toTime = null;

  /** The total maximum amount of records that can be retrieved */
  private Integer maxRecords = null;
  
  /** The format used when having date and time */
  private final SimpleDateFormat dateTimeFormat;

  /**
   * @param tagIds
   *          The tag ids to request
   * @param fromTime
   *          From time
   * @param toTime
   *          To time (excluding)
   * @param maxRecords
   *          The total maximum amount of records that can be retrieved. If this
   *          is specified it will get the records that is newest first.
   */
  public ShortTermLogHistoryRequestBean(final Long[] tagIds, final Timestamp fromTime, final Timestamp toTime, final Integer maxRecords) {
    this.tagIds = tagIds;
    this.fromTime = fromTime;
    this.toTime = toTime;
    this.maxRecords = maxRecords;
    
    this.dateTimeFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
    this.dateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
  }

  /**
   * @param tagIds
   *          The tag ids to request
   * @param fromTime
   *          From time
   * @param toTime
   *          To time (excluding)
   */
  public ShortTermLogHistoryRequestBean(final Long[] tagIds, final Timestamp fromTime, final Timestamp toTime) {
    this(tagIds, fromTime, toTime, null);
  }

  /**
   * @param tagIds
   *          The tag ids to request
   * @param maxRecords
   *          Gets the newest records first, going backward in time. This is max
   *          records for the whole request, NOT per tag.
   */
  public ShortTermLogHistoryRequestBean(final Long[] tagIds, final int maxRecords) {
    this(tagIds, null, null, maxRecords);
  }

  /**
   * Copy constructor
   * 
   * @param request The request to make a copy of
   */
  public ShortTermLogHistoryRequestBean(final ShortTermLogHistoryRequestBean request) {
    this(request.tagIds, request.fromTime, request.toTime, request.maxRecords);
  }

  /**
   * @return the tagIds
   */
  public Long[] getTagIds() {
    return tagIds;
  }

  /**
   * @param tagIds
   *          the tagIds to set
   */
  public void setTagIds(final Long[] tagIds) {
    this.tagIds = tagIds;
  }

  /**
   * @return The from time in String format
   */
  public String getFromTimeStr() {
    if (getFromTime() == null) {
      return null;
    }
    return this.dateTimeFormat.format(getFromTime());
  }

  /**
   * @return The to time in String format
   */
  public String getToTimeStr() {
    if (getToTime() == null) {
      return null;
    }
    return this.dateTimeFormat.format(getToTime());
  }

  /**
   * @return the fromTime
   */
  public Timestamp getFromTime() {
    return fromTime;
  }

  /**
   * @return To-time, if any. (Which is excluding in the request)
   */
  public Timestamp getToTime() {
    return toTime;
  }

  /**
   * @param fromTime
   *          the fromTime to set
   */
  public void setFromTime(final Timestamp fromTime) {
    this.fromTime = fromTime;
  }

  /**
   * @param toTime
   *          To time (excluding)
   */
  public void setToTime(final Timestamp toTime) {
    this.toTime = toTime;
  }

  /**
   * @return The max amount of records one query will return. It gets the newest
   *         records first, going backward in time. This is max records for the
   *         whole request, NOT per tag.
   */
  public Integer getMaxRecords() {
    return maxRecords;
  }

  /**
   * @param maxRecords
   *          The max amount to set. It gets the newest records first, going
   *          backward in time. This is max records for the whole request, NOT
   *          per tag.
   */
  public void setMaxRecords(final Integer maxRecords) {
    this.maxRecords = maxRecords;
  }

}
