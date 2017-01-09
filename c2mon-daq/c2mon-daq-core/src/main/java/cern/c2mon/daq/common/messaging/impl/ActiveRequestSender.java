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

import cern.c2mon.daq.common.conf.core.ProcessConfigurationHolder;
import cern.c2mon.daq.common.messaging.ProcessRequestSender;
import cern.c2mon.daq.config.DaqProperties;
import cern.c2mon.shared.common.process.ProcessConfiguration;
import cern.c2mon.shared.daq.process.*;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;

import javax.jms.*;

/**
 * Implementation of ProcessRequestSender interface for ActiveMQ JMS middleware.
 *
 * @author mbrightw
 * @author vilches (refactoring updates)
 */
@Slf4j
public class ActiveRequestSender implements ProcessRequestSender {

  private static final long PIK_REQUEST_TIMEOUT = 5000;

  private JmsTemplate jmsTemplate;
  private ProcessMessageConverter processMessageConverter;
  private DaqProperties properties;

  @Autowired
  public ActiveRequestSender(final DaqProperties properties, final JmsTemplate processRequestJmsTemplate) {
    this.properties = properties;
    this.jmsTemplate = processRequestJmsTemplate;
    this.processMessageConverter = new ProcessMessageConverter();
  }

  /**
   * Requests the initial DAQ configuration from the server through JMS.
   *
   * @param processName name of the process which wants to connect
   * @return ProcessConfigurationResponse or null in case of a timeout
   */
  @Override
  public ProcessConfigurationResponse sendProcessConfigurationRequest(final String processName) {
    log.debug("Sending Process Configuration request to server");
    // use of JmsTemplate here means exceptions are caught by Spring and
    // converted!
    // JMS template NOT used for reply as need to use same connection &
    // session
    final Destination requestDestination = jmsTemplate.getDefaultDestination();
    ProcessConfigurationResponse processConfigurationResponse = (ProcessConfigurationResponse) jmsTemplate.execute(new SessionCallback<Object>() {

      public Object doInJms(final Session session) throws JMSException {
        TemporaryQueue replyQueue = session.createTemporaryQueue();

        ProcessConfigurationRequest processConfigurationRequest = new ProcessConfigurationRequest(processName);
        processConfigurationRequest.setProcessPIK(ProcessConfigurationHolder.getInstance().getprocessPIK());

        Message message = session.createTextMessage(processMessageConverter.toJSON(processConfigurationRequest));
        message.setJMSReplyTo(replyQueue);
        MessageProducer messageProducer = session.createProducer(requestDestination);
        try {
          Long requestTimeout = properties.getServerRequestTimeout();
          messageProducer.setTimeToLive(requestTimeout);
          messageProducer.send(message);

          // wait for reply (receive timeout is set in XML)
          MessageConsumer consumer = session.createConsumer(replyQueue);
          try {
            String replyMessage = ((TextMessage) consumer.receive(requestTimeout)).getText();
            if (replyMessage == null) {
              return null;
            } else {
              return processMessageConverter.fromJSON(replyMessage);
            }
          } finally {
            consumer.close();
          }
        } finally {
          messageProducer.close();
        }
      }
    }, true); // start the connection for receiving messages

    // Can be null if there is a TimeOut
    return processConfigurationResponse;
  }

  /**
   * Initiates the JMS server connection when the DAQ starts up.
   *
   * @param processName name of the process which wants to connect
   * @return ProcessConnectionResponse or null in case of a timeout
   */
  @Override
  public ProcessConnectionResponse sendProcessConnectionRequest(final String processName) {
    log.debug("Sending Process Connection Request to server");
    // use of JmsTemplate here means exceptions are caught by Spring and
    // converted!
    // JMS template NOT used for reply as need to use same connection &
    // session
    final Destination requestDestination = jmsTemplate.getDefaultDestination();

    ProcessConnectionResponse processConnectionResponse = (ProcessConnectionResponse) jmsTemplate.execute(new SessionCallback<Object>() {
      public Object doInJms(final Session session) throws JMSException {
        TemporaryQueue replyQueue = session.createTemporaryQueue();
        ProcessConnectionRequest processConnectionRequest = new ProcessConnectionRequest(processName);

        Message message = session.createTextMessage(processMessageConverter.toJSON(processConnectionRequest));
        message.setJMSReplyTo(replyQueue);
        MessageProducer messageProducer = session.createProducer(requestDestination);
        try {
          // TimeOut (too long for the PIK) 12000
          //messageProducer.setTimeToLive(commonConfiguration.getRequestTimeout());
          messageProducer.setTimeToLive(PIK_REQUEST_TIMEOUT);
          messageProducer.send(message);

          // If there is a timeout SystemExit

          // wait for reply (receive timeout is set in XML) -> 12000
          MessageConsumer consumer = session.createConsumer(replyQueue);
          try {
            Message replyMessage = consumer.receive(PIK_REQUEST_TIMEOUT);

            if (replyMessage == null) {
              return null;
            } else {
              return processMessageConverter.fromJSON(((TextMessage) replyMessage).getText());
            }
          } finally {
            consumer.close();
          }
        } finally {
          messageProducer.close();
        }
      }
    }, true); // start the connection for receiving PIK messages

    // Can be null if there is a TimeOut
    return processConnectionResponse;
  }

  /**
   * Initiates a DAQ shutdown. No response is needed.
   *
   * @param processConfiguration the process configuration of the DAQ which is shutting down
   * @param startupTime the timestamp when the DAQ was starting up
   */
  @Override
  public void sendProcessDisconnectionRequest(ProcessConfiguration processConfiguration, long startupTime) {
    log.debug("Sending Process Disconnection notification to server");
    ProcessDisconnectionRequest processDisconnectionRequest;

    if (processConfiguration.getProcessID() == null) {
      processConfiguration.setProcessID(ProcessDisconnectionRequest.NO_ID);
    }

    if (processConfiguration.getProcessName() == null) {
      processConfiguration.setProcessName(ProcessDisconnectionRequest.NO_PROCESS);
    }

    if (processConfiguration.getprocessPIK() == null) {
      processConfiguration.setprocessPIK(ProcessDisconnectionRequest.NO_PIK);
    }

    // We don't care if there is NO_PIK or NO_PROCESS. The server will take care
    if (processConfiguration.getProcessID() != ProcessDisconnectionRequest.NO_ID) {
      processDisconnectionRequest = new ProcessDisconnectionRequest(processConfiguration.getProcessID(), processConfiguration.getProcessName(),
          processConfiguration.getprocessPIK(), startupTime);
    } else {
      processDisconnectionRequest = new ProcessDisconnectionRequest(processConfiguration.getProcessName(), processConfiguration.getprocessPIK(),
          startupTime);
    }

    log.trace("Converting and sending disconnection message");
    jmsTemplate.send(session -> session.createTextMessage(processMessageConverter.toJSON(processDisconnectionRequest)));
    log.trace("Process Disconnection for " + processConfiguration.getProcessName() + " sent");
  }

  /**
   * Useful for overwriting the default JmsTemplate that is wired in a start-up
   * (for using a different connection for instance).
   *
   * @param jmsTemplate the jmsTemplate to set
   */
  public void setJmsTemplate(JmsTemplate jmsTemplate) {
    this.jmsTemplate = jmsTemplate;
  }
}
