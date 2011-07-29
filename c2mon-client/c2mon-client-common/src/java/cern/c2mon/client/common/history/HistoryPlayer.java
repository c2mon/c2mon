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
   * @return the start time of the time frame
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
}
