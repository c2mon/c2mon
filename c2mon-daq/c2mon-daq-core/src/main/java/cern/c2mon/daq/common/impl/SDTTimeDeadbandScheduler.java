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

import java.util.Timer;
import java.util.TimerTask;

import lombok.extern.slf4j.Slf4j;

import cern.c2mon.daq.common.IDynamicTimeDeadbandFilterer;
import cern.c2mon.daq.common.messaging.IProcessMessageSender;
import cern.c2mon.daq.tools.DataTagValueFilter;
import cern.c2mon.shared.common.datatag.SourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;
import cern.c2mon.shared.common.datatag.ValueUpdate;
import cern.c2mon.shared.common.filter.FilteredDataTagValue.FilterType;
import cern.c2mon.shared.common.type.TypeConverter;

/**
 * The SourceDataTagTimeDeadbandScheduler class models threads responsible for
 * handling source time deadband filtering. It will work with the Static TimeDeadband
 */
@Slf4j
public class SDTTimeDeadbandScheduler extends TimerTask {

  /**
   * The process message sender takes the messages actually send to the server.
   */
  private IProcessMessageSender processMessageSender;

  /**
   * The source data tag controlled by this object.
   */
  private final SourceDataTag sourceDataTag;

  /**
   * True if the current value should be send in the next run cycle.
   */
  private volatile boolean sendValue = false;

  /**
   * Last source Data Tag Value sent to the server
   */
  private SourceDataTag lastSourceDataTag;

  /**
   * The class with the message sender to send filtered tag values
   */
  private EquipmentSenderFilterModule equipmentSenderFilterModule;

  /**
   * Filters for Data Tag outgoing Values
   */
  private DataTagValueFilter dataTagValueFilter;

  /**
   * The timer to schedule this task on
   */
  private Timer timeDeadbandTimer;

  /**
   * The dynamic time dead band filterer for recording the current source data tag
   */
  private IDynamicTimeDeadbandFilterer dynamicTimeDeadbandFilterer;

  /**
   * Indicates if there is a new value to send to the server.
   *
   * @return True if in the next cycle a value should be send to the server else
   * false.
   */
  public boolean isScheduledForSending() {
    return sendValue;
  }

  /**
   * Creates a new SDTTimeDeadbandscheduler
   *
   * @param sourceDataTag               The source data tag controlled by this object.
   * @param processMessageSender        Takes the messages sent to the server
   * @param equipmentSenderValid        Used to send messages to the server.
   * @param timeDeadbandTimer           The timer to schedule this task on.
   * @param valueChecker                Value checker object to avoid repeated values.
   * @param dynamicTimeDeadbandFilterer The dynamic time dead band filterer for recording the current source data tag
   */
  public SDTTimeDeadbandScheduler(final SourceDataTag sourceDataTag,
                                  final IProcessMessageSender processMessageSender,
                                  final EquipmentSenderFilterModule equipmentSenderFilterModule,
                                  final Timer timeDeadbandTimer,
                                  final DataTagValueFilter dataTagValueFilter,
                                  final IDynamicTimeDeadbandFilterer dynamicTimeDeadbandFilterer) {
    this.dataTagValueFilter = dataTagValueFilter;
    this.processMessageSender = processMessageSender;
    this.equipmentSenderFilterModule = equipmentSenderFilterModule;
    this.dynamicTimeDeadbandFilterer = dynamicTimeDeadbandFilterer;

    this.timeDeadbandTimer = timeDeadbandTimer;

    log.debug("creating time-deadband scheduler for tag : " + sourceDataTag.getId());

    this.sourceDataTag = sourceDataTag;
  }

  /**
   * Starts the timer task with the scheduled fixed rate defined for the given {@link SourceDataTag}
   */
  public void start() {
    // create timers
    if (sourceDataTag.getAddress().isTimeDeadbandEnabled()) {
      log.debug("\tscheduler[{}] : setting scheduling interval to : {} miliseconds", this.sourceDataTag.getId(), this.sourceDataTag.getAddress().getTimeDeadband());
      this.timeDeadbandTimer.scheduleAtFixedRate(this, 0, this.sourceDataTag.getAddress().getTimeDeadband());
      log.debug("\tscheduler[{}] : setting scheduled", this.sourceDataTag.getId());
    }
  }

