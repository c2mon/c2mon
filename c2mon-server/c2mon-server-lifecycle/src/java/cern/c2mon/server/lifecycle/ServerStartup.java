/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2005-2010 CERN.
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
package cern.c2mon.server.lifecycle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * The class containing the main method for starting a server.
 * It loads the Spring XML files (one for each module).
 *
 * Distributed configuration details are kept in the ClusterCache
 * Spring bean (in server-cache).
 *
 * The following system properties are available:
 *
 *  -Dlog4j.configuration         - location of the log4j configuration file                                REQUIRED
 *  -Dc2mon.home                  - home directory of the server (used for conf & log location ...)         REQUIRED
 *  -Dc2mon.properties.location   - location of the c2mon.properties file                                   OPTIONAL
 *                                  (optional - default is .c2mon.properties in the user home directory)
 *  -DtestMode                    - starts the Server in test mode. Accept all incoming updates no matter   OPTIONAL
 *                                  the PIK and allows normal startup of test DAQs ignoring production
 *                                  DAQs updates (true/false)
 *
 *  The c2mon.home directory must have a "conf" subdirectory containing the following files:
 *    c2mon-modules.xml           - list of modules the server should run
 *    c2mon-datasource.xml        - a java.sql.DataSource bean used for the cache persistence
 *
 *  !You will also need to include the correct SQL driver dependency in the your final project! (server deploy module)
 *
 * @author Mark Brightwell, Nacho Vilches
 *
 */
public final class ServerStartup {

  /**
   * Class logger.
   */
  private static Logger logger = Logger.getLogger(ServerStartup.class);

  private static final String LOG4J_CONF_PROPERTY = "log4j.configuration";

  private static final String C2MON_CONF_PROPERTY = "c2mon.properties.location";

  /**
   * Override public constructor.
   */
  private ServerStartup() {
  }

  /**
   * Checks, if the most important environment variables have been set and initializes
   * them with default values.
   * However, this covers only those environment variables
   * that have to be present before the Spring context is started. The other variables
   * are initialized by Spring. See also server-lifecycle-properties.xml
   */
  private static void initProperties() {
    if (System.getProperty("c2mon.home") == null) {
      System.err.println("Please specify the C2MON home directory using Java VM argument -Dc2mon.home.");
      System.exit(-1);
    }

    if (System.getProperty("c2mon.log.dir") == null) {
      final String logDir = System.getProperty("c2mon.home") + "/log";
      System.out.println("Setting log directory to: " + logDir);
      System.setProperty("c2mon.log.dir", logDir);
    }

    //initialize log4j
    if (System.getProperty(LOG4J_CONF_PROPERTY) == null) {
      final String defaultConfigurationFile = System.getProperty("c2mon.home") + "/conf/log4j.xml";
      System.out.println("No log4j location specified with Java VM argument -Dlog4j.configuration. Trying with default location: " + defaultConfigurationFile);
      System.setProperty(LOG4J_CONF_PROPERTY, defaultConfigurationFile);
    }


    // File watchdog configuration for Log4j changes
    try {
      DOMConfigurator.configureAndWatch(System.getProperty(LOG4J_CONF_PROPERTY));
    }
    catch (Exception ex) {
      ex.printStackTrace();
      System.exit(-1);
    }

    logger.info("C2MON server startup initiated");

    //set default c2mon.properties location if not specified as Dc2mon.properties.location
    if (System.getProperty(C2MON_CONF_PROPERTY) == null) {
      System.setProperty(C2MON_CONF_PROPERTY, System.getProperty("c2mon.home") + "/conf/c2mon.properties");
    }
    logger.info("Using c2mon.properties file at: " + System.getProperty(C2MON_CONF_PROPERTY));

    // Test Mode
    if ((System.getProperty("testMode")) != null && (System.getProperty("testMode").equals("true"))) {
      logger.info("The Server is starting in TEST mode");
    }
  }

  /**
   * Initialized the Spring lifecycle context that will instantiate all C2MON beans
   */
  private static void initSpringContext() {
  //by default run in single-server mode
    List<String> cacheModeModules;
    final String cacheMode = System.getProperty("c2mon.cache.mode");

    if (cacheMode != null && cacheMode.equalsIgnoreCase("multi")) {
      logger.info("C2MON server running in distributed cache mode");
      cacheModeModules = new ArrayList<String>(Arrays.asList("cern/c2mon/server/lifecycle/config/server-lifecycle-multi.xml",
                                         "cern/c2mon/server/cache/config/server-cache-multi-server.xml"));
    } else if (cacheMode != null && cacheMode.equalsIgnoreCase("single")) {
      logger.info("C2MON server running in local cache mode (not distributed)");
      cacheModeModules = new ArrayList<String>(Arrays.asList("cern/c2mon/server/lifecycle/config/server-lifecycle-single.xml",
      "cern/c2mon/server/cache/config/server-cache-single-server.xml"));
    } else {
      // "single-nonpersistent" mode
      logger.info("C2MON server running in nonpersistent local cache mode (not distributed)");
      cacheModeModules = new ArrayList<String>(Arrays.asList("cern/c2mon/server/lifecycle/config/server-lifecycle-single.xml",
          "cern/c2mon/server/cache/config/server-cache-nonpersistent-server.xml"));
    }

    //core modules (in classpath); optional modules are imported in server-startup.xml
    List<String> coreModules = new ArrayList<String>(Arrays.asList(
                                         "cern/c2mon/server/cache/dbaccess/config/server-cachedbaccess.xml",
                                         "cern/c2mon/server/cache/loading/config/server-cacheloading.xml",
                                         "cern/c2mon/server/supervision/config/server-supervision.xml",
                                         "cern/c2mon/server/daqcommunication/in/config/server-daqcommunication-in.xml",
                                         "cern/c2mon/server/daqcommunication/out/config/server-daqcommunication-out.xml",
                                         "cern/c2mon/server/rule/config/server-rule.xml",
                                         "cern/c2mon/server/configuration/config/server-configuration.xml"
                                         ));

    coreModules.addAll(cacheModeModules);

    final ClassPathXmlApplicationContext xmlContext = new ClassPathXmlApplicationContext(coreModules.toArray(new String[0])) {

      @Override
      protected DefaultListableBeanFactory createBeanFactory() {
        final DefaultListableBeanFactory vResult = super.createBeanFactory();
        vResult.setAllowBeanDefinitionOverriding(false);
        return vResult;
        };

    };

    logger.info("Starting the beans in application context.");
    //start all components that need manually starting
    xmlContext.start();
    xmlContext.registerShutdownHook();
  }

  /**
   * Main server start-up method
   * @param args - ignored
   */
  public static void main(final String[] args) {

    initProperties();
    initSpringContext();
  }

}
