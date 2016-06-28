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

import cern.c2mon.daq.common.IDynamicTimeDeadbandFilterer;
import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.common.logger.EquipmentLoggerFactory;
import cern.c2mon.daq.common.messaging.IProcessMessageSender;
import cern.c2mon.daq.tools.DataTagValueFilter;
import cern.c2mon.daq.tools.DataTagValueValidator;
import cern.c2mon.daq.tools.EquipmentSenderHelper;
import cern.c2mon.shared.common.datatag.SourceDataQuality;
import cern.c2mon.shared.common.datatag.SourceDataTag;
import cern.c2mon.shared.common.filter.FilteredDataTagValue.FilterType;
import cern.c2mon.shared.common.type.TypeConverter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.sql.Timestamp;

import static cern.c2mon.shared.common.type.TypeConverter.*;
import static java.lang.String.format;

/**
 * This class is used to send valid messages to the server.
 *
 * @author vilches
 * @author Franz Ritter
 *
 */
class EquipmentSenderValid {

  private static ObjectMapper mapper = new ObjectMapper();

  static {
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  }

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
  public EquipmentSenderValid(final EquipmentSenderFilterModule equipmentSenderFilterModule,
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
   * @param sourceTimestamp The source timestamp of the tag.
   * @param tagValue The tag value to send.
   * @return True if the tag has been send successfully to the server.
   * False if the tag has been invalidated or filtered out.
   */
  public boolean sendTagFiltered(final SourceDataTag currentTag, final Object tagValue, final long sourceTimestamp) {
    return sendTagFiltered(currentTag, tagValue, sourceTimestamp, null);
  }

  /**
   * This method decides how the tag should be sent to the server.
   * If the value is an array or an arbitrary object the sending changes as it is not a primitive type.
   *
   * @param currentTag The tag to which the value belongs.
   * @param sourceTimestamp The timestamp of the tag.
   * @param newTagValue The tag value to send.
   * @param pValueDescr A description belonging to the value.
   * @return True if the tag has been send successfully to the server.
   * False if the tag has been invalidated or filtered out.
   */
  public boolean sendTagFiltered(final SourceDataTag currentSourceDataTag, final Object newTagValue,
                                 final long sourceTimestamp, String pValueDescr) {
    boolean successfullySent = false;
    this.equipmentLogger.trace("sendTagFiltered - entering sendTagFiltered()");

    try {

      successfullySent = doSendTagFiltered(currentSourceDataTag, newTagValue, sourceTimestamp, pValueDescr);

    } catch (Exception ex) {
      this.equipmentLogger.error("sendTagFiltered - Unexpected exception caught for tag " + currentSourceDataTag.getId() + ", " + ex.getStackTrace(), ex);

      this.equipmentSenderInvalid.sendInvalidTag(currentSourceDataTag, SourceDataQuality.UNKNOWN,
          "Could not send incoming valid source update to server: " + ex.getMessage() + ". Please review tag configuration.");

    }

    this.equipmentLogger.trace("sendTagFiltered - leaving sendTagFiltered()");
    return successfullySent;
  }


  /**
   * Internal method to send only Tags with primitive Types to the server.
   * This method is called by every other sendTag method of this class.
   *
   * @param currentTag The tag to which the value belongs.
   * @param sourceTimestamp The timestamp of the tag.
   * @param newTagValue The tag value to send.
   * @param pValueDescr A description belonging to the value.
   * @return True if the tag has been send successfully to the server.
   * False if the tag has been invalidated or filtered out.
   */
  private boolean doSendTagFiltered(final SourceDataTag currentSourceDataTag, final Object newTagValue,
                                    final long sourceTimestamp, String pValueDescr) {
    Object newValueCasted;

    // do a validation check on the new value:
    if (!checkValidation(currentSourceDataTag, newTagValue, sourceTimestamp, pValueDescr)) {
      return false;
    }


    // cast the value to the defined dataType if the type is not 'ArbitraryObject':
    if (isKnownClass(currentSourceDataTag.getDataType())) {
      newValueCasted = TypeConverter.isConvertible(newTagValue, currentSourceDataTag.getDataType()) ?
          cast(newTagValue, currentSourceDataTag.getDataType()) : instantiate(newTagValue, getType(currentSourceDataTag.getDataType()));
    } else {
      newValueCasted = newTagValue;
    }

    // do a filtering on the new value:
    if (!checkFiltering(currentSourceDataTag, newValueCasted, sourceTimestamp, pValueDescr)) {
      return false;
    }

    // check if the new value is in a time deadband:
    if (!checkTimeDeadband(currentSourceDataTag, newValueCasted, sourceTimestamp, pValueDescr)) {
      return false;
    }

    // All checks and filters are successful, send the tag to the server:
    sendTag(newValueCasted, sourceTimestamp, pValueDescr, currentSourceDataTag);

    // Checks if the dynamic TimeDeadband filter is enabled, Static disable and record it depending on the priority
    this.dynamicTimeDeadbandFilterer.recordTag(currentSourceDataTag);

    // no validation detected --> dataValue is okay return true
    return true;
  }


  /**
   * Helper method which validates the new tag value.
   *
   * @param currentTag The tag to which the value belongs.
   * @param sourceTimestamp The timestamp of the tag.
   * @param newTagValue The tag value to send.
   * @param pValueDescr A description belonging to the value.
   * @return If something is invalid the method returns false.
   */
  private boolean checkValidation(final SourceDataTag currentSourceDataTag, final Object newTagValue,
                                  final long sourceTimestamp, String pValueDescr) {

    // check if the timestamp is valid.
    if (!isTimestampValid(currentSourceDataTag, newTagValue, sourceTimestamp, pValueDescr)) {
      return false;
    }

    // If the DataType is not an arbitrary object check if the Type if the value is convertible.
    if (!isConvertible(currentSourceDataTag, newTagValue, sourceTimestamp)) {
      return false;
    }

    // if the dataType is a number check if the value is convertible and in the defined range.
    if (isNumber(currentSourceDataTag.getDataType())
        && !isInRange(currentSourceDataTag, newTagValue, sourceTimestamp, pValueDescr)) {
      return false;
    }

    return true;
  }

  /**
   * Helper method which evaluate if a filter for the new value is triggered.
   *
   * @param currentSourceDataTag The tag to which the value belongs.
   * @param newValueCasted The casted new value of the tag.
   * @param sourceTimestamp The timestamp of the new value.
   * @param pValueDescr A description belonging to the value.
   * @return
   */
  private boolean checkFiltering(final SourceDataTag currentSourceDataTag, final Object newValueCasted,
                                 final long sourceTimestamp, String pValueDescr) {
    // New quality needed for comparing
    SourceDataQuality newSDQuality = this.equipmentSenderHelper.createTagQualityObject(SourceDataQuality.OK, "");

    // is Candidate for filtering?
    FilterType filterType = this.dataTagValueFilter.isCandidateForFiltering(currentSourceDataTag, newValueCasted, pValueDescr,
        newSDQuality, sourceTimestamp);

    this.equipmentLogger.debug("sendTagFiltered - tag #" + currentSourceDataTag.getId() + " with Filter Type " + filterType);

    // Check filters on (OLD_UPDATE, VALUE_DEADBAND, REPEATED_VALUE or none)
    if (!isFilterOk(filterType, currentSourceDataTag.getId())) {

      // send filtered message to statistics module
      this.equipmentSenderFilterModule.sendToFilterModule(currentSourceDataTag, newValueCasted, sourceTimestamp, pValueDescr,
          filterType.getNumber());

      // filtering performed --> dataValue is not okay, return false.
      return false;
    } else {

      // no filtering performed --> dataValue is okay, return true.
      return true;
    }
  }

  /**
   * Helper method which checks if there is a time deadband according to the new value.
   *
   * @param currentSourceDataTag The tag to which the value belongs.
   * @param newValueCasted The casted new value of the tag.
   * @param sourceTimestamp The timestamp of the new value.
   * @param pValueDescr A description belonging to the value.
   * @return If there is an deadBand according to the value the method returns false.
   */
  private boolean checkTimeDeadband(final SourceDataTag currentSourceDataTag, final Object newValueCasted,
                                    final long sourceTimestamp, String pValueDescr) {

    if (currentSourceDataTag.getAddress().isTimeDeadbandEnabled()) {

      this.equipmentLogger.debug("sendTagFiltered - passing update to time-deadband scheduler for tag " + currentSourceDataTag.getId());
      this.equipmentTimeDeadband.addToTimeDeadband(currentSourceDataTag, newValueCasted, sourceTimestamp, pValueDescr);

      // Deadband detected --> DataValue is not okay, return false.
      return false;

    } else {
      if (this.equipmentTimeDeadband.getSdtTimeDeadbandSchedulers().containsKey(currentSourceDataTag.getId())) {
        this.equipmentLogger.debug("sendInvalidTag - remove time-deadband scheduler for tag " + currentSourceDataTag.getId());
        this.equipmentTimeDeadband.removeFromTimeDeadband(currentSourceDataTag);
      }

      // No deadband detected -> DataValue is not okay, return true
      return true;

    }
  }

  /**
   *
   * Helper method which evaluate the filter status.
   * @param filterType The type of the filter.
   * @param tagId the id of the tag which belongs to the filter status.
   * @return If the Filter is not ok the method returns false.
   */
  private boolean isFilterOk(final FilterType filterType, final Long tagId) {

    if (filterType != FilterType.NO_FILTERING) {
      // OLD_UPDATE filter on
      if ((filterType == FilterType.OLD_UPDATE)) {
        this.equipmentLogger.debug(format(
            "\told update filtering : [%d] update was filtered out because the new value timestamp is equal or older than the current value timestamp "
                + " and the current value has Good Quality or both new and current value has Bad Quality.",
            tagId));

        this.equipmentLogger.debug("sendTagFiltered - sending value to statistics module: old update Filter");

        // VALUE_DEADBAND filter on
      } else if ((filterType == FilterType.VALUE_DEADBAND)) {
        this.equipmentLogger.debug(format(
            "\tvalue-deadband filtering : the value of tag [%d] was filtered out due to value-deadband filtering rules and will not be sent to the server",
            tagId));

        this.equipmentLogger.debug("sendTagFiltered - sending value to statistics module: Value Deadband Filter");

        // REPEATED_VALUE filter on
      } else if ((filterType == FilterType.REPEATED_VALUE)) {
        this.equipmentLogger.debug(format(
            "\ttrying to send twice the same tag [%d] update (with exactly the same value and value description).",
            tagId));

        this.equipmentLogger.debug("sendTagFiltered - sending value to statistics module: Same Value Filter");
      }

      // return false because the filter is not ok
      return false;

    } else {

      // return false because the filter is ok
      return true;
    }

  }

  /**
   * Updates the tag value and sends it. This method should be only used in core.
   *
   * @param tagValue The new value of the tag.
   * @param sourceTimestamp The source timestamp in milliseconds.
   * @param pValueDescr The description of the value.
   * @param tag The tag to update.
   */
  public void sendTag(final Object tagValue, final long sourceTimestamp, final String pValueDescr,
                      final SourceDataTag tag) {
    this.processMessageSender.addValue(tag.update(tagValue, pValueDescr, new Timestamp(sourceTimestamp)));
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

  private boolean isTimestampValid(final SourceDataTag currentSourceDataTag, final Object newTagValue,
                                   final long sourceTimestamp, String pValueDescr) {
    boolean result = false;

    if (!this.dataTagValueValidator.isTimestampValid(sourceTimestamp)) {
      equipmentLogger
          .warn(format(
              "\tvalid timestamp : the timestamp of tag[%d] is out of range (in the future)",
              currentSourceDataTag.getId()));

      equipmentLogger.debug(format("\tinvalidating tag [%d] with quality FUTURE_SOURCE_TIMESTAMP", currentSourceDataTag.getId()));

      // Get the source data quality from the quality code and description
      SourceDataQuality newSDQuality = this.equipmentSenderHelper.createTagQualityObject(SourceDataQuality.FUTURE_SOURCE_TIMESTAMP,
          "Value received with source timestamp in the future! Please inform the data source responsible about this issue.");

      // Send Invalid Tag
      this.equipmentSenderInvalid.sendInvalidTag(currentSourceDataTag, newTagValue, pValueDescr, newSDQuality, new Timestamp(sourceTimestamp));

      // Remove tags which have not convertible values
    } else {
      result = true;
    }
    return result;
  }

  /**
   * Checks if the new value is convertible. That is the case if the defined class of the value is known by the daq
   * and the {@link TypeConverter} can cast the value.
   * If the {@link TypeConverter} cannot cast the value check also if the value can be instantiated through jackson.
   *
   * If both fail, the value is invalid and the method returns false.
   * @param currentSourceDataTag The current {@link SourceDataTag} known by the daq.
   * @param newTagValue the new tag value.
   * @param sourceTimestamp actual sourceTimeStamp.
   * @return True if the new value is Convertible.
   */
  private boolean isConvertible(final SourceDataTag currentSourceDataTag, final Object newTagValue,
                                final long sourceTimestamp) {
    boolean result = false;

    if ((isKnownClass(currentSourceDataTag.getDataType())
        && !(this.dataTagValueValidator.isConvertible(currentSourceDataTag, newTagValue)
        || isInstantiable(newTagValue, getType(currentSourceDataTag.getDataType()))))) {
      String description = format(
          "\tconvertible : The value (%s) received for tag[%d] and the tag's type (" + currentSourceDataTag.getDataType() + ") are not compatible.",
          newTagValue, currentSourceDataTag.getId());

      this.equipmentLogger.warn(description);
      this.equipmentLogger.debug(format("\tinvalidating tag[%d] with quality CONVERSION_ERROR", currentSourceDataTag.getId()));

      this.equipmentSenderInvalid.sendInvalidTag(currentSourceDataTag, SourceDataQuality.CONVERSION_ERROR, description, new Timestamp(sourceTimestamp));

      // Remove tags which are out of their range
    } else {
      result = true;
    }
    return result;
  }

  /**
   * Helper method which removes values which are out of their range.
   *
   * @param currentSourceDataTag The current {@link SourceDataTag} known by the daq.
   * @param newTagValue the new tag value.
   * @param sourceTimestamp actual sourceTimeStamp.
   * @param pValueDescr The description of the value.
   * @return If its in range return true.
   */
  private boolean isInRange(final SourceDataTag currentSourceDataTag, final Object newTagValue,
                            final long sourceTimestamp, String pValueDescr) {
    boolean result = false;

    if (!this.dataTagValueValidator.isInRange(currentSourceDataTag, newTagValue)) {
      this.equipmentLogger.warn(format(
          "\tin range : the value of tag[%d] was out of range and will only be propagated the first time to the server",
          currentSourceDataTag.getId()));
      this.equipmentLogger.debug(format("\tinvalidating tag[%d] with quality OUT_OF_BOUNDS", currentSourceDataTag.getId()));

      StringBuffer qDesc = new StringBuffer("source value is out of bounds (");
      if (currentSourceDataTag.getMinValue() != null)
        qDesc.append("min: ").append(currentSourceDataTag.getMinValue()).append(" ");
      if (currentSourceDataTag.getMaxValue() != null)
        qDesc.append("max: ").append(currentSourceDataTag.getMaxValue());
      qDesc.append(")");

      // Get the source data quality from the quality code and description
      SourceDataQuality newSDQuality = this.equipmentSenderHelper.createTagQualityObject(SourceDataQuality.OUT_OF_BOUNDS, qDesc.toString());

      // Send Invalid Tag
      this.equipmentSenderInvalid.sendInvalidTag(currentSourceDataTag, newTagValue, pValueDescr, newSDQuality, new Timestamp(sourceTimestamp));

      // Checks for Value_Deadband and Same_Value filtering => isCandidateForFiltering
    } else {
      result = true;
    }
    return result;
  }


  private static boolean isInstantiable(Object tagValue, final Class<?> dataType) {
    return instantiate(tagValue, dataType) != null;
  }

  private static Object instantiate(Object tagValue, final Class<?> dataType) {
    try {
      return mapper.readValue(mapper.writeValueAsString(tagValue), dataType);
    } catch (IOException e) {
      return null;
    }
  }
}
