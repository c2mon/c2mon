package cern.c2mon.daq.opcua.connection.common;

import cern.tim.shared.daq.datatag.ISourceDataTag;

/**
 * Generic listener for an OPCEndpoint.
 * 
 * @author Andreas Lang
 *
 */
public interface IOPCEndpointListener {
    
    /**
     * Called when a value of a tag changes.
     * 
     * @param dataTag The tag whose value changed.
     * @param timestamp The timestamp when the value was updated.
     * @param value The new value.
     */
    void onNewTagValue(final ISourceDataTag dataTag, long timestamp,
            final Object value);
    
    /**
     * Called in case an invalid tag causes an exception.
     * 
     * @param dataTag The tag which caused the exception.
     * @param cause The cause of the exception.
     */
    void onTagInvalidException(
            final ISourceDataTag dataTag, final Throwable cause);
    
    /**
     * Called in case a subscription fails.
     * 
     * @param cause The cause of the subscription failure.
     */
    void onSubscriptionException(final Throwable cause);
    
}
