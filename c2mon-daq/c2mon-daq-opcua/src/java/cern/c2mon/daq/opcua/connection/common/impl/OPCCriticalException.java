package cern.c2mon.daq.opcua.connection.common.impl;
/**
 * Exception which is not solvable without a configuration change.
 * 
 * @author Andreas Lang
 *
 */
public class OPCCriticalException extends RuntimeException {

    /**
     * Default serial version id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new OPCCriticalException.
     */
    public OPCCriticalException() {
        super();
    }

    /**
     * Creates a new OPCCriticalException.
     * 
     * @param message The message of the exception.
     */
    public OPCCriticalException(final String message) {
        super(message);
    }

    /**
     * Creates a new OPCCriticalException.
     * 
     * @param cause Throwable which is the cause of this exception.
     */
    public OPCCriticalException(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new OPCCriticalException.
     * 
     * @param message The message of the exception.
     * @param cause Throwable which is the cause of this exception.
     */
    public OPCCriticalException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
