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
package cern.c2mon.daq.ssh;

import java.util.concurrent.ConcurrentHashMap;

import cern.c2mon.daq.ssh.tools.PeriodicSSHCommandExecutor;
import cern.c2mon.daq.ssh.tools.SSHHelper;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.address.SSHHardwareAddress;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ChangeReport.CHANGE_STATE;

/**
 * SSH Controller to control tag and command configuration
 *
 * @author vilches
 *
 */
public class SSHController {
  /**
   * SSH Helper class with some helping methods.
   */
  private SSHHelper sshHelper;

  /**
   * A HashMap for handling Command Executors for a given data tag id
   */
  private final ConcurrentHashMap<Long, PeriodicSSHCommandExecutor> sshCommandExecutors = new ConcurrentHashMap<Long, PeriodicSSHCommandExecutor>();

  /**
   * Conversion to pass Seconds to Miliseconds
   */
  private static final int SC2MS_CONVERSION = 1000;

  /**
   * Constructor
   *
   */
  public SSHController(SSHHelper sshHelper) {
    this.sshHelper = sshHelper;
  }

  /**
   * Connection
   *
   * @param sourceDataTag SourceDataTag tp connect
   */
  public CHANGE_STATE connection(ISourceDataTag sourceDataTag, ChangeReport changeReport) {
    this.sshHelper.getEquipmentLogger().debug("connection - Connecting " + sourceDataTag.getId());

    if (((SSHHardwareAddress) sourceDataTag.getHardwareAddress()).getCallInterval() * SC2MS_CONVERSION > 0) {
      this.sshCommandExecutors.put(sourceDataTag.getId(), new PeriodicSSHCommandExecutor(sourceDataTag, this.sshHelper));

      return CHANGE_STATE.SUCCESS;
    }

    if (changeReport != null) {
      changeReport.appendError("connection - pull event interval("
          + ((SSHHardwareAddress) sourceDataTag.getHardwareAddress()).getCallInterval() + ") too short for " + sourceDataTag.getId()
          + ". It should be greater than 1 second.");
    }

    return CHANGE_STATE.FAIL;
  }

  /**
   * Disconnection
   *
   * @param sourceDataTag
   * @param changeReport
   */
  public CHANGE_STATE disconnection(ISourceDataTag sourceDataTag, ChangeReport changeReport) {
    return disconnection(sourceDataTag.getId(), changeReport);
  }

  /**
   * Disconnection
   *
   * @param tagID ID of the Data Tag to disconnect
   * @param changeReport
   */
  public CHANGE_STATE disconnection(Long tagID, ChangeReport changeReport) {
    this.sshHelper.getEquipmentLogger().debug("disconnection - Disconnecting " + tagID);

    // If the Tag ID exists we stop the timer and remove it from list
    if (this.sshCommandExecutors.get(tagID) == null) {
      // Terminate timer
      this.sshCommandExecutors.get(tagID).terminateTimer();

      // Remove key
      this.sshCommandExecutors.remove(tagID);

      return CHANGE_STATE.SUCCESS;
    }

    if (changeReport != null) {
      changeReport.appendError("connection - wrong Tag ID " + tagID);
    }

    return CHANGE_STATE.FAIL;
  }

  /**
   * @return the sshCommandExecutors map
   */
  public ConcurrentHashMap<Long, PeriodicSSHCommandExecutor> getSshCommandExecutors() {
      return this.sshCommandExecutors;
  }

  /**
   * @return the sshHelper
   */
  public SSHHelper getSSHHelper() {
    return this.sshHelper;
  }

}
