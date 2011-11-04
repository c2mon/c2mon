/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN. This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.client.core.configloader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.springframework.beans.factory.annotation.Autowired;

import cern.tim.shared.client.configuration.ConfigurationRequest;
import cern.tim.shared.client.configuration.ConfigurationRequestConverter;
import cern.tim.util.jms.JmsSender;

/**
 * JMS sender class for sending the Configuration request
 * to the server and waiting for the response.
 * 
 * @author Mark Brightwell
 *
 */
public class ConfigRequestSender {

  /**
   * The JMS sender.
   */
  @Autowired
  private JmsSender jmsSender;
  
  /**
   * Config listener destination on the server.
   */
  private String destination;
  
  /**
   * JmsSender timeout setting.
   */
  private int requestTimeout;
  
  /**
   * The converter class for encoding the request in XML.
   */
  @Autowired
  private ConfigurationRequestConverter configurationRequestConverter;
  
  /**
   * Sends a request to perform the configuration with the given id.
   * 
   * @param configId the id of the configuration to apply
   * @return the XML report with details of how the configuration proceeded
   * @throws ParserConfigurationException
   * @throws TransformerException
   */
  public String sendConfigRequest(final int configId) throws ParserConfigurationException, TransformerException {
    ConfigurationRequest request = new ConfigurationRequest(configId);
    String xmlMessage = configurationRequestConverter.toXML(request);    
    String reply = jmsSender.sendRequestToQueue(xmlMessage, destination, requestTimeout);    
    return reply;
  }

  /**
   * Setter.
   * @param requestTimeout timeout for the config request response
   */
  public void setRequestTimeout(final int requestTimeout) {
    this.requestTimeout = requestTimeout;
  }

  /**
   * @param destination the destination to set
   */
  public void setDestination(final String destination) {
    this.destination = destination;
  }
  
}

