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

import java.sql.Timestamp;

import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.common.logger.EquipmentLoggerFactory;
import cern.c2mon.daq.filter.IFilterMessageSender;
import cern.tim.shared.daq.datatag.SourceDataQuality;
import cern.tim.shared.daq.datatag.SourceDataTag;
import cern.tim.shared.daq.filter.FilteredDataTagValue;

/**
 * This class is a helper to deal with all sender methods that use the Filter Message Sender
 * 
 * @author vilches
 *
 */
class EquipmentSenderFilterModule {
	
	/**
	 * The filter message sender. All tags a filter rule matched are added to this.
	 */
	private IFilterMessageSender filterMessageSender;

	/**
	 * The logger for this class.
	 */
	private EquipmentLogger equipmentLogger;
    
    /**
     * Creates a new EquipmentSenderFilterModule.
     * 
     * @param filterMessageSender The filter message sender to send filtered tag values.
     * @param equipmentLoggerFactory Equipment Logger factory to create the class logger
     */
    public EquipmentSenderFilterModule (final IFilterMessageSender filterMessageSender, 
    		final EquipmentLoggerFactory equipmentLoggerFactory) {
   
        this.filterMessageSender = filterMessageSender;
        this.equipmentLogger = equipmentLoggerFactory.getEquipmentLogger(getClass());
    }
    
    
    
	/**
     * Sends a message to the statistics module. Should only be used in the core.
     * 
     * @param currentSourceDataTag The tag to send.
     * @param quality The quality of the tag.
     * @param tagValue The value of the tag.
     * @param milisecTimestamp The timestamp in ms.
     * @param pValueDescr A description of the value (optional)
     * @param dynamicFiltered True if the tag was dynamic filtered.
     * @param filterType The type of the applied filter, see {@link FilteredDataTagValue} constants
     */
    public void sendToFilterModule(final SourceDataTag currentSourceDataTag, final SourceDataQuality quality,
            final Object tagValue, final long milisecTimestamp, final String pValueDescr,
            final boolean dynamicFiltered, final short filterType) {
    	this.equipmentLogger.trace("sendToFilterModule - entering sendToFilterModule() for tag #" + currentSourceDataTag.getId());

    	this.filterMessageSender.addValue(currentSourceDataTag.makeFilterValue(quality, new Timestamp(milisecTimestamp),
    			dynamicFiltered, filterType));

    	this.equipmentLogger.trace("sendToFilterModule - leaving sendToFilterModule() for tag #" + currentSourceDataTag.getId());
    }

    /**
     * Sends a message to the filter log. Should only be used in the core.
     * 
     * @param currentSourceDataTag The tag to send.
     * @param tagValue tagValue The value of the tag.
     * @param milisecTimestamp The timestamp in ms.
     * @param pValueDescr A description of the value (optional)
     * @param dynamicFiltered True if the tag was dynamic filtered.
     * @param filterType The type of the applied filter, see {@link FilteredDataTagValue} constants
     */
    public void sendToFilterModule(final SourceDataTag currentSourceDataTag, final Object tagValue,
            final long milisecTimestamp, final String pValueDescr, final boolean dynamicFiltered, 
            final short filterType) {
    	this.equipmentLogger.trace("sendToFilterModule - entering sendToFilterModule() for tag #" + currentSourceDataTag.getId());
    	
        this.filterMessageSender.addValue(currentSourceDataTag.makeFilterValue(new Timestamp(milisecTimestamp), tagValue,
                pValueDescr, dynamicFiltered, filterType));
        
        this.equipmentLogger.trace("sendToFilterModule - leaving sendToFilterModule() for tag #" + currentSourceDataTag.getId());
    }
}
