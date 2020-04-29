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
import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.daq.common.messaging.IProcessMessageSender;
import cern.c2mon.daq.tools.DataTagValueFilter;
import cern.c2mon.daq.tools.DataTagValueValidator;
import cern.c2mon.shared.common.datatag.*;
import cern.c2mon.shared.common.filter.FilteredDataTagValue.FilterType;
import cern.c2mon.shared.common.type.TypeConverter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static cern.c2mon.shared.common.type.TypeConverter.*;
import static java.lang.String.format;

/**
 * This class is used to send valid messages to the server.
 *
 * @author vilches
 * @author Franz Ritter
 */
@Slf4j
class EquipmentSenderValid {

  private static ObjectMapper mapper = new ObjectMapper();

  static {
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  }

  /**
   * The process message sender takes the messages actually send to the server.
   */
  private final IProcessMessageSender processMessageSender;

  /**
   * Filters for Data Tag outgoing Values
   */
  private final DataTagValueFilter dataTagValueFilter;

  /**
   * Validation for Data Tag outgoing Values
   */
  private final DataTagValueValidator dataTagValueValidator;

  /**
   * Invalid Sender
   */
  private final IEquipmentMessageSender equipmentSender;

  /**
   * The dynamic time dead band filterer for recording the current source data tag
   */
  private final IDynamicTimeDeadbandFilterer dynamicTimeDeadbandFilterer;

  /**
   * Filter module Sender
   */
  private final EquipmentSenderFilterModule equipmentSenderFilterModule;

  /**
   * Time deadband helper class
   */
  private final EquipmentTimeDeadband equipmentTimeDeadband;


  /**
   * Creates a new EquipmentValidSender.
   *
   * @param filterMessageSender         The filter message sender to send filtered tag values.
   * @param processMessageSender        The process message sender to send tags to the server.
   * @param dynamicTimeDeadbandFilterer
   */
  public EquipmentSenderValid(final EquipmentSenderFilterModule equipmentSenderFilterModule,
                              final IProcessMessageSender processMessageSender,
                              final IEquipmentMessageSender equipmentSender,
                              final EquipmentTimeDeadband equipmentTimeDeadband,
                              final IDynamicTimeDeadbandFilterer dynamicTimeDeadbandFilterer) {

    this.equipmentSenderFilterModule = equipmentSenderFilterModule;
    this.processMessageSender = processMessageSender;
    this.equipmentSender = equipmentSender;
    this.equipmentTimeDeadband = equipmentTimeDeadband;
    this.dynamicTimeDeadbandFilterer = dynamicTimeDeadbandFilterer;

    this.dataTagValueFilter = new DataTagValueFilter();
    this.dataTagValueValidator = new DataTagValueValidator();
  }

  /**
   * This method decides how the tag should be sent to the server.
   * If the value is an array or an arbitrary object the sending changes as it is not a primitive type.
   *
   * @param currentTag       The tag to which the value belongs.
   * @param sourceTimestamp  The timestamp of the tag.
   * @param newTagValue      The tag value to send.
   * @param valueDescription A description belonging to the value.
   * @return True if the tag has been send successfully to the server.
   * False if the tag has been invalidated or filtered out.
   */
  public boolean update(final SourceDataTag currentSourceDataTag, final ValueUpdate update) {
    boolean successfullySent = false;
    log.trace("update - entering update()");

    try {

      successfullySent = doUpdate(currentSourceDataTag, update);

    } catch (Exception ex) {
      log.error("update - Unexpected exception caught for tag " + currentSourceDataTag.getId() + ", " + ex.getStackTrace(), ex);

      SourceDataTagQuality quality = new SourceDataTagQuality(SourceDataTagQualityCode.UNKNOWN, "Could not send incoming valid source update to server: " + ex.getMessage());
      this.equipmentSender.update(currentSourceDataTag.getId(), quality, update.getSourceTimestamp());
    }

    log.trace("update - leaving update()");
    return successfullySent;
  }


