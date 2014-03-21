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

import cern.c2mon.daq.common.IDynamicTimeDeadbandFilterer;
import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.common.logger.EquipmentLoggerFactory;
import cern.c2mon.daq.common.messaging.IProcessMessageSender;
import cern.c2mon.daq.common.vcm.ValueChangeMonitorEngine;
import cern.c2mon.daq.common.vcm.ValueChangeMonitorEvent;
import cern.c2mon.daq.tools.DataTagValueFilter;
import cern.c2mon.daq.tools.DataTagValueValidator;
import cern.c2mon.daq.tools.EquipmentSenderHelper;
import cern.c2mon.shared.daq.datatag.SourceDataQuality;
import cern.c2mon.shared.daq.datatag.SourceDataTag;
import cern.c2mon.shared.daq.filter.FilteredDataTagValue.FilterType;

/**
 * This class is used to send valid messages to the server.
 * 
 * @author vilches
 *
 */
class EquipmentSenderValid {
	
	/**
	 * The logger for this class.
	 */
	private EquipmentLogger equipmentLogger;

    /**
     * The process message sender takes the messages actually send to the server.
     */
    private IProcessMessageSender processMessageSender;

    /**
     * Filters for Data Tag outgoing Values
     */    
    private DataTagValueFilter dataTagValueFilter;
    
    /**
     * Validation for Data Tag outgoing Values
     */    
    private DataTagValueValidator dataTagValueValidator;
    
    /**
     * Invalid Sender
     */
    private EquipmentSenderInvalid equipmentSenderInvalid;
    
    /**
     * The equipment sender helper with many common and useful methods shared by sending classes
     */
    private EquipmentSenderHelper equipmentSenderHelper = new EquipmentSenderHelper();
    
    /**
     * The dynamic time dead band filterer for recording the current source data tag
     */
    private IDynamicTimeDeadbandFilterer dynamicTimeDeadbandFilterer;
    
    /**
     * Filter module Sender
     */
    private EquipmentSenderFilterModule equipmentSenderFilterModule;
    
    /**
     * Time deadband helper class
     */
    private EquipmentTimeDeadband equipmentTimeDeadband;
    

    /**
     * Creates a new EquipmentValidSender.
     * 
     * @param filterMessageSender The filter message sender to send filtered tag values.
     * @param processMessageSender The process message sender to send tags to the server.
     * @param dynamicTimeDeadbandFilterer 
     * @param equipmentLoggerFactory 
     */
    public EquipmentSenderValid (final EquipmentSenderFilterModule equipmentSenderFilterModule,
                                 final IProcessMessageSender processMessageSender, 
                                 final EquipmentSenderInvalid equipmentSenderInvalid, 
                                 final EquipmentTimeDeadband equipmentTimeDeadband,
                                 final IDynamicTimeDeadbandFilterer dynamicTimeDeadbandFilterer,
                                 final EquipmentLoggerFactory equipmentLoggerFactory) {
   
        this.equipmentSenderFilterModule = equipmentSenderFilterModule;
        this.processMessageSender = processMessageSender;
        this.equipmentSenderInvalid = equipmentSenderInvalid;
        this.equipmentTimeDeadband = equipmentTimeDeadband;
        this.dynamicTimeDeadbandFilterer = dynamicTimeDeadbandFilterer;
        this.equipmentLogger = equipmentLoggerFactory.getEquipmentLogger(getClass());
        
        this.dataTagValueFilter = new DataTagValueFilter(equipmentLoggerFactory);
        this.dataTagValueValidator = new DataTagValueValidator(equipmentLoggerFactory);
    }
    
    /**
     * Tries to send a new value to the server.
     * 
     * @param currentTag The tag to which the value belongs.
     * @param milisecTimestamp The timestamp of the tag.
     * @param tagValue The tag value to send.
     * @return True if the tag has been send successfully to the server.
     * False if the tag has been invalidated or filtered out.
     */
    public boolean sendTagFiltered(final SourceDataTag currentTag, final Object tagValue, final long milisecTimestamp) {
        return sendTagFiltered(currentTag, tagValue, milisecTimestamp, null);
    }

    /**
     * Tries to send a new value to the server.
     * 
     * @param currentTag The tag to which the value belongs.
     * @param tagValue The tag value to send.
     * @param milisecTimestamp The timestamp of the tag.
     * @param pValueDescr A description belonging to the value.
     * @return True if the tag has been send successfully to the server.
     * False if the tag has been invalidated or filtered out.
     */
    public boolean sendTagFiltered(final SourceDataTag currentTag, final Object tagValue, final long milisecTimestamp,
            String pValueDescr) {
        return sendTagFiltered(currentTag, tagValue, milisecTimestamp, pValueDescr, false);
    }
    