  /**
   * flushes and resets the scheduler
   */
  public void flushAndCancel() {
    synchronized (this.sourceDataTag) {
      log.debug("\tscheduler[{}] : flush and reset", this.sourceDataTag.getId());

      // Execute the run method to empty the scheduler
      this.cancel();
      this.run();
      this.lastSourceDataTag = null;
    }
  }

  /**
   * Informs the scheduler, that next time it wakes up, it should send the value
   * of the source data tag
   */
  public void scheduleValueForSending() {
    this.sendValue = true;
  }

  /**
   * The run method called from the timer.
   */
  @Override
  public void run() {
    log.debug("scheduler[{}] : entering run()..", this.sourceDataTag.getId());

    try {
      synchronized (this.sourceDataTag) {
        if (isScheduledForSending()) {

          SourceDataTagValue currentSDValue = this.sourceDataTag.getCurrentValue();

          FilterType filterType;
          // The first time the lastSentSDTagValue is empty
          if (this.lastSourceDataTag == null) {
            filterType = FilterType.NO_FILTERING;
            log.debug("\tscheduler[{}] : first time running scheduler", this.sourceDataTag.getId() );
          } else {
            // Check the current Source Data tag against the last one sent since
            // they have never been compared

            // Cast the value to the proper type before sending it
            Object newValueCasted = TypeConverter.cast(currentSDValue.getValue(), this.lastSourceDataTag.getDataType());

            ValueUpdate update = new ValueUpdate(newValueCasted, currentSDValue.getValueDescription(), currentSDValue.getTimestamp().getTime());
            filterType = this.dataTagValueFilter.isCandidateForFiltering(this.lastSourceDataTag, update, currentSDValue.getQuality());

            log.debug("\tscheduler[" + this.sourceDataTag.getId() + "] : Filter type: " + filterType);
          }

          // The new value is not filtered out
          if (filterType == FilterType.NO_FILTERING) {
            // Clone the last value sent to the server
            this.lastSourceDataTag = this.sourceDataTag.clone();

            currentSDValue.setValueDescription(createValueDescription(this.lastSourceDataTag));
            // Add the value sent
            this.processMessageSender.addValue(currentSDValue);

            log.debug("\tscheduler[{}] : sending value: {}", this.sourceDataTag.getId(), currentSDValue.getValue());
          } else {
            // The new value is filtered out
            ValueUpdate update = new ValueUpdate(currentSDValue.getValue(), currentSDValue.getValueDescription(), currentSDValue.getTimestamp().getTime());

            // Send to filter module (Dynamic or Static information added)
            if (this.dynamicTimeDeadbandFilterer.isDynamicTimeDeadband(this.sourceDataTag)) {
              log.debug("\tscheduler[" + this.sourceDataTag.getId() + "] : value filtered with Dynamic TimeDeadband : "
                    + currentSDValue.getValue());

              this.equipmentSenderFilterModule.sendToFilterModuleByDynamicTimedeadbandFilterer(this.sourceDataTag, update, filterType.getNumber());
            } else {
              log.debug("\tscheduler[" + this.sourceDataTag.getId() + "] : value filtered with Static TimeDeadband: "
                    + currentSDValue.getValue());

              this.equipmentSenderFilterModule.sendToFilterModule(this.sourceDataTag, update, filterType.getNumber());
            }
          }

          // Reset the sendValue variable
          this.sendValue = false;
        } else {
          log.debug("\tscheduler[#{}] : no new value to be sent", this.sourceDataTag.getId());
        }
      } // synchronized
    } catch (Exception exception) {
      log.error("Critical error in scheduler for tag #{}", this.sourceDataTag.getId(), exception);
    }
    log.debug("scheduler[#{}] : leaving run()", this.sourceDataTag.getId());
  }
  
  private String createValueDescription(SourceDataTag sdt) {
    StringBuilder valueDescription = new StringBuilder(40);
    String originalValueDescription = sdt.getCurrentValue().getValueDescription();
    
    if (this.dynamicTimeDeadbandFilterer.isDynamicTimeDeadband(sdt)) {
      valueDescription.append("Dynamic ");
    } else if (sdt.getAddress().isStaticTimedeadband()){
      valueDescription.append("Static ");
    } else {
      return originalValueDescription;
    }
    valueDescription.append("time-deadband filtering enabled.");
    
    if (originalValueDescription != null && !originalValueDescription.isEmpty()) {
      valueDescription.append(' ');
      valueDescription.append(sdt.getCurrentValue().getValueDescription());
    }
    
    return valueDescription.toString();
  }

}
