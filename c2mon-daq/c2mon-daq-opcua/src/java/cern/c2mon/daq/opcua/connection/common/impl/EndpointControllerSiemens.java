/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005 - 2014 CERN This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/

package cern.c2mon.daq.opcua.connection.common.impl;

import java.util.List;

import org.apache.log4j.Logger;

import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.daq.common.conf.equipment.IEquipmentConfiguration;
import cern.c2mon.daq.common.logger.EquipmentLoggerFactory;
import cern.c2mon.daq.opcua.EndpointEquipmentLogListener;
import cern.c2mon.daq.opcua.connection.common.AbstractEndpointController;
import cern.c2mon.daq.opcua.connection.common.IOPCEndpointFactory;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.common.datatag.address.impl.OPCHardwareAddressImpl;
import cern.c2mon.shared.daq.datatag.ISourceDataTag;
import cern.c2mon.shared.daq.datatag.SourceDataTag;

/**
 * EndpointControllerSiemens class
 * 
 * @author vilches
 */
public class EndpointControllerSiemens extends AbstractEndpointController {
    
    /**
     * Private class logger.
     */
    private static final Logger logger = Logger.getLogger(EndpointControllerSiemens.class);
    
    /**
     * fake Data Tag ID use for reading the Redundant Server State 
     */
    public static final Long REDUNDANT_SERVER_STATE_TAG_ID = 0L;
    
    /**
     * Redundant Server State types enum
     */
    public enum ServerStateType {
      INIT("Initialization", 0), 
      MASTER("Master Server", 1), 
      STANDBY("Standby Server", 2), 
      ERROR_REDUNDANCE("Redundancy error", 3),
      NO_REDUNDANCE("No redundancy", 4);
      
      // Server State status description
      private String description;
      
      // Server State status value number
      private int value;
      
      /**
       * The Server State Type
       * 
       * @param description The description of the status
       * @param value The status value number
       */
      ServerStateType(final String description, final int value) {
        this.description = description;
        this.value = value;
      }
      
      /**
       * @return The description of the status
       */
      public final String getDescription() {
        return this.description;
      }
      
      /**
       * @return The status value number
       */
      public final int getValue() {
        return this.value;
      }
    }     
    
    /**
     * Creates a new SiemensEndpointController
     * 
     * @param endPointFactory
     *            The endpoint factory to create OPC endpoints.
     * @param sender
     *            The equipment message sender to send updates to.
     * @param factory
     *            Factory to crate equipmen bound loggers.
     * @param opcAddresses
     *            The Siemens addresses for the endpoints.
     * @param equipmentConfiguration
     *            The equipment configuration for this controller.
     */
    public EndpointControllerSiemens(final IOPCEndpointFactory endPointFactory, final IEquipmentMessageSender sender,
            final EquipmentLoggerFactory factory, final List<OPCUASiemensAddress> opcAddresses,
            final IEquipmentConfiguration equipmentConfiguration) {
        this.opcEndpointFactory = endPointFactory;
        this.sender = sender;
        this.factory = factory;
        //this.logger = factory.getEquipmentLogger(getClass());
        logListener = new EndpointEquipmentLogListener(factory);
        this.opcAddresses = opcAddresses;
        this.equipmentConfiguration = equipmentConfiguration;
    }
    
   
    @Override
    public synchronized void startEndpoint() {        
        try {
            createEndpoint();
            this.endpoint.registerEndpointListener(this.logListener);
            this.endpoint.registerEndpointListener(this);
            
            this.endpoint.addDataTags(this.equipmentConfiguration.getSourceDataTags().values());
            this.endpoint.addDataTag(createDataTagForRedServerState());
            
            this.endpoint.addCommandTags(this.equipmentConfiguration.getSourceCommandTags().values());
            this.sender.confirmEquipmentStateOK("Connected to " + currentAddress.getUri().getHost());
            startAliveTimer();
            setUpStatusChecker();
            this.endpoint.setStateOperational();
        } catch (OPCCommunicationException e) {
            logger.error("Siemens Endpoint creation failed. Controller will try again. ", e);
            triggerEndpointRestart("Problems connecting to " + currentAddress.getUri().getHost() + ": " + e.getMessage());
        }
    }
    
    /**
     * Create Data Tag on the fly to read the redundant Server State name
     */
    private SourceDataTag createDataTagForRedServerState() {
        // Data Tag to read redundant server state name
        SourceDataTag sourceDataTag = new SourceDataTag(REDUNDANT_SERVER_STATE_TAG_ID, "REDUNDANT_SERVER_STATE", false);
        
        // DataTag Address
        DataTagAddress dataTagAddress = new DataTagAddress();
        HardwareAddress hwAddress = new OPCHardwareAddressImpl(((OPCUASiemensAddress)getCurrentOPCAddress()).redundantServerStateName());
        dataTagAddress.setHardwareAddress(hwAddress);
        sourceDataTag.setAddress(dataTagAddress);
        // Data Type
        sourceDataTag.setDataType("Integer");
        // Mode
        sourceDataTag.setMode((short) 2);
        // Min/Max values
        sourceDataTag.setMinValue(0);
        sourceDataTag.setMaxValue(4);    
        
        return sourceDataTag;
    }
    
    @Override
    public void onNewTagValue(final ISourceDataTag dataTag, final long timestamp, final Object tagValue) {
        // If the Data Tag ID belongs to the redundant server state
        if(dataTag.getId() == REDUNDANT_SERVER_STATE_TAG_ID) {      
            int value = Integer.valueOf(tagValue.toString());
            
            // If the Server went to Stand by or error restart connections
            if (value == ServerStateType.STANDBY.getValue()) {
                logger.debug("onNewTagValue - New Redundant Server State Tag value received: " + ServerStateType.STANDBY.getDescription());
                triggerEndpointRestart("New Redundant Server State value received: " + ServerStateType.STANDBY.getDescription());
            } else  if (value == ServerStateType.ERROR_REDUNDANCE.getValue()) {
                logger.debug("onNewTagValue - New Redundant Server State Tag value received: " + ServerStateType.ERROR_REDUNDANCE.getDescription());
                triggerEndpointRestart("New Redundant Server State value received: " + ServerStateType.ERROR_REDUNDANCE.getDescription());
            } else  if (value == ServerStateType.NO_REDUNDANCE.getValue()) {
                logger.debug("onNewTagValue - New Redundant Server State Tag value received: " + ServerStateType.NO_REDUNDANCE.getDescription());
                triggerEndpointRestart("New Redundant Server State value received: " + ServerStateType.NO_REDUNDANCE.getDescription());
            } else  if (value == ServerStateType.INIT.getValue()) {
                // Initialization Received so do nothing
                logger.debug("onNewTagValue - New Redundant Server State Tag value received: " + ServerStateType.INIT.getDescription());
            } else {
                // Master Received so do nothing
                logger.debug("onNewTagValue - New Redundant Server State Tag value received: " + ServerStateType.MASTER.getDescription());
            }
            
        } else {
            // Normal DataTag
            super.onNewTagValue(dataTag, timestamp, tagValue);
        }
    }
}
