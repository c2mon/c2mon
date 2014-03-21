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

import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.daq.common.IDynamicTimeDeadbandFilterer;
import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.common.logger.EquipmentLoggerFactory;
import cern.c2mon.daq.common.messaging.IProcessMessageSender;
import cern.c2mon.daq.filter.IFilterMessageSender;
import cern.c2mon.daq.tools.DataTagValueFilter;
import cern.c2mon.daq.tools.EquipmentSenderHelper;
import cern.c2mon.shared.daq.datatag.SourceDataQuality;
import cern.c2mon.shared.daq.datatag.SourceDataTag;
import cern.c2mon.shared.daq.datatag.SourceDataTagValue;
import cern.c2mon.shared.daq.filter.FilteredDataTagValue;
import cern.c2mon.shared.daq.filter.FilteredDataTagValue.FilterType;

/**
 * This class is used to send invalid messages to the server.
 * 
 * @author vilches
 *
 */
class EquipmentSenderInvalid {
    
    /**
     * The logger for this class.
     */
    private EquipmentLogger equipmentLogger;
    
    /**
     * The process message sender takes the messages actually send to the server.
     */
    private IProcessMessageSender processMessageSender;
    
    /**
     * The equipment sender helper with many common and useful methods shared by sending classes
     */
    private EquipmentSenderHelper equipmentSenderHelper = new EquipmentSenderHelper();
    
    /**
     * Filters for Data Tag outgoing Values
     */    
    private DataTagValueFilter dataTagValueFilter;
    
    /**
     * The dynamic time dead band filterer for recording the current source data tag
     */
    private IDynamicTimeDeadbandFilterer dynamicTimeDeadbandFilterer;
    
    /**
     * The class with the message sender to send filtered tag values
     */
    private EquipmentSenderFilterModule equipmentSenderFilterModule;
    
    /**
     * Equipment Time Deadband
     */
    private EquipmentTimeDeadband equipmentTimeDeadband;
    
    /**
     * Creates a new EquipmentInvalidSender. 
     * 
     * @param processMessageSender The process message sender to send tags to the server.
     * @param equipmentSenderHelper
     * @param equipmentLogger
     */
    @Autowired
    public EquipmentSenderInvalid (final EquipmentSenderFilterModule equipmentSenderFilterModule,
    		                           final IProcessMessageSender processMessageSender, 
    		                           final EquipmentTimeDeadband equipmentTimeDeadband,
    		                           final IDynamicTimeDeadbandFilterer dynamicTimeDeadbandFilterer,
    		                           final EquipmentLoggerFactory equipmentLoggerFactory) {
      this.equipmentSenderFilterModule = equipmentSenderFilterModule;
    	this.processMessageSender = processMessageSender;
    	this.equipmentTimeDeadband = equipmentTimeDeadband;
    	this.dynamicTimeDeadbandFilterer = dynamicTimeDeadbandFilterer;
    	this.equipmentLogger = equipmentLoggerFactory.getEquipmentLogger(getClass());
    	
    	this.dataTagValueFilter = new DataTagValueFilter(equipmentLoggerFactory);
    }
    
	/**
     * This method sends an invalid SourceDataTagValue to the server. Source and DAQ timestamps are set to the current
     * DAQ system time.
     * 
     * @param sourceDataTag SourceDataTag object
     * @param pQualityCode the SourceDataTag's quality see {@link SourceDataQuality} class for details
     * @param pDescription the quality description (optional)
     */
    public void sendInvalidTag(final SourceDataTag sourceDataTag, final short pQualityCode, final String pDescription) {
        sendInvalidTag(sourceDataTag, pQualityCode, pDescription, null);
    }

    /**
     * This method sends an invalid SourceDataTagValue to the server, without changing its origin value.
     * 
     * @param sourceDataTag SourceDataTag object
     * @param pQualityCode the SourceDataTag's quality see {@link SourceDataQuality} class for details
     * @param qualityDescription the quality description (optional)
     * @param pTimestamp time when the SourceDataTag's value has become invalid; if null the source timestamp and DAQ
     *            timestamp will be set to the current DAQ system time
     */
    public void sendInvalidTag(final SourceDataTag sourceDataTag, 
                               final short qualityCode, 
                               final String qualityDescription, 
                               final Timestamp pTimestamp) {
      // Get the source data quality from the quality code
      SourceDataQuality newSDQuality = this.equipmentSenderHelper.createTagQualityObject(qualityCode, qualityDescription);
      
      // The sendInvalidTag function with the value argument will take are of it
      if (sourceDataTag.getCurrentValue() != null) {
        sendInvalidTag(sourceDataTag, sourceDataTag.getCurrentValue().getValue(), sourceDataTag.getCurrentValue().getValueDescription(), 
            newSDQuality, pTimestamp);
      }
      else {
        sendInvalidTag(sourceDataTag, null, "", newSDQuality, pTimestamp);
      }
    }

