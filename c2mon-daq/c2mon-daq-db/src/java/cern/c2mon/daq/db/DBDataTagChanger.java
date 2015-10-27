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
package cern.c2mon.daq.db;

import static java.lang.String.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.daq.common.conf.core.ConfigurationController;
import cern.c2mon.daq.common.conf.equipment.IDataTagChanger;
import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ChangeReport.CHANGE_STATE;

/**
 * The DBDataTagChanger is used for dynamic reconfiguration of Data Tags
 * 
 * @author Nacho Vilches
 * 
 */
public class DBDataTagChanger implements IDataTagChanger {
  
  /**
   * DB controller
   */
  private DBController dbController;
  
  /**
   * The equipment logger of this class.
   */
  private EquipmentLogger equipmentLogger;
  
  /**
   * Creates a new DBDataTagChanger.
   *
   */
  public DBDataTagChanger(DBController dbController, EquipmentLogger equipmentLogger) {
   this.dbController = dbController;
   this.equipmentLogger = equipmentLogger;
  }

  @Override
  public void onAddDataTag(ISourceDataTag sourceDataTag, ChangeReport changeReport) {
    if (this.equipmentLogger.isDebugEnabled()) {
      this.equipmentLogger.debug(format("entering onAddCommandTag(%d)..", sourceDataTag.getId()));
    }

    // Connect the Data Tag
    this.equipmentLogger.trace("onAddDataTag - Connecting ...");
    changeReport.setState(this.dbController.connection(sourceDataTag, changeReport));
    this.equipmentLogger.trace("onAddDataTag - Connected ...");
    this.equipmentLogger.trace("onAddDataTag - Status " + changeReport.getState());
    if (changeReport.getState() == CHANGE_STATE.SUCCESS) {
      changeReport.appendInfo("onAddDataTag - SourceDataTag added ...");
    } else {
      changeReport.appendInfo("onAddDataTag - SourceDataTag not added ...");
    }

    if (this.equipmentLogger.isDebugEnabled()) {
      this.equipmentLogger.debug(format("leaving onAddCommandTag(%d)", sourceDataTag.getId()));
    }   
  }

  @Override
  public void onRemoveDataTag(ISourceDataTag sourceDataTag, ChangeReport changeReport) {   
    if (this.equipmentLogger.isDebugEnabled()) {
      this.equipmentLogger.debug(format("entering onRemoveDataTag(%d)..", sourceDataTag.getId()));
    }

    // Disconnect the Data Tag
    this.equipmentLogger.trace("onRemoveDataTag - Disconnecting ...");
    changeReport.setState(this.dbController.disconnection(sourceDataTag, changeReport));
    this.equipmentLogger.trace("onRemoveDataTag - Disconnect ...");
    this.equipmentLogger.trace("onRemoveDataTag - Status " + changeReport.getState());
    if (changeReport.getState() == CHANGE_STATE.SUCCESS) {
      changeReport.appendInfo("onRemoveDataTag - SourceDataTag removed ...");
    } else {
      changeReport.appendInfo("onRemoveDataTag - SourceDataTag not removed ...");
    }

    if (this.equipmentLogger.isDebugEnabled()) {
      this.equipmentLogger.debug(format("leaving onRemoveDataTag(%d)", sourceDataTag.getId()));
    }
  }

  @Override
  public void onUpdateDataTag(ISourceDataTag sourceDataTag, ISourceDataTag oldSourceDataTag, ChangeReport changeReport) {
    if (this.equipmentLogger.isDebugEnabled()) {
      this.equipmentLogger.debug(format("entering onUpdateDataTag(%d)..", sourceDataTag.getId()));
    }

    // Disconnect old Data Tag 
    changeReport.setState(this.dbController.disconnection(oldSourceDataTag, changeReport));
    
    // Connect the new Data Tag only if the disconnection was ok
    if (changeReport.getState() == CHANGE_STATE.SUCCESS) {
      changeReport.setState(this.dbController.connection(sourceDataTag, changeReport));
    } else {
      changeReport.appendInfo("onUpdateDataTag - problems disconnecing the old source fata tag ...");
    }
    
    if (changeReport.getState() == CHANGE_STATE.SUCCESS) {
      changeReport.appendInfo("onUpdateDataTag - SourceDataTag updated ...");
    } else {
      changeReport.appendInfo("onUpdateDataTag - SourceDataTag not updated ...");
    }

    if (this.equipmentLogger.isDebugEnabled()) {
      this.equipmentLogger.debug(format("leaving onUpdateDataTag(%d)", sourceDataTag.getId()));
    }
    
  }

}
