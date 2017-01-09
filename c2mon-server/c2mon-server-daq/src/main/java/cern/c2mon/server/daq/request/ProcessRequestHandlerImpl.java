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
package cern.c2mon.server.daq.request;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.stereotype.Service;

import cern.c2mon.server.supervision.SupervisionManager;
import cern.c2mon.shared.daq.process.*;


/**
 * Handles all incoming requests from the DAQ layer.
 *
 * @author Mark Brightwell
 * @author vilches
 *
 */
@Slf4j
@Service("processRequestHandler")
public class ProcessRequestHandlerImpl implements SessionAwareMessageListener<Message> {

  /**
   * Reference to the {@link SupervisionManager} bean.
   */
  private SupervisionManager supervisionManager;
  private ProcessMessageConverter processMessageConverter;

  /**
   * Constructor used to instantiate the bean.
   * @param supervisionManager the supervision manager to wire in
   */
  @Autowired
  public ProcessRequestHandlerImpl(final SupervisionManager supervisionManager) {
    super();
    this.supervisionManager = supervisionManager;
    this.processMessageConverter = new ProcessMessageConverter();
  }


  /**
   * Called on an incoming request from the DAQ layer.
   *
   * <p>Throws a MessageConversionException if the conversion of the XML message fails. Notice the thrown
   * exceptions should be caught and logged by Spring container.
   *
   * @param message the incoming message
   * @param session the active session
   * @throws JMSException if JMS problems occur while treating the message
   * @throws NullPointerException if passed message or session is null
   */
  @Override
  public void onMessage(final Message message, final Session session) throws JMSException {
    log.debug("JMS message received " + message);

    String text = "";
    if (((TextMessage) message).getText() != null) {
      text = ((TextMessage) message).getText();
    } else {
      log.error("Error occurred, incoming JMS message is empty");
    }

    boolean isJSONRequest = isJSON(text);
    Object processRequest;
    try {
      if (isJSONRequest) {
        processRequest = this.processMessageConverter.fromJSON(text);
      } else {
        processRequest = this.processMessageConverter.fromXML(text);
      }

      // ProcessDisconnectionRequest
      if (processRequest instanceof ProcessDisconnectionRequest) {
        this.supervisionManager.onProcessDisconnection((ProcessDisconnectionRequest) processRequest);
        if (log.isDebugEnabled()) {

          log.debug("Process disconnection completed for DAQ " + ((ProcessDisconnectionRequest) processRequest).getProcessName());
        }
      }
      // processConnectionRequest
      else if (processRequest instanceof ProcessConnectionRequest) {
        ProcessConnectionRequest processConnectionRequest = (ProcessConnectionRequest)processRequest;
        log.info("DAQ Connection request received from DAQ " + processConnectionRequest.getProcessName());

        // Create the processConnectionResponse
        ProcessConnectionResponse processConnectionResponse = this.supervisionManager.onProcessConnection(processConnectionRequest);

        // Send reply to DAQ on reply queue
        if (log.isDebugEnabled()) {
          log.debug("Sending Connection response to DAQ " + processConnectionRequest.getProcessName());
        }
        MessageProducer messageProducer = session.createProducer(message.getJMSReplyTo());
        try {
          TextMessage replyMessage = session.createTextMessage();
          if (isJSONRequest) {
            replyMessage.setText(processMessageConverter.toJSON(processConnectionResponse));
          } else {
            replyMessage.setText(processMessageConverter.toXML(processConnectionResponse));
          }
          messageProducer.send(replyMessage);
        } finally {
          messageProducer.close();
        }
      }
      // ProcessConfigurationRequest
      else if (processRequest instanceof ProcessConfigurationRequest) {
        ProcessConfigurationRequest processConfigurationRequest = (ProcessConfigurationRequest) processRequest;
        log.info("DAQ configuration request received from DAQ " + processConfigurationRequest.getProcessName());

        // Create the processConfigurationResponse
        ProcessConfigurationResponse processConfigurationResponse = this.supervisionManager.onProcessConfiguration(processConfigurationRequest);

        //send reply to DAQ on reply queue
        if (log.isDebugEnabled()) {
          log.debug("Sending Configuration Response to DAQ " + processConfigurationRequest.getProcessName());
        }
        MessageProducer messageProducer = session.createProducer(message.getJMSReplyTo());
        try {
          TextMessage replyMessage = session.createTextMessage();
          if (isJSONRequest) {
            replyMessage.setText(processMessageConverter.toJSON(processConfigurationResponse));
          } else {
            replyMessage.setText(processMessageConverter.toXML(processConfigurationResponse));
          }
          messageProducer.send(replyMessage);
        } finally {
          messageProducer.close();
        }
      } else {
        log.error("Incoming ProcessRequest object not recognized! - ignoring the request");
      }
    } catch (MessageConversionException e) {
      log.error("Exception caught while converting incoming DAQ request - unable to process request", e);
    }
  }

  private static boolean isJSON(String message) {
    message = message.trim();
    return message.startsWith("{") && message.endsWith("}")
        || message.startsWith("[") && message.endsWith("]");
  }
}
