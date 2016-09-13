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

import java.util.HashMap;
import java.util.Map;

import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.common.logger.EquipmentLoggerFactory;
import cern.c2mon.daq.common.messaging.IProcessMessageSender;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.common.datatag.SourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;
import cern.c2mon.shared.common.datatag.ValueUpdate;
import cern.c2mon.shared.common.type.TypeConverter;

import static java.lang.String.format;

/**
 * This class has all methods for sending the Equipment Supervision Alive Tags
 * to the Process Message Sender
 *
 * @author vilches
 *
 */
class EquipmentAliveSender {

  /**
   * Constant to prevent from frequent equipment alives
   */
  private static final boolean PREVENT_TOO_FREQUENT_EQUIPMENT_ALIVES = Boolean.getBoolean("c2mon.daq.equipment.alive.filtering");

  /**
   * The logger for this class.
   */
  private EquipmentLogger equipmentLogger;

  /**
   * The process message sender takes the messages actually send to the server.
   */
  private IProcessMessageSender processMessageSender;

  /**
   * Map to store the last sent Equipment or Sub-Equipment Alive timestamps for filtering purposes. <p>
   * Key = alive tag id <br>
   * value = timestamp in milliseconds
   *
   * @see #sendEquipmentAliveFiltered(SourceDataTagValue, long)
   */
  private final Map<Long, Long> lastEquipmentAlives = new HashMap<>();

  /**
   * The Equipment alive tag interval to be used for sending or not the
   * equipment alive
   */
  private Long aliveTagInterval;

  /**
   * The Equipment Configuration Name
   */
  private String confName;

  /** The equipment supervision alive tag id */
  private final Long aliveTagId;

  /**
   * Creates a new EquipmentAliveSender.
   *
   * @param processMessageSender Process Message Sender
   * @param equipmentLoggerFactory Equipment Logger factory to create the class
   *          logger
   * @param aliveTagId The equipment supervision alive tag id
   */
  public EquipmentAliveSender(final IProcessMessageSender processMessageSender, final Long aliveTagId, final EquipmentLoggerFactory equipmentLoggerFactory) {
    this.processMessageSender = processMessageSender;
    this.aliveTagId = aliveTagId;
    this.equipmentLogger = equipmentLoggerFactory.getEquipmentLogger(getClass());
  }

  /**
   * Init function
   *
   * @param aliveTagInterval Equipment configuration Tag interval
   * @param confName Equipment configuration name
   */
  public void init(final Long aliveTagInterval, final String confName) {
    this.aliveTagInterval = aliveTagInterval;
    this.confName = confName;
  }

  /**
   * Sends an equipment alive tag with the value set as the current timestamp.
   *
   * @param aliveTag the alive tag to send
   * @return
   */
  public boolean sendEquipmentAlive(final SourceDataTag aliveTag) {
    long currentTimestamp = System.currentTimeMillis();

    SourceDataTagValue aliveTagValue;

    if (aliveTag != null) {
      Object value = null;

      if (aliveTag.getDataType().equalsIgnoreCase("Integer")) {
        value = TypeConverter.cast(Long.valueOf(currentTimestamp % Integer.MAX_VALUE).toString(), aliveTag.getDataType());
      } else {
        value = TypeConverter.cast(currentTimestamp, aliveTag.getDataType());
      }

      if (value == null) {
        this.equipmentLogger.warn("sendEquipmentAlive() - Could not cast current timestamp to value type "
            + aliveTag.getDataType() + " of alive tag #" + aliveTag.getId() + " => value set to null!");
      }

      ValueUpdate update = new ValueUpdate(value, "Auto-generated alive value of Equipment " + confName, currentTimestamp);
      aliveTagValue = aliveTag.update(update);
    } else {

      int ttl = DataTagConstants.TTL_FOREVER;
      if (aliveTagInterval <= Integer.MAX_VALUE) {
        ttl = aliveTagInterval.intValue();
      }

      aliveTagValue = new SourceDataTagValue(this.aliveTagId, "EQUIPMENT_ALIVE_" + confName, true, currentTimestamp, null, currentTimestamp, DataTagConstants.PRIORITY_HIGH, false,
          "Alive tag for Equipment " + confName, ttl);
    }

    this.equipmentLogger.debug("sendEquipmentAlive() - Sending equipment alive message with timestamp " + currentTimestamp);
    return sendEquipmentAliveFiltered(aliveTagValue, currentTimestamp);
  }

