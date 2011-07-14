/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN. This program is free software; you can
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
 ******************************************************************************/
package cern.c2mon.client.core.manager;

import java.util.Collection;

import cern.c2mon.client.core.C2monSupervisionManager;
import cern.c2mon.client.jms.SupervisionListener;

/**
 * This interface extends the <code>C2monSupervisionManager</code>
 * interface and provides additional functionalities to interact with
 * the <code>SupervisionManager</code>. This interface is only
 * visible for other manager classes in that package.
 *
 * @author Matthias Braeger
 */
interface CoreSupervisionManager extends C2monSupervisionManager {

  /**
   * Registers a <code>SupervisionListener</code> which is then getting informed
   * about <code>SupervisionEvent</code> updates for the registered processes and
   * equipments
   * 
   * @param listener The listener to be registered
   * @param processIds List of process id's for which the listeners wants to receive notifications
   * @param equipmentIds List of equipment id's for which the listeners wants to receive notifications
   */
  void addSupervisionListener(SupervisionListener listener, final Collection<Long> processIds, final Collection<Long> equipmentIds);
  
  /**
   * Removes a previously registered <code>SupervisionListener</code> from all notifications.
   * @param listener The listener that shall be removed
   */
  void removeSupervisionListener(SupervisionListener listener);
}
