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
package cern.c2mon.daq.common.messaging.impl;

import cern.c2mon.daq.common.conf.core.ConfigurationController;
import cern.c2mon.daq.common.messaging.JmsSender;
import cern.c2mon.shared.common.datatag.DataTagValueUpdate;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;
import cern.c2mon.shared.common.process.ProcessConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.JMSException;

/**
 * Implementation of the JMSSender interface for sending update messages to
 * ActiveMQ brokers.
 *
 * @author mbrightw
 */
@Slf4j
public class ActiveJmsSender implements JmsSender {

  /**
   * The Spring JmsTemplate managing the calls to JMS.
   */
  private JmsTemplate jmsTemplate;

  /**
   * Configuration controller to access the configuration data.
   */
  private ConfigurationController configurationController;

  /**
   * Enabling/disabling the action of sending information to the brokers
   */
  private boolean isEnabled = true;

  //    /**
  //     * The Spring name for the ActiveJmsSender
  //     */
  //    private String beanName;

  /**
   * Unique constructor. Notice the JmsTemplate needs a Qualifier annotation
   * for correct autowiring as there are several JmsTemplate's in the
   * container.
   *
   * @param configurationController Configuration controller to access the configuration data.
   * @param jmsTemplate             The JMS template.
   */
  @Autowired
  public ActiveJmsSender(final ConfigurationController configurationController, @Qualifier("sourceUpdateJmsTemplate") final JmsTemplate jmsTemplate) {
    this.configurationController = configurationController;
    this.jmsTemplate = jmsTemplate;
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
  }

  /**
   * Sends a single update value to the brokers.
   *
   * @param sourceDataTagValue the data tag update to be sent
   */
  @Override
  public final void processValue(final SourceDataTagValue sourceDataTagValue) {
    log.debug("entering processValue()..");
    ProcessConfiguration processConfiguration = configurationController.getProcessConfiguration();

    // The PIK is also check before building the XML in DataTagValueUpdate class
    DataTagValueUpdate dataTagValueUpdate;
    dataTagValueUpdate = new DataTagValueUpdate(processConfiguration.getProcessID(), processConfiguration.getprocessPIK());

    dataTagValueUpdate.addValue(sourceDataTagValue);
    log.trace("value added to value update message");

    // If the sending action is Enabled
    if (this.isEnabled) {

      log.trace("not in test mode.");

      // set message properties
      jmsTemplate.setPriority(sourceDataTagValue.getPriority());
      jmsTemplate.setTimeToLive(sourceDataTagValue.getTimeToLive());

      // set appropriate priority
      if (sourceDataTagValue.isGuaranteedDelivery()) {
        log.debug("\t sending PERSISTENT message");
        // set message delivery mode
        jmsTemplate.setDeliveryMode(javax.jms.DeliveryMode.PERSISTENT);
      } else {
        log.debug("\t sending NON-PERSISTENT message");
        // send the message (put it into the queue)
        jmsTemplate.setDeliveryMode(javax.jms.DeliveryMode.NON_PERSISTENT);
      } // else

      // send the message
      jmsTemplate.convertAndSend(dataTagValueUpdate);
    }
    log.debug("leaving processValue()");
  }

  /**
   * Send the collection of updates using the ActiveMQ JmsTemplate.
   *
   * @param dataTagValueUpdate the values to send
   *
   * @throws JMSException if problem sending the values
   */
  @Override
  public final void processValues(final DataTagValueUpdate dataTagValueUpdate) throws JMSException {
    if (log.isDebugEnabled()) {
      log.debug("entering processValues()...");
    }

    // If the sending action is Enabled
    if (this.isEnabled) {
      // take the first SourceDataTagValue object from collection and find
      // out
      // if it's persistent or not (all those in the collection will have
      // the same persistence setting).
      // The message's alive-time will be also set by taking the value
      SourceDataTagValue sdtValue = (SourceDataTagValue) dataTagValueUpdate.getValues().iterator().next();

      // set priority and TTL from the first value in the message
      // (priority is always LOW for values
      // put in the synchrobuffer)
      jmsTemplate.setPriority(sdtValue.getPriority());
      jmsTemplate.setTimeToLive(sdtValue.getTimeToLive());

      if (sdtValue.isGuaranteedDelivery()) {
        log.debug("\t sending PERSISTENT message");

        // set message delivery mode
        jmsTemplate.setDeliveryMode(javax.jms.DeliveryMode.PERSISTENT);

      } else {
        log.debug("\t sending NON-PERSISTENT message");

        // set message delivery NON-PERSISTENT
        jmsTemplate.setDeliveryMode(javax.jms.DeliveryMode.NON_PERSISTENT);
      }

      // convert and send the collection of updates
      jmsTemplate.convertAndSend(dataTagValueUpdate);

    } else {
      log.debug("DAQ in test mode; not sending the value to JMS");
    }

    log.debug("leaving processValues()");
  }

  /**
   * @param jmsTemplate the jmsTemplate to set
   */
  public final void setJmsTemplate(final JmsTemplate jmsTemplate) {
    this.jmsTemplate = jmsTemplate;
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
}
