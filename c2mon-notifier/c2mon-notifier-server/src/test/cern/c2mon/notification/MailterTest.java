/*
 * $Id $
 *
 * $Date$
 * $Revision$
 * $Author$
 *
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.notification;

import java.io.IOException;

import javax.mail.MessagingException;

import org.junit.Test;

import cern.c2mon.notification.impl.MailerImpl;

public class MailterTest {

   @Test
   public void test() throws IllegalArgumentException, MessagingException, IOException {

       //MailerImpl mailer = MailerImpl.fromPropertiesFile("mailer.properties");
       //mailer.sendEmail("felix.ehm@cern.ch", "test", "test-body");
   }
   
}
