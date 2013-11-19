package cern.c2mon.shared.util.mail;

import cern.c2mon.shared.util.mail.bean.MailDetailsBean;
import cern.c2mon.shared.util.mail.exception.MailSenderException;

/**
 * Interface that should be implemented for the different classes that would
 * want to act as a mail sender
 * 
 * @author mruizgar
 * 
 */

public interface MailSender {

    /**
     * It sends an email using all the information received as a parameter
     * 
     * @param mDetails
     *            Object containing all the information that is needed to send a
     *            proper email
     * @throws MailSenderException
     *             Exception that is thrown when something wrong occurs during
     *             the process of sending an email
     */
    void sendMail(MailDetailsBean mDetails) throws MailSenderException;

}
