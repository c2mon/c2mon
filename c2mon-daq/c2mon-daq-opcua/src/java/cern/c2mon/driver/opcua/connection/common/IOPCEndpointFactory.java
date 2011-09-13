package cern.c2mon.driver.opcua.connection.common;

/**
 * The factory to create OPCEndpoints.
 * 
 * @author Andreas Lang
 *
 */
public interface IOPCEndpointFactory {

    /**
     * Creates a new OPCEndpoitn of the correct type. If the type specified
     * can not be found an {@link EndpointTypesUnknownException} is thrown.
     * 
     * @param type The type of te endpoint to create.
     * @return The new endpoint.
     */
    IOPCEndpoint createEndpoint(String type);

}
