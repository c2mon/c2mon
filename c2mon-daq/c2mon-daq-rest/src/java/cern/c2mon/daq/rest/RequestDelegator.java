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
import cern.c2mon.daq.rest.controller.RestController;
import cern.c2mon.daq.rest.address.RestGetAddress;
import cern.c2mon.daq.rest.address.RestAddressFactory;
import cern.c2mon.daq.rest.address.RestPostAddress;
import cern.c2mon.daq.rest.scheduling.GetScheduler;
import cern.c2mon.daq.rest.scheduling.PostScheduler;
import cern.c2mon.daq.rest.scheduling.RestScheduler;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataTag;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.common.process.IEquipmentConfiguration;

/**
 * @author Franz Ritter
 */

/**
 * This is a helper Class to coordinate all functionality of this DAQ.
 * There are two different ways to trigger Actions.
 * The first is to use the {@link IDataTagChanger} to add, update and remove DataTags of this DAQ.
 * The second way comes through the MessageHandler itself, which can trigger a refresh of all DataTags of this daq.
 * <p/>
 */
public class RequestDelegator {

  /**
   * Holds all Configuration information of this daq
   */
  private IEquipmentConfiguration equipmentConfiguration;


  private GetScheduler getScheduler;

  private PostScheduler postScheduler;

  public RequestDelegator(IEquipmentMessageSender sender, IEquipmentConfiguration configuration, EquipmentLogger logger, RestController controller) {

    this.equipmentConfiguration = configuration;
    this.getScheduler = new GetScheduler(sender, configuration, logger);
    this.postScheduler = new PostScheduler(sender, configuration, logger);

    // add the Scheduler to the controller
    controller.setPostScheduler(postScheduler);
  }

  /**
   * Adds a new DataTag to the daq.
   * In order to do this the manager determines which kind of functionality (GET or POST) thos
   * DataTag belongs to.
   * Furthermore a HardwareAddress based on this functionality will be created and
   * the tag will be attached to the correct scheduler.
   * @param sourceDataTag The new DataTag which needs to be added.
   */
  public void addDataTag(ISourceDataTag sourceDataTag) {

    if (sourceDataTag.getAddressParameters() != null ) {
      SourceDataTag sdt = (SourceDataTag) sourceDataTag;

        // Add HardwareAddress to the SourceDataTag values
        HardwareAddress hardwareAddress = RestAddressFactory.createHardwareAddress(sdt.getAddressParameters());
        sdt.setHardwareAddress(hardwareAddress);

        // get the right scheduler for this DataTag and add the task to it
        RestScheduler scheduler = getScheduler(hardwareAddress);
        scheduler.addTask(sourceDataTag.getId());

    } else {

      throw new IllegalArgumentException("Cant add DataTag to the DAQ without HardwareAddress information - addressParameters are null.");
    }

  }

  /**
   * removes a DataTag from the daq.
   * @param sourceDataTag The DataTag which needs to be removed.
   */
  public void removeDataTag(ISourceDataTag sourceDataTag) {

    RestScheduler scheduler = getScheduler(sourceDataTag.getHardwareAddress());
    scheduler.removeTask(sourceDataTag.getId());

  }

  /**
   * Updates the DataTag.
   * @param sourceDataTag The new DataTag with the new informations.
   * @param oldSourceDataTag The old DataTag.
   */
  public void updateDataTag(ISourceDataTag sourceDataTag, ISourceDataTag oldSourceDataTag) {

    if (sourceDataTag.getAddressParameters() != null && oldSourceDataTag.getAddressParameters() != null) {
      SourceDataTag sdt = (SourceDataTag) sourceDataTag;
      SourceDataTag oldSdt = (SourceDataTag) oldSourceDataTag;

      Long tagId = sdt.getId();

      //delete teh old task from the old scheduler
      RestScheduler scheduler = getScheduler(oldSdt.getHardwareAddress());
      scheduler.removeTask(tagId);

      // Add the new HardwareAddress to the SourceDataTag values
      HardwareAddress hardwareAddress = RestAddressFactory.createHardwareAddress(sdt.getAddressParameters());
      sdt.setHardwareAddress(hardwareAddress);

      // get the right scheduler for this DataTag and add the new task to it
      scheduler = getScheduler(hardwareAddress);
      scheduler.addTask(tagId);


    } else {
      throw new UnsupportedOperationException("DataTag "+sourceDataTag.getId()+" needs first to be initialized with a HardwareAddress before the update method gets called");
    }

  }

  /**
   * Refreshes the DataTag value.
   * If the DataTag belongs to the {@link GetScheduler} a new get request will be triggered.
   * If it belongs to the {@link PostScheduler} the last received value will be send to the server.
   *
   * @param id  The id of the DataTag which needs to be refreshed.
   */
  public void refreshDataTag(Long id) {

    RestScheduler scheduler = getScheduler(equipmentConfiguration.getSourceDataTag(id).getHardwareAddress());
    scheduler.refreshDataTag(id);

  }

  /**
   * HelperMethod to determine which Scheduler is used for the given HardwareAddress.
   * @param hardwareAddress The HardwareAddress with the information of the DataTag.
   * @return The Scheduler which belongs to the address.
   */
  private RestScheduler getScheduler(HardwareAddress hardwareAddress) {

    if(hardwareAddress == null){
      throw new IllegalArgumentException("First initialize the hardwareAddress of the DataTag");
    }

    if (hardwareAddress instanceof RestGetAddress) {
      return this.getScheduler;

    } else if (hardwareAddress instanceof RestPostAddress) {
      return this.postScheduler;

    } else {
      throw new IllegalArgumentException("The HardwareAddress::" + hardwareAddress+" is not supported by the RestDaq.");
    }
  }

}
