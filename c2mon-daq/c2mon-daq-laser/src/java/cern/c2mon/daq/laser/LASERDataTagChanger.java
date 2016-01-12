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
package cern.c2mon.daq.laser;

import static java.lang.String.format;
import cern.c2mon.daq.common.conf.equipment.IDataTagChanger;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ChangeReport.CHANGE_STATE;

/**
 * The LASERDataTagChanger is used for dynamic reconfiguration of Data Tags
 *
 * @author Nacho Vilches
 *
 */
public class LASERDataTagChanger implements IDataTagChanger {

  /**
   * LASER controller
   */
  private LASERController laserController;

  /**
   * Creates a new SSHDataTagChanger.
   *
   * @param laserController
   *
   */
  public LASERDataTagChanger(final LASERController laserController) {
   this.laserController = laserController;
  }

  @Override
  public final void onAddDataTag(final ISourceDataTag sourceDataTag, final ChangeReport changeReport) {
    if (this.laserController.getEquipmentLogger().isDebugEnabled()) {
      this.laserController.getEquipmentLogger().debug(format("onAddDataTag - entering onAddCommandTag(%d)..", sourceDataTag.getId()));
    }

    // Connect the Data Tag
    changeReport.setState(this.laserController.connection(sourceDataTag, changeReport));
    if (changeReport.getState() == CHANGE_STATE.SUCCESS) {
      changeReport.appendInfo("onAddDataTag - SourceDataTag added ...");
    } else {
      changeReport.appendInfo("onAddDataTag - SourceDataTag not added ...");
    }

    if (this.laserController.getEquipmentLogger().isDebugEnabled()) {
      this.laserController.getEquipmentLogger().debug(format("onAddDataTag - leaving onAddCommandTag(%d)", sourceDataTag.getId()));
    }

  }

  @Override
  public void onRemoveDataTag(final ISourceDataTag sourceDataTag, final ChangeReport changeReport) {
    if (this.laserController.getEquipmentLogger().isDebugEnabled()) {
      this.laserController.getEquipmentLogger().debug(format("onRemoveDataTag - entering onRemoveDataTag(%d)..", sourceDataTag.getId()));
    }

    // Disconnect the Data Tag (nothing to do)
    changeReport.appendInfo("SourceDataTag removed ...");
    changeReport.setState(CHANGE_STATE.SUCCESS);

    if (this.laserController.getEquipmentLogger().isDebugEnabled()) {
      this.laserController.getEquipmentLogger().debug(format("onRemoveDataTag - leaving onRemoveDataTag(%d)", sourceDataTag.getId()));
    }

  }

  @Override
  public void onUpdateDataTag(final ISourceDataTag sourceDataTag, final ISourceDataTag oldSourceDataTag, final ChangeReport changeReport) {
    if (this.laserController.getEquipmentLogger().isDebugEnabled()) {
      this.laserController.getEquipmentLogger().debug(format("onUpdateDataTag - entering onUpdateDataTag(%d)..", sourceDataTag.getId()));
    }

    // Disconnect old Data Tag (nothing to do)
    // Connect the new Data Tag
    changeReport.setState(this.laserController.connection(sourceDataTag, changeReport));
    if (changeReport.getState() == CHANGE_STATE.SUCCESS) {
      changeReport.appendInfo("onUpdateDataTag - SourceDataTag updated ...");
    } else {
      changeReport.appendInfo("onUpdateDataTag - SourceDataTag not updated ...");
    }

    if (this.laserController.getEquipmentLogger().isDebugEnabled()) {
      this.laserController.getEquipmentLogger().debug(format("onUpdateDataTag - leaving onUpdateDataTag(%d)", sourceDataTag.getId()));
    }
  }

}
