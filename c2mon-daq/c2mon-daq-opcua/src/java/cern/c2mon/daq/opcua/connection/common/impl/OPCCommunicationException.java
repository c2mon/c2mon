package cern.c2mon.daq.opcua.connection.common.impl;

/**
 * Exception while communicating with the OPC server. This exception can be due
 * to a temporary reason (network down). It might work to retry after this
 * exception.
 * 
 * @author Andreas Lang
 *
 */
public class OPCCommunicationException extends RuntimeException {

    /**
     * Default serial version id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new OPCCommunicationException.
     */
    public OPCCommunicationException() {
        super();
    }

    /**
     * Creates a new OPCCommunicationException.
     * 
     * @param message Message with additional information.
     */
    public OPCCommunicationException(final String message) {
        super(message);
    }

    /**
     * Creates a new OPCCommunicationException.
     * 
     * @param cause Throwable which caused the exception.
     */
    public OPCCommunicationException(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new OPCCommunicationException.
     * 
     * @param message Message with additional information.
     * @param cause Throwable which caused the exception.
     */
    public OPCCommunicationException(final String message,
            final Throwable cause) {
        super(message, cause);
    }

}
