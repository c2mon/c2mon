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
  private static final Timestamp DEFAULT_EARLIEST_TIMESTAMP = new Timestamp(System.currentTimeMillis() - 31L * 24L * 60L * 60L * 1000L);

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

  /** <code>true</code> if the supervision events should be loaded */
  private boolean loadSupervisionEvents;

  /**
   * <code>true</code> to remove the last record when two records appear twice in
   * a row
   */
  private boolean removeRedundantData;

  /**
   * Constructor
   */
  public HistoryLoadingConfiguration() {
    this.loadInitialValues = false;
    this.loadSupervisionEvents = true;
    this.removeRedundantData = true;
  }

  /**
   * Copy constructor
   * 
   * @param historyLoadingConfiguration
   *          the historyLoadingParameters to copy
   */
  public HistoryLoadingConfiguration(final HistoryLoadingConfiguration historyLoadingConfiguration) {
    this.startTime = historyLoadingConfiguration.startTime;
    this.endTime = historyLoadingConfiguration.endTime;
    this.numberOfDays = historyLoadingConfiguration.numberOfDays;
    this.maximumRecords = historyLoadingConfiguration.maximumRecords;
    this.loadInitialValues = historyLoadingConfiguration.loadInitialValues;
    this.loadSupervisionEvents = historyLoadingConfiguration.loadSupervisionEvents;
    this.removeRedundantData = historyLoadingConfiguration.removeRedundantData;
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
   * The number of days to retrieve, starting either from the end time or from
   * the current time if the end time is not set.
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
   *          the maximum amount of records to retrieve in total.
   *          <code>null</code> for no limit
   */
  public void setMaximumRecords(final Integer maximum) {
    this.maximumRecords = maximum;
  }

  /**
   * 
   * @return the maximum amount of records to retrieve in total.
   *         <code>null</code> if there is no limit
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
   * @return the number of days to get, beginning from the end time or from the
   *         current time if the end time is not set.
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
   *          should be loaded. (<code>false</code> by default)
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

  /**
   * @return <code>true</code> if the supervision events will be loaded
   *         (default)
   */
  public boolean isLoadSupervisionEvents() {
    return loadSupervisionEvents;
  }

  /**
   * @param loadSupervisionEvents
   *          <code>true</code> if the supervision events should be loaded,
   *          <code>false</code> otherwise
   */
  public void setLoadSupervisionEvents(final boolean loadSupervisionEvents) {
    this.loadSupervisionEvents = loadSupervisionEvents;
  }

  /**
   * @return <code>true</code> if removing the last record when two records
   *         appears twice in a row
   */
  public boolean isRemoveRedundantData() {
    return removeRedundantData;
  }

  /**
   * @param removeRedundantData
   *          <code>true</code> to remove the last record when two records
   *          appear twice in a row, <code>false</code> to keep all.
   */
  public void setRemoveRedundantData(final boolean removeRedundantData) {
    this.removeRedundantData = removeRedundantData;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((earliestTimestamp == null) ? 0 : earliestTimestamp.hashCode());
    result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
    result = prime * result + (loadInitialValues ? 1231 : 1237);
    result = prime * result + (loadSupervisionEvents ? 1231 : 1237);
    result = prime * result + (removeRedundantData ? 1231 : 1237);
    result = prime * result + ((maximumRecords == null) ? 0 : maximumRecords.hashCode());
    result = prime * result + ((numberOfDays == null) ? 0 : numberOfDays.hashCode());
    result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof HistoryLoadingConfiguration))
      return false;
    HistoryLoadingConfiguration other = (HistoryLoadingConfiguration) obj;
    if (earliestTimestamp == null) {
      if (other.earliestTimestamp != null)
        return false;
    }
    else if (!earliestTimestamp.equals(other.earliestTimestamp))
      return false;
    if (endTime == null) {
      if (other.endTime != null)
        return false;
    }
    else if (!endTime.equals(other.endTime))
      return false;
    if (loadInitialValues != other.loadInitialValues)
      return false;
    if (loadSupervisionEvents != other.loadSupervisionEvents)
      return false;
    if (removeRedundantData != other.removeRedundantData)
      return false;
    if (maximumRecords == null) {
      if (other.maximumRecords != null)
        return false;
    }
    else if (!maximumRecords.equals(other.maximumRecords))
      return false;
    if (numberOfDays == null) {
      if (other.numberOfDays != null)
        return false;
    }
    else if (!numberOfDays.equals(other.numberOfDays))
      return false;
    if (startTime == null) {
      if (other.startTime != null)
        return false;
    }
    else if (!startTime.equals(other.startTime))
      return false;
    return true;
  }

}
