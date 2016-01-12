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

import cern.c2mon.daq.opcua.EndpointTypesUnknownException;
import cern.c2mon.daq.opcua.connection.common.IOPCEndpoint;
import cern.c2mon.daq.opcua.connection.common.IOPCEndpointFactory;
import cern.c2mon.daq.opcua.connection.dcom.DADCOMEndpoint;
import cern.c2mon.daq.opcua.connection.dcom.DADCOMItemDefintion;
import cern.c2mon.daq.opcua.connection.dcom.DADCOMItemDefintionFactory;
import cern.c2mon.daq.opcua.connection.soap.DASoapEndpoint;
import cern.c2mon.daq.opcua.connection.soap.DASoapItemDefintion;
import cern.c2mon.daq.opcua.connection.soap.DASoapItemDefintionFactory;
import cern.c2mon.daq.opcua.connection.ua.UAEndpoint;
import cern.c2mon.daq.opcua.connection.ua.UAItemDefintion;
import cern.c2mon.daq.opcua.connection.ua.UaItemDefintionFactory;

/**
 * The default factory to create OPCEndpoints.
 * 
 * @author Andreas Lang
 *
 */
public class DefaultOPCEndpointFactory implements IOPCEndpointFactory {
    
    /**
     * The OPC UA TCP connection type.
     */
    public static final String UA_TCP_TYPE = "opc.tcp";
    
    /**
     * The classic OPC DA via DCOM connection type.
     */
    public static final String DA_DCOM_TYPE = "dcom";
    
    /**
     * The classic OPC DA via SOAP connection type.
     */
    public static final String DA_SOAP_TYPE = "http";

    /**
     * Creates a new endpoint based on the provided type String or throws
     * and {@link EndpointTypesUnknownException} if the type does not exist.
     * 
     * @param type The name of the type.
     * @return The matching IOPCEndpoint.
     */
    @Override
    public IOPCEndpoint createEndpoint(final String type) {
        IOPCEndpoint endpoint;
        if (UA_TCP_TYPE.equals(type)) {
            endpoint = new UAEndpoint(
                    new UaItemDefintionFactory(),
                    new DefaultGroupProvider<UAItemDefintion>());
        } else if (DA_SOAP_TYPE.equals(type)) {
            endpoint = new DASoapEndpoint(
                    new DASoapItemDefintionFactory(),
                    new DefaultGroupProvider<DASoapItemDefintion>());
        } else if (DA_DCOM_TYPE.equals(type)) {
            endpoint = new DADCOMEndpoint(
                    new DADCOMItemDefintionFactory(),
                    new DefaultGroupProvider<DADCOMItemDefintion>());
        } else {
            throw new EndpointTypesUnknownException();
        }
        return endpoint;
    }

}
