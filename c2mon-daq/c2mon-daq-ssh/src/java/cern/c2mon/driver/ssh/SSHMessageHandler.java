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
//Source file: E:\\development\\CERN\\tim-daq\\tim-daq-ssh\\src\\java\\ch\\cern\\tim\\driver\\ssh\\SSHMessageHandler.java

// TIM. CERN. All rights reserved.
//  
// T Nick:           Date:       Info:
// -------------------------------------------------------------------------
// D wbuczak    19/Sep/2005     Class generation from the model
// P wbuczak    20/Sep/2005     First implementation
// 
// -------------------------------------------------------------------------

package cern.c2mon.driver.ssh;

import cern.c2mon.driver.common.EquipmentMessageHandler;
import cern.c2mon.driver.ssh.tools.SSHHelper;
import cern.c2mon.driver.tools.equipmentexceptions.EqException;
import cern.c2mon.driver.tools.equipmentexceptions.EqIOException;
import cern.tim.shared.daq.datatag.ISourceDataTag;

/**
 * The SSHMessageHandler is used for handling TIM ssh requests in the data
 * acquisition layer.
 * 
 * @author vilches
 */
public class SSHMessageHandler extends EquipmentMessageHandler {



  //private List<PeriodicSSHCommandExecutor> sshCommandExecutors = new LinkedList<PeriodicSSHCommandExecutor>();

  /**
   * SSH controller
   */
  private SSHController sshController;

  /**
   * SSH Command Runner to process commands.
   */
  private SSHCommandRunner sshCommandRunner;

  /**
   * SSH Helper class with some helping methods.
   */
  private SSHHelper sshHelper;

  
  /**
   * @throws cern.c2mon.driver.tools.equipmentexceptions.EqIOException
   */
  public void connectToDataSource() throws EqIOException {
    if (getEquipmentLogger().isDebugEnabled()) {
      getEquipmentLogger().debug("connectToDataSource - entering connectToDataSource()..");
    }
    
    // Create the Helper object
    this.sshHelper = new SSHHelper(getEquipmentLogger(), getEquipmentConfiguration(), getEquipmentMessageSender());

    // try to parse the equipment address
    try {
      this.sshHelper.parseExuipmentAddress(this.getEquipmentConfiguration().getAddress());
    } catch (EqException ex) {
      throw new EqIOException(ex.getErrorDescription());
    }

    getEquipmentMessageSender().confirmEquipmentStateOK();

    // Create the command Runner
    this.sshCommandRunner = new SSHCommandRunner(this.sshHelper);
    // Create the Controller
    this.sshController = new SSHController(this.sshHelper);

    // Add the Data Tag Changer
    SSHDataTagChanger dataTagChanger = new SSHDataTagChanger(this.sshController);
    getEquipmentConfigurationHandler().setDataTagChanger(dataTagChanger);

    // Add the Command Tag Changer
    SSHCommandTagChanger commandTagChanger = new SSHCommandTagChanger(this.sshController);
    getEquipmentConfigurationHandler().setCommandTagChanger(commandTagChanger);

    // Go through all data tags and start-up the periodic executors..
    for (ISourceDataTag sdt : getEquipmentConfiguration().getSourceDataTags().values()) {
      this.sshController.connection(sdt, null);        
    }

    // Add the Command Runner
    getEquipmentCommandHandler().setCommandRunner(this.sshCommandRunner);

    if (getEquipmentLogger().isDebugEnabled()) {
      getEquipmentLogger().debug("connectToDataSource - leaving connectToDataSource()");
    }
  }

  /**
   * @throws EqIOException
   */
  public void disconnectFromDataSource() throws EqIOException {
    if (getEquipmentLogger().isDebugEnabled()) {
      getEquipmentLogger().debug("disconnectFromDataSource - entering diconnectFromDataSource()..");
    }
    
    // Disconnect all
    if (this.sshController != null) {
      for (Long tagID : this.sshController.getSshCommandExecutors().keySet()) {
        this.sshController.disconnection(tagID, null);
      }

      this.sshController.getSshCommandExecutors().clear();
    }

    if (getEquipmentLogger().isDebugEnabled()) {
      getEquipmentLogger().debug("disconnectFromDataSource - leaving diconnectFromDataSource()");
    }
  }    

  @Override
  public void refreshAllDataTags() {
    // TODO Implement this method at the moment it might be part of the
    // connectToDataSourceMehtod
  }

  @Override
  public void refreshDataTag(long dataTagId) {
    // TODO Implement this method.
  }
}
