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
package cern.c2mon.daq.common.impl;

import java.sql.Timestamp;

import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.common.logger.EquipmentLoggerFactory;
import cern.c2mon.daq.filter.IFilterMessageSender;
import cern.c2mon.shared.common.datatag.SourceDataQuality;
import cern.c2mon.shared.common.datatag.SourceDataTag;
import cern.c2mon.shared.common.filter.FilteredDataTagValue;

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
     * @param filterType The type of the applied filter, see {@link FilteredDataTagValue} constants
     */
    public void sendToFilterModule(final SourceDataTag currentSourceDataTag, 
                                   final SourceDataQuality quality,
                                   final Object tagValue, 
                                   final long milisecTimestamp, 
                                   final String pValueDescr, 
                                   final short filterType) {
      doSendToFilterModule(currentSourceDataTag, quality, tagValue, milisecTimestamp, pValueDescr, false, filterType);
    }

    /**
     * Sends a message to the statistics module with Dynamic Timedeadband. Should only be used in the core.
     * 
     * @param currentSourceDataTag The tag to send.
     * @param quality The quality of the tag.
     * @param tagValue The value of the tag.
     * @param milisecTimestamp The timestamp in ms.
     * @param pValueDescr A description of the value (optional)
     * @param filterType The type of the applied filter, see {@link FilteredDataTagValue} constants
     */
    public void sendToFilterModuleByDynamicTimedeadbandFilterer(final SourceDataTag currentSourceDataTag, 
                                                                final SourceDataQuality quality,
                                                                final Object tagValue, 
                                                                final long milisecTimestamp, 
                                                                final String pValueDescr, 
                                                                final short filterType) {
      doSendToFilterModule(currentSourceDataTag, quality, tagValue, milisecTimestamp, pValueDescr, true, filterType);
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
    private void doSendToFilterModule(final SourceDataTag currentSourceDataTag, 
                                      final SourceDataQuality quality, 
                                      final Object tagValue,
                                      final long milisecTimestamp, 
                                      final String pValueDescr, 
                                      final boolean dynamicFiltered, 
                                      final short filterType) {
      this.equipmentLogger.trace("sendToFilterModule - entering sendToFilterModule() for tag #" + currentSourceDataTag.getId());
  
      this.filterMessageSender.addValue(currentSourceDataTag.makeFilterValue(quality, new Timestamp(milisecTimestamp), dynamicFiltered, filterType));
  
      this.equipmentLogger.trace("sendToFilterModule - leaving sendToFilterModule() for tag #" + currentSourceDataTag.getId());
    }

    /**
     * Sends a message to the filter log. Should only be used in the core.
     * 
     * @param currentSourceDataTag The tag to send.
     * @param tagValue tagValue The value of the tag.
     * @param milisecTimestamp The timestamp in ms.
     * @param pValueDescr A description of the value (optional)
     * @param filterType The type of the applied filter, see {@link FilteredDataTagValue} constants
     */
    public void sendToFilterModule(final SourceDataTag currentSourceDataTag, 
                                   final Object tagValue,
                                   final long milisecTimestamp, 
                                   final String pValueDescr, 
                                   final short filterType) {	
      doSendToFilterModule(currentSourceDataTag, tagValue, milisecTimestamp, pValueDescr, false, filterType);
    }
    
    /**
     * Sends a message to the filter log with Dynamic Deadband. Should only be used in the core.
     * 
     * @param currentSourceDataTag The tag to send.
     * @param tagValue tagValue The value of the tag.
     * @param milisecTimestamp The timestamp in ms.
     * @param pValueDescr A description of the value (optional)
     * @param filterType The type of the applied filter, see {@link FilteredDataTagValue} constants
     */
    public void sendToFilterModuleByDynamicTimedeadbandFilterer(final SourceDataTag currentSourceDataTag, 
                                   final Object tagValue,
                                   final long milisecTimestamp, 
                                   final String pValueDescr, 
                                   final short filterType) {  
      doSendToFilterModule(currentSourceDataTag, tagValue, milisecTimestamp, pValueDescr, true, filterType);
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
    private void doSendToFilterModule(final SourceDataTag currentSourceDataTag, 
                                      final Object tagValue,
                                      final long milisecTimestamp, 
                                      final String pValueDescr, 
                                      final boolean dynamicFiltered,
                                      final short filterType) {
      this.equipmentLogger.trace("sendToFilterModule - entering sendToFilterModule() for tag #" + currentSourceDataTag.getId());

      this.filterMessageSender.addValue(currentSourceDataTag.makeFilterValue(new Timestamp(milisecTimestamp), tagValue,
          pValueDescr, dynamicFiltered, filterType));
      
      this.equipmentLogger.trace("sendToFilterModule - leaving sendToFilterModule() for tag #" + currentSourceDataTag.getId());
    }
}
