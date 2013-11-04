package cern.c2mon.daq.opcua.connection.soap;

/**
 * Interface for a SoapLongPollExceptionHandler.
 * 
 * @author Andreas Lang
 *
 */
public interface ISoapLongPollExceptionHandler {
    
    /**
     * Called when an exception during polling happens.
     * 
     * @param t The exception which happened.
     * @param poll The poll object in which the exception happened.
     */
    void onConnectionException(final Throwable t, final SoapLongPoll poll);

}
