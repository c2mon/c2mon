/*
 * 
 * Copyright CERN 2013, All Rights Reserved.
 */
package cern.c2mon.notification;

/**
 * Interface which describes a facility to send emails.
 * 
 * @author felixehm
 */
public interface Mailer {

    /**
     * Sends an email to a given address.
     * 
     * @param to the email address
     * @param subject the subject
     * @param content the body
     * @throws Exception in case of an error
     */
    void sendEmail(String to, String subject, String content) throws Exception;

}
