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
    private static final Logger logger = Logger.getLogger(EndpointControllerDefault.class);
    
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
