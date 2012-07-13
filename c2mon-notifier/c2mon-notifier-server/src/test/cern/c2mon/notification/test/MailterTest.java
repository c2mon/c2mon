/*
 * $Id $
 *
 * $Date$
 * $Revision$
 * $Author$
 *
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.notification.test;

import javax.mail.MessagingException;

import org.junit.Test;

import cern.c2mon.notification.impl.MailerImpl;

public class MailterTest {

   @Test
   public void test() throws IllegalArgumentException, MessagingException {
       MailerImpl mailer = new MailerImpl("diamon-support@cern.ch", "diamonop", "D1agm0nitor", "cernmx.cern.ch");
       
       mailer.sendEmail("felix.ehm@cern.ch", "test", "test-body");
   }
   
}
