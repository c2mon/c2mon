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
import cern.c2mon.server.common.component.Lifecycle;

/**
 * Interface to bean responsible for notifying listeners interested
 * in receiving supervision updates. 
 * 
 * <p>The listener can register to be notified on any number of threads.
 * All methods all called on the same threads (in this case, the
 * listener can receive up/down messages for the same device in rapid
 * succession).
 * 
 * @author Mark Brightwell
 *
 */
public interface SupervisionNotifier {

  
  /**
   * Register to receive supervision notifications. 
   * 
   * <p><b>Preferably use this method for registering (currently registers 
   * on a single thread, one for each listener so the thread does not need 
   * releasing rapidly and notifications will be received sequentially).</b> 
   * 
   * @param supervisionListener the listener to register
   * @return a handle on the lifecycle of the threads notifying this listener; call stop/start during
   *  listener lifecycle methods (stop *before* shutting down listener; start *after* listener is ready(
   */
  Lifecycle registerAsListener(SupervisionListener supervisionListener);
  
  /**
   * Register the listener to received updates about the supervision
   * status. If registering on several threads, keep in mind that
   * up/down notifications for the same equipment/process could be
   * called before a previous one has returned!
   *   
   * @param supervisionListener the listener to register
   * @param numberThreads the core=max number of threads invoking the listener  (default max queue size is Integer.MAX)            
   * @return a handle on the lifecycle of the threads notifying this listener; call stop/start during
   *  listener lifecycle methods (stop *before* shutting down listener; start *after* listener is ready)
   */
  Lifecycle registerAsListener(SupervisionListener supervisionListener, int numberThreads);

  /**
   * This registration allows a custom executor queue size specification. 
   * 
   * @param supervisionListener the listener to register
   * @param numberThreads the core=max number of threads <b>this</b> listener should be notified on
   * @param queueSize size of the executor queue
   * @return a handle on the lifecycle of the threads notifying this listener; call stop/start during
   *  listener lifecycle methods (stop *before* shutting down listener; start *after* listener is ready(
   */
  Lifecycle registerAsListener(SupervisionListener supervisionListener, int numberThreads, int queueSize);  

  /**
   * Call this method when a supervision event should be notified to
   * all listeners. 
   * 
   * @param supervisionEvent the event that has taken place
   */
  void notifySupervisionEvent(SupervisionEvent supervisionEvent);
  
}
