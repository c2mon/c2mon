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

import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.common.conf.equipment.IDataTagChanger;
import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.rest.controller.RestController;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataQuality;
import org.springframework.context.ApplicationContext;
import org.springframework.web.client.RestClientException;


/**
 * Entry point of the REST module. The connectToDataSource() method is called at the DAQ startup phase.
 * It initializes the Spring boot context, which triggers
 * the tag subscriptions for all GET REST requests.
 *
 * @author Franz Ritter
 */
public class RESTMessageHandler extends EquipmentMessageHandler {

  /** The equipment logger of this class */
  private EquipmentLogger equipmentLogger;

  /** The class which communicates with the RestServices */
  private RequestDelegator requestDelegator;

  /** Spring boot context */
  public ApplicationContext context ;


  @Override
  public void connectToDataSource() throws EqIOException {
    // class initialization
    this.context = ApplicationContextProvider.getApplicationContext();
    RestController restController = context.getBean(RestController.class);

    this.equipmentLogger = getEquipmentLogger(RESTMessageHandler.class);
    equipmentLogger.trace("enter connectToDataSource()");

    requestDelegator = new RequestDelegator(getEquipmentMessageSender(), getEquipmentConfiguration(), equipmentLogger, restController);

    IDataTagChanger dataTagChanger = new RESTDataTagChanger(getEquipmentMessageSender(), equipmentLogger, requestDelegator);
    getEquipmentConfigurationHandler().setDataTagChanger(dataTagChanger);


    // Adding DataTags to the equipment
    for (ISourceDataTag dataTag : getEquipmentConfiguration().getSourceDataTags().values()) {
      try {
        requestDelegator.addDataTag(dataTag);

      } catch (IllegalArgumentException ex) {

        equipmentLogger.warn("DataTag " + dataTag.getId() + " not configurable - Reason: " + ex.getMessage());
        getEquipmentMessageSender().sendInvalidTag(dataTag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS, "DataTag " + dataTag.getId() + " not configurable - Reason: " + ex.getMessage());

      }
    }

    getEquipmentMessageSender().confirmEquipmentStateOK("setConnected - Accessed all web services");
    equipmentLogger.info("connectToDataSource succeed.");
  }

  @Override
  public void disconnectFromDataSource() throws EqIOException {
    equipmentLogger.trace("Entering disconnectFromDataSource method.");

    for (ISourceDataTag dataTag : getEquipmentConfiguration().getSourceDataTags().values()) {
      try {

        requestDelegator.removeDataTag(dataTag);

      } catch (IllegalArgumentException ex) {

        equipmentLogger.warn("Problem caused by disconnecting: "+ ex.getMessage());
      }
    }

    equipmentLogger.info("Equipment disconnected.");
    equipmentLogger.trace("Leaving disconnectFromDataSource method.");
  }

  @Override
  public void refreshAllDataTags() {
    equipmentLogger.trace("Entering refreshAllDataTags method.");

    for (ISourceDataTag dataTag : getEquipmentConfiguration().getSourceDataTags().values()) {
      try {

        requestDelegator.refreshDataTag(dataTag.getId());

      } catch (IllegalArgumentException ex) {

        equipmentLogger.warn("Problem causes by refreshing. Reason: " + ex.getMessage());
        getEquipmentMessageSender().sendInvalidTag(dataTag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS, "Problem causes by refreshing. Reason: " + ex.getMessage());

      } catch (RestClientException ex ){

        equipmentLogger.warn("Connection problem causes by refreshing:: " + ex.getMessage());
        getEquipmentMessageSender().sendInvalidTag(dataTag, SourceDataQuality.DATA_UNAVAILABLE, "Connection problem causes by refreshing:: " + ex.getMessage());
      }
    }

    equipmentLogger.trace("Leaving refreshAllDataTags method.");
  }

  @Override
  public void refreshDataTag(long dataTagId) {
    equipmentLogger.trace("Entering refreshDataTag method.");
    try {

      requestDelegator.refreshDataTag(dataTagId);

    } catch (IllegalArgumentException ex) {

      equipmentLogger.warn("Problem causes by refreshing. Reason: " + ex.getMessage());
      getEquipmentMessageSender().sendInvalidTag(getEquipmentConfiguration().getSourceDataTag(dataTagId), SourceDataQuality.INCORRECT_NATIVE_ADDRESS, "Problem causes by refreshing. Reason: " + ex.getMessage());

    } catch (RestClientException ex ){

      equipmentLogger.warn("Connection problem causes by refreshing:: " + ex.getMessage());
      getEquipmentMessageSender().sendInvalidTag(getEquipmentConfiguration().getSourceDataTag(dataTagId), SourceDataQuality.DATA_UNAVAILABLE, "Connection problem causes by refreshing:: " + ex.getMessage());
    }

    equipmentLogger.trace("Leaving refreshDataTag method.");
  }

}
