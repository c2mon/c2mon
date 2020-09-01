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

import java.security.InvalidParameterException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import cern.c2mon.daq.common.IDynamicTimeDeadbandFilterer;
import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.daq.common.conf.equipment.ICoreDataTagChanger;
import cern.c2mon.daq.common.messaging.IProcessMessageSender;
import cern.c2mon.daq.common.timer.FreshnessMonitor;
import cern.c2mon.daq.config.DaqProperties;
import cern.c2mon.daq.filter.IFilterMessageSender;
import cern.c2mon.daq.filter.dynamic.IDynamicTimeDeadbandFilterActivator;
import cern.c2mon.shared.common.datatag.*;
import cern.c2mon.shared.common.datatag.util.SourceDataTagQualityCode;
import cern.c2mon.shared.common.process.EquipmentConfiguration;
import cern.c2mon.shared.common.process.SubEquipmentConfiguration;
import cern.c2mon.shared.daq.config.ChangeReport;

/**
 * EquipmentMessageSender to control all filtering and sending.
 *
 * @author vilches
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class EquipmentMessageSender implements ICoreDataTagChanger, IEquipmentMessageSender, IDynamicTimeDeadbandFilterer {

  /**
   * The filter message sender. All tags a filter rule matched are added to
   * this.
   */
  private final IFilterMessageSender filterMessageSender;

  /**
   * The process message sender takes the messages actually send to the server.
   */
  private final IProcessMessageSender processMessageSender;

  /**
   * The dynamic time deadband filter activator activates time deadband filtering
   * based on tag occurrence.
   */
  private final IDynamicTimeDeadbandFilterActivator dynamicTimeDeadbandFilterActivator;

  /**
   * The equipment configuration of this sender.
   */
  private EquipmentConfiguration equipmentConfiguration;

  /**
   * Valid Sender helper class
   */
  private EquipmentSenderValid equipmentSenderValid;

  /**
   * Invalid Sender helper class
   */
  private EquipmentSenderInvalid equipmentSenderInvalid;

  /**
   * The Equipment Alive sender helper class
   */
  private EquipmentAliveSender equipmentAliveSender;

  /**
   * Time deadband helper class
   */
  private EquipmentTimeDeadband equipmentTimeDeadband;

  /**
   * The class with the message sender to send filtered tag values
   */
  private EquipmentSenderFilterModule equipmentSenderFilterModule;

  private final FreshnessMonitor freshnessMonitor;
  
  private final DaqProperties daqProperties;
  
  private EquipmentStateSender equipmentStateSender;

  /**
   * Creates a new EquipmentMessageSender.
   *
   * @param filterMessageSender The filter message sender to send filtered tag values.
   * @param processMessageSender The process message sender to send tags to the server.
   * @param dynamicTimeDeadbandFilterActivator The dynamic time deadband activator.
   * @param freshnessMonitor Monitors tag that shall be updated regularly
   */
  @Autowired
  public EquipmentMessageSender(final IFilterMessageSender filterMessageSender,
                                final IProcessMessageSender processMessageSender,
                                final IDynamicTimeDeadbandFilterActivator dynamicTimeDeadbandFilterActivator,
                                final FreshnessMonitor freshnessMonitor,
                                final DaqProperties daqProperties) {
    super();
    this.filterMessageSender = filterMessageSender;
    this.processMessageSender = processMessageSender;
    this.dynamicTimeDeadbandFilterActivator = dynamicTimeDeadbandFilterActivator;
    this.freshnessMonitor = freshnessMonitor;
    this.daqProperties = daqProperties;
  }

  /**
   * Initialize the {@link EquipmentStateSender} with the equipment configuration
   *
   * @param equipmentConfiguration the equipment configuration parameters
   */
  public void init(final EquipmentConfiguration equipmentConfiguration) {
    // Configuration
    setEquipmentConfiguration(equipmentConfiguration);
    
    this.equipmentStateSender = new EquipmentStateSender(equipmentConfiguration, processMessageSender);

    // Filter module
    this.equipmentSenderFilterModule = new EquipmentSenderFilterModule(this.filterMessageSender);

    // Time Deadband
    this.equipmentTimeDeadband = new EquipmentTimeDeadband(this, this.processMessageSender, this.equipmentSenderFilterModule);

    // Invalid Sender
    this.equipmentSenderInvalid = new EquipmentSenderInvalid(this.equipmentSenderFilterModule, this.processMessageSender, this.equipmentTimeDeadband,
            this);

    // Valid Sender
    this.equipmentSenderValid = new EquipmentSenderValid(this.equipmentSenderFilterModule, this.processMessageSender, this,
            this.equipmentTimeDeadband, this);

    // Alive Sender
    boolean aliveFilteringEnabled = daqProperties.getEquipment().getAlive().isFiltering();
    this.equipmentAliveSender = new EquipmentAliveSender(this.processMessageSender, this.equipmentConfiguration.getAliveTagId(), aliveFilteringEnabled);
    this.equipmentAliveSender.init(this.equipmentConfiguration.getAliveTagInterval(), this.equipmentConfiguration.getName());
    this.freshnessMonitor.setIEquipmentMessageSender(this);
  }

  /**
   * Check whether the given tag id corresponds to the alive tag of the
   * equipment, or any sub equipments.
   *
   * @param tagId the id of the tag to check
   *
   * @return true if the tag id corresponds to an alive tag, false otherwise
   */
  boolean isAliveTag(Long tagId) {
    if (equipmentConfiguration.getAliveTagId() == tagId) {
      return true;
    }

    for (SubEquipmentConfiguration subEquipmentConfiguration : equipmentConfiguration.getSubEquipmentConfigurations().values()) {
      if (subEquipmentConfiguration.getAliveTagId() != null && subEquipmentConfiguration.getAliveTagId().equals(tagId)) {
        return true;
      }
    }

    return false;
  }

  /**
   * This method should be invoked each time you want to propagate the
   * supervision alive coming from the supervised equipment.
   */
  @Override
  public void sendSupervisionAlive() {
    Long supAliveTagId = Long.valueOf(this.equipmentConfiguration.getAliveTagId());

    if (supAliveTagId == null) {
      log.debug("sendSupervisionAlive() - No alive tag specified. Ignoring request.");
      return;
    }

    SourceDataTag supAliveTag = null;
    if (this.equipmentConfiguration.isSourceDataTagConfigured(supAliveTagId)) {
      supAliveTag = getTag(supAliveTagId);
    }

    this.equipmentAliveSender.sendEquipmentAlive(supAliveTag);
  }

  @Override
  public boolean update(String tagName, ValueUpdate update) {
    return update(equipmentConfiguration.getSourceDataTagIdByName(tagName), update);
  }

  @Override
  public boolean update(Long tagId, ValueUpdate update) {
    SourceDataTag sdt = getTag(tagId);
    freshnessMonitor.reset(sdt);

    if (isAliveTag(tagId)) {
      return this.equipmentAliveSender.sendEquipmentAlive(getTag(tagId), update);
    } else {
      return this.equipmentSenderValid.update(getTag(tagId), update);
    }
  }

  @Override
  public void update(String tagName, SourceDataTagQuality quality) {
    update(equipmentConfiguration.getSourceDataTagIdByName(tagName), quality);
  }

  @Override
  public void update(String tagName, SourceDataTagQuality quality, long sourceTimestamp) {
    update(equipmentConfiguration.getSourceDataTagIdByName(tagName), quality, sourceTimestamp);
  }

  @Override
  public void update(String tagName, ValueUpdate update, SourceDataTagQuality quality) {
    update(equipmentConfiguration.getSourceDataTagIdByName(tagName), update, quality);
  }

  @Override
  public void update(Long tagId, SourceDataTagQuality quality) {
    update(tagId, quality, System.currentTimeMillis());
  }

  @Override
  public void update(Long tagId, SourceDataTagQuality quality, long sourceTimestamp) {
    SourceDataTag sdt = getTag(tagId);
    String valueDescription = sdt.getCurrentValue() == null ? "" : sdt.getCurrentValue().getValueDescription();
    Object currentValue = sdt.getCurrentValue() == null ? null : sdt.getCurrentValue().getValue();

    update(tagId, new ValueUpdate(currentValue, valueDescription, sourceTimestamp), quality);
  }

  @Override
  public void update(Long tagId, ValueUpdate update, SourceDataTagQuality quality) {
    SourceDataTag sdt = getTag(tagId);
    if (quality.getQualityCode() == null ||
        (quality.getQualityCode() != null && quality.getQualityCode() != SourceDataTagQualityCode.STALE)) {
      freshnessMonitor.reset(sdt);
    }

    if (update.getValueDescription() == null) {
      update.setValueDescription("");
    }

    if (quality.getQualityCode() == null || quality.getQualityCode() == SourceDataTagQualityCode.OK) {
      this.equipmentSenderValid.update(sdt, update);
    } else {
      this.equipmentSenderInvalid.invalidate(sdt, update, quality);

    }
  }

  /**
   * Static TimeDeadband has more priority than the Dynamic one. So if the
   * Static TimeDeadband for the current Tag is disable and the DAQ has the
   * Dynamic TimeDeadband enabled then the Tag will be recorded for dynamic time
   * deadband filtering (except for control tags).
   *
   * @param tag The tag to be recorded.
   */
  @Override
  public void recordTag(final SourceDataTag tag) {
    if (isDynamicTimeDeadband(tag)) {
      this.dynamicTimeDeadbandFilterActivator.newTagValueSent(tag.getId());
    }
  }

  /**
   * Checks if Dynamic Timedeadband can be appliyed or not
   *
   * @param tag The tag to be recorded.
   *
   * @return True if the Dynamic Timedeadband can be apply or false if not
   */
  @Override
  public boolean isDynamicTimeDeadband(final SourceDataTag tag) {
    DataTagAddress address = tag.getAddress();
    return (!address.isStaticTimedeadband() && this.equipmentConfiguration.isDynamicTimeDeadbandEnabled() && !tag.isControlTag());
  }

  @Override
  public final void confirmEquipmentStateIncorrect() {
    equipmentStateSender.confirmEquipmentStateIncorrect();
  }

  @Override
  public final void confirmEquipmentStateIncorrect(final String pDescription) {
    equipmentStateSender.confirmEquipmentStateIncorrect(pDescription);
  }

  @Override
  public final void confirmEquipmentStateOK() {
    equipmentStateSender.confirmEquipmentStateOK();
  }

  @Override
  public final void confirmEquipmentStateOK(final String pDescription) {
    equipmentStateSender.confirmEquipmentStateOK(pDescription);
  }

  /**
   * Sets the equipment configuration
   *
   * @param equipmentConfiguration The equipment configuration.
   */
  private void setEquipmentConfiguration(final EquipmentConfiguration equipmentConfiguration) {
    this.equipmentConfiguration = equipmentConfiguration;
    Map<Long, SourceDataTag> sourceDataTags = equipmentConfiguration.getDataTags();
    for (SourceDataTag sdt : sourceDataTags.values()) {
      if (isDynamicTimeDeadband(sdt)) {
        this.dynamicTimeDeadbandFilterActivator.addDataTag(sdt);
      }
    }
  }

  /**
   * Sends all through timedeadband delayed values immediately
   */
  public void sendDelayedTimeDeadbandValues() {
    log.debug("sendDelayedTimeDeadbandValues - Sending all time deadband delayed values to the server");

    this.equipmentSenderValid.sendDelayedTimeDeadbandValues();
  }

  /**
   * Gets a source data tag with the provided id.
   *
   * @param tagId The id of the tag to get.
   * @return The SourceDataTag with this id.
   */
  private SourceDataTag getTag(final Long tagId) {
    if (tagId == null) {
      throw new InvalidParameterException("Passed null parameter as tag ID");
    }

    SourceDataTag sdt = (SourceDataTag) this.equipmentConfiguration.getSourceDataTag(tagId);

    if (sdt == null) {
      throw new InvalidParameterException("Could not get the SourceDataTag for tag " + tagId + ". The tag is not registered in the equipment configuration cache. No update is sent!");
    }

    return sdt;
  }

  /**
   * @return equipmentSenderValid
   */
  protected EquipmentSenderValid getEquipmentSenderValid() {
    return this.equipmentSenderValid;
  }

  /**
   * Reconfiguration functions Add/Remove/Update
   */

  /**
   * Adds a data tag to this sender.
   *
   * @param sourceDataTag The data tag to add.
   * @param changeReport  The change report to fill with the results of the
   *                      change.
   */
  @Override
  public void onAddDataTag(final SourceDataTag sourceDataTag, final ChangeReport changeReport) {
    if (isDynamicTimeDeadband(sourceDataTag)) {
      this.dynamicTimeDeadbandFilterActivator.addDataTag(sourceDataTag);
      changeReport.appendInfo("Data tag " + sourceDataTag.getId() + " added to dynamic time-deadband filter.");
    }
  }

  /**
   * Removes a data tag from this sender.
   *
   * @param sourceDataTag The data tag to remove.
   * @param changeReport  The change report to fill with the results of the
   *                      change.
   */
  @Override
  public void onRemoveDataTag(final SourceDataTag sourceDataTag, final ChangeReport changeReport) {
    this.dynamicTimeDeadbandFilterActivator.removeDataTag(sourceDataTag);
    changeReport.appendInfo("Data tag " + sourceDataTag.getId() + " removed from dynamic time-deadband filter.");
  }

  /**
   * Updates a data tag of this sender.
   *
   * @param sourceDataTag    The data tag to update.
   * @param oldSourceDataTag The old source data tag to identify if necessary
   *                         for changes.
   * @param changeReport     The change report to fill with the results.
   */
  @Override
  public void onUpdateDataTag(final SourceDataTag sourceDataTag, final SourceDataTag oldSourceDataTag, final ChangeReport changeReport) {
    if (sourceDataTag.getAddress().isStaticTimedeadband() != oldSourceDataTag.getAddress().isStaticTimedeadband()) {
      onRemoveDataTag(sourceDataTag, changeReport);
      onAddDataTag(sourceDataTag, changeReport);
    }
  }
}
