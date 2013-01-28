package cern.c2mon.notification.impl;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import cern.c2mon.notification.Mailer;

/**
 * An implementation of the {@link Mailer} interface. You can create a own one or use the
 * {@link #fromPropertiesFile(String)} to have a {@link MailerImpl} created from a properties file. In order to use
 * latter, please check {@link #fromPropertiesFile(String)}.
 * 
 * @author felixehm
 * @see #fromPropertiesFile(String)
 */
public class MailerImpl implements Mailer {

    /**
     * our logger
     */
    private Logger logger = Logger.getLogger(MailerImpl.class);

    /**
     * For speed reasons we initialize it only once.
     */
    private InternetAddress me = new InternetAddress();

    private Session session = null;

    /**
     * Creates a MailerImpl from a properties file: 
     * <ul>
     * <li>mailer.from</li>
     * <li>mailer.name</li>
     * <li>mailer.password</li>
     * <li>mailer.server</li>
     * <li>mailer.port</li>
     * </ul>
     * 
     * @param fileName the properties-conform file 
     * @return a new MailerImpl object
     * @throws Exception in case of an error
     */
    public static MailerImpl fromPropertiesFile(String fileName) throws Exception {
        Properties properties = new Properties();
        BufferedInputStream stream = new BufferedInputStream(new FileInputStream(fileName));
        properties.load(stream);
        stream.close();

        return new MailerImpl(properties.getProperty("mailer.from"), properties.getProperty("mailer.name"), properties
                .getProperty("mailer.password"), properties.getProperty("mailer.server"), Integer.parseInt(properties
                .getProperty("mailer.port")));

    }

    /**
     * @param from the sender email address
     * @param name the username for authorizing at the mail server.
     * @param password the password for authorizing at the mail server.
     * @param server the mailserver hostname
     * @param port the mailserver port
     * @throws AddressException in case this {@link MailerImpl} instance cannot create an {@link InetAddress} object
     *             (required).
     */
    public MailerImpl(String from, final String name, final String password, String server, int port)
            throws AddressException {
        Properties props = System.getProperties();

        props.put("mail.smtp.host", server);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.from", from);
        // props.put("mail.smtp.auth", "true");

        props.put("mail.transport.protocol", "smtp");

        // props.put("mail.smtp.starttls.enable", "true");

        // props.put("mail.debug", "true");

        me = new InternetAddress(from);

        // get the session / connection to the mailserver
        session = Session.getInstance(props, new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(name, password);
            }
        });

        logger.info("Started Mailer. FROM=" + from + ", SERVER=" + server + ":" + port);
    }

    /**
     * Constructor. Provides access to a remote mail server on port 25.
     * 
     * @param from the sender email address
     * @param name the username for authorizing at the mail server.
     * @param password the password for authorizing at the mail server.
     * @param server the mailserver hostname
     * @throws AddressException in case this {@link MailerImpl} instance cannot create an {@link InetAddress} object
     *             (required).
     */
    public MailerImpl(final String from, final String name, final String password, final String server)
            throws AddressException {
        this(from, name, password, server, 25);
    }

    /**
     * Threadsafe call to send a mail.
     * 
     * @param to the user mail address. Cannot be null.
     * @param subject the subject of the mail. If null it will be set to an empty string.
     * @param mailText the mail text.
     * @throws MessagingException in case the message cannot be sent
     * @throws IllegalArgumentException in case the passed user mail is null.
     */
    public synchronized void sendEmail(String to, String subject, String mailText) throws MessagingException,
            IllegalArgumentException {
        if (to == null) {
            throw new IllegalArgumentException("Passed argument for recipient is null.");
        }

        if (subject == null) {
            subject = "";
        }

        if (mailText == null) {
            mailText = "";
        }

        MimeMessage simpleMessage = new MimeMessage(session);

        // MimeMultipart content = new MimeMultipart();
        // MimeBodyPart html = new MimeBodyPart();
        // html.setContent(mailText, "text/html");
        // content.addBodyPart(html);

        simpleMessage.setContent(mailText, "text/html");
        //
        simpleMessage.setFrom(me);
        simpleMessage.setRecipient(RecipientType.TO, new InternetAddress(to));
        simpleMessage.setSubject(subject);
        // simpleMessage.setText(mailText);

        Transport.send(simpleMessage);
    }
}
