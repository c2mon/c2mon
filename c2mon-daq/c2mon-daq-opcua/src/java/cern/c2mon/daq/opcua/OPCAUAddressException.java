package cern.c2mon.daq.opcua;

import java.net.URISyntaxException;

import cern.c2mon.daq.opcua.connection.common.impl.OPCCriticalException;

/**
 * Thrown if the OPC address could not be created.
 * 
 * @author Andreas Lang
 *
 */
public class OPCAUAddressException extends OPCCriticalException {

    /**
     * Default serial version id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new {@link OPCAUAddressException}.
     * 
     * @param message The message for the exception.
     * @param e The exception which was thrown.
     */
    public OPCAUAddressException(
            final String message, final URISyntaxException e) {
        super(message, e);
    }

}
