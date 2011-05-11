/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2009 CERN This program is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.pmanager.mock;

import java.io.File;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import cern.c2mon.pmanager.IAlarmListener;
import cern.c2mon.pmanager.alarm.FallbackAlarmsInterface;
import cern.tim.util.mail.bean.MailDetailsBean;
import cern.tim.util.mail.exception.MailSenderException;
import cern.tim.util.mail.impl.SMTPMailSender;

/**
 * Implements the IAlarmListener interface just for testing purposes
 * @author mruizgar
 *
 */
public class AlarmListenerImpl implements IAlarmListener {

    
    /** Log4j logger for the class*/ 
    private static final Logger LOG = Logger.getLogger(AlarmListenerImpl.class);
    
    /**
     * Sends an email warning about the DB problems
     * @param alarmUp Indicates whether the email has to be sent or not
     * @param dbInfo Indicates the user account and the DB for which the problems arose
     * @param exceptionMsg A message indicating the exception details 
     */
    public final void dbUnavailable(final boolean alarmUp, final String exceptionMsg, final String dbInfo) {
        if (alarmUp == FallbackAlarmsInterface.ACTIVATED) {
        
            ArrayList recipients = new ArrayList();
            
            MailDetailsBean mailDetails = new MailDetailsBean();
            mailDetails.setSender("mruizgar@cern.ch");
            recipients.add("mruizgar@cern.ch");
            mailDetails.setToRecipients(recipients);
            mailDetails.setSubject("DB Unavailable");
            mailDetails.setMessage("The DB connection to " + dbInfo + "could not be established due to: " + exceptionMsg);
            mailDetails.setServer("cernmx.cern.ch");
            SMTPMailSender mailSender = new SMTPMailSender();
            
            try {
                mailSender.sendMail(mailDetails);
            } catch (MailSenderException e) {
                LOG.debug(e.getMessage());
            }
        }
    }
    
    /**
     * Sends an email warning about the disk getting full
     * @param alarmUp Indicates whether the email has to be sent or not
     * @param directoryName Indicates for which directory the disk is getting full
     */
    public final void diskFull(final boolean alarmUp, final String directoryName) {
        
        if (alarmUp == FallbackAlarmsInterface.ACTIVATED) {
        
            ArrayList recipients = new ArrayList();
            
            MailDetailsBean mailDetails = new MailDetailsBean();
            mailDetails.setSender("mruizgar@cern.ch");
            recipients.add("mruizgar@cern.ch");
            mailDetails.setToRecipients(recipients);
            mailDetails.setSubject("Disk Full");
            mailDetails.setMessage("The disk " + directoryName + " is getting full");
            mailDetails.setServer("cernmx.cern.ch");
            SMTPMailSender mailSender = new SMTPMailSender();
            
            try {
                mailSender.sendMail(mailDetails);
            } catch (MailSenderException e) {
                LOG.debug(e.getMessage());
            }
        }
    }
    
    /**
     * Sends an email warning about not being able to access to a concrete file
     * @param alarmUp Indicates whether the email has to be sent or not
     * @param file The file descriptor pointing to the file which have access problems
     */
    public final void fileNotReachable(final boolean alarmUp, final File file) {
        if (alarmUp == FallbackAlarmsInterface.ACTIVATED) {
            ArrayList recipients = new ArrayList();
            
            MailDetailsBean mailDetails = new MailDetailsBean();
            mailDetails.setSender("mruizgar@cern.ch");
            recipients.add("mruizgar@cern.ch");
            mailDetails.setToRecipients(recipients);
            mailDetails.setSubject("File not reachable");
            mailDetails.setServer("cernmx.cern.ch");
            mailDetails.setMessage("The file " + file.getAbsolutePath() + " could not be reached");
            
            SMTPMailSender mailSender = new SMTPMailSender();            
            
            try {
                mailSender.sendMail(mailDetails);
            } catch (MailSenderException e) {
                LOG.debug(e.getMessage());
            }
        }
    }
}
