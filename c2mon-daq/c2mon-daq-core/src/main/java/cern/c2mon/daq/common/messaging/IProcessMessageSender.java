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
package cern.c2mon.daq.common.messaging;

import cern.c2mon.shared.common.datatag.SourceDataTagValue;

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

    /**
     * Sends a communication fault tag message.
     * @param tagId The tag ID to use.
     * @param commOK The value to send.
     * @param description The description to add to the message
     */
    void sendCommfaultTag(long tagId, String tagName, boolean commOK, String description);

    /**
     * This method is responsible for creating a JMS XML message containing
     * alive tag and putting it to the TIM JMS queue.
     */
    void sendProcessAlive();
}
