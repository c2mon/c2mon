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

import javax.jms.DeliveryMode;

import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.QosSettings;

import lombok.extern.slf4j.Slf4j;

import cern.c2mon.daq.common.conf.core.ProcessConfigurationHolder;
import cern.c2mon.daq.common.messaging.JmsSender;
import cern.c2mon.daq.config.JmsUpdateQueueTemplateFactory;
import cern.c2mon.shared.common.datatag.DataTagValueUpdate;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;
import cern.c2mon.shared.common.process.ProcessConfiguration;

/**
 * Implementation of the JMSSender interface for sending update messages to
 * ActiveMQ brokers.
 *
 * @author mbrightw
 */
@Slf4j
public class ActiveJmsSender implements JmsSender {

  /**
   * The JmsTemplateFactory managing the calls to JMS.
   */
  private JmsUpdateQueueTemplateFactory jmsUpdateQueueTemplateFactory;

  /**
   * Enabling/disabling the action of sending information to the brokers
   */
  private boolean isEnabled = true;

  /**
   * Unique constructor.
   *
   * @param jmsUpdateQueueTemplateFactory The JMS template factory
   */
  public ActiveJmsSender(final JmsUpdateQueueTemplateFactory jmsUpdateQueueTemplateFactory) {
    this.jmsUpdateQueueTemplateFactory = jmsUpdateQueueTemplateFactory;
  }

  /**
   * Not necessary for the Spring managed sender.
   */
  @Override
  public void connect() {
    // do nothing, connection is managed by Spring
  }

  /**
   * Do nothing here - updates should have stopped arriving from EMH.
   */
  @Override
  public final void disconnect() {
    // do nothing
  }

  /**
   * Sends a single update value to the brokers.
   *
   * @param sourceDataTagValue the data tag update to be sent
   * @throws JmsException if there is a problem sending the values
   */
  @Override
  public final void processValue(final SourceDataTagValue sourceDataTagValue) {
    log.debug("entering processValue()..");
    ProcessConfiguration processConfiguration = ProcessConfigurationHolder.getInstance();

    // The PIK is also check before building the XML in DataTagValueUpdate class
    DataTagValueUpdate dataTagValueUpdate;
    dataTagValueUpdate = new DataTagValueUpdate(processConfiguration.getProcessID(), processConfiguration.getprocessPIK());

    dataTagValueUpdate.addValue(sourceDataTagValue);
    processValues(dataTagValueUpdate);
  }

  /**
   * Send the collection of updates using the ActiveMQ JmsTemplate.
   *
   * @param dataTagValueUpdate the values to send
   * @throws JmsException if there is a problem sending the values
   */
  @Override
  public final void processValues(final DataTagValueUpdate dataTagValueUpdate) {
    // If the sending action is Enabled
    if (this.isEnabled) {
      SourceDataTagValue sdtValue = dataTagValueUpdate.getValues().iterator().next();
      QosSettings settings = extractQosSettings(sdtValue);
      // convert and send the collection of updates
      jmsUpdateQueueTemplateFactory.getDataTagValueUpdateJmsTemplate(settings).convertAndSend(dataTagValueUpdate);
    } else {
      log.debug("DAQ in test mode; not sending the value to JMS");
    }
  }

  @Override
  public void shutdown() {
    disconnect();
  }

  /**
   * Sets the isEnabled current value
   *
   * @param value Enabling/disabling the action of sending information to the brokers
   */
  @Override
  public final void setEnabled(final boolean value) {
    this.isEnabled = value;
  }

  /**
   * Gets the isEnabled current value
   *
   * @return isEnabled Current status of the action of sending information to the brokers
   */
  @Override
  public final boolean getEnabled() {
    return this.isEnabled;
  }
  
  /**
   * We Take the first {@link SourceDataTagValue} object from collection to determine
   * the Quality-of-Service settings for the message sending
   * @param sourceDataTagValue the first tag extracted from {@link DataTagValueUpdate}
   * @return the Quality-of-Service settings for determine the {@link JmsTemplate}
   */
  private QosSettings extractQosSettings(SourceDataTagValue sourceDataTagValue) {
    QosSettings settings = new QosSettings();
    
    settings.setPriority(sourceDataTagValue.getPriority());
    settings.setTimeToLive(sourceDataTagValue.getTimeToLive());

    if (sourceDataTagValue.isGuaranteedDelivery()) {
      log.debug("\t sending PERSISTENT message");
      settings.setDeliveryMode(DeliveryMode.PERSISTENT);
    } else {
      log.debug("\t sending NON-PERSISTENT message");
      settings.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
    }
    
    return settings;
  }
}
