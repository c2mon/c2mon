/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project. See http://ts-project-tim.web.cern.ch
 * Copyright (C) 2005 - 2011 CERN This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA. Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.daq.common;

import java.sql.Timestamp;

import cern.tim.shared.daq.datatag.ISourceDataTag;

/**
 * Interface for equipment message senders.
 * 
 * @author Andreas Lang
 */
public interface IEquipmentMessageSender {

    /**
     * This method should be invoked each time you want to propagate the supervision alive coming from the supervised
     * equipment.
     */
    void sendSupervisionAlive();

    /**
     * This method should be invoked each time you want to propagate the supervision alive coming from the supervised
     * equipment.
     * 
     * @param milisecTimestamp the timestamp (in milliseconds)
     */
    void sendSupervisionAlive(final long milisecTimestamp);

    /**
     * Tries to send a new value to the server.
     * 
     * @param currentTag The tag to which the value belongs.
     * @param milisecTimestamp The timestamp of the tag.
     * @param tagValue The tag value to send.
     * @return True if the tag has been send successfully to the server.
     */
    boolean sendTagFiltered(final ISourceDataTag currentTag, final Object tagValue, final long milisecTimestamp);

    /**
     * Tries to send a new value to the server.
     * 
     * @param currentTag The tag to which the value belongs.
     * @param milisecTimestamp The timestamp of the tag.
     * @param tagValue The tag value to send.
     * @param pValueDescr A description belonging to the value.
     * @return True if the tag has been send successfully to the server.
     */
    boolean sendTagFiltered(final ISourceDataTag currentTag, final Object tagValue, final long milisecTimestamp,
            final String pValueDescr);

    boolean sendTagFiltered(final ISourceDataTag currentTag, final Object tagValue, final long milisecTimestamp,
            final String pValueDescr, boolean sentByValueCheckMonitor);

    /**
     * This method sends an invalid SourceDataTagValue to the server.
     * 
     * @param sourceDataTag SourceDataTag object
     * @param pQualityCode the SourceDataTag's quality see {@link SourceDataQuality} class for details
     * @param pDescription the quality description (optional)
     */
    void sendInvalidTag(final ISourceDataTag sourceDataTag, final short pQualityCode, final String pDescription);

    /**
     * This method sends an invalid SourceDataTagValue to the server.
     * 
     * @param sourceDataTag SourceDataTag object
     * @param pQualityCode the SourceDataTag's quality see {@link SourceDataQuality} class for details
     * @param pDescription the quality description (optional)
     * @param pTimestamp time when the SourceDataTag's value has become invalid
     */
    void sendInvalidTag(final ISourceDataTag sourceDataTag, final short pQualityCode, final String pDescription,
            final Timestamp pTimestamp);

    /**
     * Sends a note to the business layer, to confirm that the equipment is not properly configured, or connected to its
     * data source
     */
    void confirmEquipmentStateIncorrect();

    /**
     * Sends a note to the business layer, to confirm that the equipment is not properly configured, or connected to its
     * data source
     * 
     * @param pDescription additional description
     */
    void confirmEquipmentStateIncorrect(final String pDescription);

    /**
     * Sends a note to the business layer, to confirm that the equipment is properly configured, connected to its source
     * and running
     */
    void confirmEquipmentStateOK();

    /**
     * Sends a note to the business layer, to confirm that the equipment is properly configured, connected to its source
     * and running
     * 
     * @param pDescription additional description
     */
    void confirmEquipmentStateOK(final String pDescription);

    /**
     * Sends all through timedeadband delayed values immediately
     */
    void sendDelayedTimeDeadbandValues();

}
