package cern.c2mon.daq.db;

/**
 * This class represents a timeout exception that is thrown if the process waiting for
 * a database alert times out.
 * 
 *  @author Aleksandra Wardzinska
 * */
public class AlertTimeOutException extends Exception {
    
    /**
     */
    private static final long serialVersionUID = -8983041834254433761L;

    /**
     * A default constructor 
     */
    public AlertTimeOutException() {
        super("Connection timed out while waiting for alert.");
    }
    
}

