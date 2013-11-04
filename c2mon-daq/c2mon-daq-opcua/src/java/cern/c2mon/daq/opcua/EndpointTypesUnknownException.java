package cern.c2mon.daq.opcua;

import cern.c2mon.daq.opcua.connection.common.impl.OPCCriticalException;

/**
 * Exception which is thrown if there are no working endpoint types provided.
 * 
 * @author Andreas Lang
 *
 */
public class EndpointTypesUnknownException extends OPCCriticalException {
    
    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new EndpointTypeUnknownException.
     */
    public EndpointTypesUnknownException() {
        super("There are no implementations for the selected endpoint types.");
    }

}
