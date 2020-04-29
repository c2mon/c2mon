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
package cern.c2mon.daq.common.messaging.impl;

import java.sql.Timestamp;
import java.util.Collection;

import javax.jms.JMSException;

import org.springframework.jms.support.QosSettings;

import lombok.extern.slf4j.Slf4j;

import cern.c2mon.daq.common.DriverKernel;
import cern.c2mon.daq.common.conf.core.ProcessConfigurationHolder;
import cern.c2mon.daq.common.messaging.IProcessMessageSender;
import cern.c2mon.daq.common.messaging.JmsSender;
import cern.c2mon.daq.config.DaqProperties;
import cern.c2mon.shared.common.datatag.*;
import cern.c2mon.shared.common.process.ProcessConfiguration;

/**
 * The ProcessMessageSender class is responsible for sending JMS messages from
 * the DAQ to the application server. <p>
 *
 * This class supports sending the updates to
 * multiple JMS connections. The sending itself is performed by a JMSSender
 * class. Several of these can be specified in the jmsSenders collection field.
 * Notice that the calls to these senders are made on the same threads, so it is
 * up to the JMSSenders to release the threads, if for instance they are not
 * critical.
 *
 * For low priority messages, two synchrobuffer's are used (one for persistent,
 * the other for non-persistent messages).
 */
@Slf4j
public class ProcessMessageSender implements IProcessMessageSender {

  /**
   * The reference for the AliveTimer object
   */
  private AliveTimer aliveTimer;
  
  private SynchroBufferFactory synchroBufferFactory;

  /**
   * The collection of JMS senders (each responsible for sending updates to a
   * specific broker). Injected in Spring XML configuration file.
   */
  private final Collection<JmsSender> jmsSenders;
  
  private final DaqProperties daqProperties;
  
  public ProcessMessageSender(final Collection<JmsSender> jmsSenders, DaqProperties properties) {
    this.daqProperties = properties;
    this.jmsSenders = jmsSenders;
  }

  /**
   * Gets called by the {@link DriverKernel}
   */
  public void init() {
    aliveTimer = new AliveTimer(this);
    synchroBufferFactory = new SynchroBufferFactory(daqProperties, this);
  }

  /**
   * This method initializes and starts the AliveTimer. Since it's initialized
   * it periodically takes action to send AliveTag to TIM server (using
   * ProcessMessageSender's JMS queue connection)
   */
  public final void startAliveTimer() {
    ProcessConfiguration processConfiguration = ProcessConfigurationHolder.getInstance();
    aliveTimer.setInterval(processConfiguration.getAliveInterval());
  }

  /**
   * Stops the Process alive timer. Used at final DAQ shutdown.
   */
  public final void stopAliveTimer() {
    if (aliveTimer != null) {
      aliveTimer.terminateTimer();
    }
  }

  /**
   * This method is responsible for creating a JMS XML message containing alive
   * tag and putting it to the TIM JMS queue
   */
  @Override
  public final void sendProcessAlive() {
    ProcessConfiguration processConfiguration = ProcessConfigurationHolder.getInstance();
    log.debug("sending AliveTag. tag id : " + processConfiguration.getAliveTagID());

    long timestamp = System.currentTimeMillis();
    SourceDataTagValue aliveTagValue = SourceDataTagValue.builder()
        .id(Long.valueOf(processConfiguration.getAliveTagID()))
        .name(processConfiguration.getProcessName() + ":AliveTag")
        .controlTag(true)
        .value(Long.valueOf(timestamp))
        .quality(new SourceDataTagQuality())
        .timestamp(new Timestamp(timestamp))
        .daqTimestamp(new Timestamp(timestamp))
        .priority(DataTagAddress.PRIORITY_HIGHEST)
        .guaranteedDelivery(false)
        .valueDescription("")
        .timeToLive(processConfiguration.getAliveInterval())
        .build();

    distributeValue(aliveTagValue);
  }

  @Override
  public void sendCommfaultTag(long tagID, String tagName, boolean value, String pDescription) {
    log.debug("Sending CommfaultTag tag {} (#{})", tagName, tagID);

    long timestamp = System.currentTimeMillis();
    SourceDataTagValue commfaultTagValue = SourceDataTagValue.builder()
        .id(tagID)
        .name(tagName)
        .controlTag(true)
        .value(value)
        .quality(new SourceDataTagQuality())
        .timestamp(new Timestamp(timestamp))
        .daqTimestamp(new Timestamp(timestamp))
        .priority(DataTagAddress.PRIORITY_HIGHEST)
        .timeToLive(DataTagConstants.TTL_FOREVER)
        .valueDescription(pDescription)
        .build();
    
    distributeValue(commfaultTagValue);
  }

  @Override
  public final void addValue(final SourceDataTagValue dataTagValue) throws InterruptedException {
    if (dataTagValue.getPriority() == DataTagConstants.PRIORITY_HIGHEST) {
      distributeValue(dataTagValue);
    } else {
      QosSettings settings = QosSettingsFactory.extractQosSettings(dataTagValue);
      synchroBufferFactory.getSynchroBuffer(settings).put(dataTagValue);
    }
  }

  /**
   * Connects to all the registered brokers (individual JMSSenders should
   * implement this on separate threads if the connection is unessential).
   */
  public final void connect() {
    for (JmsSender jmsSender : jmsSenders) {
      // Connection
      jmsSender.connect();
    }
  }

  /**
   * Forwards the value to all the JMS senders.
   *
   * @param dataTagValue the value to send
   * @throws JMSException if one of the senders fails
   */
  private void distributeValue(final SourceDataTagValue dataTagValue) {
    for (JmsSender jmsSender : jmsSenders) {
      try {
        jmsSender.processValue(dataTagValue);
      } catch (Exception e) {
        log.error("Unhandled exception caught while sending a source value (tag id {}) - the value update is lost.", dataTagValue.getId(), e);
      }
    }
    // log value in appropriate log file
    dataTagValue.log();
  }

  /**
   * Forwards the list of values to all the JMS senders.
   *
   * @throws JMSException if one of the senders throws one (individual senders
   *           should also listen to these locally to take any necessary action)
   * @param dataTagValueUpdate the values to send
   */
   void distributeValues(final DataTagValueUpdate dataTagValueUpdate) {
    for (JmsSender jmsSender : jmsSenders) {
      try {
        jmsSender.processValues(dataTagValueUpdate);
      } catch (Exception e) {
        log.error("Unhandled exception caught while sending a collection of source values - the updates will be lost.", e);
      }
    }
    // log value in appropriate log file
    dataTagValueUpdate.log();
  }

  /**
   * Shuts down all JmsSenders.
   */
  public void shutdown() {
    jmsSenders.stream().forEach(JmsSender::shutdown);
  }
}
