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
  HistoryUpdateId getDataId();

  /**
   * 
   * @return the time of when this update should execute
   */
  Timestamp getExecutionTimestamp();

}
