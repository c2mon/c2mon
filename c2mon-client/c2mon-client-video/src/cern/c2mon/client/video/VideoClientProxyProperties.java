/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2008  CERN
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/

package cern.c2mon.client.video;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * This class provides similar to the TimClienProperties class the property
 * values for the TIM Video Viewer JMS connection handling
 * 
 * @author Matthias Braeger
 */
public class VideoClientProxyProperties {
  /** The logger instance. */
  private static final Logger LOG = Logger.getLogger(VideoClientProxyProperties.class);
  /** The default name of the property file. */
  public static final String PROPERTY_FILE_NAME = "client.properties";
  /** The default Topic factory name. */
  public static final String DEFAULT_TCF_NAME = "jms/client/factories/TCF";
  /** The default Queue factory name. */
  public static final String DEFAULT_QCF_NAME = "jms/client/factories/QCF";
  /** The default queue for sending the video requests. */
  public static final String DEFAULT_REQUEST_QUEUE_NAME = "jms/client/destinations/queues/VideoRequest";
  /** The default JMS user. */
  public static final String DEFAULT_JMS_USER = "Client-Any";
  /** The default JMS user password. */
  public static final String DEFAULT_JMS_PASSWORD = "Any-Client";
  /** The default timeout. */
  public static final int DEFAULT_REQUEST_TIMEOUT = 5000;
  /** Used to store the received topic factory name. */
  private String tcfName = DEFAULT_TCF_NAME;
  /** Used to store the received queue factory name. */
  private String qcfName = DEFAULT_QCF_NAME;
  /** Used to store the received queue name of the requestor queue. */
  private String requestQueueName = DEFAULT_REQUEST_QUEUE_NAME;
  /** Used to store the received JMS user name */
  private String jmsUser = DEFAULT_JMS_USER;
  /** Used to store the received JMS user password. */
  private String jmsPassword = DEFAULT_JMS_PASSWORD;
  /** Used to store the received request timeout value. */
  private int requestTimeout = DEFAULT_REQUEST_TIMEOUT;

  /**
   * Default Constructor
   */
  public VideoClientProxyProperties() {
    // Load client properties from file
    if (LOG.isDebugEnabled()) {
      LOG.debug(new StringBuffer("init() : loading properties from file ").append(PROPERTY_FILE_NAME));
    }
    try {
      Properties clientProperties = new Properties(); 
      InputStream cps = getClass().getClassLoader().getResourceAsStream(PROPERTY_FILE_NAME);
      if (cps != null) {
        clientProperties.load(cps);
        this.tcfName = clientProperties.getProperty("clientTopicConnectionFactoryName", DEFAULT_TCF_NAME);
        this.qcfName = clientProperties.getProperty("clientQueueConnectionFactoryName", DEFAULT_QCF_NAME);
        this.requestQueueName = clientProperties.getProperty("videoRequestDestinationName", DEFAULT_REQUEST_QUEUE_NAME);
        this.jmsUser = clientProperties.getProperty("clientJMSConnectionUserName", DEFAULT_JMS_USER);
        this.jmsPassword = clientProperties.getProperty("clientJMSConnectionPassword", DEFAULT_JMS_PASSWORD);
        String tempRequestTimeout = clientProperties.getProperty("clientRequestTimeout");
        if (tempRequestTimeout != null) {
          try {
            this.requestTimeout = Integer.parseInt(tempRequestTimeout);
          }
          catch (Exception e) {
            LOG.error("init() : Unable to parse request timeout from properties file: " + tempRequestTimeout);
            LOG.info("init() : Using default request timeout: " + DEFAULT_REQUEST_TIMEOUT);
            this.requestTimeout = DEFAULT_REQUEST_TIMEOUT;
          }
        }
        else {
          LOG.info("init() : Using default request timeout: " + DEFAULT_REQUEST_TIMEOUT);
          this.requestTimeout = DEFAULT_REQUEST_TIMEOUT;          
        }
      }
      else {
        LOG.warn(new StringBuffer("init() : Unable to find/read properties file ").append(PROPERTY_FILE_NAME));
        LOG.info("init() : Using default values for JMS connection properties.");
      }
    }
    catch (FileNotFoundException fnfe) {
      LOG.warn(new StringBuffer("init() : Unable to find properties file ").append(PROPERTY_FILE_NAME));
      LOG.info("init() : Using default values for JMS connection properties.");
    }
    catch (IOException ioe) {
      LOG.warn(new StringBuffer("init() : Unable to read properties file ").append(PROPERTY_FILE_NAME));
      LOG.info("init() : Using default values for JMS connection properties.");
    }
  }

  /**
   * @return The JMS user name
   */
  public final String getUserName() {
    return this.jmsUser;
  }

  /**
   * @return The JMS user password
   */
  public final String getPassword() {
    return this.jmsPassword;
  }

  /**
   * @return The JNDI lookup name of the JMS request queue
   */
  public final String getRequestQueueJndiName() {
    return this.requestQueueName;
  }

  /**
   * @return The queue connection factory JNDI name
   */
  public final String getQcfJndiName() {
    return this.qcfName;
  }

  /**
   * @return The topic connection factory JNDI name
   */
  public final String getTcfJndiName() {
    return this.tcfName;
  }

  /**
   * @return The request timeout
   */
  public final int getRequestTimeout() {
    return this.requestTimeout;
  }
}
