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

import cern.c2mon.server.supervision.impl.event.ProcessEvents;
import cern.c2mon.shared.daq.process.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.stereotype.Service;

import javax.jms.*;


/**
 * Handles all incoming requests from the DAQ layer.
 *
 * @author Mark Brightwell
 * @author vilches
 *
 */
@Service("processRequestHandler")
public class ProcessRequestHandlerImpl implements SessionAwareMessageListener<Message> {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessRequestHandlerImpl.class);

  /**
   * Reference to the {@link ProcessEvents} bean.
   */
  private ProcessEvents processEvents;

  /**
   * ProcessMessageConverter helper class (fromMessage/ToMessage)
   */
  private ProcessMessageConverter processMessageConverter;

  /**
   * Constructor used to instantiate the bean.
   * @param processEvents the supervision manager to wire in
   */
  @Autowired
  public ProcessRequestHandlerImpl(final ProcessEvents processEvents) {
    super();
    this.processEvents = processEvents;
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
    LOGGER.debug("onMessage() - Message coming " + message);

    try {
      ProcessRequest processRequest = (ProcessRequest) this.processMessageConverter.fromMessage(message);

      // ProcessDisconnectionRequest
      if (processRequest instanceof ProcessDisconnectionRequest) {
        processEvents.onDisconnection((ProcessDisconnectionRequest) processRequest);
        if (LOGGER.isDebugEnabled()) {

          LOGGER.debug("onMessage() - Process disconnection completed for DAQ " + ((ProcessDisconnectionRequest) processRequest).getProcessName());
        }
      }
      // processConnectionRequest
      else if (processRequest instanceof ProcessConnectionRequest) {
        ProcessConnectionRequest processConnectionRequest = (ProcessConnectionRequest)processRequest;
        LOGGER.info("onMessage - DAQ Connection request received from DAQ " + processConnectionRequest.getProcessName());

        // Create the processConnectionResponse
        String processConnectionResponse = processEvents.onConnection(processConnectionRequest);

        // Send reply to DAQ on reply queue
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("onMessage - Sending Connection response to DAQ " + processConnectionRequest.getProcessName());
        }
        MessageProducer messageProducer = session.createProducer(message.getJMSReplyTo());
        try {
          TextMessage replyMessage = session.createTextMessage();
          replyMessage.setText(processConnectionResponse);
          messageProducer.send(replyMessage);
        } finally {
          messageProducer.close();
        }
      }
      // ProcessConfigurationRequest
      else if (processRequest instanceof ProcessConfigurationRequest) {
        ProcessConfigurationRequest processConfigurationRequest = (ProcessConfigurationRequest) processRequest;
        LOGGER.info("onMessage - DAQ configuration request received from DAQ " + processConfigurationRequest.getProcessName());

        // Create the processConfigurationResponse
        String processConfiguration = processEvents.onConfiguration(processConfigurationRequest);

        //send reply to DAQ on reply queue
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("onMessage - Sending Configuration Response to DAQ " + processConfigurationRequest.getProcessName());
        }
        MessageProducer messageProducer = session.createProducer(message.getJMSReplyTo());
        try {
          TextMessage replyMessage = session.createTextMessage();
          replyMessage.setText(processConfiguration);
          messageProducer.send(replyMessage);
        } finally {
          messageProducer.close();
        }
      } else {
        LOGGER.error("onMessage - Incoming ProcessRequest object not recognized! - ignoring the request");
      }
    } catch (MessageConversionException e) {
      LOGGER.error("onMessage - Exception caught while converting incoming DAQ request - unable to process request", e);
    }
  }

}
