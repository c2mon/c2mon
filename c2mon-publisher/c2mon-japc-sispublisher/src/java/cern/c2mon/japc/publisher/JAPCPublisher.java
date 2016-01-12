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
package cern.c2mon.japc.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.japc.Selector;
import cern.japc.SimpleParameterValue;
import cern.japc.ext.remote.ParameterValuePublisher;
import cern.japc.ext.remote.ServerRemoteFactory;
import cern.japc.factory.ParameterValueFactory;

import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.core.C2monServiceGateway;

/**
 * This Singleton class provides a method to publish <code>ClientDataTag</code> objects
 * to JAPC. Please notice that we curently only support <code>Boolean</code> values! 
 * 
 * @author Matthias Braeger
 */
public class JAPCPublisher {

  /** The Log4j's logger */
  private static Logger logger = LoggerFactory.getLogger(JAPCPublisher.class);

  private ServerRemoteFactory serverRemoteFactory;
  
  /** A standard selector which is need for publishing the data to JAPC */
  private Selector selector = null;
  
  /** Singleton instance of this class */
  private static JAPCPublisher japcPublisher = null;
  
  /** The service name that shall be used to publish the TIM data to JAPC*/
  private static final String SERVICE_NAME = "tim";
  
  /** Device name which is mandatory for the publication to JAPC */
  private static final String DEVICE_NAME = "TIM_DEVICE/";

  
  ////////////////////////////////////////
  // The JAPC values that are published //
  ////////////////////////////////////////

  /** String that is published in case that the data tag is invalid */
  private static final String INVALID = "INVALID";

  /** String that is published in case that the data tag value is <code>true</code> */
  private static final String TRUE = "TRUE";

  /** String that is published in case that the data tag value is <code>false</code> */
  private static final String FALSE = "FALSE";

  /** String that is published in case that the data tag is uninitialized */
  private static final String UNINITIALIZED = "UNINITIALIZED";
  
  /** String that is published in case that the data tag is not known by the server */
  private static final String UNKNOWN = "UNKNOWN";

  
  /**
   * Hidden Default Constructor
   */
  private JAPCPublisher() {
    
    this.serverRemoteFactory = new ServerRemoteFactory("no", SERVICE_NAME);
    this.selector = ParameterValueFactory.newSelector("");
  }
  
  public static JAPCPublisher getInstance() {
    
    if (japcPublisher == null)
      japcPublisher = new JAPCPublisher();
    
    return japcPublisher;
  }
  

  /**
   * Publishes the data to JAPC
   * 
   * @param clientDataTag
   *          the data tag update to be published on JAPC
   */
  public void publish(final ClientDataTagValue clientDataTag) {
    logger.debug("entering publish()...");
    
    String value = null; 

    if (clientDataTag != null) {
      if (!clientDataTag.getDataTagQuality().isExistingTag())
        value = UNKNOWN;
      else if (!clientDataTag.getDataTagQuality().isInitialised())
        value = UNINITIALIZED;
      else if (clientDataTag.getType() != null && clientDataTag.getType().equals(Boolean.class)) {
        if (!clientDataTag.isValid())
          value = INVALID;
        else if ((Boolean) clientDataTag.getValue())
          value = TRUE;
        else
          value = FALSE;
      } else {
        logger.warn("Data tag of type " + clientDataTag.getType()
            + " is not supported, yet.");
      }
      
      if (value != null) {
        logger.debug("Data tag " + clientDataTag.getId() + " is " + value);
        ParameterValuePublisher publisher = serverRemoteFactory.getPublisher(DEVICE_NAME + clientDataTag.getId());
        logger.debug("Created ParameterValuePublisher with parameterName " + DEVICE_NAME + clientDataTag.getId());
        SimpleParameterValue spv = ParameterValueFactory.newParameterValue(value);
        try {
          publisher.publish(selector, spv);
        } catch (RuntimeException ex) {
          logger.error(
              "An error occured while trying to publish the value for data tag " 
              + clientDataTag.getId(), ex);
        }
      }
    }
    else {
      logger.debug("ClientDataTag object is null --> Do nothing!");
    }
 
    logger.debug("leaving publish()");
  }
}