  /**
   * Sends an equipment alive tag with the given value, timestamp and
   * description.
   *
   * @param aliveTag the alive tag to send
   * @param tagValue the tag value
   * @param sourceTimestamp the source timestamp (in milliseconds)
   * @param valueDescription the description of the value
   *
   * @return true if the alive was sent, false otherwise
   */
  public boolean sendEquipmentAlive(final SourceDataTag aliveTag, ValueUpdate update) {
    if (TypeConverter.isConvertible(update.getValue(), aliveTag.getDataType())) {
      Object convertedTagValue = TypeConverter.cast(update.getValue(), aliveTag.getDataType());
      update.setValue(convertedTagValue);

      SourceDataTagValue aliveTagValue = aliveTag.update(update);
      this.equipmentLogger.debug("sendEquipmentAlive() - Sending equipment alive message with source timestamp " + update.getSourceTimestamp());
      return sendEquipmentAliveFiltered(aliveTagValue, update.getSourceTimestamp());
    }
    else {
      this.equipmentLogger.warn("sendEquipmentAlive() - Value ["
        + update.getValue() + "] received for alive tag #" + aliveTag.getId()
        + " is not convertible to data type "
        + aliveTag.getDataType() + ". Trying to send the current timestamp as number instead.");
      return sendEquipmentAlive(aliveTag);
    }
  }

  /**
   * Sends the alive tag value with some filtering protection against too
   * frequent updates.
   *
   * @param aliveTagValue the equipment alive tag value
   *
   * @return true, if the alive is sent, false otherwise.
   */
  private boolean sendEquipmentAliveFiltered(final SourceDataTagValue aliveTagValue, final long timestamp) {

    if (PREVENT_TOO_FREQUENT_EQUIPMENT_ALIVES) {
      Long lastEquipmentAliveTimestamp = this.lastEquipmentAlives.get(aliveTagValue.getId());
      boolean isSendEquipmentAlive = true;
      if (lastEquipmentAliveTimestamp != null) {

        // if the time difference between the last eq. heartbeat and the current
        // one is at least half of the eq. alive interval defined
        long diff = timestamp - lastEquipmentAliveTimestamp;
        long halfTime = Math.round(this.aliveTagInterval / 2.0);

        if (diff < halfTime) {
          if (this.equipmentLogger.isDebugEnabled()) {
            this.equipmentLogger.debug(format("this EquipmentAlive of equipment %s will be skipped "
                + "and will not be sent the server due to enabled equipment alive filtering policy", this.confName));
          }

          isSendEquipmentAlive = false;
        }
      }

      if (isSendEquipmentAlive) {
        doSendEquipmentAlive(aliveTagValue);
        this.lastEquipmentAlives.put(aliveTagValue.getId(), timestamp);
        return true;

      } else {
        return false;
      }

    }

    // If PREVENT_TOO_FREQUENT_EQUIPMENT_ALIVES is disabled (by default it is)
    else {
      doSendEquipmentAlive(aliveTagValue);
      return true;
    }
  }

  /**
   * Sends that alive tag by adding it to the process message sender queue.
   *
   * @param aliveTagValue the alive tag value to be sent.
   */
  private void doSendEquipmentAlive(final SourceDataTagValue aliveTagValue) {
    int ttl = aliveTagValue.getTimeToLive();
    if (aliveTagInterval <= Integer.MAX_VALUE) {
      ttl = aliveTagInterval.intValue();
    }

    // Make sure expired alive tags get discarded from the broker
    aliveTagValue.setTimeToLive(ttl);
    aliveTagValue.setPriority(DataTagConstants.PRIORITY_HIGH);
    this.processMessageSender.addValue(aliveTagValue);
  }
}
