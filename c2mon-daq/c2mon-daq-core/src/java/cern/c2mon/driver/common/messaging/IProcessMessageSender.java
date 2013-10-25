/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005 - 2011 CERN This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.driver.common.messaging;

import cern.tim.shared.daq.datatag.SourceDataTagValue;

/**
 * This interface specifies all operations, that ProcessMessageSender class MUST
 * implement. The EquipmentMessageHandler "see" the ProcessMessageSender through
 * this interface
 */
public interface IProcessMessageSender {

    /**
     * The method checks the priority of the given SourceDataTagValue object and
     * takes the decision whether it should be sent directly or put into the
     * buffer. If the priority is HIGH it invokes processValue method so that
     * the SourceDataTagValue object could be sent immediately. Otherwise (if
     * the priority is LOW), the method puts the SourceDataTagValue object into
     * the buffer. The content of the buffer will be later encapsulated in a JMS
     * message and sent to recipients via JMS
     * 
     * @param dataTagValue
     *            the SourceDataTagValue object
     */
    void addValue(SourceDataTagValue dataTagValue);

    // TODO Why is an object used? Commfault tag value is in general boolean.
    /**
     * This methods is responsible for sending CommfaultTag message
     * @param tagID The tag ID to use.
     * @param value The value to send.
     */
    void sendCommfaultTag(long tagID, Object value);

    /**
     * Sends a communication fault tag message.
     * @param tagID The tag ID to use.
     * @param value The value to send.
     * @param pDesctiption The description to add to the message
     */
    void sendCommfaultTag(long tagID, Object value, String pDesctiption);

    /**
     * This method is responsible for creating a JMS XML message containing
     * alive tag and putting it to the TIM JMS queue.
     */
    void sendAlive();
    
}