  /**
   * Internal method to send only Tags with primitive Types to the server.
   * This method is called by every other sendTag method of this class.
   *
   * @return True if the tag has been send successfully to the server.
   * False if the tag has been invalidated or filtered out.
   */
  private boolean doUpdate(final SourceDataTag currentSourceDataTag, final ValueUpdate update) {
    // do a validation check on the new value:
    if (!checkValidation(currentSourceDataTag, update)) {
      return false; //TODO Check, if that case is correctly treated by upper logic
    }

    // cast the value to the defined dataType if the type is not 'ArbitraryObject':
    if (isKnownClass(currentSourceDataTag.getDataType())) {
      Object newValueCasted = cast(update.getValue(), currentSourceDataTag.getDataType());
      if (newValueCasted != null) {
        update.setValue(newValueCasted);
      }
    }

    // do a filtering on the new value:
    if (!checkFiltering(currentSourceDataTag, update)) {
      return false;
    }

    // check if the new value is in a time deadband:
    if (!checkTimeDeadband(currentSourceDataTag, update)) {
      return false;
    }

    // All checks and filters are successful, send the tag to the server:
    SourceDataTagValue tagValue = currentSourceDataTag.update(update);
    try {
      this.processMessageSender.addValue(tagValue);
    } catch (InterruptedException e) {
      log.error("Data could not be sent and is lost!: {}", tagValue);
    }

    // Checks if the dynamic TimeDeadband filter is enabled, Static disable and record it depending on the priority
    this.dynamicTimeDeadbandFilterer.recordTag(currentSourceDataTag);

    // no validation detected --> dataValue is okay return true
    return true;
  }


  /**
   * Helper method which validates the new tag value and triggers an invalidation in case of a problem
   *
   * @return false, if validation was unsuccessful
   */
  private boolean checkValidation(final SourceDataTag currentSourceDataTag, final ValueUpdate update) {

    // check if the timestamp is valid.
    if (!isTimestampValid(currentSourceDataTag, update)) {
      return false;
    }

    // If the DataType is not an arbitrary object check if the Type if the value is convertible.
    if (!isConvertible(currentSourceDataTag, update)) {
      return false;
    }

    // if the dataType is a number check if the value is convertible and in the defined range.
    if (isNumber(currentSourceDataTag.getDataType())
        && !isInRange(currentSourceDataTag, update)) {
      return false;
    }

    return true;
  }

