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

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import cern.c2mon.daq.tools.DataTagValueFilter;
import cern.tim.shared.daq.datatag.SourceDataTag;
import cern.tim.shared.daq.datatag.SourceDataTagValue;
import cern.tim.shared.daq.filter.FilteredDataTagValue;

/**
 * The SourceDataTagTimeDeadbandScheduler class models threads responsible for handling source time deadband filtering
 */
public class SDTTimeDeadbandScheduler extends TimerTask {
    /**
     * Log4j Logger for this class
     */
    private static final Logger LOGGER = Logger.getLogger(SDTTimeDeadbandScheduler.class);

    /**
     * The source data tag controlled by this object.
     */
    private final SourceDataTag sourceDataTag;

    /**
     * True if the current value should be send in the next run cycle.
     */
    private volatile boolean sendValue = false;

    /**
     * The last value sent to the server.
     */
    private Object lastSentValue;

    /**
     * The description of the last value sent to the server.
     */
    private String lastSentValueDescr;

    /**
     * Used to send the messages to the server.
     */
    private EquipmentSenderValid equipmentSenderValid;

    /**
     * Filters for Data Tag outgoing Values
     */    
    private DataTagValueFilter dataTagValueFilter;

    /**
     * 
     */
    private Timer timeDeadbandTimer;
    
    /**
     * Indicates if there is a new value to send to the server.
     * 
     * @return True if in the next cycle a value should be send to the server else false.
     */
    public boolean isScheduledForSending() {
        return sendValue;
    }

    /**
     * Creates a new SDTTimeDeadbandscheduler
     * 
     * @param sourceDataTag The source data tag controlled by this object.
     * @param equipmentSenderValid Used to send messages to the server.
     * @param timeDeadbandTimer The timer to schedule this task on.
     * @param valueChecker Value checker object to avoid repeated values.
     */
    public SDTTimeDeadbandScheduler(final SourceDataTag sourceDataTag, final EquipmentSenderValid equipmentSenderValid, 
    		final Timer timeDeadbandTimer, final DataTagValueFilter dataTagValueFilter) {
        this.dataTagValueFilter = dataTagValueFilter;
        this.equipmentSenderValid = equipmentSenderValid;

        this.timeDeadbandTimer = timeDeadbandTimer;

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("creating time-deadband scheduler for tag : " + sourceDataTag.getId());

        this.sourceDataTag = sourceDataTag;

        // create timers
        if (sourceDataTag.getAddress().isTimeDeadbandEnabled()) {
            if (sourceDataTag.getAddress().getTimeDeadband() > 0)
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("\tscheduler[" + this.sourceDataTag.getId() + "] : setting scheduling interval to : "
                            + this.sourceDataTag.getAddress().getTimeDeadband() + " miliseconds");
            this.timeDeadbandTimer.scheduleAtFixedRate(this, 0, this.sourceDataTag.getAddress().getTimeDeadband());
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("\tscheduler[" + this.sourceDataTag.getId() + "] : setting scheduled");

        }
    }

    /**
     * flushes and resets the scheduler
     */
    public void flushAndReset() {
        synchronized (this.sourceDataTag) {
            this.run();
            this.lastSentValue = null;
            this.lastSentValueDescr = null;
        }
    }

    /**
     * Informs the scheduler, that next time it wakes up, it should send the value of the source data tag
     */
    public void scheduleValueForSending() {
        this.sendValue = true;
    }

    /**
     * The run method called from the timer.
     */
    @Override
    public void run() {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("scheduler[" + this.sourceDataTag.getId() + "] : entering run()..");
        try {
            synchronized (this.sourceDataTag) {
                if (this.sendValue) {
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug("\tscheduler[" + this.sourceDataTag.getId() + "] : sending value");
                    // recheck value deadband and equality
                    SourceDataTagValue value = sourceDataTag.getCurrentValue();
                    if (this.dataTagValueFilter.isValueDeadbandFiltered(this.sourceDataTag, this.lastSentValue, this.lastSentValueDescr)) {
                    	this.equipmentSenderValid.getEquipmentSenderFilterModule().sendToFilterModule(this.sourceDataTag, 
                    			value.getValue(), System.currentTimeMillis(), null, false, FilteredDataTagValue.VALUE_DEADBAND);
                        // Filter tags which didn't change their value
                    } else if (this.dataTagValueFilter.isSameValue(this.sourceDataTag, this.lastSentValue, this.lastSentValueDescr)) {
                    	this.equipmentSenderValid.getEquipmentSenderFilterModule().sendToFilterModule(this.sourceDataTag, 
                    			value.getValue(), System.currentTimeMillis(), null, false, FilteredDataTagValue.REPEATED_VALUE);
                    } else {
                        this.lastSentValue = value.getValue();
                        this.lastSentValueDescr = value.getValueDescription();
                        this.equipmentSenderValid.sendTag(this.lastSentValue, System.currentTimeMillis(), null, 
                        		this.sourceDataTag);
                    }
                    this.sendValue = false;
                } else {
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug("\tscheduler[" + this.sourceDataTag.getId() + "] : no new value to be sent");
                }
            } // synchronized
        } catch (Exception exception) {
            LOGGER.error("Critical error in scheduler for tag " + this.sourceDataTag.getId(), exception);
        }
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("scheduler[" + this.sourceDataTag.getId() + "] : leaving run()");
    }

}
