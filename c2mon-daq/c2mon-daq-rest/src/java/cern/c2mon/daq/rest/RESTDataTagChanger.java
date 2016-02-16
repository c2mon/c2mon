/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * <p/>
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * <p/>
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.daq.rest;

import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.daq.common.conf.equipment.IDataTagChanger;
import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataQuality;
import cern.c2mon.shared.daq.config.ChangeReport;

import static java.lang.String.format;

/**
 * @author Franz Ritter
 */

/**
 * Handles the DataTagChange operations triggered by a new Configuration.
 */
public class RESTDataTagChanger implements IDataTagChanger {

  /**
   * The equipment logger of this class.
   */
  private EquipmentLogger equipmentLogger;

  private IEquipmentMessageSender equipmentMessageSender;


  private RequestDelegator requestDelegator;

  public RESTDataTagChanger(IEquipmentMessageSender sender, EquipmentLogger logger, RequestDelegator requestDelegator) {
    this.equipmentMessageSender = sender;
    this.equipmentLogger = logger;
    this.requestDelegator = requestDelegator;
  }

  @Override
  public void onAddDataTag(ISourceDataTag sourceDataTag, ChangeReport changeReport) {
    equipmentLogger.trace("Entering onAddDataTag method.");

    try {

      requestDelegator.addDataTag(sourceDataTag);
      changeReport.appendInfo("URL successful tested and added");

    } catch (IllegalArgumentException ex) {

      equipmentLogger.warn("DataTag " + sourceDataTag.getId() + " not configurable - Reason: " + ex.getMessage());
      equipmentMessageSender.sendInvalidTag(sourceDataTag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS, "DataTag " + sourceDataTag.getId() + " not configurable - Reason: " + ex.getMessage());
      changeReport.appendError("DataTag " + sourceDataTag.getId() + " cant be add to the Equipment - Reason: " + ex.getMessage());
    }

    equipmentLogger.trace("Leaving onAddDataTag method.");
  }

  @Override
  public void onRemoveDataTag(ISourceDataTag sourceDataTag, ChangeReport changeReport) {
    equipmentLogger.trace("Entering onRemoveDataTag method.");

    try {

      requestDelegator.removeDataTag(sourceDataTag);

    } catch (IllegalArgumentException ex) {

      equipmentLogger.warn("Problem caused by removing of DataTag " + sourceDataTag.getId() + ": " + ex.getMessage());
      changeReport.appendWarn("Problem caused by removing of the DataTag " + sourceDataTag.getId() + ": " + ex.getMessage());
    }

    equipmentLogger.trace("Leaving onRemoveDataTag method.");
  }

  @Override
  public void onUpdateDataTag(ISourceDataTag sourceDataTag, ISourceDataTag oldSourceDataTag, ChangeReport changeReport) {

    try {

      requestDelegator.updateDataTag(sourceDataTag, oldSourceDataTag);

    } catch (IllegalArgumentException ex) {
      equipmentLogger.warn("Problem caused by updating of of DataTag " + sourceDataTag.getId() + ": " + ex.getMessage());
      changeReport.appendError("Problem caused by updating of of DataTag " + sourceDataTag.getId() + ": " + ex.getMessage());
    }


  }


}
