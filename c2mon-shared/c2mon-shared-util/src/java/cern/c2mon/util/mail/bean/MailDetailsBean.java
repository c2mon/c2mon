package cern.c2mon.util.mail.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * JavaBean that encapsulates all the details that are needed to be able to send
 * a plain text email using JAVAMAIL
 * 
 * @author mruizgar
 * 
 */

public class MailDetailsBean {

    /* The email address of the person/list that sends the email */
    String sender;

    /*
     * The emails of those person that should receive the email and act
     * accordingly
     */
    List toRecipients = new ArrayList();

    /*
     * The emails of those persons that should receive the email just for
     * information (carbon copy or courtesy copy)
     */
    List ccRecipients = new ArrayList();

    /*
     * The emails of those persons that should receive the email, but as a blind
     * carbon copy
     */
    List bccRecipients = new ArrayList();

    /* The text that should appear in the email as the Subject */
    String subject;

    /* The text that should go inside the email */
    String message;

    /* The connection string to the server used for sending the emails */
    String server;

    /**
     * Default constructor
     */
    public MailDetailsBean() {

    }

    /**
     * MailDetailsBean constructor, it receives the essential information to be
     * able to compose and send the email
     * 
     * @param server
     *            The connection string to the email server
     * @param sender
     *            The person that sends the email
     * @param recipients
     *            The person to which the email has to be sent
     * @param subject
     *            The subject of the email
     * @param message
     *            The text content of the email
     */
    public MailDetailsBean(String server, String sender, List recipients,
            String subject, String message) {
        this.sender = sender;
        toRecipients = recipients;
        this.subject = subject;
        this.message = message;
        this.server = server;
    }

    /**
     * @return the toRecipients
     */
    public List getToRecipients() {
        return toRecipients;
    }

    /**
     * @param toRecipients
     *            the toRecipients to set
     */
    public void setToRecipients(ArrayList toRecipients) {
        this.toRecipients = toRecipients;
    }

    /**
     * @return the ccRecipients
     */
    public List getCcRecipients() {
        return ccRecipients;
    }

    /**
     * @param ccRecipients
     *            the ccRecipients to set
     */
    public void setCcRecipients(ArrayList ccRecipients) {
        this.ccRecipients = ccRecipients;
    }

    /**
     * @return the bccRecipients
     */
    public List getBccRecipients() {
        return bccRecipients;
    }

    /**
     * @param bccRecipients
     *            the bccRecipients to set
     */
    public void setBccRecipients(ArrayList bccRecipients) {
        this.bccRecipients = bccRecipients;
    }

    /**
     * @return the sender
     */
    public String getSender() {
        return sender;
    }

    /**
     * @param sender
     *            the sender to set
     */
    public void setSender(String sender) {
        this.sender = sender;
    }

    /**
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @param subject
     *            the subject to set
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message
     *            the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @param toRecipients
     *            the toRecipients to set
     */
    public void setToRecipients(List toRecipients) {
        this.toRecipients = toRecipients;
    }

    /**
     * @param ccRecipients
     *            the ccRecipients to set
     */
    public void setCcRecipients(List ccRecipients) {
        this.ccRecipients = ccRecipients;
    }

    /**
     * @param bccRecipients
     *            the bccRecipients to set
     */
    public void setBccRecipients(List bccRecipients) {
        this.bccRecipients = bccRecipients;
    }

    /**
     * @return the server
     */
    public String getServer() {
        return server;
    }

    /**
     * @param server
     *            the server to set
     */
    public void setServer(String server) {
        this.server = server;
    }

}
