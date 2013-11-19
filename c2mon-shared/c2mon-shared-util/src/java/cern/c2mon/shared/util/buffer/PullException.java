package cern.c2mon.shared.util.buffer;

/**
 * Exception in calling the callback function to pull data from
 * the SynchroBuffer.
 * @author F.Calderini
 */
public class PullException extends Exception {

    /**
     * Creates new <code>PullException</code> without detail message.
     */
    public PullException() {
        super();
    }


    /**
     * Constructs an <code>PullException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public PullException(String msg) {
        super(msg);
    }
}
