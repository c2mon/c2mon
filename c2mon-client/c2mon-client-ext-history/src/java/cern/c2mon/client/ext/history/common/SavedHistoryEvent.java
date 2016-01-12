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

import java.util.Date;

/**
 * Describes the methods a saved history event must have
 * 
 * @author vdeila
 * 
 */
public interface SavedHistoryEvent {

  /**
   * @return the ID
   */
  long getId();

  /**
   * @return the name
   */
  String getName();

  /**
   * @return the description
   */
  String getDescription();

  /**
   * @return the start date
   */
  Date getStartDate();

  /**
   * @return the end date
   */
  Date getEndDate();

}
