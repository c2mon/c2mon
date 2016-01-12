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
package cern.c2mon.client.ext.history.dbaccess.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Methods for managing time zones
 * 
 * @author vdeila
 * 
 */
public final class TimeZoneUtil {

  /**
   * Converts a date into another timezone
   * 
   * @param timeZone
   *          The timezone to convert into
   * @param date
   *          The date to convert into the timezone
   * @return The converted date
   */
  public static Date convertDateFromLocalToTimezone(final TimeZone timeZone, final Date date) {
    final String dateTimeFormat = "dd.MM.yyyy HH:mm:ss";
    final SimpleDateFormat dateWithTimezone = new SimpleDateFormat(dateTimeFormat);
    dateWithTimezone.setTimeZone(timeZone);
    try {
      final Date gmtDate = new SimpleDateFormat(dateTimeFormat).parse(dateWithTimezone.format(date));
      return gmtDate;
    }
    catch (ParseException e) {
      throw new RuntimeException("Error occured while converting a date into another timezone.", e);
    }
  }

  /**
   * 
   * @param date
   *          the date which is in UTC timezone
   * @return the date in the local time zone
   */
  public static Timestamp convertDateFromUtcToLocalTimezone(final Date date) {
    final TimeZone localTimezone = TimeZone.getDefault();
    final long newTime = date.getTime() + localTimezone.getOffset(date.getTime());
    return new Timestamp(newTime);
  }

  /**
   * Converts a date from one timezone to another
   * 
   * @param newTimezone
   *          the new timezone that is wanted for the date
   * @param date
   *          the date is will be converted. Assumed to be in the
   *          <code>dateTimezone</code>
   * @param dateTimezone
   *          the timezone of the <code>date</code>
   * @return the converted date
   */
  public static Timestamp convertDateTimezone(final TimeZone newTimezone, final Date date, final TimeZone dateTimezone) {
    long newTime = 
      date.getTime() 
      - dateTimezone.getOffset(date.getTime()) 
      + newTimezone.getOffset(date.getTime());
    return new Timestamp(newTime);
  }

  /**
   * Private constructor, is only a utility class
   */
  private TimeZoneUtil() {

  }
}
