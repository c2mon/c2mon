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
package cern.tim.server.lifecycle;

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
 * Distributed configuration details are kept in the DistributedParams
 * Spring bean (in server-common).
 * 
 * The following system properties are available:
 * 
 *  -Dlog4j.configuration         - location of the log4j configuration file                                REQUIRED
 *  -Dc2mon.home                  - home directory of the server (used for conf & log location ...)         REQUIRED
 *  -Dc2mon.properties.location   - location of the c2mon.properties file                                   OPTIONAL
 *                                  (optional - default is .c2mon.properties in the user home directory)  
 *  
 *  The c2mon.home directory must have a "conf" subdirectory containing the following files:
 *    c2mon-modules.xml           - list of modules the server should run
 *    c2mon-datasource.xml        - a java.sql.DataSource bean used for the cache persistence
 *    
 *  !You will also need to include the correct SQL driver dependency in the your final project! (server deploy module)
 *  
 * @author Mark Brightwell
 *
 */
public final class ServerStartup {
  
  /**
   * Class logger.
   */
  private static Logger logger = Logger.getLogger(ServerStartup.class);  
  
  /**
   * Override public constructor.
   */
  private ServerStartup() { 
  }
  
  /**
   * Main server start-up method
   * @param args - ignored
   */
  public static void main(final String[] args) {
    
    //initialize log4j
    if (System.getProperty("log4j.configuration") == null) {
      System.err.println("Please specify log4j location using Java VM argument -Dlog4j.configuration.");
      System.exit(-1);
    }
    
    try {          
      DOMConfigurator.configureAndWatch(System.getProperty("log4j.configuration"));      
    }
    catch (Exception ex) {       
      ex.printStackTrace();            
      System.exit(-1);
    }
    
    logger.info("C2MON server startup initiated");
    
    //set default c2mon.properties location if not specified as Dc2mon.properties.location     
    if (System.getProperty("c2mon.properties.location") == null) {
      System.setProperty("c2mon.properties.location", System.getProperty("user.home") + "/.c2mon.properties");
    }
    logger.info("Using c2mon.properties file at: " + System.getProperty("c2mon.properties.location"));
    
    //by default run in single-server mode
    List<String> cacheModeModules;
    if (System.getProperty("cern.c2mon.cache.mode") != null && System.getProperty("cern.c2mon.cache.mode").equals("multi")) {
      logger.info("C2MON server running in distributed cache mode");
      cacheModeModules = new ArrayList<String>(Arrays.asList("cern/tim/server/lifecycle/config/server-lifecycle-multi.xml",         
                                         "cern/tim/server/cache/config/server-cache-multi-server.xml")); 
    } else {
      logger.info("C2MON server running in local cache mode (not distributed)");
      cacheModeModules = new ArrayList<String>(Arrays.asList("cern/tim/server/lifecycle/config/server-lifecycle-single.xml",         
      "cern/tim/server/cache/config/server-cache-single-server.xml"));
    }
    
    //core modules (in classpath); optional modules are imported in server-startup.xml
    List<String> coreModules = new ArrayList<String>(Arrays.asList(
                                         "cern/tim/server/common/config/server-common.xml",                                         
                                         "cern/tim/server/cache/dbaccess/config/server-cachedbaccess.xml",
                                         "cern/tim/server/cache/loading/config/server-cacheloading.xml",
                                         "cern/tim/server/supervision/config/server-supervision.xml",
                                         "cern/tim/server/daqcommunication/in/config/server-daqcommunication-in.xml",
                                         "cern/tim/server/daqcommunication/out/config/server-daqcommunication-out.xml",
                                         "cern/c2mon/server/configuration/config/server-configuration.xml"
                                         )); 
    
    coreModules.addAll(cacheModeModules);
    
    final ClassPathXmlApplicationContext xmlContext = new ClassPathXmlApplicationContext(coreModules.toArray(new String[0])) {
      
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
  
}
