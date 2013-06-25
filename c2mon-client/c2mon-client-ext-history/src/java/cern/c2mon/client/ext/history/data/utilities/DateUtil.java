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
package cern.c2mon.client.ext.history.data.utilities;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

/**
 * Easy functions to manipulate the date
 * 
 * @author vdeila
 *
 */
public final class DateUtil {

  /**
   * 
   * @param time
   *          the reference time
   * @return the latest possible time at the given day
   */
  public static Timestamp latestTimeInDay(final long time) {
    final Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(time);
    calendar.set(Calendar.HOUR_OF_DAY, calendar.getMaximum(Calendar.HOUR_OF_DAY));
    calendar.set(Calendar.MINUTE, calendar.getMaximum(Calendar.MINUTE));
    calendar.set(Calendar.SECOND, calendar.getMaximum(Calendar.SECOND));
    calendar.set(Calendar.MILLISECOND, calendar.getMaximum(Calendar.MILLISECOND));
    return new Timestamp(calendar.getTimeInMillis());
  }
  
  /**
   * 
   * @param time
   *          the reference time
   * @return the earliest possible time at the given day (ie. midnight)
   */
  public static Timestamp earliestTimeInDay(final long time) {
    final Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(time);
    calendar.set(Calendar.HOUR_OF_DAY, calendar.getMinimum(Calendar.HOUR_OF_DAY));
    calendar.set(Calendar.MINUTE, calendar.getMinimum(Calendar.MINUTE));
    calendar.set(Calendar.SECOND, calendar.getMinimum(Calendar.SECOND));
    calendar.set(Calendar.MILLISECOND, calendar.getMinimum(Calendar.MILLISECOND));
    return new Timestamp(calendar.getTimeInMillis());
  }
  
  /**
   * Compares to see if the two times is on the same day
   * 
   * @param time1
   *          a time
   * @param time2
   *          a time
   * @return <code>true</code> if the two times is on the same day
   */
  public static boolean isDaysEqual(final long time1, final long time2) {
    final Calendar calendar1 = Calendar.getInstance();
    final Calendar calendar2 = Calendar.getInstance();
    calendar1.setTimeInMillis(time1);
    calendar2.setTimeInMillis(time2);
    
    return calendar1.get(Calendar.DATE) == calendar2.get(Calendar.DATE)
        && calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH)
        && calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR);
  }
  
  /**
   * 
   * @param time
   *          the reference time
   * @param days
   *          the number of days to go forward or backward (minus for backward)
   * @return the new timestamp with the offset of days added
   */
  public static Timestamp addDays(final Long time, final int days) {
    final Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(time);
    calendar.add(Calendar.DAY_OF_YEAR, days);
    return new Timestamp(calendar.getTimeInMillis());
  }
  
  /**
   * 
   * @param timestamp
   *          the timestamp to truncate
   * @return the timestamp truncated to only the day. (Hour, minutes, seconds
   *         and milliseconds is set to zero)
   */
  public static Date truncateToDay(final Date timestamp) {
    final Calendar calendar = Calendar.getInstance();
    calendar.setTime(timestamp);
    calendar.set(Calendar.MILLISECOND, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    return calendar.getTime();
  }
  
  /** Utility class */
  private DateUtil() { }
}
