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

package cern.c2mon.daq.opcua.connection.common.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.daq.common.logger.EquipmentLoggerFactory;
import cern.c2mon.daq.opcua.EndpointEquipmentLogListener;
import cern.c2mon.daq.opcua.connection.common.AbstractEndpointController;
import cern.c2mon.daq.opcua.connection.common.IOPCEndpointFactory;
import cern.c2mon.shared.common.process.IEquipmentConfiguration;

/**
 * EndpointControllerDefault class
 * 
 * @author vilches
 */
public class EndpointControllerDefault extends AbstractEndpointController {
    
    /**
     * Private class logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(EndpointControllerDefault.class);
    
    /**
     * Creates a new default EndpointController
     * 
     * @param endPointFactory
     *            The endpoint factory to create OPC endpoints.
     * @param sender
     *            The equipment message sender to send updates to.
     * @param factory
     *            Factory to crate equipmen bound loggers.
     * @param OPCUADefaultAddress
     *            The default addresses for the endpoints.
     * @param equipmentConfiguration
     *            The equipment configuration for this controller.
     */
    public EndpointControllerDefault(final IOPCEndpointFactory endPointFactory, final IEquipmentMessageSender sender,
            final EquipmentLoggerFactory factory, final List<OPCUADefaultAddress> opcAddresses,
            final IEquipmentConfiguration equipmentConfiguration) {
        this.opcEndpointFactory = endPointFactory;
        this.sender = sender;
        this.factory = factory;
        //this.logger = factory.getEquipmentLogger(getClass());
        logListener = new EndpointEquipmentLogListener(factory);
        this.opcAddresses = opcAddresses;
        this.equipmentConfiguration = equipmentConfiguration;
    }
}
