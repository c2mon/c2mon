/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.alarm;

/**
 * The AlarmAggregator bean listens for Tag updates,
 * evaluates all associated alarms and passes the result
 * to registered {@link AlarmAggregatorListener}s.
 * 
 * <p>Standard usage involves wiring it into your class and
 * calling the registerForUpdates method to register your
 * listener.
 * 
 * <p>If alarms do not need publishing to the client together
 * with the tags, there is also the option of using the simpler
 * AlarmEvaluator, which simply evaluates the alarms in an
 * asynchronous manner.
 * 
 * <p>Listeners are notified on the cache notification threads
 * (i.e. this aggregator does not create any extra threads).
 * 
 * @author Mark Brightwell
 *
 */
public interface AlarmAggregator {

  /**
   * Register this listener to received alarm & tag update notifications.
   * Notice that supervision changes are not taken into account here. For
   * these, a module should register directly with the cache.
   * 
   * @param aggregatorListener the listener that should be notified
   */
  void registerForTagUpdates(AlarmAggregatorListener aggregatorListener);
  
}
