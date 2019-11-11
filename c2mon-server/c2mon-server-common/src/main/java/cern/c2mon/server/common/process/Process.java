/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.common.process;

import cern.c2mon.server.common.process.ProcessCacheObject.LocalConfig;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.shared.common.supervision.SupervisionConstants;

import java.sql.Timestamp;
import java.util.Collection;

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
public interface Process extends Supervised {

  /**
   * Returns the name of the process.
   *
   * @return the name
   */
  @Override
  String getName();

  String getDescription();

  /**
   * Returns the live list of Equipment ids attached to this Process; locking on
   * Process level required if accessing this.
   *
   * @return list of Equipment ids
   */
  Collection<Long> getEquipmentIds();

  /**
   * Returns true if the DAQ requires a reboot to obtain the latest
   * configuration from the server.
   *
   * @return true if restart required
   */
  Boolean getRequiresReboot();

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

  @Override
  default void resume(final Timestamp timestamp, final String message) {
    Supervised.super.resume(timestamp,message);
    if (getLocalConfig() != null && getLocalConfig().equals(ProcessCacheObject.LocalConfig.Y)) {
      setSupervision(SupervisionConstants.SupervisionStatus.RUNNING_LOCAL, message, timestamp);
    }
  }
}
