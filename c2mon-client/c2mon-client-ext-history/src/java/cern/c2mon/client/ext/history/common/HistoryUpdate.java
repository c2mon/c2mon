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

import cern.c2mon.client.ext.history.common.id.HistoryUpdateId;

/**
 * This interface is used to keep track of the data which is from the history.
 * It have a function to get the execution time of the update so the player will know
 * when to execute the update. And it also have an identifier.
 * 
 * @author vdeila
 * 
 */
public interface HistoryUpdate {

  /**
   * 
   * @return the id of the update
   */
  HistoryUpdateId getUpdateId();

  /**
   * 
   * @return the time of when this update should execute
   */
  Timestamp getExecutionTimestamp();

}
