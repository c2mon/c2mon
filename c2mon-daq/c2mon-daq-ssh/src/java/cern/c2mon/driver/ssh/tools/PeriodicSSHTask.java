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
package cern.c2mon.driver.ssh.tools;

import java.util.TimerTask;

import cern.c2mon.driver.tools.equipmentexceptions.EqDataTagException;
import cern.tim.shared.daq.datatag.ISourceDataTag;
import cern.tim.shared.daq.datatag.SourceDataQuality;

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
