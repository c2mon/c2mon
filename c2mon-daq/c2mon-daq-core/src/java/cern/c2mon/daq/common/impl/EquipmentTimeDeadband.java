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
import java.util.Hashtable;
import java.util.Timer;

import cern.c2mon.daq.common.IDynamicTimeDeadbandFilterer;
import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.common.logger.EquipmentLoggerFactory;
import cern.c2mon.daq.common.messaging.IProcessMessageSender;
import cern.c2mon.daq.tools.DataTagValueFilter;
import cern.c2mon.shared.daq.datatag.SourceDataQuality;
import cern.c2mon.shared.daq.datatag.SourceDataTag;
import cern.c2mon.shared.daq.filter.FilteredDataTagValue.FilterType;

/**
 * This class has all methods related with the Equipment Time Deadband (filter, scheduled, ...)
 * 
 * @author vilches
 *
 */
class EquipmentTimeDeadband {
  /**
   * The logger for this class.
   */
  private EquipmentLogger equipmentLogger;

  /**
   * The timedeadband schedulers hold tags which have time deadband scheduling activated.
   */
  private Hashtable<Long, SDTTimeDeadbandScheduler> sdtTimeDeadbandSchedulers = new Hashtable<Long, SDTTimeDeadbandScheduler>();

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
   * the current source data tag
   * @param equipmentSenderFilterModule The class with the message sender to send filtered tag values
   * @param equipmentLoggerFactory  Equipment Logger factory to create the class logger
   */
  public EquipmentTimeDeadband (final IDynamicTimeDeadbandFilterer dynamicTimeDeadbandFilterer,
                                final IProcessMessageSender processMessageSender,
                                final  EquipmentSenderFilterModule equipmentSenderFilterModule,
                                final EquipmentLoggerFactory equipmentLoggerFactory) {
    this.dynamicTimeDeadbandFilterer = dynamicTimeDeadbandFilterer;
    this.processMessageSender = processMessageSender;
    this.equipmentSenderFilterModule = equipmentSenderFilterModule;
    this.equipmentLogger = equipmentLoggerFactory.getEquipmentLogger(getClass());

    this.dataTagValueFilter = new DataTagValueFilter(equipmentLoggerFactory);
  }

  /**
   * Creates the time deadband scheduler for this tag.
   * 
   * @param currentTag The tag which should have a time deadband scheduler.
   */
  private void createSDTtimeDeadbandScheduler(final SourceDataTag currentTag) {
    if (currentTag.getAddress().isTimeDeadbandEnabled()) {
      if (currentTag.getAddress().getTimeDeadband() > 0) {
        this.sdtTimeDeadbandSchedulers.put(currentTag.getId(), new SDTTimeDeadbandScheduler(currentTag, this.processMessageSender,
            this.equipmentSenderFilterModule, timeDeadbandTimer, this.dataTagValueFilter, this.dynamicTimeDeadbandFilterer));
      }
    }
  }

  /**
   * Adds the provided tag value to the tagScheduler of this tag.
   * 
   * @param currentTag The tag of which the tag scheduler should be used.
   * @param tagValue The value of the tag.
   * @param milisecTimestamp A timestamp in ms.
   * @param pValueDescr An optional value description.
   */
  public void addToTimeDeadband(final SourceDataTag currentTag, final Object tagValue, final long milisecTimestamp,
      final String pValueDescr) {
    addToTimeDeadband(currentTag, tagValue, milisecTimestamp, pValueDescr, new SourceDataQuality());
  }

