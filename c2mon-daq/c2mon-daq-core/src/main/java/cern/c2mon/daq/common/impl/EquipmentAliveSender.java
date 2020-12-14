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

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import cern.c2mon.daq.common.messaging.IProcessMessageSender;
import cern.c2mon.shared.common.datatag.*;
import cern.c2mon.shared.common.datatag.util.JmsMessagePriority;
import cern.c2mon.shared.common.type.TypeConverter;

/**
 * This class has all methods for sending the Equipment Supervision Alive Tags
 * to the Process Message Sender
 *
 * @author vilches
 */
@Slf4j
class EquipmentAliveSender {

  /**
   * Constant to prevent from frequent equipment alives
   */
  private final boolean aliveFilteringEnabled;

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
  private long aliveTagInterval;

  /**
   * The Equipment Configuration Name
   */
  private String confName;

  /**
   * The equipment supervision alive tag id
   */
  private final Long aliveTagId;

  /**
   * Creates a new EquipmentAliveSender.
   *
   * @param processMessageSender Process Message Sender
   * @param aliveTagId           The equipment supervision alive tag id
   * @param aliveFilteringEnabled If true, then not necessary alive updates are filtered out 
   */
  public EquipmentAliveSender(final IProcessMessageSender processMessageSender, final Long aliveTagId, boolean aliveFilteringEnabled) {
    this.processMessageSender = processMessageSender;
    this.aliveTagId = aliveTagId;
    this.aliveFilteringEnabled = aliveFilteringEnabled;
  }

  /**
   * Init function
   *
   * @param aliveTagInterval Equipment configuration Tag interval
   * @param confName         Equipment configuration name
   */
  public void init(final long aliveTagInterval, final String confName) {
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
        log.warn("sendEquipmentAlive() - Could not cast current timestamp to value type "
            + aliveTag.getDataType() + " of alive tag #" + aliveTag.getId() + " => value set to null!");
      }

      ValueUpdate update = new ValueUpdate(value, "Auto-generated alive value of Equipment " + confName, currentTimestamp);
      aliveTagValue = aliveTag.update(update);
    } else {
      
      aliveTagValue = SourceDataTagValue.builder()
          .id(this.aliveTagId)
          .name("EQUIPMENT_ALIVE_" + confName)
          .controlTag(true)
          .value(currentTimestamp)
          .valueDescription("Alive tag for Equipment " + confName)
          .quality(new SourceDataTagQuality())
          .timestamp(new Timestamp(currentTimestamp))
          .daqTimestamp(new Timestamp(currentTimestamp))
          .priority(JmsMessagePriority.PRIORITY_HIGHEST.getPriority())
          .guaranteedDelivery(false)
          .timeToLive(aliveTagInterval)
          .build();
    }

    log.debug("Sending equipment alive message with timestamp {}", currentTimestamp);
    return sendEquipmentAliveFiltered(aliveTagValue, currentTimestamp);
  }

  /**
   * Sends an equipment alive tag with the given value, timestamp and
   * description.
   *
   * @param aliveTag         the alive tag to send
   * @param update         the tag value update
   * @return true if the alive was sent, false otherwise
   */
  public boolean sendEquipmentAlive(final SourceDataTag aliveTag, ValueUpdate update) {
    if (TypeConverter.isConvertible(update.getValue(), aliveTag.getDataType())) {
      Object convertedTagValue = TypeConverter.cast(update.getValue(), aliveTag.getDataType());
      update.setValue(convertedTagValue);

      SourceDataTagValue aliveTagValue = aliveTag.update(update);
      log.debug("Sending equipment alive message with source timestamp {}", update.getSourceTimestamp());

      return sendEquipmentAliveFiltered(aliveTagValue, update.getSourceTimestamp());
    } else {
      log.warn("Value [{}] received for alive tag #{} is not convertible to data type {}. Trying to send the current timestamp as number instead.",
          update.getValue(), aliveTag.getId(), aliveTag.getDataType());
      return sendEquipmentAlive(aliveTag);
    }
  }

  /**
   * Sends the alive tag value with some filtering protection against too
   * frequent updates.
   *
   * @param aliveTagValue the equipment alive tag value
   * @return true, if the alive is sent, false otherwise.
   */
  private boolean sendEquipmentAliveFiltered(final SourceDataTagValue aliveTagValue, final long timestamp) {

    if (aliveFilteringEnabled) {
      Long lastEquipmentAliveTimestamp = this.lastEquipmentAlives.get(aliveTagValue.getId());
      boolean isSendEquipmentAlive = true;
      if (lastEquipmentAliveTimestamp != null) {

        // if the time difference between the last eq. heartbeat and the current
        // one is at least half of the eq. alive interval defined
        long diff = timestamp - lastEquipmentAliveTimestamp;
        long halfTime = Math.round(this.aliveTagInterval / 2.0);

        if (diff < halfTime) {
          log.debug("Not sending alive for equipment #{} due to filtering policy", this.confName);
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
    } else {
      // If PREVENT_TOO_FREQUENT_EQUIPMENT_ALIVES is disabled (by default it is)
      doSendEquipmentAlive(aliveTagValue);
      return true;
    }
  }

  /**
   * Sends that alive tag by adding it to the process message sender queue.
   * The method assures as well that expired alive tags get discarded from the broker
   *
   * @param aliveTagValue the alive tag value to be sent.
   */
  private void doSendEquipmentAlive(final SourceDataTagValue aliveTagValue) {
    aliveTagValue.setTimeToLive(aliveTagInterval);
    aliveTagValue.setPriority(JmsMessagePriority.PRIORITY_HIGHEST.getPriority());
    aliveTagValue.setGuaranteedDelivery(false);
    try {
      this.processMessageSender.addValue(aliveTagValue);
    } catch (InterruptedException e) {
      log.error("Equipment Alive tag could not be sent and is lost!: {}", aliveTagValue);
    }
  }
}
