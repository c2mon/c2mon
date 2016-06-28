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
package cern.c2mon.client.core.configloader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.shared.client.configuration.ConfigurationRequest;
import cern.c2mon.shared.client.configuration.ConfigurationRequestConverter;
import cern.c2mon.shared.util.jms.JmsSender;

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

