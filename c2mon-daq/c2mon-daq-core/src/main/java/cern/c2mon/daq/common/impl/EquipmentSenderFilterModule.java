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

import cern.c2mon.daq.filter.IFilterMessageSender;
import cern.c2mon.shared.common.datatag.SourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataTagQuality;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;
import cern.c2mon.shared.common.datatag.ValueUpdate;
import cern.c2mon.shared.common.filter.FilteredDataTagValue;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;

/**
 * This class is a helper to deal with all sender methods that use the Filter Message Sender
 *
 * @author vilches
 */
@Slf4j
class EquipmentSenderFilterModule {

  /**
   * The filter message sender. All tags a filter rule matched are added to this.
   */
  private IFilterMessageSender filterMessageSender;

  /**
   * Creates a new EquipmentSenderFilterModule.
   *
   * @param filterMessageSender The filter message sender to send filtered tag values.
   */
  public EquipmentSenderFilterModule(final IFilterMessageSender filterMessageSender) {
    this.filterMessageSender = filterMessageSender;
  }

  /**
   * Sends a message to the statistics module. Should only be used in the core.
   *
   * @param currentSourceDataTag The tag to send.
   * @param update               The new tag update value
   * @param quality              The quality of the tag.
   * @param filterType           The type of the applied filter, see
   *                             {@link FilteredDataTagValue} constants
   */
  public void sendToFilterModule(final SourceDataTag currentSourceDataTag,
                                 final ValueUpdate update,
                                 final SourceDataTagQuality quality,
                                 final int filterType) {
    doSendToFilterModule(currentSourceDataTag, update, quality, false, filterType);
  }

  /**
   * Sends a message to the statistics module with Dynamic Timedeadband. Should
   * only be used in the core.
   *
   * @param currentSourceDataTag The tag to send.
   * @param update               The new tag update value
   * @param quality              The quality of the tag.
   * @param filterType           The type of the applied filter, see
   *                             {@link FilteredDataTagValue} constants
   */
  public void sendToFilterModuleByDynamicTimedeadbandFilterer(final SourceDataTag currentSourceDataTag,
                                                              final ValueUpdate update,
                                                              final SourceDataTagQuality quality,
                                                              final int filterType) {
    doSendToFilterModule(currentSourceDataTag, update, quality, true, filterType);
  }

  /**
   * Sends a message to the statistics module. Should only be used in the core.
   *
   * @param currentSourceDataTag The tag to send
   * @param update               The new tag update value
   * @param quality              The quality of the tag.
   * @param dynamicFiltered      True if the tag was dynamic filtered.
   * @param filterType           The type of the applied filter, see
   *                             {@link FilteredDataTagValue} constants
   */
  private void doSendToFilterModule(final SourceDataTag currentSourceDataTag,
                                    final ValueUpdate update,
                                    final SourceDataTagQuality quality,
                                    final boolean dynamicFiltered,
                                    final int filterType) {
    log.trace("sendToFilterModule - entering sendToFilterModule() for tag #" + currentSourceDataTag.getId());

    this.filterMessageSender.addValue(makeFilterValue(currentSourceDataTag, update, quality, dynamicFiltered, filterType));

    log.trace("sendToFilterModule - leaving sendToFilterModule() for tag #" + currentSourceDataTag.getId());
  }

  /**
   * Sends a message to the filter log. Should only be used in the core.
   *
   * @param filterType The type of the applied filter, see
   *                   {@link FilteredDataTagValue} constants
   */
  public void sendToFilterModule(final SourceDataTag currentSourceDataTag, final ValueUpdate update, final int filterType) {
    doSendToFilterModule(currentSourceDataTag, update, new SourceDataTagQuality(), false, filterType);
  }

  /**
   * Sends a message to the filter log with Dynamic Deadband. Should only be
   * used in the core.
   *
   * @param currentSourceDataTag The tag to send.
   * @param tagValue             tagValue The value of the tag.
   * @param milisecTimestamp     The timestamp in ms.
   * @param pValueDescr          A description of the value (optional)
   * @param filterType           The type of the applied filter, see
   *                             {@link FilteredDataTagValue} constants
   */
  public void sendToFilterModuleByDynamicTimedeadbandFilterer(final SourceDataTag currentSourceDataTag, final ValueUpdate update, final int filterType) {
    doSendToFilterModule(currentSourceDataTag, update, new SourceDataTagQuality(), true, filterType);
  }

  /**
   * Returns a FilteredDataTagValue from the current tag, with adjusted quality fields. Is used to send invalidation
   * messages to the filter queue.
   *
   * @return the filtered value object
   */
  private FilteredDataTagValue makeFilterValue(SourceDataTag sdt, final ValueUpdate update, final SourceDataTagQuality sourceQuality,
                                               final boolean dynamicFiltered, final int filterApplied) {
    SourceDataTagValue currentVal = sdt.getCurrentValue();

    FilteredDataTagValue returnValue = new FilteredDataTagValue(currentVal.getId(), currentVal.getName(),
        update.getValue().toString(), update.getValueDescription(), sourceQuality.getQualityCode().getQualityCode(),
        sourceQuality.getDescription(), new Timestamp(update.getSourceTimestamp()),
        sdt.getDataType(), dynamicFiltered, filterApplied);

    return returnValue;
  }
}