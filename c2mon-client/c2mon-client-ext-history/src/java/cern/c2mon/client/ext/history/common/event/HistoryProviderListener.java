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
package cern.c2mon.client.ext.history.common.event;

import cern.c2mon.client.ext.history.common.HistoryProvider;

/**
 * Used by {@link HistoryProvider} to inform about events
 * 
 * @author vdeila
 */
public interface HistoryProviderListener {

  /**
   * Invoked when a query is starting
   */
  void queryStarting();

  /**
   * Invoked when a query is finished, and is just to return.
   */
  void queryFinished();

  /**
   * Is invoked when the progress percentage changed.
   * 
   * @param percent
   *          How many percent of the query is finished
   */
  void queryProgressChanged(double percent);
}
