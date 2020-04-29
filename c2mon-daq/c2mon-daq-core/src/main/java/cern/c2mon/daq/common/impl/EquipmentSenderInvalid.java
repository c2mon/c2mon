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
import cern.c2mon.daq.common.messaging.IProcessMessageSender;
import cern.c2mon.daq.tools.DataTagValueFilter;
import cern.c2mon.shared.common.datatag.SourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataTagQuality;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;
import cern.c2mon.shared.common.datatag.ValueUpdate;
import cern.c2mon.shared.common.filter.FilteredDataTagValue.FilterType;
import cern.c2mon.shared.common.type.TypeConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import static java.lang.String.format;

/**
 * This class is used to send invalid messages to the server.
 *
 * @author vilches
 */
@Slf4j
class EquipmentSenderInvalid {

  /**
   * The process message sender takes the messages actually send to the server.
   */
  private final IProcessMessageSender processMessageSender;

  /**
   * Filters for Data Tag outgoing Values
   */
  private final DataTagValueFilter dataTagValueFilter;

  /**
   * The dynamic time dead band filterer for recording the current source data
   * tag
   */
  private final IDynamicTimeDeadbandFilterer dynamicTimeDeadbandFilterer;

  /**
   * The class with the message sender to send filtered tag values
   */
  private final EquipmentSenderFilterModule equipmentSenderFilterModule;

  /**
   * Equipment Time Deadband
   */
  private final EquipmentTimeDeadband equipmentTimeDeadband;

  /**
   * Creates a new EquipmentInvalidSender.
   *
   * @param processMessageSender  The process message sender to send tags to the
   *                              server.
   * @param equipmentSenderHelper
   */
  @Autowired
  public EquipmentSenderInvalid(final EquipmentSenderFilterModule equipmentSenderFilterModule,
                                final IProcessMessageSender processMessageSender,
                                final EquipmentTimeDeadband equipmentTimeDeadband,
                                final IDynamicTimeDeadbandFilterer dynamicTimeDeadbandFilterer) {
    this.equipmentSenderFilterModule = equipmentSenderFilterModule;
    this.processMessageSender = processMessageSender;
    this.equipmentTimeDeadband = equipmentTimeDeadband;
    this.dynamicTimeDeadbandFilterer = dynamicTimeDeadbandFilterer;

    this.dataTagValueFilter = new DataTagValueFilter();
  }

  /**
   * This method sends both an invalid and updated SourceDataTagValue to the
   * server.
   *
   * @param sourceDataTag   SourceDataTag object
   * @param newValue        The new update value that we want set to the tag
   * @param newTagValueDesc The new value description
   * @param newSDQuality    the new SourceDataTag see {@link SourceDataTagQuality}
   * @param timestamp       time when the SourceDataTag's value has become invalid;
   */
  public void invalidate(final SourceDataTag sourceDataTag, final ValueUpdate update, final SourceDataTagQuality newSDQuality) {

    try {
      // We check first is the new value has to be filtered out or not
      FilterType filterType;

      // Cast the value to the proper type before sending it
      // NOTE: if it comes from a isConvertible filter to be invalidate because
      // it did not pass it the new value will be null
      Object newValueCasted = TypeConverter.cast(update.getValue(), sourceDataTag.getDataType());
      update.setValue(newValueCasted);

      // We check first is the new value has to be filtered out or not
      filterType = this.dataTagValueFilter.isCandidateForFiltering(sourceDataTag, update, newSDQuality);

      log.debug("Filter type: " + filterType);

      // The new value will not be filtered out
      if (filterType == FilterType.NO_FILTERING) {
        // Send the value
        sendValueWithTimeDeadbandCheck(sourceDataTag, update, newSDQuality);
      }
      // The new value will be filtered out
      else {
        // If we are here the new value will be filtered out
        StringBuilder msgBuf = new StringBuilder();
        msgBuf.append("\tthe tag [" + sourceDataTag.getId() + "] has already been invalidated with quality code : " + newSDQuality.getQualityCode());
        msgBuf.append(" at " + sourceDataTag.getCurrentValue().getTimestamp());
        msgBuf.append(" The DAQ has not received any values with different quality since then, Hence, the");
        msgBuf.append(" invalidation procedure will be canceled this time");
        log.debug(msgBuf.toString());

        /*
         * the value object can be null if several invalid data tags are sent
         * when the DAQ is started up (the value object is still null, but the
         * currentValue object is not anymore) in this case, we choose not to
         * send it to the filter path
         */
        if (newValueCasted != null) {
          // send a corresponding INVALID tag to the statistics module
          log.debug("Sending an invalid tag #{} to the statistics module", sourceDataTag.getId());

          // send filtered message to statistics module
          this.equipmentSenderFilterModule.sendToFilterModule(sourceDataTag, update, newSDQuality, filterType.getNumber());

        } else {
          log.debug("Value has still not been initialised: not sending the invalid tag #{} to the statistics module", sourceDataTag.getId());
        }
      }
    } catch (Exception ex) {
      log.error("Unexpected exception caught for tag {}", sourceDataTag.getId(), ex);

    }
  }

  /**
   * This method checks the time deadband and according to the result it sends
   * the updated value to the server
   */
  private void sendValueWithTimeDeadbandCheck(final SourceDataTag sourceDataTag, final ValueUpdate castedUpdate, final SourceDataTagQuality newSDQuality) {

    // TimeDeadband for the current Data Tag (Static or Dynamic since this
    // variable can be enabled at runtime when the Dynamic
    // filter gets enabled)
    if (sourceDataTag.getAddress().isTimeDeadbandEnabled()) {
      log.debug("Passing update to time-deadband scheduler for tag #{}", sourceDataTag.getId());
      this.equipmentTimeDeadband.addToTimeDeadband(sourceDataTag, castedUpdate, newSDQuality);
    } else {
      if (this.equipmentTimeDeadband.getSdtTimeDeadbandSchedulers().containsKey(sourceDataTag.getId())) {
        log.debug("Removeing time-deadband scheduler for tag #{}", sourceDataTag.getId());
        this.equipmentTimeDeadband.removeFromTimeDeadband(sourceDataTag);
      }

      // All checks and filters are done
      log.debug("Invalidating and sending invalid tag #{} update to the server", sourceDataTag.getId());

      SourceDataTagValue newSDValue = sourceDataTag.update(castedUpdate, newSDQuality);
      // Special case Quality OK
      if (newSDValue == null) {
        // this means we have a valid quality code 0 (OK)
        log.warn("Method called with 0(OK) quality code for tag #{}. This should normally not happen! " +
            "sendTagFiltered() method should have been called before.", sourceDataTag.getId());
      } else {
        try {
          this.processMessageSender.addValue(newSDValue);
        } catch (InterruptedException e) {
          log.error("Data could not be sent and is lost!: {}", newSDQuality);
        }

        // Checks if the dynamic TimeDeadband filter is enabled, Static disable
        // and record it depending on the priority
        this.dynamicTimeDeadbandFilterer.recordTag(sourceDataTag);
      }
    }
  }
}
