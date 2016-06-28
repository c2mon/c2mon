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
package cern.c2mon.web.configviewer.util;

/**
 * Helper Class for the TrendViewer.
 * 
 * Includes simple information about an invalidation point (Time and Reason for the invalidation).
 * Passed to the .jsp TrendView. 
 * 
 * @see https://issues.cern.ch/browse/TIMS-873
 * 
 * @author ekoufaki
 */
public class InvalidPoint {

  /** Time of invalidation */
  private final String time;
  
  /** Reason for the invalidation */
  private final String invalidationReason;
  
  public InvalidPoint(final String time, final String invalidationReason) {
    this.time = time;
    this.invalidationReason = invalidationReason.replaceAll("\n","");
  }
  
  public String getTime() {
    return time;
  }
  
  public String getInvalidationReason() {
    return invalidationReason;
  }
}
