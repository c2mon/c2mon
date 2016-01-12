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

import org.junit.Test;

import cern.c2mon.daq.opcua.EndpointTypesUnknownException;
import cern.c2mon.daq.opcua.connection.common.IOPCEndpoint;
import cern.c2mon.daq.opcua.connection.common.impl.DefaultOPCEndpointFactory;
import cern.c2mon.daq.opcua.connection.dcom.DADCOMEndpoint;
import cern.c2mon.daq.opcua.connection.soap.DASoapEndpoint;
import cern.c2mon.daq.opcua.connection.ua.UAEndpoint;
import static org.junit.Assert.*;

public class DefaultOPCEndpointFactoryTest {
    
    private DefaultOPCEndpointFactory factory = new DefaultOPCEndpointFactory();
    
    @Test
    public void testUAEndpoint() {
        IOPCEndpoint endpoint = 
            factory.createEndpoint(DefaultOPCEndpointFactory.UA_TCP_TYPE);
        assertEquals(endpoint.getClass(), UAEndpoint.class);
    }
    
    @Test
    public void testSOAPEndpoint() {
        IOPCEndpoint endpoint = 
            factory.createEndpoint(DefaultOPCEndpointFactory.DA_SOAP_TYPE);
        assertEquals(endpoint.getClass(), DASoapEndpoint.class);
    }
    
    @Test
    public void testDCOMEndpoint() {
        IOPCEndpoint endpoint = 
            factory.createEndpoint(DefaultOPCEndpointFactory.DA_DCOM_TYPE);
        assertEquals(endpoint.getClass(), DADCOMEndpoint.class);
    }
    
    @Test(expected=EndpointTypesUnknownException.class)
    public void testNULLEndpoint() {
        factory.createEndpoint(null);
    }

}