    /**
     * This method sends both an invalid and updated SourceDataTagValue to the server.
     * 
     * @param sourceDataTag SourceDataTag object
     * @param newValue The new update value that we want set to the tag 
     * @param newTagValueDesc The new value description
     * @param newSDQuality the new SourceDataTag see {@link SourceDataQuality}
     * @param pTimestamp time when the SourceDataTag's value has become invalid; if null the source timestamp and DAQ
     *            timestamp will be set to the current DAQ system time
     */
    protected void sendInvalidTag(final SourceDataTag sourceDataTag,
                               final Object newValue,
                               final String newTagValueDesc,
                               final SourceDataQuality newSDQuality,
                               final Timestamp pTimestamp) {
      this.equipmentLogger.debug("sendInvalidTag - entering sendInvalidTag() for tag #" + sourceDataTag.getId());

      // Create time stamp for filterng or invalidating
      Timestamp timestamp;
      if (pTimestamp == null) {
        timestamp = new Timestamp(System.currentTimeMillis());
      } else {
        timestamp = pTimestamp;
      }

      try {
        // We check first is the new value has to be filtered out or not
        FilterType filterType = this.dataTagValueFilter.isCandidateForFiltering(sourceDataTag, newValue, newTagValueDesc, 
            newSDQuality, timestamp.getTime());
        
        // The new value will not be filtered out
        if(filterType == FilterType.NO_FILTERING) {
          // Send the value
          sendValueWithTimeDeadbandCheck(sourceDataTag, newValue, newTagValueDesc, newSDQuality, timestamp);
        }
        // The new value will be filtered out
        else {
          // If we are here the new Value will be filtered out
          if (this.equipmentLogger.isDebugEnabled()) {
            StringBuilder msgBuf = new StringBuilder();
            msgBuf.append("\tthe tag [" + sourceDataTag.getId()
                + "] has already been invalidated with quality code : " + newSDQuality.getQualityCode());
            msgBuf.append(" at " + sourceDataTag.getCurrentValue().getTimestamp());
            msgBuf.append(" The DAQ has not received any values with different quality since than, Hence, the");
            msgBuf.append(" invalidation procedure will be canceled this time");
            this.equipmentLogger.debug(msgBuf.toString());
          }

          /*
           * the value object can be null if several invalid data tags are sent when the DAQ is started up (the
           * value object is still null, but the currentValue object is not anymore) in this case, we choose not
           * to send it to the filter path
           */
          if (newValue != null) {
            // send a corresponding INVALID tag to the statistics module
            this.equipmentLogger.debug("sendInvalidTag - sending an invalid tag ["+sourceDataTag.getId()+"] to the statistics module");

            // send filtered message to statistics module
            this.equipmentSenderFilterModule.sendToFilterModule(sourceDataTag, newSDQuality, newValue, timestamp.getTime(), 
                newTagValueDesc, filterType.getNumber());

          } else if (this.equipmentLogger.isDebugEnabled()) {
            this.equipmentLogger.debug("sendInvalidTag - value has still not been initialised: not sending the invalid tag ["+sourceDataTag.getId()+"] to the statistics module");
          }
        }
      } catch (Exception ex) {
        this.equipmentLogger.error("sendInvalidTag - Unexpected exception caught for tag " + sourceDataTag.getId() + ", " + ex.getStackTrace(), ex);
        
      }
      this.equipmentLogger.debug("sendInvalidTag - leaving sendInvalidTag()");
    }
    
    /**
     * This method checks the time deadband and according to the result it sends the updated value to the server
     * 
     * @param sourceDataTag SourceDataTag object
     * @param newValue The new update value that we want set to the tag 
     * @param newTagValueDesc The new value description
     * @param newSDQuality the new SourceDataTag see {@link SourceDataQuality}
     * @param timestamp time when the SourceDataTag's value has become invalid; if null the source timestamp and DAQ
     *            timestamp will be set to the current DAQ system time
     */
    private void sendValueWithTimeDeadbandCheck(final SourceDataTag sourceDataTag, 
                                                final Object newValue, 
                                                final String newTagValueDesc, 
                                                final SourceDataQuality newSDQuality,
                                                final Timestamp timestamp) {
      
      // TimeDeadband for the current Data Tag (Static or Dynamic since this variable can be enabled at runtime when the Dynamic 
      // filter gets enabled)
      if (sourceDataTag.getAddress().isTimeDeadbandEnabled()) {
        this.equipmentLogger.debug("sendInvalidTag - passing update to time-deadband scheduler for tag " + sourceDataTag.getId());
        this.equipmentTimeDeadband.addToTimeDeadband(sourceDataTag, newValue, timestamp.getTime(), newTagValueDesc, newSDQuality);
      } else {
        if (this.equipmentTimeDeadband.getSdtTimeDeadbandSchedulers().containsKey(sourceDataTag.getId())) {
          this.equipmentLogger.debug("sendInvalidTag - remove time-deadband scheduler for tag " + sourceDataTag.getId());
          this.equipmentTimeDeadband.removeFromTimeDeadband(sourceDataTag);
        }

        // All checks and filters are done
        this.equipmentLogger.debug(format("sendInvalidTag - invalidating and sending invalid tag (%d) update to the server", sourceDataTag.getId()));

        SourceDataTagValue newSDValue = sourceDataTag.update(newSDQuality, newValue, newTagValueDesc, timestamp);
        // Special case Quality OK     
        if (newSDValue == null) {
          // this means we have a valid quality code 0 (OK)
          this.equipmentLogger.warn("sendInvalidTag - method called with 0(OK) quality code for tag " + sourceDataTag.getId()
              + ". This should normally not happen! sendTagFiltered() method should have been called before.");
        }
        else {
          this.processMessageSender.addValue(newSDValue);
          
          // Checks if the dynamic TimeDeadband filter is enabled, Static disable and record it depending on the priority
          this.dynamicTimeDeadbandFilterer.recordTag(sourceDataTag);
        }
      }
    }
}
