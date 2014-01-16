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
package cern.c2mon.server.daqcommunication.in;

import cern.c2mon.server.common.process.Process;

/**
 * Manages the JMS containers listening for incoming updates.
 * Allows the adding and removal of containers dedicated
 * to listening for updates from a particular Process.
 * 
 * <p>At startup, the manager will subscribe to those Processes found
 * in the cache.
 * 
 * @author Mark Brightwell
 *
 */
public interface JmsContainerManager {
  
  /**
   * Exclusive config lock located in ClusterCache.
   * 
   * <p>Distributed lock used to get exclusive configuration access to the server
   * (applying configurations is forced to be sequential).
   * @see cern.c2mon.server.configuration.impl.ConfigurationLoaderImpl
   */
  String CONFIG_LOCK_KEY = "c2mon.configuration.configlock";

  /**
   * Adds a JMS container listening for process update messages. If one is already
   * registered for this process, logs a warning.
   * 
   * @param process the Process to receive messages for
   */
  void subscribe(Process process);
  
  /**
   * Removes the container listening to updates for the given
   * process. If no subscription can be found for this process,
   * logs a warning. 
   * 
   * @param process stop receving message for this Process
   * @throws NullPointerException if the specified Process is null
   */
  void unsubscribe(Process process);

  /**
   * Set the initial number of consumer threads for new Spring listener containers.
   * 
   * <p>Default is 1.
   * @param consumersInitial number of initial threads
   */
  void setConsumersInitial(int consumersInitial);

  /**
   * Set the max number of consumer threads for the Spring listener containers.
   * At start-up, max is only increased from the initial value to this one after
   * the warm-up time has passed.
   * 
   * <p>Default is 1.
   * @param consumersMax max number of listener threads
   */
  void setConsumersMax(int consumersMax);

  /**
   * Should the JMS sessions be transacted.
   * 
   * <p>Default is false.
   * @param sessionTransacted true if transactions should be used
   */
  void setSessionTransacted(boolean sessionTransacted);

  /**
   * The common trunk part of the queue name of a process
   * update queue.
   * 
   * <p>Default is "default.update.trunk"
   * @param jmsUpdateQueueTrunk the common part of the name
   */
  void setJmsUpdateQueueTrunk(String jmsUpdateQueueTrunk);

  /**
   * Sets the warm-up time. During warm-up time, only the initial
   * number of threads is allowed per container.
   *  
   * <p>Default is 10.
   * @param updateWarmUpSeconds the time in seconds before max threads are allowd
   */
  void setUpdateWarmUpSeconds(int updateWarmUpSeconds);
  
}
