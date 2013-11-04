package cern.c2mon.daq.opcua.connection.soap;

/**
 * Listener to value changes in the polling mechanism.
 * 
 * @author Andreas Lang
 *
 */
public interface ISoapLongPollListener {
    
    /**
     * Called when a value changes.
     * 
     * @param clientHandle The client handle of the changed value.
     * @param timeStamp The timestamp when the object was changed.
     * @param value The changed value.
     */
    void valueChanged(String clientHandle, long timeStamp, Object value);

}
