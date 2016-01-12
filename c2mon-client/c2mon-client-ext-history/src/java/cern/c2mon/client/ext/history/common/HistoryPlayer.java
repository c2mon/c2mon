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
package cern.c2mon.client.ext.history.common;

import java.sql.Timestamp;
import java.util.Date;

import cern.c2mon.client.ext.history.common.exception.IllegalTimespanException;

/**
 * This interface describes the methods which are provided by the history
 * player.
 * 
 * @author vdeila
 * 
 */
public interface HistoryPlayer {

  /**
   * This method checks how far the records have been loaded and returns it.
   * 
   * @return the time of how far the records have been loaded until.
   */
  Timestamp getHistoryLoadedUntil();

  /**
   * @return the start time of the time frame
   */
  Date getStart();

  /**
   * @return the end time of the time frame
   */
  Date getEnd();

  /**
   * 
   * @return A interface which can be used to control the playback
   */
  PlaybackControl getPlaybackControl();

  /**
   * 
   * @return the history provider which is currently used to fetch data
   */
  HistoryProvider getHistoryProvider();
  
  /**
   * Extends the timespan which will be loaded
   * 
   * @param extendedTimespan
   *          the new extended time span. The timespan must be within the dates
   *          of {@link HistoryProvider#getDateLimits()}. The start date must
   *          earlier or equal to {@link #getStart()}, and the end date must be
   *          equal to {@link #getEnd()} (cannot be extended)
   * 
   * @throws IllegalTimespanException
   *           if the timespan is illegal.
   */
  void extendTimespan(final Timespan extendedTimespan) throws IllegalTimespanException;
}
