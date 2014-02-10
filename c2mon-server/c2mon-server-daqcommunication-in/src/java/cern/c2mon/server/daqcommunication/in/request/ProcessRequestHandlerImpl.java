/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
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
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.daqcommunication.in.request;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.stereotype.Service;

import cern.c2mon.server.supervision.SupervisionManager;
import cern.c2mon.shared.daq.process.ProcessConfigurationRequest;
import cern.c2mon.shared.daq.process.ProcessConnectionRequest;
import cern.c2mon.shared.daq.process.ProcessDisconnectionRequest;
import cern.c2mon.shared.daq.process.ProcessMessageConverter;
import cern.c2mon.shared.daq.process.ProcessRequest;


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
  private static final Logger LOGGER = Logger.getLogger(ProcessRequestHandlerImpl.class);

  /**
   * Reference to the {@link SupervisionManager} bean.
   */
  private SupervisionManager supervisionManager;
  
  /**
   * ProcessMessageConverter helper class (fromMessage/ToMessage)
   */
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
    LOGGER.debug("onMessage() - Message coming " + message);
    
    try {
      ProcessRequest processRequest = (ProcessRequest) this.processMessageConverter.fromMessage(message);
      
      // ProcessDisconnectionRequest
      if (processRequest instanceof ProcessDisconnectionRequest) {      
        this.supervisionManager.onProcessDisconnection((ProcessDisconnectionRequest) processRequest);            
        if (LOGGER.isDebugEnabled()) {
          
          LOGGER.debug("onMessage() - Process disconnection completed for DAQ " + ((ProcessDisconnectionRequest) processRequest).getProcessName());
        }
      } 
      // processConnectionRequest
      else if (processRequest instanceof ProcessConnectionRequest) {  
        ProcessConnectionRequest processConnectionRequest = (ProcessConnectionRequest)processRequest;
        LOGGER.info("onMessage - DAQ Connection request received from DAQ " + processConnectionRequest.getProcessName());
        
        // Create the processConnectionResponse
        String processConnectionResponse = this.supervisionManager.onProcessConnection(processConnectionRequest);
        
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
        String processConfiguration = this.supervisionManager.onProcessConfiguration(processConfigurationRequest);
        
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
