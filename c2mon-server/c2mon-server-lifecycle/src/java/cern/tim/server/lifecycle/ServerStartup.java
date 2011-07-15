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
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

/**
 * The class containing the main method for starting a server.
 * It loads the Spring XML files (one for each module). 
 * 
 * Distributed configuration details are kept in the DistributedParams
 * Spring bean (in server-common).
 * 
 * The main method can be called with the following arguments:
 * 
 *  -Dlog4j.configuration         - location of the log4j configuration file (compulsory)
 *  -Dc2mon.properties.location     - location of the c2mon.properties file 
 *                                  (optional - default is .c2mon.properties in the user home directory) 
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
   * Terracotta root with distributed configuration data.
   */
  //private static HashMap<String,Object> serverConfig;
  //private static DistributedParams distributedParams;
  
  /**
   * Main server start-up method
   * @param args start-up args
   */
  public static void main(final String[] args) {
    
    //load the Spring context from the XML file
    //(must be done after the DistributedParams have been initialized, as some of the Spring
    // beans will need these at startup - Spring should manage this dependency on the DistributedParams bean)
    
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
     
    //set default c2mon.properties location if not specified as Dc2mon.properties.location     
    if (System.getProperty("c2mon.properties.location") == null) {
      System.setProperty("c2mon.properties.location", System.getProperty("user.home") + "/.c2mon.properties");
    }
    logger.info("Using c2mon.properties file at: " + System.getProperty("c2mon.properties.location"));
    
    String confLocation = "file:" + System.getProperty("c2mon.home") + "/conf";
    
    String[] coreProperties = new String[]{"file:" + System.getProperty("c2mon.properties.location"),
                                           confLocation + "/c2mon-jms.properties",
                                           confLocation + "/c2mon-datasource.properties",
                                           confLocation + "/c2mon-cache.properties"};
    
    GenericBeanDefinition propertiesFactoryBean = new GenericBeanDefinition();
    propertiesFactoryBean.setBeanClass(PropertiesFactoryBean.class);
    MutablePropertyValues propertyValues = new MutablePropertyValues();
    propertyValues.addPropertyValue("locations", coreProperties);
    propertiesFactoryBean.setPropertyValues(propertyValues);    
    
    //start an initial Spring application context and register properties bean
    GenericApplicationContext ctx = new GenericApplicationContext();    
    ctx.registerBeanDefinition("serverProperties", propertiesFactoryBean);        
    ctx.refresh();
    
    Properties serverProperties = (Properties) ctx.getBean("serverProperties");
    for (Map.Entry<Object, Object> entry : serverProperties.entrySet()) {
      System.setProperty((String) entry.getKey(), (String) entry.getValue());
    }   
        
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
    
    final ClassPathXmlApplicationContext xmlContext = new ClassPathXmlApplicationContext(coreModules.toArray(new String[0]), ctx);
       
    logger.info("Starting the beans in application context.");
    //start all components that need manually starting
    xmlContext.start();
    
    //"cern/tim/server/command/config/server-command.xml"
//                                                                                                  Diamon module
    //"resources/application-context-with-tim-server.xml"
    //"cern/tim/server/benchmark/config/server-benchmark.xml",                                                                                          
    
    //add shutdown hook to context (all @PreDestroy methods will then be called)
    xmlContext.registerShutdownHook();
//    Runtime.getRuntime().addShutdownHook(new Thread() {
//      public void run() {
//        xmlContext.stop();
//        xmlContext.close();
//      }
//    });
    
  }

  
  
  //****************
  // HELPER METHODS 
  //****************
  
  //not used so far
  /**
   * Converts the Java properties object into the configuration
   * HashMap, mapping Strings to the objects as needed.
   */
//  private static HashMap<String,Object> propertiesToMap(Properties properties) {
//    return null;
//  }

}
