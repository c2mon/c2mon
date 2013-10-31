/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2013 CERN.
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
package cern.c2mon.daq.ssh;

import static java.lang.String.format;
import cern.c2mon.daq.common.conf.equipment.ICommandTagChanger;
import cern.tim.shared.daq.command.ISourceCommandTag;
import cern.tim.shared.daq.config.ChangeReport;
import cern.tim.shared.daq.config.ChangeReport.CHANGE_STATE;

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