  /**
   * Adds the provided tag value to the tagScheduler of this tag.
   * 
   * @param currentTag The tag of which the tag scheduler should be used.
   * @param tagValue The value of the tag.
   * @param milisecTimestamp A timestamp in ms.
   * @param pValueDescr An optional value description.
   * @param newSDQuality the new tag quality
   */
  public void addToTimeDeadband(final SourceDataTag currentTag, final Object tagValue, final long milisecTimestamp,
      final String pValueDescr, final SourceDataQuality newSDQuality) {
    if (this.equipmentLogger.isDebugEnabled()) {
      this.equipmentLogger.debug(format("addToTimeDeadband - entering addToTimeDeadband(%d)..", currentTag.getId()));
    }
    
    synchronized (currentTag) { // Synchronizing here, since the scheduler runs on a different thread
      long tagID = currentTag.getId();
      // Scheduler for the Static TimeDeadband
      SDTTimeDeadbandScheduler tagScheduler = this.sdtTimeDeadbandSchedulers.get(tagID);
      if (tagScheduler == null) {          
        tagScheduler = createTagScheduler(currentTag);
        startSDTtimeDeadbandScheduler(tagScheduler);
      }
      else {
        // If quality has changed we reset the scheduler
        if(tagScheduler.isNewQualityStatus(newSDQuality)) {
          // Flush the current scheduler for the Static TimeDeadband
          tagScheduler.flushAndCancel();
          tagScheduler = createTagScheduler(currentTag);
          startSDTtimeDeadbandScheduler(tagScheduler);
        }
      }

      // Checks if the dynamic TimeDeadband filter is enabled, Static disable and record it depending on the priority
      this.dynamicTimeDeadbandFilterer.recordTag(currentTag);

      // if the scheduler is set to send the current tag value,
      // then we need to send it
      // to the statistics module before updating the tag:
      if (tagScheduler.isScheduledForSending()) {
        this.equipmentLogger.debug("addToTimeDeadband - Sending time deadband filtered value to statistics module " + tagID);
        
        // Send to filter module (Dynamic or Static information added)
        if(this.dynamicTimeDeadbandFilterer.isDynamicTimeDeadband(currentTag)) {
          this.equipmentLogger.debug("Tag filtered through Dynamic time deadband filtering: '" + tagID + "'");
          
          this.equipmentSenderFilterModule.sendToFilterModuleByDynamicTimedeadbandFilterer(currentTag, 
              currentTag.getCurrentValue().getValue(), currentTag.getCurrentValue().getTimestamp().getTime(), 
              currentTag.getCurrentValue().getValueDescription(), FilterType.TIME_DEADBAND.getNumber());
        } else {
          this.equipmentLogger.debug("Tag filtered through Static time deadband filtering: '" + tagID + "'");
          
          this.equipmentSenderFilterModule.sendToFilterModule(currentTag, 
              currentTag.getCurrentValue().getValue(), currentTag.getCurrentValue().getTimestamp().getTime(), 
              currentTag.getCurrentValue().getValueDescription(), FilterType.TIME_DEADBAND.getNumber());
        }
      }

      // update the tag value
      currentTag.update(newSDQuality, tagValue, pValueDescr, new Timestamp(milisecTimestamp));

      this.equipmentLogger.debug("addToTimeDeadband - scheduling value update due to time-deadband filtering rule");
      // notify the scheduler that it contains a value that needs sending
      tagScheduler.scheduleValueForSending();
    }
    
    if (this.equipmentLogger.isDebugEnabled()) {
      this.equipmentLogger.debug(format("addToTimeDeadband - leaving addToTimeDeadband(%d)", currentTag.getId()));
    }
  }
  
  /**
   * 
   * @param currentTag The tag of which the tag scheduler should be used
   * @return the new Tag Scheduler
   */
  protected SDTTimeDeadbandScheduler createTagScheduler(final SourceDataTag currentTag) {
    long tagID = currentTag.getId();
    
    createSDTtimeDeadbandScheduler(currentTag);
    return this.sdtTimeDeadbandSchedulers.get(tagID);
  }
  
 /**
  * 
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
    if (this.equipmentLogger.isDebugEnabled()) {
      this.equipmentLogger.debug(format("removeFromTimeDeadband - entering removeFromTimeDeadband(%d)..", currentTag.getId()));
    }
    
    SDTTimeDeadbandScheduler tagScheduler = this.sdtTimeDeadbandSchedulers.remove(currentTag.getId());
    if (tagScheduler != null) {
      this.equipmentLogger.debug("\tcancelling scheduler");
      tagScheduler.cancel();
      
      if (tagScheduler.isScheduledForSending()) {
        this.equipmentLogger
        .debug("\tforcing scheduler to run its run() in order to send the flush buffered message (if any)");
        
        tagScheduler.run();
      }
    }

    if (this.equipmentLogger.isDebugEnabled()) {
      this.equipmentLogger.debug(format("removeFromTimeDeadband - leaving removeFromTimeDeadband(%d)", currentTag.getId()));
    }
  }

  /**
   * Sends all through timedeadband delayed values immediately
   */
  public void sendDelayedTimeDeadbandValues() {
    this.equipmentLogger.trace("sendDelayedTimeDeadbandValues - Sending all time deadband delayed values to the server");

    for (SDTTimeDeadbandScheduler tagScheduler : this.sdtTimeDeadbandSchedulers.values()) {
      if (tagScheduler.isScheduledForSending()) {
        tagScheduler.run();
      }
    }
  }

  /**
   * 
   * @return sdtTimeDeadbandSchedulers
   */
  public Hashtable<Long, SDTTimeDeadbandScheduler> getSdtTimeDeadbandSchedulers() {
    return this.sdtTimeDeadbandSchedulers;
  }
}
