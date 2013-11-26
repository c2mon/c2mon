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
import java.util.Hashtable;
import java.util.Timer;

import cern.c2mon.daq.common.IDynamicTimeDeadbandFilterer;
import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.common.logger.EquipmentLoggerFactory;
import cern.c2mon.daq.tools.DataTagValueFilter;
import cern.tim.shared.daq.datatag.SourceDataTag;
import cern.tim.shared.daq.filter.FilteredDataTagValue;

/**
 * This class has all methods related with the Equipment Time Deadband (filter, scheduled, ...)
 * 
 * @author vilches
 *
 */
class EquipmentTimeDeadband {
	/**
	 * The logger for this class.
	 */
	private EquipmentLogger equipmentLogger;
	
	/**
     * The timedeadband schedulers hold tags which have time deadband scheduling activated.
     */
    private Hashtable<Long, SDTTimeDeadbandScheduler> sdtTimeDeadbandSchedulers = new Hashtable<Long, SDTTimeDeadbandScheduler>();
    
    /**
     * Filters for Data Tag outgoing Values
     */    
    private DataTagValueFilter dataTagValueFilter;
    
    /**
     * This is the time deadband scheduler timer where all schedulers are scheduled on.
     */
    private static Timer timeDeadbandTimer = new Timer("Time deadband timer", true);
    
    /**
     * The dynamic time dead band filterer for recording the current source data tag
     */
    private IDynamicTimeDeadbandFilterer dynamicTimeDeadbandFilterer;
    
    /**
     * Sender valid object
     */
    private EquipmentSenderValid equipmentSenderValid;
    
    /**
     * Creates a new EquipmentTimeDeadband.
     * 
     * @param equipmentSenderValid Sender Valid class for sending valid values
     * @param dynamicTimeDeadbandFilterer The dynamic time dead band filterer for recording 
     * the current source data tag
     * @param equipmentLoggerFactory  Equipment Logger factory to create the class logger
     */
    public EquipmentTimeDeadband (final IDynamicTimeDeadbandFilterer dynamicTimeDeadbandFilterer,
            final EquipmentLoggerFactory equipmentLoggerFactory) {
        this.dynamicTimeDeadbandFilterer = dynamicTimeDeadbandFilterer;
        this.equipmentLogger = equipmentLoggerFactory.getEquipmentLogger(getClass());
        
        this.dataTagValueFilter = new DataTagValueFilter(equipmentLoggerFactory);
    }
    
    /**
     * init
     * 
     * @param equipmentSenderValid
     */
    public void init(EquipmentSenderValid equipmentSenderValid) {
        setEquipmentSenderValid(equipmentSenderValid);
    }
    
    /**
     * Setter
     * 
     * @param equipmentSenderValid
     */
    public void setEquipmentSenderValid(EquipmentSenderValid equipmentSenderValid) {
        this.equipmentSenderValid = equipmentSenderValid;
    }
    
    
	/**
     * Starts the time deadband scheduler for this tag.
     * 
     * @param currentTag The tag which should have a time deadband scheduler.
     */
    private void startSDTtimeDeadbandScheduler(final SourceDataTag currentTag) {
        if (currentTag.getAddress().isTimeDeadbandEnabled()) {
            if (currentTag.getAddress().getTimeDeadband() > 0) {
                this.sdtTimeDeadbandSchedulers.put(currentTag.getId(), new SDTTimeDeadbandScheduler(currentTag, 
                		this.equipmentSenderValid, timeDeadbandTimer, this.dataTagValueFilter));
            }
        }
    }
    
