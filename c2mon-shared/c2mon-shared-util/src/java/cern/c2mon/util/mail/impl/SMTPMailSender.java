package cern.c2mon.util.mail.impl;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import cern.c2mon.util.mail.MailSender;
import cern.c2mon.util.mail.bean.MailDetailsBean;
import cern.c2mon.util.mail.exception.MailSenderException;

/**
 * MailSender that only sends plain text emails using the JAVAMAIL API
 * 
 * @author mruizgar
 * 
 */

public class SMTPMailSender implements MailSender {
    /* STMPMailSender logger */
    private static final Logger LOG = Logger.getLogger(SMTPMailSender.class);

    /* It holds the session that has been created within the mail server */
    private Session session;

    /* It contains all the email contents */
    private MimeMessage message;

    /**
     * It sends the email using the information provided by the MailDetailsBean
     * object
     */
    public void sendMail(MailDetailsBean mDetails) throws MailSenderException {
        createConnection(mDetails.getServer());
        createMessage(mDetails);
        setTransportForm();
    }

    /**
     * It creates a connection to the specified mail server
     * 
     * @param server
     *            The mail server host
     */
    private void createConnection(String server) {
        // Get system properties
        Properties props = System.getProperties();
        // Setup the smtp server
        props.put("mail.smtp.host", server);
        // Get session
        LOG.debug("createConnection() - The connection with the mail server has been created");
        this.session = Session.getInstance(props, null);
    }

    /**
     * Creates the message that would be sent within the email with the
     * information specified by the parameter
     * 
     * @param mDetails
     *            Contains all the information for creating the email message
     * @throws MailSenderException
     *             An exception is sent if something wrong happens while
     *             composing the email
     */
    private void createMessage(MailDetailsBean mDetails) throws MailSenderException {
        // Define message
        message = new MimeMessage(session);

        try {
            // Set the sender of the email
            message.setFrom(new InternetAddress(mDetails.getSender()));
            // Set the subject of the email
            message.setSubject(mDetails.getSubject());
            // Set the text message of the email
            message.setText(mDetails.getMessage());
            // Set the TO recipients of the email
            for (int i = 0; i < mDetails.getToRecipients().size(); i++) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(
                        (String) mDetails.getToRecipients().get(i)));
            }
            // Set the CC email recipients
            for (int i = 0; i < mDetails.getCcRecipients().size(); i++) {
                message.addRecipient(Message.RecipientType.CC, new InternetAddress(
                        (String) mDetails.getCcRecipients().get(i)));
            }
            // Set the BCC email recipients
            for (int i = 0; i < mDetails.getBccRecipients().size(); i++) {
                message.addRecipient(Message.RecipientType.BCC, new InternetAddress(
                        (String) mDetails.getBccRecipients().get(i)));
            }
           LOG.debug("createMessage() - The email "+mDetails.getSubject()+" has being successfully composed ");
        } catch (AddressException e) {
            throw new MailSenderException(
                    "One of the emails adrresses was not correctly formatted ");
        } catch (MessagingException e) {
            throw new MailSenderException(
                    "An error ocurred while preparing the message to be sent ");
        }
    }

    /**
     * Send the transport protocol and send the email
     * 
     * @throws MailSenderException
     *             An exception is thrown if something goes wrong while sending
     *             the email
     */
    private void setTransportForm() throws MailSenderException {
        try {
            Transport.send(message);
            LOG.debug("setTransportForm() - The email has been sent");
        } catch (MessagingException e) {
            throw new MailSenderException(
                    "An error ocurred while trying to send the email");
        }
    }
}
