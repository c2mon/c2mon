package cern.c2mon.client.core.device.exception;

import cern.c2mon.client.core.device.Device;

/**
 * This class represents an exception thrown when the server returns a malformed {@link Device}.
 */
public class ImproperDeviceException extends Exception {

    /**
     * Creates a new ImproperDeviceException wrapping another exception with a detail message
     * @param message details on the exception
     */
    public ImproperDeviceException(String message) {
        super(message);
    }
    /**
     * Creates a new ImproperDeviceException wrapping another exception with a detail message
     * @param message details on the exception
     * @param exception the wrapped exception.
     */
    public ImproperDeviceException(String message, Exception exception) {
        super(message, exception);
    }
}
