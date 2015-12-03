/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.mobicall;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Internal mailer to signal problems with the process to the support team. Messages go by default
 * to the author of this app. In production, the process should be started with 
 * -Ddiamon.support=diamon-support@cern.ch
 * 
 * @author mbuttner
 */
public class DiamonSupport {
    
    private static final Logger LOG = LoggerFactory.getLogger(DiamonSupport.class);
    private static DiamonSupport support;

    private Properties props;
    private String dest;
    
    //
    // --- CONSTRUCTION -------------------------------------------------------------------------------
    //
    private DiamonSupport() {
        props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.from", "diamon-support@cern.ch");
        props.setProperty("mail.smtp.starttls.enable", Boolean.toString(true));        
        
        dest = System.getProperty("diamon.support", "mark.buttner@cern.ch");
    }
    
    public static DiamonSupport getSupport() {
        if (support == null) {
            support = new DiamonSupport();
        }
        return support;
    }
    
    //
    // --- PUBLIC METHODS ----------------------------------------------------------------------------
    //
    public void notifyTeam(String title, String message) {

        Session session = Session.getDefaultInstance(props, null);
        Message msg = new MimeMessage(session);
        try {
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(dest));
            msg.setSubject(title);
            msg.setText(message);
            Transport.send(msg);
            LOG.warn("Import problem [{}] notified to {} ", title, dest);
        } catch (Exception e) {
            LOG.error("Failed to send message to DIAMON support!", e);
            e.printStackTrace();
            
        }
    }
    
    //
    // --- MAIN for test only -----------------------------------------------------------------------
    //
    public static void main(String[] args) {
        DiamonSupport.getSupport().notifyTeam("Test mail", "Body of test mail");
    }
}
