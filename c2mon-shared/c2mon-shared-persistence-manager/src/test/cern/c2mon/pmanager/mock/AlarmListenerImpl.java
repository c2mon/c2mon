/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.pmanager.mock;

import java.io.File;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.pmanager.IAlarmListener;
import cern.c2mon.pmanager.alarm.FallbackAlarmsInterface;
import cern.c2mon.shared.util.mail.bean.MailDetailsBean;
import cern.c2mon.shared.util.mail.exception.MailSenderException;
import cern.c2mon.shared.util.mail.impl.SMTPMailSender;

/**
 * Implements the IAlarmListener interface just for testing purposes
 * @author mruizgar
 *
 */
public class AlarmListenerImpl implements IAlarmListener {

    
    /** Log4j logger for the class*/ 
    private static final Logger LOG = LoggerFactory.getLogger(AlarmListenerImpl.class);
    
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
