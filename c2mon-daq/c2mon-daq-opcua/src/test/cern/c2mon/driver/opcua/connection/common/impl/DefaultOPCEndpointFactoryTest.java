package cern.c2mon.driver.opcua.connection.common.impl;

import org.junit.Test;

import cern.c2mon.driver.opcua.EndpointTypesUnknownException;
import cern.c2mon.driver.opcua.connection.common.IOPCEndpoint;
import cern.c2mon.driver.opcua.connection.common.impl.DefaultOPCEndpointFactory;
import cern.c2mon.driver.opcua.connection.dcom.DADCOMEndpoint;
import cern.c2mon.driver.opcua.connection.soap.DASoapEndpoint;
import cern.c2mon.driver.opcua.connection.ua.UAEndpoint;
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
