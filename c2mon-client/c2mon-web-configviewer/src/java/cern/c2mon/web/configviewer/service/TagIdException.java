package cern.c2mon.web.configviewer.service;

/**
 * Exception representing invalid tag id or tag not found.
 * */
public class TagIdException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructs the exception with a given message
     * @param message exception message
     */
    public TagIdException(final String message) {
        super(message);
    }
}