    /**
     * Adds the provided tag value to the tagScheduler of this tag.
     * 
     * @param currentTag The tag of which the tag scheduler should be used.
     * @param tagValue The value of the tag.
     * @param milisecTimestamp A timestamp in ms.
     * @param pValueDescr An optional value description.
     */
    public void addToTimeDeadband(final SourceDataTag currentTag, final Object tagValue, final long milisecTimestamp,
            final String pValueDescr) {
        long tagID = currentTag.getId();
        SDTTimeDeadbandScheduler tagScheduler = this.sdtTimeDeadbandSchedulers.get(tagID);
        if (tagScheduler == null) {
            startSDTtimeDeadbandScheduler(currentTag);
            tagScheduler = this.sdtTimeDeadbandSchedulers.get(tagID);
        }

        this.dynamicTimeDeadbandFilterer.recordTag(currentTag);

        // if the scheduler is set to send the current tag value,
        // then we need to send it
        // to the statistics module before updating the tag:
        if (tagScheduler.isScheduledForSending()) {
            this.equipmentLogger.debug("addToTimeDeadband - Sending time deadband filtered value to statistics module " + tagID);
            boolean dynamicFiltered = !currentTag.getAddress().isStaticTimedeadband();
            this.equipmentLogger.debug("Tag filtered through time deadband filtering: '" + tagID + "'");
            
            this.equipmentSenderValid.getEquipmentSenderFilterModule().sendToFilterModule(currentTag, 
                    currentTag.getCurrentValue().getValue(), currentTag.getCurrentValue().getTimestamp().getTime(), 
                    currentTag.getCurrentValue().getValueDescription(), dynamicFiltered, 
                    FilteredDataTagValue.TIME_DEADBAND);
        }

        // update the tag value
        currentTag.update(tagValue, pValueDescr, new Timestamp(milisecTimestamp));

        this.equipmentLogger.debug("addToTimeDeadband - scheduling value update due to time-deadband filtering rule");
        // notify the scheduler that it contains a value that needs sending
        tagScheduler.scheduleValueForSending();
    }

    /**
     * Stops the time deadband scheduler of this tag and removes it from the map of schedulers.
     * 
     * @param currentTag The tag to remove.
     */
    public void removeFromTimeDeadband(final SourceDataTag currentTag) {
        if (this.equipmentLogger.isTraceEnabled())
            this.equipmentLogger.trace(format("removeFromTimeDeadband - entering removeFromTimeDeadband(%d)..", currentTag.getId()));
        SDTTimeDeadbandScheduler scheduler = this.sdtTimeDeadbandSchedulers.remove(currentTag.getId());
        if (scheduler != null) {
            this.equipmentLogger.trace("\tcancelling scheduler");
            scheduler.cancel();
            if (scheduler.isScheduledForSending())
                this.equipmentLogger
                        .trace("\tforcing scheduler to run its run() in order to send the flush buffered message (if any)");
            scheduler.run();
        }

        if (this.equipmentLogger.isTraceEnabled())
            this.equipmentLogger.trace(format("removeFromTimeDeadband - leaving removeFromTimeDeadband(%d)", currentTag.getId()));
    }

    /**
     * Flushes and resets the scheduler of this tag
     * 
     * @param currentTag The tag
     */
    public void flushAndResetTimeDeadband(final SourceDataTag currentTag) {
        if (this.equipmentLogger.isTraceEnabled())
            this.equipmentLogger.trace(format("flushAndResetTimeDeadband - entering flushAndResetTimeDeadband(%d)..", currentTag.getId()));
        SDTTimeDeadbandScheduler scheduler = this.sdtTimeDeadbandSchedulers.get(currentTag.getId());
        if (scheduler != null) {
            scheduler.flushAndReset();
        }

        if (this.equipmentLogger.isTraceEnabled())
            this.equipmentLogger.trace(format("flushAndResetTimeDeadband - leaving flushAndResetTimeDeadband(%d)", currentTag.getId()));
    }
    
    /**
     * Sends all through timedeadband delayed values immediately
     */
    public void sendDelayedTimeDeadbandValues() {
        this.equipmentLogger.trace("sendDelayedTimeDeadbandValues - Sending all time deadband delayed values to the server");
        
        for (SDTTimeDeadbandScheduler scheduler : this.sdtTimeDeadbandSchedulers.values()) {
            if (scheduler.isScheduledForSending()) {
                scheduler.run();
            }
        }
    }
    
    /**
     * 
     * @return sdtTimeDeadbandSchedulers
     */
    public Hashtable<Long, SDTTimeDeadbandScheduler> getSdtTimeDeadbandSchedulers() {
    	return this.sdtTimeDeadbandSchedulers;
    }
}
