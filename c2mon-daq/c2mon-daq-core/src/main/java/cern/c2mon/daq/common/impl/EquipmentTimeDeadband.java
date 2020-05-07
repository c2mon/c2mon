/******************************************************************************
 * Copyright (C) 2010-2020 CERN. All rights not expressly granted are reserved.
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

import static java.lang.String.format;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import lombok.extern.slf4j.Slf4j;

import cern.c2mon.daq.common.IDynamicTimeDeadbandFilterer;
import cern.c2mon.daq.common.messaging.IProcessMessageSender;
import cern.c2mon.daq.tools.DataTagValueFilter;
import cern.c2mon.shared.common.datatag.SourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataTagQuality;
import cern.c2mon.shared.common.datatag.ValueUpdate;
import cern.c2mon.shared.common.filter.FilteredDataTagValue.FilterType;

/**
 * This class has all methods related with the Equipment Time Deadband (filter, scheduled, ...)
 *
 * @author vilches
 */
@Slf4j
class EquipmentTimeDeadband {

  /**
   * The timedeadband schedulers hold tags which have time deadband scheduling activated.
   */
  private Map<Long, SDTTimeDeadbandScheduler> sdtTimeDeadbandSchedulers = new HashMap<>();

  /**
   * Filters for Data Tag outgoing Values
   */
  private DataTagValueFilter dataTagValueFilter;

  /**
   * The process message sender takes the messages actually send to the server.
   */
  private IProcessMessageSender processMessageSender;

  /**
   * This is the time deadband scheduler timer where all schedulers are scheduled on.
   */
  private static Timer timeDeadbandTimer = new Timer("Time deadband timer", true);

  /**
   * The dynamic time dead band filterer for recording the current source data tag
   */
  private IDynamicTimeDeadbandFilterer dynamicTimeDeadbandFilterer;

  /**
   * The class with the message sender to send filtered tag values
   */
  private EquipmentSenderFilterModule equipmentSenderFilterModule;

  /**
   * Creates a new EquipmentTimeDeadband.
   *
   * @param dynamicTimeDeadbandFilterer The dynamic time dead band filterer for recording
   *                                    the current source data tag
   * @param equipmentSenderFilterModule The class with the message sender to send filtered tag values
   */
  public EquipmentTimeDeadband(final IDynamicTimeDeadbandFilterer dynamicTimeDeadbandFilterer,
                               final IProcessMessageSender processMessageSender,
                               final EquipmentSenderFilterModule equipmentSenderFilterModule) {
    this.dynamicTimeDeadbandFilterer = dynamicTimeDeadbandFilterer;
    this.processMessageSender = processMessageSender;
    this.equipmentSenderFilterModule = equipmentSenderFilterModule;

    this.dataTagValueFilter = new DataTagValueFilter();
  }

  /**
   * Creates the time deadband scheduler for this tag.
   *
   * @param currentTag The tag which should have a time deadband scheduler.
   */
  private void createSDTtimeDeadbandScheduler(final SourceDataTag currentTag) {
    if (currentTag.getAddress().isTimeDeadbandEnabled() && currentTag.getAddress().getTimeDeadband() > 0) {
      log.debug("createSDTtimeDeadbandScheduler - creating time-deadband scheduler for tag #{}", currentTag.getId());
      this.sdtTimeDeadbandSchedulers.put(currentTag.getId(), new SDTTimeDeadbandScheduler(currentTag, this.processMessageSender,
      this.equipmentSenderFilterModule, timeDeadbandTimer, this.dataTagValueFilter, this.dynamicTimeDeadbandFilterer));
    }
  }

  /**
   * Adds the provided tag value to the tagScheduler of this tag.
   *
   * @param currentTag       The tag of which the tag scheduler should be used.
   * @param tagValue         The value of the tag.
   * @param milisecTimestamp A timestamp in ms.
   * @param pValueDescr      An optional value description.
   */
  public void addToTimeDeadband(final SourceDataTag currentTag, final ValueUpdate update) {
    addToTimeDeadband(currentTag, update, new SourceDataTagQuality());
  }

