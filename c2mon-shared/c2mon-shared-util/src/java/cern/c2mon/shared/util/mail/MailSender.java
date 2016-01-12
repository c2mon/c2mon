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
