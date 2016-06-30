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
import cern.c2mon.daq.common.messaging.ProcessMessageReceiver;
import cern.c2mon.shared.common.process.ProcessConfiguration;
import cern.c2mon.shared.daq.command.SourceCommandTagValue;
import cern.c2mon.shared.daq.config.ChangeRequest;
import cern.c2mon.shared.daq.datatag.SourceDataTagValueRequest;
import cern.c2mon.shared.daq.messaging.DAQResponse;
import cern.c2mon.shared.daq.messaging.ServerRequest;
import cern.c2mon.shared.daq.messaging.response.ServerErrorResponse;
import cern.c2mon.shared.daq.serialization.MessageConverter;
import org.apache.activemq.command.ActiveMQQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.jms.*;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Implementation of the ProcessMessageReceiver interface for ActiveMQ JMS
 * middleware (in fact, implementation is generic - except in init() - and could
 * be used with other providers if the connection and session management can be
 * done in the Spring XML).
 *
 * In the XML, the message listener destination is set to a temporary value,
 * which is then overridden at runtime in the init() method below to the correct
 * value. Notice this causes a circular reference, which can be resolved ().
 *
 * @author mbrightw
 *
 */
@Component
@Profile({ "single", "double" })
public class ActiveMessageReceiver extends ProcessMessageReceiver implements SessionAwareMessageListener {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ActiveMessageReceiver.class);

  /**
   * Wire field to resolve circular reference.
   */
  private DefaultMessageListenerContainer listenerContainer;

  /**
   * The configuration controller which allows the ActiveMessageReceiver
   * the configuration to know on which topic he has to listen.
   */
  private ConfigurationController configurationController;

  /**
   * Unique constructor (uses Qualifier annotation for wiring the listener
   * container.
   *
   * @param configurationController
   *            the DAQ configuration
   * @param serverRequestJmsContainer
   *            the message listener
   */
  @Autowired
  public ActiveMessageReceiver(final ConfigurationController configurationController, @Qualifier
      ("serverRequestListenerContainer") final DefaultMessageListenerContainer serverRequestJmsContainer) throws ParserConfigurationException {
    super();
    this.configurationController = configurationController;
    this.listenerContainer = serverRequestJmsContainer;
  }

  /**
   * Initialization method that must be called after bean creation (done
   * automatically in Spring). Sets the destination and registers as listener
   * in the Spring listener container.
   */
  @PostConstruct
  public void init() {
    ProcessConfiguration processConfiguration = configurationController.getProcessConfiguration();
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("init - Setting ActiveMessageReceiver listener destination to " + processConfiguration.getJmsDaqCommandQueue());
    }
    listenerContainer.setMessageListener(this);
    listenerContainer.setDestination(new ActiveMQQueue(processConfiguration.getJmsDaqCommandQueue()));
    listenerContainer.initialize();
    listenerContainer.start();
  }

  /**
   * Connect method not needed in this case. Reliable connections are managed
   * by ActiveMQ Spring library.
   */
  @Override
  public void connect() {
    // do nothing
  }

  /**
   * Shutdown listener container.
   */
  @Override
  public void disconnect() {
    ProcessConfiguration processConfiguration = configurationController.getProcessConfiguration();
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("disconnect - Disconnecting ActiveMessageReceiver listener with destination to " + processConfiguration.getJmsDaqCommandQueue());
    }
    listenerContainer.shutdown();
  }

  /**
   * Sends a data tag value response to the server.
   * @param response the response which is send to the server
   * @param destination The destination
   * @param session The JMSSession which should be used.
   * @throws JMSException Throws a JMSException if the sending of
   * the message failed.
   */
  @Override
  public void sendDAQResponse(final DAQResponse response, final Destination destination, final Session session) throws
      JMSException {
    sendTextMessage(MessageConverter.responseToJson(response), destination, session);
  }

  /**
   * Sends a text message via JMS to the server.
   *
   * @param messageText The text of the message to send.
   * @param destination The destination of the message.
   * @param session The session to use.
   * @throws JMSException Throws a JMSException if the sending of the message failed.
   */
  public void sendTextMessage(final String messageText, final Destination destination, final Session session) throws JMSException {
    MessageProducer messageProducer = session.createProducer(destination);
    try {
      TextMessage message = session.createTextMessage();
      message.setText(messageText);
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Sending response to DataTagValueRequest.");
      }
      messageProducer.send(destination, message);
    } finally {
      messageProducer.close();
    }
  }

  /**
   * Called after a message arrived from the server.
   *
   * @param message The received message.
   * @param session The session to of the received message.
   * @throws JMSException Throws a JMSException if the sending of the
   * answer message failed.
   */
  @Override
  // TODO change commands & requests to Queue (need change in DAQ tim-shared,
  // so postponed until prototype is working)
  public void onMessage(final Message message, final Session session) throws JMSException {
    LOGGER.debug("Request/Command received from the server.");
    if (message.getJMSReplyTo() == null) {
      LOGGER.warn("\tthe \"replyTo\" property was not set in the JMS message. Ignoring request.");
      return;
    }
    DAQResponse response;


    if (message instanceof TextMessage) {
      TextMessage textMessage = (TextMessage) message;
      String messageContent = textMessage.getText();

      LOGGER.trace("message received from server: " + textMessage.getText());
      try {
        ServerRequest request = MessageConverter.requestFromJson(messageContent);

        if (request instanceof SourceDataTagValueRequest) {
          LOGGER.debug("Processing server request for current data tag values.");
          response = onSourceDataTagValueUpdateRequest((SourceDataTagValueRequest) request);

        } else if (request instanceof SourceCommandTagValue) {
          response = onExecuteCommand((SourceCommandTagValue) request);

        } else if (request instanceof ChangeRequest) {
          response = onReconfigureProcess((ChangeRequest) request);

        } else {
          LOGGER.warn("Request received from server not recognized");
          response = new ServerErrorResponse("Request received from server not recognized: " + request.getClass() + " not supported from the DAQ.");
        }
        sendDAQResponse(response, message.getJMSReplyTo(), session);

      } catch (Exception e) {
        LOGGER.error("Unexpected exception caught while processing server request.", e);
      }
    } else {
      LOGGER.warn("Received non-text message - unable to process.");
    }
  }

  @Override
  public void shutdown() {
    disconnect();
  }

  /**
   * Useful for overwriting the defaultListener container (which is wired in automatically).
   * @param listenerContainer the listenerContainer to set
   */
  public void setListenerContainer(final DefaultMessageListenerContainer listenerContainer) {
    this.listenerContainer = listenerContainer;
  }
}
