/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2010 CERN.
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

import cern.c2mon.shared.client.supervision.SupervisionEvent;

/**
 * Interface that must be implemented by any class interested
 * in receiving supervision updates. 
 * 
 * <p>The listener must then be registered with the SupervisionNotifier
 * bean.
 * 
 * @author Mark Brightwell
 *
 */
public interface SupervisionListener {

  /**
   * Called when the C2MON server detects a change in the supervision
   * status of one of the supervised entities (Process, Equipment, Sub-equipment).
   * If registered on multiple threads may be called unordered.
   * 
   * @param supervisionEvent the event details (all fields except String message
   * can be assumed non null)
   */
  void notifySupervisionEvent(SupervisionEvent supervisionEvent);
  
}
