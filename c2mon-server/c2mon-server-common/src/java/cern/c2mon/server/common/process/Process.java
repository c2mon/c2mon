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
package cern.c2mon.server.common.process;

import java.util.Collection;

import cern.c2mon.server.common.process.ProcessCacheObject.LocalConfig;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.shared.common.Cacheable;

/**
 * Interface to the process cache object used in the server process cache.
 *
 * <p>
 * More complicated queries of cache objects can be made through the
 * ProcessFacade bean.
 *
 * @author Mark Brightwell
 *
 */
public interface Process extends Supervised, Cacheable {

  /**
   * Returns the name of the process.
   *
   * @return the name
   */
  @Override
  String getName();

  /**
   * Returns the live list of Equipment ids attached to this Process; locking on
   * Process level required if accessing this.
   *
   * @return list of Equipment ids
   */
  Collection<Long> getEquipmentIds();

  /**
   * Returns the name of the JMS command queue on which the Process (DAQ) is
   * listening for commands during the current Process lifecycle.
   *
   * @return the command queue name as String
   */
  String getJmsDaqCommandQueue();

  /**
   * Returns true if the DAQ requires a reboot to obtain the latest
   * configuration from the server.
   *
   * @return true if restart required
   */
  Boolean getRequiresReboot();

  /**
   * Sets the command queue on which this process will listen for server
   * request.
   *
   * @param jmsDaqCommandQueue the command queue name
   */
  void setJmsDaqCommandQueue(String jmsDaqCommandQueue);

  /**
   * Returns the process PIK
   *
   * @return The process PIK
   */
  Long getProcessPIK();

  /**
   * Returns the name of the host on which the DAQ process has been started.
   *
   * @return the host
   */
  String getCurrentHost();

  /**
   * Checks whether this process is running under a local configuration.
   *
   * @return {@link LocalConfig.Y} if the process is running locally,
   *         {@link LocalConfig.N} otherwise
   */
  LocalConfig getLocalConfig();
}