    /**
     * Tries to send a new value to the server.
     * 
     * @param currentTag The tag to which the value belongs.
     * @param milisecTimestamp The timestamp of the tag.
     * @param newTagValue The tag value to send.
     * @param pValueDescr A description belonging to the value.
     * @return True if the tag has been send successfully to the server. 
     * False if the tag has been invalidated or filtered out.
     */
    public boolean sendTagFiltered(final SourceDataTag currentSourceDataTag, final Object newTagValue, 
        final long milisecTimestamp, String pValueDescr, boolean sentByValueCheckMonitor) {

      this.equipmentLogger.trace("sendTagFiltered - entering sendTagFiltered()");

      boolean successfulSent = false;  	

      try {
        // Remove tags with invalid timestamps
        if (!this.dataTagValueValidator.isTimestampValid(milisecTimestamp)) {
          equipmentLogger
          .warn(format(
              "\tvalid timestamp : the timestamp of tag[%d] is out of range (in the future)",
              currentSourceDataTag.getId()));

          equipmentLogger.debug(format("\tinvalidating tag [%d] with quality FUTURE_SOURCE_TIMESTAMP", currentSourceDataTag.getId()));

          // Get the source data quality from the quality code and description
          SourceDataQuality newSDQuality = this.equipmentSenderHelper.createTagQualityObject(SourceDataQuality.FUTURE_SOURCE_TIMESTAMP, 
              "Value received with source timestamp in the future! Please inform the data source responsible about this issue.");

          // Send Invalid Tag
          this.equipmentSenderInvalid.sendInvalidTag(currentSourceDataTag, newTagValue, pValueDescr, newSDQuality, new Timestamp(milisecTimestamp));

          // if tag has value checker monitor registered
        } else if (currentSourceDataTag.hasValueCheckMonitor() && !sentByValueCheckMonitor) {

          if (newTagValue instanceof Number) {
            ValueChangeMonitorEngine.getInstance().sendEvent(
                new ValueChangeMonitorEvent(currentSourceDataTag.getId(), ((Number) newTagValue).doubleValue(), pValueDescr,
                    milisecTimestamp));
          } else if (newTagValue instanceof Boolean) {
            Boolean v = (Boolean) newTagValue;
            ValueChangeMonitorEngine.getInstance().sendEvent(
                new ValueChangeMonitorEvent(currentSourceDataTag.getId(), v.booleanValue() == true ? 1 : 0, pValueDescr,
                    milisecTimestamp));
          } else {
            // for strings the value is not important - we can anyway just monitor if the events arrive in regular
            // time-windows
            ValueChangeMonitorEngine.getInstance()
            .sendEvent(
                new ValueChangeMonitorEvent(currentSourceDataTag.getId(), 0, pValueDescr,
                    milisecTimestamp));
          }
          
          successfulSent = true;

          // Remove tags which have not convertible values
        } else if (!this.dataTagValueValidator.isConvertible(currentSourceDataTag, newTagValue)) {
          String description = format(
              "\tconvertible : The value (%s) received for tag[%d] and the tag's type ("+currentSourceDataTag.getDataType()+") are not compatible.",
              newTagValue, currentSourceDataTag.getId());

          this.equipmentLogger.warn(description);
          this.equipmentLogger.debug(format("\tinvalidating tag[%d] with quality CONVERSION_ERROR", currentSourceDataTag.getId()));

          this.equipmentSenderInvalid.sendInvalidTag(currentSourceDataTag, SourceDataQuality.CONVERSION_ERROR, description, new Timestamp(milisecTimestamp));

          // Remove tags which are out of their range
        } else if (!this.dataTagValueValidator.isInRange(currentSourceDataTag, newTagValue)) {
          this.equipmentLogger.warn(format(
              "\tin range : the value of tag[%d] was out of range and will only be propagated the first time to the server",
              currentSourceDataTag.getId()));
          this.equipmentLogger.debug(format("\tinvalidating tag[%d] with quality OUT_OF_BOUNDS", currentSourceDataTag.getId()));

          StringBuffer qDesc = new StringBuffer("source value is out of bounds (");
          if (currentSourceDataTag.getMinValue() != null)
            qDesc.append("min: ").append(currentSourceDataTag.getMinValue()).append(" ");
          if (currentSourceDataTag.getMaxValue() != null)
            qDesc.append("max: ").append(currentSourceDataTag.getMaxValue());

          // Get the source data quality from the quality code and description
          SourceDataQuality newSDQuality = this.equipmentSenderHelper.createTagQualityObject(SourceDataQuality.OUT_OF_BOUNDS, qDesc.toString());

          // Send Invalid Tag
          this.equipmentSenderInvalid.sendInvalidTag(currentSourceDataTag, newTagValue, pValueDescr, newSDQuality, new Timestamp(milisecTimestamp));

          // Checks for Value_Deadband and Same_Value filtering => isCandidateForFiltering
        } else {
          // New quality needed for comparing
          SourceDataQuality newSDQuality = this.equipmentSenderHelper.createTagQualityObject(SourceDataQuality.OK, "");

          // is Candidate for filtering?
          FilterType filterType = this.dataTagValueFilter.isCandidateForFiltering(currentSourceDataTag, newTagValue, pValueDescr, 
                newSDQuality, milisecTimestamp); 
          
          // Check filters on (VALUE_DEADBAND, REPEATED_VALUE or none)
          if (filterType != FilterType.NO_FILTERING) {
            // OLD_UPDATE filter on
            if ((filterType == FilterType.OLD_UPDATE)) {
              this.equipmentLogger.debug(format(
                  "\told update filtering : [%d] update was filtered out because the new value timestamp is equal or older than the current value timestamp " 
                      + " and the current value has Good Quality or both new and current value has Bad Quality.",
                  currentSourceDataTag.getId()));

              this.equipmentLogger.debug("sendTagFiltered - sending value to statistics module: old update Filter");
            // VALUE_DEADBAND filter on
            } else if ((filterType == FilterType.VALUE_DEADBAND)) {
              this.equipmentLogger.debug(format(
                  "\tvalue-deadband filtering : the value of tag [%d] was filtered out due to value-deadband filtering rules and will not be sent to the server",
                  currentSourceDataTag.getId()));

              this.equipmentLogger.debug("sendTagFiltered - sending value to statistics module: Value Deadband Filter");
              // REPEATED_VALUE filter on
            } else if ((filterType == FilterType.REPEATED_VALUE)) {
              this.equipmentLogger.debug(format(
                  "\ttrying to send twice the same tag [%d] update (with exactly the same value and value description).",
                  currentSourceDataTag.getId()));

              this.equipmentLogger.debug("sendTagFiltered - sending value to statistics module: Same Value Filter");
            }

            // send filtered message to statistics module
            this.equipmentSenderFilterModule.sendToFilterModule(currentSourceDataTag, newTagValue, milisecTimestamp, pValueDescr, 
                filterType.getNumber());

            // NO_FILTERING => TimeDeadband for the current Data Tag (Static or Dynamic since this variable can be enabled 
            // at runtime when the Dynamic filter gets enabled)
          } else if (currentSourceDataTag.getAddress().isTimeDeadbandEnabled()) {
            this.equipmentLogger.debug("sendTagFiltered - passing update to time-deadband scheduler for tag " + currentSourceDataTag.getId());
            this.equipmentTimeDeadband.addToTimeDeadband(currentSourceDataTag, newTagValue, milisecTimestamp, pValueDescr);
            // NO_FILTERING and NO Time Deadband => Send to the server
          } else {
            if (this.equipmentTimeDeadband.getSdtTimeDeadbandSchedulers().containsKey(currentSourceDataTag.getId())) {
              this.equipmentLogger.debug("sendInvalidTag - remove time-deadband scheduler for tag " + currentSourceDataTag.getId());
              this.equipmentTimeDeadband.removeFromTimeDeadband(currentSourceDataTag);
            }

            // All checks and filters are done
            sendTag(newTagValue, milisecTimestamp, pValueDescr, currentSourceDataTag);

            // Checks if the dynamic TimeDeadband filter is enabled, Static disable and record it depending on the priority
            this.dynamicTimeDeadbandFilterer.recordTag(currentSourceDataTag);

            successfulSent = true;
          }
        }
      } catch (Exception ex) {
        this.equipmentLogger.error("sendTagFiltered - Unexpected exception caught for tag " + currentSourceDataTag.getId() + ", " + ex.getStackTrace(), ex);
        
        this.equipmentSenderInvalid.sendInvalidTag(currentSourceDataTag, SourceDataQuality.UNKNOWN, 
            "Could not send incoming valid source update to server: " + ex.getMessage() + ". Please review tag configuration.");
        
      }

      this.equipmentLogger.trace("sendTagFiltered - leaving sendTagFiltered()");
    	return successfulSent;
    }

    /**
     * Updates the tag value and sends it. This method should be only used in core.
     * 
     * @param tagValue The new value of the tag.
     * @param milisecTimestamp The timestamp to use.
     * @param pValueDescr The description of the value.
     * @param tag The tag to update.
     */
    public void sendTag(final Object tagValue, final long milisecTimestamp, final String pValueDescr,
            final SourceDataTag tag) {
        this.processMessageSender.addValue(tag.update(this.equipmentSenderHelper.convertValue(tag, tagValue), pValueDescr, 
        		new Timestamp(milisecTimestamp)));
    }

    /**
     * Sends all through timedeadband delayed values immediately
     */
    public void sendDelayedTimeDeadbandValues() {
        equipmentLogger.debug("Sending all time deadband delayed values to the server");
        
        this.equipmentTimeDeadband.sendDelayedTimeDeadbandValues();
    }
    
    /**
     * Get the Equipment Time Deadband
     * 
     * @return equipmentTimeDeadband
     */
    public EquipmentTimeDeadband getEquipmentTimeDeadband() {
    	return equipmentTimeDeadband;
    }
}
