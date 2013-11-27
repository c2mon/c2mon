/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2013 CERN.
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
 * Author: C2MON team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.daq.common.startup;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import cern.c2mon.daq.tools.CommandParamsHandler;


/**
 * The main DAQ start up class. Parses the command line arguments and properties file, 
 * and loads the Spring context. The main Spring XML file can be specified using
 * the c2mon.daq.spring.context Java property. If not specified, the default daq-service-core.xml
 * is used in the classpath.
 * 
 * TODO should unify all start up class in a single class, with a start up
 * option passed as command line argument. Will do this once we have a clearer
 * picture of the different start up options that are required. Currently only
 * support the usual and test web. 
 * 
 * @author Mark Brightwell
 *
 */
public final class DaqStartup {

  /**
   * The log4j logger.
   */
  private static Logger logger;  
    
  /**
   * Override the default public constructor.
   */
  private DaqStartup() {
  }
  
  /**
   * The DAQ main start up method. Accesses the required command line arguments, parses the properties
   * file and loads the Spring context.
   * 
   * The properties are loaded from .c2mon.properties in the user home directory unless specified otherwise
   * with the -c2monProperties command line argument. Further properties can also be loaded using the
   * optional -daqConf option.
   * 
   * @param args the required start up arguments are -log4j and -processName
   */
  public static void main(final String[] args) {
    
    //*******************************************************
    // Parse the command line parameters to configure log4j *
    //*******************************************************
    
    CommandParamsHandler commandParams = new CommandParamsHandler(args);
    
    // make sure all obligatory parameters are specified on command line
    if (!commandParams.hasParam("-log4j")
        || !commandParams.hasParam("-processName")) {
       System.out.println();
       System.out.println("********************************************************************************");
       System.out.println("**                C2MON Data Acquisition                                       **");
       System.out.println("** usage :                                                                    **");
       System.out.println("** java DaqStartup [-c2monProperties c2mon.properties file path]                  **");
       System.out.println("**                   (defaults to .c2mon.properties in home dir)               **");
       System.out.println("**                   -daqConf common DAQ configuration file                   **");       
       System.out.println("**                   -log4j logerConfXMLFile                                  **");
       System.out.println("**                   -processName ProcessName                                 **");
       System.out.println("**                   [-s saveConfXMLFile]                                     **");
       System.out.println("**                   [-c ProcessconfXML]                                      **");
       System.out.println("**                   [-eqLoggers]                                             **");
       System.out.println("**                   [-eqAppendersOnly]                                       **");
       System.out.println("**                   [-t | -testMode]                                         **");
       System.out.println("**                   [-nf | -nofilter]                                        **");
       System.out.println("**                   [-noDeadband] (disables dynamic deadband)                **");
       System.out.println("**                   [-noPIK] (starts DAQ without asking C2MON for a Process  **");
       System.out.println("**                             Identification Key)                            **");
       System.out.println("********************************************************************************");
       System.exit(-1);
    }
    
    // set the process name (used in the log4j file name)
    System.setProperty("c2mon.process.name", commandParams.getParamValue("-processName"));
    
    configureLogging(commandParams);
    
    logger.info("Starting the DAQ process...");

    //****************************
    // Initialize Spring context *
    //****************************
    
    //load command parameters into Spring context (new bean is created - is done this way 
    // to allow for log4j initialization BEFORE starting Spring!)
    GenericBeanDefinition commandParamsBean = new GenericBeanDefinition();
    commandParamsBean.setBeanClass(CommandParamsHandler.class);
    
    ConstructorArgumentValues constructorArgs = new ConstructorArgumentValues();
    constructorArgs.addGenericArgumentValue(args);
    commandParamsBean.setConstructorArgumentValues(constructorArgs);
        
    //create Java Properties Spring bean from the provided properties file 
    //(defaults to .c2mon.properties in user home directory)
    GenericBeanDefinition propertiesFactoryBean = new GenericBeanDefinition();
    propertiesFactoryBean.setBeanClass(PropertiesFactoryBean.class);
    MutablePropertyValues propertyValues = new MutablePropertyValues();
    List<String> propertyList = new ArrayList<String>();
    if (commandParams.hasParam("-c2monProperties")) {
      propertyList.add("file:" + commandParams.getParamValue("-c2monProperties"));
      logger.info("Using c2mon.properties at " + commandParams.getParamValue("-c2monProperties"));
    } else {
      propertyList.add("file:" + System.getProperty("user.home") + "/.c2mon.properties"); 
      logger.info("Using c2mon.properties at " + System.getProperty("user.home") + "/.c2mon.properties");
    }
    if (commandParams.hasParam("-daqConf")) {
      propertyList.add("file:" + commandParams.getParamValue("-daqConf"));  
      logger.info("Using DAQ common properties at " + commandParams.getParamValue("-daqConf"));
    } else {
      logger.info("No common DAQ properties file specified.");
    }
    String[] propertyLocations = propertyList.toArray(new String[0]);
    propertyValues.addPropertyValue("locations", propertyLocations);    
    
    propertiesFactoryBean.setPropertyValues(propertyValues);    

    //start an initial Spring application context and register these beans
    GenericApplicationContext ctx = new GenericApplicationContext();
    ctx.registerBeanDefinition("commandParamsHandler", commandParamsBean);
    ctx.registerBeanDefinition("daqProperties", propertiesFactoryBean);        
    ctx.refresh();
    
    //load Spring XML either from external file or classpath default
    //for shutdown: we do not rely on the Spring shutdown hook, as the EMH disconnect methods need to be called first
    try {
      @SuppressWarnings("unused")
      ApplicationContext xmlContext;

      // If testMode there is a special configuration 
      if (commandParams.hasParam("-testMode") || commandParams.hasParam("-t")) {
        logger.info("The DAQ process is starting in TEST mode (no JMS connections will be opened, no real PIK will be requested from the server)");
        
        logger.info("Loading testMode Spring context configuration file from classpath");
        xmlContext = new ClassPathXmlApplicationContext(new String[] {"resources/daq-core-service-testmode.xml"}, ctx);
      } else {
        if (System.getProperty("c2mon.daq.spring.context") != null) {
          logger.info("Loading Spring context from external configuration file: " + System.getProperty("c2mon.daq.spring.context"));
          xmlContext = new FileSystemXmlApplicationContext(new String[] {System.getProperty("c2mon.daq.spring.context")}, ctx);
        } 
        else {        
          logger.info("Loading default Spring context configuration file from classpath");
          xmlContext = new ClassPathXmlApplicationContext(new String[] {"resources/daq-core-service.xml"}, ctx);      
        } 
      }
    } catch (Exception e) {
      logger.error("Exception caught during DAQ start up: ", e);
      e.printStackTrace();
    }        
  }
  
  
  /**
   * Configure log4j from the command arguments. This method may need modifying if log4j
   * is already configured elsewhere. It should always initialize the local logger.
   * @param commandParams the command line arguments object
   */
  private static void configureLogging(final CommandParamsHandler commandParams) {
    try {
      // Load the log4j configuration
      DOMConfigurator.configureAndWatch(commandParams.getParamValue("-log4j"));
      logger = Logger.getLogger(DaqStartup.class);     
      logger.info("[preDeploy] Configured log4j from " + commandParams.getParamValue("-log4j"));
     }
     catch (Exception ex) {
       logger.fatal("Unable to load log4j configuration file : " + ex.getMessage());
       ex.printStackTrace();            
       System.exit(-1);
     }
  }
  
}
