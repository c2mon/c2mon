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
package cern.c2mon.server.configuration.impl;

import javax.jms.Destination;
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

import cern.c2mon.server.configuration.ConfigurationLoader;
import cern.tim.shared.client.configuration.ConfigurationException;
import cern.tim.shared.client.configuration.ConfigurationReport;
import cern.tim.shared.client.configuration.ConfigurationRequest;
import cern.tim.shared.client.configuration.ConfigurationRequestConverter;

/**
 * Handles configuration requests received on JMS from
 * clients.
 * 
 * <p>The request is processed and a configuration report
 * is returned to the sender (in the form of an XML message
 * on a reply topic specified by the client).
 * 
 * @author Mark Brightwell
 *
 */
@Service("configurationRequestHandler")
public class ConfigurationRequestHandler implements SessionAwareMessageListener<Message> {

  /**
   * Private class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(ConfigurationRequestHandler.class);
  
  /**
   * Reference to the configuration loader.
   */
  @Autowired
  private ConfigurationLoader configurationLoader;
  
  /**
   * Reference to the JMS message converter class.
   */
  @Autowired
  private ConfigurationRequestConverter configurationRequestConverter;
  
  @Override
  public void onMessage(Message message, Session session) throws JMSException {    
    ConfigurationRequest configRequest = (ConfigurationRequest) configurationRequestConverter.fromMessage(message);
    LOGGER.info("Reconfiguration request received for configuration with id " + configRequest.getConfigId());
    ConfigurationReport configurationReport;
    try {
       configurationReport = configurationLoader.applyConfiguration(configRequest.getConfigId(), configRequest.getSessionId());
    } catch (ConfigurationException ex) {
      configurationReport = ex.getConfigurationReport();
    }    
    // Extract reply topic
    Destination replyDestination = null;
    try {
      replyDestination = message.getJMSReplyTo();
    } catch (JMSException jmse) {
      LOGGER.error("onMessage() : Cannot extract ReplyTo from message.", jmse);
      throw jmse;
    }
    if (replyDestination != null) {
      MessageProducer messageProducer = session.createProducer(replyDestination);      
      TextMessage replyMessage = session.createTextMessage();
      replyMessage.setText(configurationReport.toXML());
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Sending reconfiguration report to client.");
      }
      messageProducer.send(replyMessage);
    } else {
      LOGGER.error("onMessage(): JMSReplyTo destination is null - cannot send reply.");
      throw new MessageConversionException("JMS reply queue could not be extracted (returned null).");
    }    
  }

  
  
}
