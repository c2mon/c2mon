package cern.c2mon.client.auth.impl;

import org.apache.log4j.Logger;
import java.util.Properties;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class TimSessionManagerProperties  {
  private static final Logger LOG = Logger.getLogger(TimSessionManagerProperties.class);

  public static final String PROPERTY_FILE_NAME = "sessionmanager.properties";

  public static final String DEFAULT_TCF_NAME = "jms/client/factories/TCF";

  public static final String DEFAULT_QCF_NAME = "jms/client/factories/QCF";

  public static final String DEFAULT_REQUEST_QUEUE_NAME = "jms/client/destinations/queues/AuthRequest";

  public static final String DEFAULT_JMS_USER = "Client-Any";

  public static final String DEFAULT_JMS_PASSWORD = "Any-Client";

  public static final int DEFAULT_REQUEST_TIMEOUT = 5000;

  private String tcfName = DEFAULT_TCF_NAME;

  private String qcfName = DEFAULT_QCF_NAME;

  private String requestQueueName = DEFAULT_REQUEST_QUEUE_NAME;

  private String jmsUser = DEFAULT_JMS_USER;

  private String jmsPassword = DEFAULT_JMS_PASSWORD;

  private int requestTimeout = DEFAULT_REQUEST_TIMEOUT;

  public TimSessionManagerProperties() {
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
        this.requestQueueName = clientProperties.getProperty("clientRequestDestinationName", DEFAULT_REQUEST_QUEUE_NAME);
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
      LOG.error(new StringBuffer("init() : Unable to find properties file ").append(PROPERTY_FILE_NAME));
      LOG.info("init() : Using default values for JMS connection properties.");
    }
    catch (IOException ioe) {
      LOG.error(new StringBuffer("init() : Unable to read properties file ").append(PROPERTY_FILE_NAME));
      LOG.info("init() : Using default values for JMS connection properties.");
    }
  }

  public String getUserName() {
    return this.jmsUser;
  }

  public String getPassword() {
    return this.jmsPassword;
  }

  public String getRequestQueueJndiName() {
    return this.requestQueueName;
  }

  public String getQcfJndiName() {
    return this.qcfName;
  }

  public String getTcfJndiName() {
    return this.tcfName;
  }

  public int getRequestTimeout() {
    return this.requestTimeout;
  }
}