  /**
   * Helper method which evaluate if a filter for the new value is triggered.
   */
  private boolean checkFiltering(final SourceDataTag currentSourceDataTag, final ValueUpdate castedUpdate) {
    // New quality needed for comparing
    SourceDataTagQuality newSDQuality = new SourceDataTagQuality();

    // is Candidate for filtering?
    FilterType filterType = this.dataTagValueFilter.isCandidateForFiltering(currentSourceDataTag, castedUpdate, newSDQuality);

    log.debug("checkFiltering - tag #" + currentSourceDataTag.getId() + " with Filter Type " + filterType);
    // Check filters on (OLD_UPDATE, VALUE_DEADBAND, REPEATED_VALUE or none)
    if (!isFilterOk(filterType, currentSourceDataTag.getId())) {

      // send filtered message to statistics module
      this.equipmentSenderFilterModule.sendToFilterModule(currentSourceDataTag, castedUpdate, filterType.getNumber());

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
   * @param newValueCasted       The casted new value of the tag.
   * @param sourceTimestamp      The timestamp of the new value.
   * @param pValueDescr          A description belonging to the value.
   * @return If there is an deadBand according to the value the method returns false.
   */
  private boolean checkTimeDeadband(final SourceDataTag currentSourceDataTag, final ValueUpdate castedUpdate) {

    if (currentSourceDataTag.getAddress().isTimeDeadbandEnabled()) {
      log.debug("checkTimeDeadband - passing update to time-deadband scheduler for tag #{}", currentSourceDataTag.getId());
      this.equipmentTimeDeadband.addToTimeDeadband(currentSourceDataTag, castedUpdate);

      // Deadband detected --> DataValue is not okay, return false.
      return false;

    } else {
      if (this.equipmentTimeDeadband.getSdtTimeDeadbandSchedulers().containsKey(currentSourceDataTag.getId())) {
        log.debug("checkTimeDeadband - remove time-deadband scheduler for tag #{}", currentSourceDataTag.getId());
        this.equipmentTimeDeadband.removeFromTimeDeadband(currentSourceDataTag);
      }

      // No deadband detected -> DataValue is not okay, return true
      return true;

    }
  }

  /**
   * Helper method which evaluate the filter status.
   *
   * @param filterType The type of the filter.
   * @param tagId      the id of the tag which belongs to the filter status.
   * @return If the Filter is not ok the method returns false.
   */
  private boolean isFilterOk(final FilterType filterType, final Long tagId) {
    if (filterType != FilterType.NO_FILTERING) {
      // OLD_UPDATE filter on
      if ((filterType == FilterType.OLD_UPDATE)) {
          log.debug("old update filtering : #{} update was filtered out because the new value timestamp is equal or older than the current value timestamp "
                  + " and the current value has Good Quality or both new and current value has Bad Quality.", tagId);

          log.debug("isFilterOk - sending value to statistics module: old update Filter");
        // VALUE_DEADBAND filter on
      } else if ((filterType == FilterType.VALUE_DEADBAND)) {
        log.debug("value-deadband filtering : the value of tag #{} was filtered out due to value-deadband filtering rules and will not be sent to the server",
              tagId);

        log.debug("isFilterOk - sending value to statistics module: Value Deadband Filter");
        // REPEATED_VALUE filter on
      } else if ((filterType == FilterType.REPEATED_VALUE)) {
        log.debug("trying to send twice the same tag #{} update (with exactly the same value and value description).", tagId);

        log.debug("sendTagFiltered - sending value to statistics module: Same Value Filter");
        log.debug("isFilterOk - sending value to statistics module: Same Value Filter");
      }
      // return false because the filter is not ok
      return false;
    } else {
      // return false because the filter is ok
      return true;
    }
  }

  /**
   * Sends all through timedeadband delayed values immediately
   */
  public void sendDelayedTimeDeadbandValues() {
    log.debug("Sending all time deadband delayed values to the server");

    this.equipmentTimeDeadband.sendDelayedTimeDeadbandValues();
  }


  private boolean isTimestampValid(final SourceDataTag currentSourceDataTag, final ValueUpdate update) {
    boolean result = false;

    if (!this.dataTagValueValidator.isTimestampValid(update.getSourceTimestamp())) {
      log.warn(
          format("\tvalid timestamp : the timestamp of tag[%d] is out of range (in the future)",
              currentSourceDataTag.getId()));

      log.debug(format("\tinvalidating tag [%d] with quality FUTURE_SOURCE_TIMESTAMP", currentSourceDataTag.getId()));

      SourceDataTagQuality quality = new SourceDataTagQuality(SourceDataTagQualityCode.FUTURE_SOURCE_TIMESTAMP, "Value received with source timestamp in the future!");
      // Send Invalid Tag
      this.equipmentSender.update(currentSourceDataTag.getId(), update, quality);

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
   * <p>
   * If both fail, the value is invalid and the method returns false.
   *
   * @param currentSourceDataTag The current {@link SourceDataTag} known by the daq.
   * @param newTagValue          the new tag value.
   * @param sourceTimestamp      actual sourceTimeStamp.
   * @return True if the new value is Convertible.
   */
  private boolean isConvertible(final SourceDataTag currentSourceDataTag, final ValueUpdate update) {
    boolean result = false;

    if ((isKnownClass(currentSourceDataTag.getDataType())
        && !(this.dataTagValueValidator.isConvertible(currentSourceDataTag, update.getValue())
        || isInstantiable(update.getValue(), getType(currentSourceDataTag.getDataType()))))) {
      String description = format(
          "\tconvertible : The value (%s) received for tag[%d] and the tag's type (" + currentSourceDataTag.getDataType() + ") are not compatible.",
          update.getValue(), currentSourceDataTag.getId());

      log.warn(description);
      log.debug(format("\tinvalidating tag[%d] with quality CONVERSION_ERROR", currentSourceDataTag.getId()));

      SourceDataTagQuality quality = new SourceDataTagQuality(SourceDataTagQualityCode.CONVERSION_ERROR, description);
      this.equipmentSender.update(currentSourceDataTag.getId(), quality, update.getSourceTimestamp());

      // Remove tags which are out of their range
    } else {
      result = true;
    }
    return result;
  }

  /**
   * Helper method which removes values which are out of their range.
   *
   * @return If its in range return true.
   */
  private boolean isInRange(final SourceDataTag currentSourceDataTag, final ValueUpdate update) {
    boolean result = false;

    if (!this.dataTagValueValidator.isInRange(currentSourceDataTag, update.getValue())) {
      log.warn(format(
          "\tin range : the value of tag[%d] was out of range and will only be propagated the first time to the server",
          currentSourceDataTag.getId()));
      log.debug(format("\tinvalidating tag[%d] with quality OUT_OF_BOUNDS", currentSourceDataTag.getId()));
      StringBuffer qDesc = new StringBuffer("source value is out of bounds (");
      if (currentSourceDataTag.getMinValue() != null)
        qDesc.append("min: ").append(currentSourceDataTag.getMinValue()).append(" ");
      if (currentSourceDataTag.getMaxValue() != null)
        qDesc.append("max: ").append(currentSourceDataTag.getMaxValue());
      qDesc.append(")");

      SourceDataTagQuality quality = new SourceDataTagQuality(SourceDataTagQualityCode.OUT_OF_BOUNDS, qDesc.toString());

      // Send Invalid Tag
      this.equipmentSender.update(currentSourceDataTag.getId(), update, quality);

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
