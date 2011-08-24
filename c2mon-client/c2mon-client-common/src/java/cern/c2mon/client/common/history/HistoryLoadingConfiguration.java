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
package cern.c2mon.client.common.history;

import java.security.InvalidParameterException;
import java.sql.Timestamp;


/**
 * This is supplied to a {@link HistoryLoadingManager} to set the parameters for
 * the loading
 * 
 * @author vdeila
 * 
 */
public class HistoryLoadingConfiguration {

  /** The default value for the earliest timestamp to request data from */
  private final Timestamp DEFAULT_EARLIEST_TIMESTAMP = new Timestamp(System.currentTimeMillis() - 31L * 24L * 60L * 60L * 1000L);
  
  /** The earliest timestamp to request data from */
  private Timestamp earliestTimestamp = DEFAULT_EARLIEST_TIMESTAMP;
  
  /** the start time from where to retrieve data */
  private Timestamp startTime;

  /** the ending time of to where to retrieve data */
  private Timestamp endTime;

  /** the number of days to get, beginning from today */
  private Integer numberOfDays;

  /** the maximum amount of records to retrieve. <code>null</code> for no limit */
  private Integer maximumRecords;

  /** <code>true</code> if the initial values at time 0 also should be loaded */
  private boolean loadInitialValues;

  /**
   * Constructor
   */
  public HistoryLoadingConfiguration() {
    loadInitialValues = false;
  }

  /**
   * Copy constructor
   * 
   * @param historyLoadingConfiguration the historyLoadingParameters to copy
   */
  public HistoryLoadingConfiguration(final HistoryLoadingConfiguration historyLoadingConfiguration) {
    this.startTime = historyLoadingConfiguration.startTime;
    this.endTime = historyLoadingConfiguration.endTime;
    this.numberOfDays = historyLoadingConfiguration.numberOfDays;
    this.maximumRecords = historyLoadingConfiguration.maximumRecords;
    this.loadInitialValues = historyLoadingConfiguration.loadInitialValues;
  }

  /**
   * 
   * @param start
   *          the start time from where to retrieve data
   */
  public void setStartTime(final Timestamp start) {
    this.startTime = start;
  }

  /**
   * 
   * @param end
   *          the ending time of to where to retrieve data
   */
  public void setEndTime(final Timestamp end) {
    this.endTime = end;
  }

  /**
   * This function will edit the start and end time to be correct for the number
   * of days, starting from the current time.
   * 
   * @param days
   *          the number of days to get, beginning from today.
   */
  public void setNumberOfDays(final int days) {
    this.numberOfDays = days;
  }

  /**
   * 
   * @param maximum
   *          the maximum amount of records to retrieve. <code>null</code> for
   *          no limit
   */
  public void setMaximumRecords(final Integer maximum) {
    this.maximumRecords = maximum;
  }

  /**
   * 
   * @return the maximum amount of records to retrieve. <code>null</code> if
   *         there is no limit
   */
  public Integer getMaximumRecords() {
    return this.maximumRecords;
  }

  /**
   * @return the start time from where to retrieve data
   */
  public Timestamp getStartTime() {
    return startTime;
  }

  /**
   * @return the ending time of to where to retrieve data
   */
  public Timestamp getEndTime() {
    return endTime;
  }

  /**
   * @return the number of days to get, beginning from today.
   */
  public Integer getNumberOfDays() {
    return numberOfDays;
  }

  /**
   * @return <code>true</code> if the initial values at time 0 also should be
   *         loaded
   */
  public boolean isLoadInitialValues() {
    return loadInitialValues;
  }

  /**
   * @param loadInitialValues
   *          set to <code>true</code> if the initial values at time 0 also
   *          should be loaded
   */
  public void setLoadInitialValues(final boolean loadInitialValues) {
    this.loadInitialValues = loadInitialValues;
  }

  /**
   * @return the earliest timestamp to request data from
   */
  public Timestamp getEarliestTimestamp() {
    return earliestTimestamp;
  }

  /**
   * @param earliestTimestamp
   *          set a new timestamp for the earliest timestamp to request data
   *          from. Cannot be <code>null</code>!
   */
  public void setEarliestTimestamp(final Timestamp earliestTimestamp) {
    if (earliestTimestamp == null) {
      throw new InvalidParameterException("The earliest timestamp must be set!");
    }
    this.earliestTimestamp = earliestTimestamp;
  }

  
}
