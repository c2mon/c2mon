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