  /**
   * Adds the provided tag value to the tagScheduler of this tag.
   *
   * @param currentTag       The tag of which the tag scheduler should be used.
   * @param update           The value update of the tag
   * @param newSDQuality     the new tag quality
   */
  public void addToTimeDeadband(final SourceDataTag currentTag, final ValueUpdate update, final SourceDataTagQuality newSDQuality) {
    // Synchronizing here, since the scheduler runs on a different thread
    synchronized (currentTag) {
      long tagID = currentTag.getId();
      // Scheduler for the Static TimeDeadband
      SDTTimeDeadbandScheduler tagScheduler = this.sdtTimeDeadbandSchedulers.get(tagID);

      if (tagScheduler == null) {
        tagScheduler = createTagScheduler(currentTag);
        startSDTtimeDeadbandScheduler(tagScheduler);
      }

      // Checks if the dynamic TimeDeadband filter is enabled, Static disable and record it depending on the priority
      this.dynamicTimeDeadbandFilterer.recordTag(currentTag);

      // if the scheduler is set to send the current tag value, then we need to send it
      // to the statistics module before updating the tag
      if (tagScheduler.isScheduledForSending()) {
        sendToFilterModule(currentTag);
      } else {
        log.debug("addToTimeDeadband - scheduling value update due to time-deadband filtering rule");
        tagScheduler.scheduleValueForSending();
      }

      // update the tag value
      currentTag.update(update, newSDQuality);
    }
  }
  
  /**
   * Send to filter module (Dynamic or Static information added)
   * @param currentTag The current tag value
   */
  private void sendToFilterModule(final SourceDataTag currentTag) {
    long tagID = currentTag.getId();
    ValueUpdate currentValue = new ValueUpdate(
        currentTag.getCurrentValue().getValue(),
        currentTag.getCurrentValue().getValueDescription(),
        currentTag.getCurrentValue().getTimestamp().getTime());
    
    log.debug("Sending time deadband filtered value to statistics module for #{}", tagID);
    
    // Send to filter module (Dynamic or Static information added)
    if (this.dynamicTimeDeadbandFilterer.isDynamicTimeDeadband(currentTag)) {
      log.debug("Tag filtered through Dynamic time deadband filtering: '" + tagID + "'");
      if (currentTag.getCurrentValue().getQuality() == null) {
        this.equipmentSenderFilterModule.sendToFilterModuleByDynamicTimedeadbandFilterer(currentTag, currentValue, FilterType.TIME_DEADBAND.getNumber());
      } else {
        this.equipmentSenderFilterModule.sendToFilterModuleByDynamicTimedeadbandFilterer(currentTag, currentValue, currentTag.getCurrentValue().getQuality(), FilterType.TIME_DEADBAND.getNumber());
      }
    } else {
      log.debug("Tag filtered through Static time deadband filtering: '" + tagID + "'");
      if (currentTag.getCurrentValue().getQuality() == null) {
        this.equipmentSenderFilterModule.sendToFilterModule(currentTag, currentValue, FilterType.TIME_DEADBAND.getNumber());
      } else {
        this.equipmentSenderFilterModule.sendToFilterModule(currentTag, currentValue, currentTag.getCurrentValue().getQuality(), FilterType.TIME_DEADBAND.getNumber());
      }
    }
  }

  /**
   * @param currentTag The tag of which the tag scheduler should be used
   * @return the new Tag Scheduler
   */
  protected SDTTimeDeadbandScheduler createTagScheduler(final SourceDataTag currentTag) {
    long tagID = currentTag.getId();

    createSDTtimeDeadbandScheduler(currentTag);
    return this.sdtTimeDeadbandSchedulers.get(tagID);
  }

  /**
   * @param tagScheduler The scheduler to start
   */
  protected void startSDTtimeDeadbandScheduler(final SDTTimeDeadbandScheduler tagScheduler) {
    tagScheduler.start();
  }

  /**
   * Stops the time deadband scheduler of this tag and removes it from the map of schedulers.
   *
   * @param currentTag The tag to remove.
   */
  public void removeFromTimeDeadband(final SourceDataTag currentTag) {
    log.debug(format("removeFromTimeDeadband - entering removeFromTimeDeadband(%d)..", currentTag.getId()));

    SDTTimeDeadbandScheduler tagScheduler = this.sdtTimeDeadbandSchedulers.remove(currentTag.getId());
    if (tagScheduler != null) {
      log.debug("\tcancelling scheduler");
      tagScheduler.cancel();

      if (tagScheduler.isScheduledForSending()) {
        log.debug("\tforcing scheduler to run its run() in order to send the flush buffered message (if any)");

        tagScheduler.run();
      }
    }

    log.debug(format("removeFromTimeDeadband - leaving removeFromTimeDeadband(%d)", currentTag.getId()));
  }

  /**
   * Sends all through timedeadband delayed values immediately
   */
  public void sendDelayedTimeDeadbandValues() {
    log.trace("sendDelayedTimeDeadbandValues - Sending all time deadband delayed values to the server");

    for (SDTTimeDeadbandScheduler tagScheduler : this.sdtTimeDeadbandSchedulers.values()) {
      if (tagScheduler.isScheduledForSending()) {
        tagScheduler.run();
      }
    }
  }

  /**
   * @return sdtTimeDeadbandSchedulers
   */
  public Map<Long, SDTTimeDeadbandScheduler> getSdtTimeDeadbandSchedulers() {
    return this.sdtTimeDeadbandSchedulers;
  }
}
