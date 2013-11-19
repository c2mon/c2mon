package cern.c2mon.util.mail.exception;

/**
 * Represents any failure occurred during the process of sending an email  
 * @author mruizgar
 *
 */
public class MailSenderException extends Exception{

    /**
     * Unique string for identifying the class
     */
    private static final long serialVersionUID = 3033088998313194395L;

    
    /**
     * Constructor to create an Exception with an explanatory message inside
     * @param message The message that explains what have occurred
     */
    public MailSenderException(String message){
        super(message);
    }
    
    /**
     * Default constructor
     */    
    public MailSenderException(){
        super();
    }
}
