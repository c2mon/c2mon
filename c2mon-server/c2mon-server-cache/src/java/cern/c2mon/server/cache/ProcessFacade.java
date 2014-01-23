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
package cern.c2mon.server.cache;

import java.sql.Timestamp;
import java.util.Collection;

import cern.c2mon.server.cache.common.ConfigurableCacheFacade;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.common.process.ProcessCacheObject.LocalConfig;

/**
 * The ProcessFacade bean is used for complex operations
 * on the {@link ProcessCacheObject}. Modifying the cache
 * object directly should be avoided - this bean provides
 * the required synchronisation.
 * 
 * @author Mark Brightwell
 *
 */
public interface ProcessFacade extends SupervisedFacade<Process>, ConfigurableCacheFacade<Process> {  

  /**
   * Records the start up time of the process and the host it is running on,
   * (and sets it's status to STARTUP - may remove this in the future as duplicate
   * of state tag of the DAQ)
   * 
   * <p>Also starts the alive timer.
   * 
   * @param processId the Id of the Process that is starting
   * @param hostName the hostname of the Process
   * @param startupTime the start up time
   * 
   * @return A copy of the last modifications that were added to the process cache, or <code>null</code>
   */
  Process start(Long processId, String hostName, Timestamp startupTime);
  
  /**
   * Returns a collection of the ids of all DataTags
   * registered with this DAQ (not control tags).
   * @param processId the Process id
   * @return the ids in a collection
   */
  Collection<Long> getDataTagIds(Long processId);

  /**
   * Sets the status of the process to error AND updates
   * the state tag!!
   * @param processId id of the process
   */
  void errorStatus(Long processId, String errorMessage);
  
  /**
   * Returns the process id in the cache for a given Alive Timer id.
   * 
   * <p>Throws a {@link CacheElementNotFoundException} if some cache object
   * cannot be located. Throws a {@link NullPointerException} is some parent
   * equipment or process id is not set.
   * 
   * <p>Assumes relatedId of Alive Timer is not null.
   * 
   * @param id id of Alive Timer of the process/equipment/subequipment linked to the alive
   * @return the Process id above the Alive Timer
   */
  Long getProcessIdFromAlive(Long aliveTimerId);

  /**
   * Returns the id of the Process to which this
   * Control tag belongs, for Control Tag associated to (Sub-)Equipments.
   * @param controlTagId id of Control tag
   * @return the process id; null if no Process can determined
   */
  Long getProcessIdFromControlTag(Long controlTagId);
  
  /**
   * Returns true if the DAQ requires a reboot to 
   * obtain the latest configuration from the server.
   * @param processId id of the process
   * @return true if restart required
   */
  Boolean isRebootRequired(Long processId);
  
  /**
   * Sets the Process reboot flag, indicating if the Process
   * needs restarting.
   * @param processId id of the process
   * @param reboot true if restart required
   */
  void requiresReboot(Long processId, Boolean reboot);

  /**
   * Sets the PIK of the process.
   * 
   * @param processId Id of the process
   * @param processPIK The process PIK
   */
  void setProcessPIK(Long processId, Long processPIK);
  
  /**
   * Sets the Configuration type to Y (Local) or N (Server)
   * 
   * @param processId Id of the process
   * @param localConfig Y(LOCAL_CONFIG)/N(SERVER_CONFIG)
   */
  void setLocalConfig(Long processId, LocalConfig localType);
  
}
