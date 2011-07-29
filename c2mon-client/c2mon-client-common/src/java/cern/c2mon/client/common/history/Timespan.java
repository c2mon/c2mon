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

import java.sql.Timestamp;
import java.util.Date;

/**
 * Simple class to represent a time frame
 * 
 * @author vdeila
 *
 */
public class Timespan {
  
  /** The start date */
  private Timestamp start;
  
  /** The end date */
  private Timestamp end;

  /**
   * 
   * @param start The start date
   * @param end The end date 
   */
  public Timespan(final Timestamp start, final Timestamp end) {
    this.start = start;
    this.end = end;
  }
  
  /**
   * 
   * @param start The start date
   * @param end The end date 
   */
  public Timespan(final Date start, final Date end) {
    this(new Timestamp(start.getTime()), new Timestamp(end.getTime()));
  }


  /**
   * @return the start time
   */
  public Timestamp getStart() {
    return start;
  }

  /**
   * @param start the start time to set
   */
  public void setStart(final Timestamp start) {
    this.start = start;
  }

  /**
   * @return the time end
   */
  public Timestamp getEnd() {
    return end;
  }

  /**
   * @param end the end time to set
   */
  public void setEnd(final Timestamp end) {
    this.end = end;
  }
}
