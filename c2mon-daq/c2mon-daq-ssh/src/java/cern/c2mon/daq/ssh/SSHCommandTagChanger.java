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

import static java.lang.String.format;
import cern.c2mon.daq.common.conf.equipment.ICommandTagChanger;
import cern.c2mon.shared.common.command.ISourceCommandTag;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ChangeReport.CHANGE_STATE;

/**
 * The SSHCommandTagChanger is used for dynamic reconfiguration of Commands
 *
 * @author Nacho Vilches
 *
 */
public class SSHCommandTagChanger implements ICommandTagChanger {

  /**
   * SSH controller
   */
  private SSHController sshController;

  /**
   * Creates a new SSHCommandTagChanger.
   *
   */
  public SSHCommandTagChanger(SSHController sshController) {
   this.sshController = sshController;
  }

  @Override
  public void onAddCommandTag(ISourceCommandTag sourceCommandTag, ChangeReport changeReport) {
    if (this.sshController.getSSHHelper().getEquipmentLogger().isDebugEnabled()) {
      this.sshController.getSSHHelper().getEquipmentLogger().debug(format("entering onAddCommandTag(%d)..", sourceCommandTag.getId()));
    }

    // nothing more to be done here
    changeReport.appendInfo("sourceCommandTag added ...");
    changeReport.setState(CHANGE_STATE.SUCCESS);

    if (this.sshController.getSSHHelper().getEquipmentLogger().isDebugEnabled()) {
      this.sshController.getSSHHelper().getEquipmentLogger().debug(format("leaving onAddCommandTag(%d)", sourceCommandTag.getId()));
    }

  }

  @Override
  public void onRemoveCommandTag(ISourceCommandTag sourceCommandTag, ChangeReport changeReport) {
    if (this.sshController.getSSHHelper().getEquipmentLogger().isDebugEnabled()) {
      this.sshController.getSSHHelper().getEquipmentLogger().debug(format("entering onRemoveCommandTag(%d)..", sourceCommandTag.getId()));
    }

    // nothing more to be done here
    changeReport.appendInfo("sourceCommandTag removed ...");
    changeReport.setState(CHANGE_STATE.SUCCESS);

    if (this.sshController.getSSHHelper().getEquipmentLogger().isDebugEnabled()) {
      this.sshController.getSSHHelper().getEquipmentLogger().debug(format("leaving onRemoveCommandTag(%d)", sourceCommandTag.getId()));
    }

  }

  @Override
  public void onUpdateCommandTag(ISourceCommandTag sourceCommandTag, ISourceCommandTag oldSourceCommandTag, ChangeReport changeReport) {
    if (this.sshController.getSSHHelper().getEquipmentLogger().isDebugEnabled()) {
      this.sshController.getSSHHelper().getEquipmentLogger().debug(
          format("entering onUpdateCommandTag(%d,%d)..", sourceCommandTag.getId(), oldSourceCommandTag.getId()));
    }

    // nothing more to be done here
    changeReport.appendInfo("sourceCommandTag updated ...");
    changeReport.setState(CHANGE_STATE.SUCCESS);

    if (this.sshController.getSSHHelper().getEquipmentLogger().isDebugEnabled()) {
      this.sshController.getSSHHelper().getEquipmentLogger().debug(
          format("leaving onUpdateCommandTag(%d,%d)", sourceCommandTag.getId(), oldSourceCommandTag.getId()));
    }
  }

}
