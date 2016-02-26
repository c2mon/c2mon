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
package cern.c2mon.daq.dip;

import static java.lang.String.format;
import cern.c2mon.daq.common.conf.equipment.IDataTagChanger;
import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ChangeReport.CHANGE_STATE;

/**
 * The DIPDataTagChanger is used for dynamic reconfiguration of Data Tags
 *
 * @author Nacho Vilches
 *
 */
public class DIPDataTagChanger implements IDataTagChanger {

  /**
   * DIP controller
   */
  private DIPController dipController;

  /**
   * The equipment logger of this class.
   */
  private EquipmentLogger equipmentLogger;

  /**
   * Creates a new DIPDataTagChanger.
   *
   * @param dipController
   *
   */
  public DIPDataTagChanger(DIPController dipController, EquipmentLogger equipmentLogger) {
   this.dipController = dipController;
   this.equipmentLogger = equipmentLogger;
  }

  @Override
  public void onAddDataTag(ISourceDataTag sourceDataTag, ChangeReport changeReport) {
    if (getEquipmentLogger().isTraceEnabled()) {
      getEquipmentLogger().trace(format("onAddDataTag - entering onAddDataTag(%d)..", sourceDataTag.getId()));
    }

    // Connect the Data Tag
    changeReport.setState(this.dipController.connect(sourceDataTag, changeReport));
    if (changeReport.getState() == CHANGE_STATE.SUCCESS) {
      changeReport.appendInfo("Tag successfully registered in DIP DAQ");
    } else {
      changeReport.appendError("Tag registration in DIP DAQ was NOT successful!");
    }

    if (getEquipmentLogger().isTraceEnabled()) {
      getEquipmentLogger().trace(format("onAddDataTag - leaving onAddDataTag(%d)", sourceDataTag.getId()));
    }
  }

  @Override
  public void onRemoveDataTag(ISourceDataTag sourceDataTag, ChangeReport changeReport) {
    if (getEquipmentLogger().isTraceEnabled()) {
      getEquipmentLogger().trace(format("onRemoveDataTag - entering onRemoveDataTag(%d)..", sourceDataTag.getId()));
    }

    // Connect the Data Tag
    changeReport.setState(this.dipController.disconnection(sourceDataTag, changeReport));
    if (changeReport.getState() == CHANGE_STATE.SUCCESS) {
      changeReport.appendInfo("Tag successfully removed from DIP DAQ");
    } else {
      changeReport.appendWarn("Problem occured during tag unsubscription");
    }

    if (getEquipmentLogger().isTraceEnabled()) {
      getEquipmentLogger().trace(format("onRemoveDataTag - leaving onRemoveDataTag(%d)", sourceDataTag.getId()));
    }
  }

  @Override
  public void onUpdateDataTag(ISourceDataTag sourceDataTag, ISourceDataTag oldSourceDataTag, ChangeReport changeReport) {
    if (getEquipmentLogger().isTraceEnabled()) {
      getEquipmentLogger().trace(format("onUpdateDataTag - entering onUpdateDataTag(%d)..", sourceDataTag.getId()));
    }

    if (oldSourceDataTag.getHardwareAddress().equals(sourceDataTag.getHardwareAddress())) {
      changeReport.setState(CHANGE_STATE.SUCCESS);
      getEquipmentLogger().debug("onUpdateDataTag - DIP address has not changed.");
    }
    else {
      // Disconnect old Data Tag
      this.dipController.disconnection(oldSourceDataTag, changeReport);
      
      changeReport.setState(this.dipController.connect(sourceDataTag, changeReport));
  
      if (changeReport.getState() == CHANGE_STATE.SUCCESS) {
        changeReport.appendInfo("Tag on DIP DAQ successfully updated.");
      } else {
        changeReport.appendError("Tag update in DIP DAQ to new topic was NOT successful!");
      }
    }

    if (getEquipmentLogger().isTraceEnabled()) {
      getEquipmentLogger().trace(format("onUpdateDataTag - leaving onUpdateDataTag(%d)", sourceDataTag.getId()));
    }
  }

  /**
   * @return the equipmentLogger
   */
  public EquipmentLogger getEquipmentLogger() {
      return this.equipmentLogger;
  }

}
