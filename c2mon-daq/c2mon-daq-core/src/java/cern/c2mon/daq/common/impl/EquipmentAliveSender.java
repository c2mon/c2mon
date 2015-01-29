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

import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.common.logger.EquipmentLoggerFactory;
import cern.c2mon.daq.common.messaging.IProcessMessageSender;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.common.datatag.SourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;
import cern.c2mon.shared.common.type.TypeConverter;

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
   * The last Equipment Alive Time Stamp
   */
  private Long lastEquipmentAliveTimestamp;

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
      if (aliveTag.getDataType().equalsIgnoreCase("Long")) {
        value = TypeConverter.cast(Long.valueOf(currentTimestamp).toString(), aliveTag.getDataType());
      } else if (aliveTag.getDataType().equalsIgnoreCase("Integer")) {
        value = TypeConverter.cast(Long.valueOf(currentTimestamp % Integer.MAX_VALUE).toString(), aliveTag.getDataType());
      } else {
        this.equipmentLogger.warn("sendEquipmentAlive() - Equipment alive value is neither of type Long nor Integer => value set to null!");
      }

      aliveTagValue = aliveTag.update(value, "Alive tag for Equipment set as current timestamp", new Timestamp(currentTimestamp));
    } else {

      int ttl = DataTagConstants.TTL_FOREVER;
      if (aliveTagInterval <= Integer.MAX_VALUE) {
        ttl = aliveTagInterval.intValue();
      }

      aliveTagValue = new SourceDataTagValue(this.aliveTagId, "eqalive", true, currentTimestamp, null, currentTimestamp, DataTagConstants.PRIORITY_HIGH, false,
          "Alive tag for Equipment set as current timestamp", ttl);
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
  public boolean sendEquipmentAlive(final SourceDataTag aliveTag, final Object tagValue, final long sourceTimestamp, final String valueDescription) {
    SourceDataTagValue aliveTagValue = aliveTag.update(tagValue, valueDescription, new Timestamp(sourceTimestamp));
    this.equipmentLogger.debug("sendEquipmentAlive() - Sending equipment alive message with source timestamp " + sourceTimestamp);
    return sendEquipmentAliveFiltered(aliveTagValue, sourceTimestamp);
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

      boolean isSendEquipmentAlive = true;
      if (this.lastEquipmentAliveTimestamp != null) {

        // if the time difference between the last eq. heartbeat and the current
        // one is at least half of the eq. alive interval defined
        long diff = timestamp - this.lastEquipmentAliveTimestamp;
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
        this.lastEquipmentAliveTimestamp = timestamp;
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
