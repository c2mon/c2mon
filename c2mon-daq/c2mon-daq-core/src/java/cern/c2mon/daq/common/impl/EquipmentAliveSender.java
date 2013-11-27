/******************************************************************************
 * This file is part of the CERN Control and Monitoring (C2MON) platform.
 * 
 * See http://cern.ch/c2mon
 * 
 * Copyright (C) 2005-2013 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: C2MON team, c2mon-support@cern.ch
 *****************************************************************************/
package cern.c2mon.daq.common.impl;

import static java.lang.String.format;

import java.sql.Timestamp;

import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.common.logger.EquipmentLoggerFactory;
import cern.c2mon.daq.common.messaging.IProcessMessageSender;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.common.type.TypeConverter;
import cern.c2mon.shared.daq.datatag.SourceDataTag;
import cern.c2mon.shared.daq.datatag.SourceDataTagValue;

/**
 * This class has all methods for sending the Equipment Supervision Alive Tags to the 
 * Process Message Sender
 * 
 * @author vilches
 *
 */
class EquipmentAliveSender {
	
	/**
     * Constant to prevent from frequent equipments alives
     */
    private static final boolean PREVENT_TO_FREQUENT_EQUIPMENT_ALIVES = Boolean.getBoolean("c2mon.daq.equipment.alive.filtering");
	
    /**
     * The logger for this class.
     */
    private EquipmentLogger equipmentLogger;
    
    /**
     * The process message sender takes the messages actually send to the server.
     */
    private IProcessMessageSender processMessageSender;
    
    /**
     * The last Equipment Alive Time Stamp
     */
    private Long lastEquipmentAliveTimestamp;
    
    /**
     * The Equipment alive tag interval to be used for sending or not the equipment alive
     */
    private Long aliveTagInterval;
    
    /**
     * The Equipment Configuration Name
     */
    private String confName;
    
    /** The equipment supervision alive tag id */
    private final Long aliveTagId;
    
    /**
     * Creates a new EquipmentAliveSender.
     * 
     * @param processMessageSender Process Message Sender
     * @param equipmentLoggerFactory Equipment Logger factory to create the class logger
     * @param aliveTagId The equipment supervision alive tag id
     */
    public EquipmentAliveSender (final IProcessMessageSender processMessageSender,
    		                     final Long aliveTagId, 
    		                     final EquipmentLoggerFactory equipmentLoggerFactory) {
    	this.processMessageSender = processMessageSender;
        this.aliveTagId = aliveTagId;
        this.equipmentLogger = equipmentLoggerFactory.getEquipmentLogger(getClass());
    }
    
    /**
     * Init function
     * 
     * @param aliveTagInterval Equipment configuration Tag interval
     * @param confName Equipment configuration name
     */
    public void init(final Long aliveTagInterval, final String confName) {
    	this.aliveTagInterval = aliveTagInterval;
    	this.confName = confName;
    }
    

    /**
     * This method should be invoked each time you want to propagate the supervision alive coming from the supervised
     * equipment.
     * 
     * @param supAliveTag alive Data Tag
     * @param supAliveId Supervision Alive Tag id
     * @param milisecTimestamp the timestamp (in milliseconds)
     */
    private void doSendEquipmentAlive(final SourceDataTag supAliveTag, final long milisecTimestamp) {
        SourceDataTagValue supAliveValue;
        
        if (supAliveTag != null) {
            Object value = null;
            if (supAliveTag.getDataType().equalsIgnoreCase("Long")) {
                value = TypeConverter.cast(Long.valueOf(milisecTimestamp).toString(), supAliveTag.getDataType());
            } else if (supAliveTag.getDataType().equalsIgnoreCase("Integer")) {
                value = TypeConverter.cast(Long.valueOf(milisecTimestamp % Integer.MAX_VALUE).toString(),
                		supAliveTag.getDataType());
            } else {
                this.equipmentLogger.warn("sendSupervisionAlive() - Equipment alive value is neither of type Long " +
                		"nor of Integer => value set to null!");
            }

            supAliveValue = supAliveTag.update(value,
                    "Equipment alive tag value has been overwritten by the DAQ Core with the source timestamp",
                    new Timestamp(milisecTimestamp));
        } else {
            supAliveValue = new SourceDataTagValue(this.aliveTagId, "eqalive", true, milisecTimestamp, null, milisecTimestamp,
                    DataTagConstants.PRIORITY_HIGH, false, null, DataTagConstants.TTL_FOREVER);
        }

        this.equipmentLogger.debug("sendSupervisionAlive() - Sending equipment alive message with timestamp " + milisecTimestamp);
        
        // invoke ProcessMessageSender's addValue
        this.processMessageSender.addValue(supAliveValue);
    }
    
    /**
     * 
     * @param supAliveTag The equipment Supervision Alive Tag 
     * @param milisecTimestamp TimeStamp in milisec
     * 
     * @return <code>true</code>, if the Equipment Alive is sent. Otherwise <code>false</code>. 
     */
    public boolean sendEquipmentAlive(final SourceDataTag supAliveTag, final long milisecTimestamp) {
    	// If prevent To Frequent Equipment Alives is enabled (by default it is not)
    	if (PREVENT_TO_FREQUENT_EQUIPMENT_ALIVES) {

    		boolean isSendEquipmentAlive = true;
    		if (this.lastEquipmentAliveTimestamp != null) {

    			// if the time difference between the last eq. heartbeat and the current one is at least half of the
    			// eq. alive interval defined
    			long diff = milisecTimestamp - this.lastEquipmentAliveTimestamp;
    			long halfTime = Math.round(this.aliveTagInterval / 2.0);

    			if (diff < halfTime) {
    				if (this.equipmentLogger.isDebugEnabled()) {
    					this.equipmentLogger.debug(format("this EquipmentAlive of equipment %s will be skipped "
    							+ "and will not be sent the server due to enabled equipment alive filtering policy",
    							this.confName));
    				}

    				isSendEquipmentAlive = false;
    			}
    		}

    		// If sendEquipmentAlive is true the supervision alive is sent
    		if (isSendEquipmentAlive) {
    			doSendEquipmentAlive(supAliveTag, milisecTimestamp);
    			this.lastEquipmentAliveTimestamp = milisecTimestamp;

    			return true;
    		} // If sendEquipmentAlive is false nothing is sent
    		else {
    			return false;
    		}

    	}
    	// If Prevent To Frequent Equipment Alives is disabled (by default it is)
    	else {
    		doSendEquipmentAlive(supAliveTag, milisecTimestamp);
    		
    		return true;
    	}
    }
}
