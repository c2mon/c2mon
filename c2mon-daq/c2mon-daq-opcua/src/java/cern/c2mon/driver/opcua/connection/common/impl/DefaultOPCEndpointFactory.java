package cern.c2mon.driver.opcua.connection.common.impl;

import cern.c2mon.driver.opcua.EndpointTypesUnknownException;
import cern.c2mon.driver.opcua.connection.common.IOPCEndpoint;
import cern.c2mon.driver.opcua.connection.common.IOPCEndpointFactory;
import cern.c2mon.driver.opcua.connection.dcom.DADCOMEndpoint;
import cern.c2mon.driver.opcua.connection.dcom.DADCOMItemDefintion;
import cern.c2mon.driver.opcua.connection.dcom.DADCOMItemDefintionFactory;
import cern.c2mon.driver.opcua.connection.soap.DASoapEndpoint;
import cern.c2mon.driver.opcua.connection.soap.DASoapItemDefintion;
import cern.c2mon.driver.opcua.connection.soap.DASoapItemDefintionFactory;
import cern.c2mon.driver.opcua.connection.ua.UAEndpoint;
import cern.c2mon.driver.opcua.connection.ua.UAItemDefintion;
import cern.c2mon.driver.opcua.connection.ua.UaItemDefintionFactory;

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
