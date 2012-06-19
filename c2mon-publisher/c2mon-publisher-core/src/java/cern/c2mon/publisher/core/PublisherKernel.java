/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2009 CERN This program is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.publisher.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cern.c2mon.client.core.C2monServiceGateway;

/**
 * This class contains the main() method for starting the Publisher. It is
 * reading in all program arguments, initializes the RDA publisher and start the
 * watchdog process which is checking every 60 seconds whether the TID file has
 * changed.
 * <p>
 * The following system properties are available:
 * <p>
 * <code>-Dlog4j.configuration</code> - location of the log4j configuration file
 * (REQUIRED)<br>
 * <code>-Dc2mon.publisher.tid.location</code> - location of the TID file containing
 * the tag ids which shall be published (REQUIRED)
 * 
 * @author Matthias Braeger
 */
public final class PublisherKernel {
  /** The Log4j's logger */
  private static Logger logger = null;

  /**
   * Environment variable that defines the location of the log4j configuration
   * file
   */
  private static final String LOG4J_CONFIGURATION_ENV = "log4j.configuration";
  
  /**
   * This environment variable can be set to point to another Spring context file that should
   * also be scanned and included in to the same context.
   */
  private static final String CUSTOM_SPRING_CONFIG_ENV = "c2mon.publisher.spring.configuration.location";
  
  /** The SPRING configuration of the publisher core */
  private static final String SPRING_CONFIG = "cern/c2mon/publisher/core/config/publisher-core.xml";

  /**
   * Hidden Constructor
   */
  private PublisherKernel() {
    // Do nothing!
  }

  /**
   * The main method for starting the publisher core
   * 
   * @param args not used
   */
  public static void main(final String[] args) {
    try {
      // Load log4j XML file
      final String log4jConfigFile = System.getProperty(LOG4J_CONFIGURATION_ENV, "conf/log4j.xml");
      DOMConfigurator.configureAndWatch(log4jConfigFile);
      logger = Logger.getLogger(PublisherKernel.class);
      if (logger.isInfoEnabled()) {
        logger.info("[preDeploy] Configured log4j from " + log4jConfigFile);
      }
    }
    catch (Exception ex) {
      logger.fatal("Unable to load log4j configuration file : " + ex.getMessage());
      System.exit(-1);
    }

    // Initialize C2MON Gateway
    logger.info("Initialiazing C2MON Client API...");
    C2monServiceGateway.startC2monClientSynchronous();
    
    startSpringContext();
  }

  /**
   * Starts publishers Spring context 
   */
  private static void startSpringContext() {
    List<String> appContext = new ArrayList<String>();
    appContext.add(SPRING_CONFIG);
    
    // Add the additional publisher Spring context, if defined by the user
    if (System.getProperty(CUSTOM_SPRING_CONFIG_ENV) != null) {
      appContext.add(System.getProperty(CUSTOM_SPRING_CONFIG_ENV));
    }
    
    
    final ClassPathXmlApplicationContext xmlContext = new ClassPathXmlApplicationContext(appContext.toArray(new String[0])) {
      /**
       * Prevents that two beans can be defined with the same name.
       */
      protected DefaultListableBeanFactory createBeanFactory() {
        final DefaultListableBeanFactory vResult = super.createBeanFactory();
        vResult.setAllowBeanDefinitionOverriding(false);
        return vResult;
      };

    };

    logger.info("Starting the beans in application context.");
    // start all components that need manually starting
    xmlContext.start();
    xmlContext.registerShutdownHook();
  }
}