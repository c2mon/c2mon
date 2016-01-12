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
package cern.c2mon.daq.ssh.tools;

import java.util.TimerTask;

import cern.c2mon.daq.tools.equipmentexceptions.EqDataTagException;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataQuality;

/**
 * This class models the action/task that is taken each timer's 'tick'
 *
 * @author vilches
 */
public class PeriodicSSHTask extends TimerTask {

  ISourceDataTag tag;

  /**
   * SSH Helper class with some helping methods.
   */
  private SSHHelper sshHelper;

  public PeriodicSSHTask(ISourceDataTag tag, SSHHelper sshHelper) {
    this.tag = tag;
    this.sshHelper = sshHelper;
  }

  /**
   * @roseuid 43302FED02AF
   */
  @Override
  public void run() {
    try {
      this.sshHelper.recalculateDataTagValue(tag.getId());
    } catch (EqDataTagException ex) {
      this.sshHelper.getEquipmentLogger().warn(ex.getMessage());
      this.sshHelper.getEquipmentLogger().debug("invalidating tag");
      this.sshHelper.getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.DATA_UNAVAILABLE, ex.getErrorDescription());
    }
  }
}
