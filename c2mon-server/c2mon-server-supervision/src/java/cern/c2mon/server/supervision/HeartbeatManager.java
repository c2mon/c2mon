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
package cern.c2mon.server.supervision;

/**
 * The HeartbeatManager bean generates regular server heartbeats, which
 * can then be used by clients for monitoring the alive status of the
 * server.
 * 
 * <p>The heartbeat is published directly on a JMS topic. Server modules can
 * also register as listeners for internal heatbeat notifications.
 * 
 * @author Mark Brightwell
 *
 */
public interface HeartbeatManager {

  /**
   * Register to be notified of heartbeats (all notifications are
   * on the same thread!)
   * 
   * @param heartbeatListener the listener that will be called
   */
  void registerToHeartbeat(HeartbeatListener heartbeatListener);
  
}
