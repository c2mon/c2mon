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
//su TIM. CERN. All rights reserved.
//  
// T Nick:           Date:       Info:
// -------------------------------------------------------------------------
// D wbuczak      09/Aug/2006     First implementation 
// P wbuczak      21/Aug/2006     Version 1.0b ready for tests
// 
//
// -------------------------------------------------------------------------

package cern.c2mon.daq.laser;

import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.tools.equipmentexceptions.EqCommandTagException;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.c2mon.shared.daq.command.SourceCommandTagValue;


/**
 * This is a specialized subclass of the general EquipmentMessageHandler. 
 * The class implements TIM EquipmentMessageHandler for communication with LASER system.
 * 
 * @author vilches (refactoring)
 * 
 */  
public class LASERMessageHandler extends EquipmentMessageHandler {
    
  /**
   * LASER controller
   */
  private LASERController laserController;

  /**
   * This method is called by the DAQ.core to connect the equipment handler to its dedicated data source.
   * The method is called also, if the handler detects that the connection with LASER is lost. 
   * We connection to LASER and than try to subscribe to all configured alarms. Before opening the subscription 
   * for alarms, we check one-by-one if alarms defined by tripplets (fault_family:fault_member:fault_code) are defined 
   * in LASER. If not - we invalidate the corresponding TIM tag. If no exception is caught, and we manage to subscribe 
   * to at least one alarm, we send a confirmation to TIM that the connection procedure succeeded. 
   * 
   * Temporarily, the alarm filtering mechanism is not used (the handler receives callbacks for all alarms from the category, and later 
   * filters them itself)
   * 
   * @throws EqIOException 
   */
  public final void connectToDataSource() throws EqIOException {
  
    if (getEquipmentLogger().isDebugEnabled()) {
      getEquipmentLogger().debug("connectToDataSource - entering connectToDataSource(). Creating subscriptions for all registered DataTags..");
    }
    
    // Controller
    this.laserController = new LASERController(getEquipmentLoggerFactory(), getEquipmentConfiguration(), getEquipmentMessageSender());
    
    // Data Tag Changer
    LASERDataTagChanger dataTagChanger = new LASERDataTagChanger(this.laserController);
    getEquipmentConfigurationHandler().setDataTagChanger(dataTagChanger);
    
    // Connection
    this.laserController.connection();
      
    if (getEquipmentLogger().isDebugEnabled()) {
      getEquipmentLogger().debug("connectToDataSource - leaving connectToDataSource()");
    }
           
  }

  /**
   * Disconnect all data tags from the data source
   * 
   * @throws EqIOException 
   */
  public final void disconnectFromDataSource() throws EqIOException {
    if (getEquipmentLogger().isDebugEnabled()) {
      getEquipmentLogger().debug("disconnectFromDataSource - entering diconnectFromDataSource()..");
    }
    
    if (this.laserController != null) {
      this.laserController.disconnection();
    }

    if (getEquipmentLogger().isDebugEnabled()) {
      getEquipmentLogger().debug("disconnectFromDataSource - leaving diconnectFromDataSource()");
    }
  }

  /**
   * 
   * @param p0 
   * @throws EqCommandTagException 
   */
  protected final void sendCommand(final SourceCommandTagValue p0) throws EqCommandTagException {
    if (getEquipmentLogger().isDebugEnabled()) {
      getEquipmentLogger().debug("sendCommand() called.");
    }
    
    throw new EqCommandTagException("LASERMessageHandler does not support commands");
  }

  @Override
  public void refreshAllDataTags() {
      // TODO Implement this method at the moment it might be part of the connectToDataSourceMehtod
  }
  
  @Override
  public void refreshDataTag(final long dataTagId) {
      // TODO Implement this method.
  }

}
