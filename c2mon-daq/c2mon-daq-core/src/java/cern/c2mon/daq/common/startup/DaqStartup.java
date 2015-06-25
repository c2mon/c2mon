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

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
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
 * The main DAQ start up class. Parses the command line arguments and properties
 * file, and loads the Spring context. The main Spring XML file can be specified
 * using the c2mon.daq.spring.context Java property. If not specified, the
 * default daq-service-core.xml is used in the classpath.
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
   * The DAQ main start up method. Accesses the required command line arguments,
   * parses the properties file and loads the Spring context.
   * 
   * The properties are loaded from .c2mon.properties in the user home directory
   * unless specified otherwise with the -c2monProperties command line argument.
   * Further properties can also be loaded using the optional -daqConf option.
   * 
   * @param args the required start up arguments are -log4j and -processName
   */
  public static void main(final String[] args) {

    // *******************************************************
    // Parse the command line parameters to configure log4j *
    // *******************************************************

    CommandParamsHandler commandParams = new CommandParamsHandler(args);

    // make sure all obligatory parameters are specified on command line
    if (!commandParams.hasParam("processName")) {
      System.out.println();
      System.out.println("********************************************************************************");
      System.out.println("**                C2MON Data Acquisition                                      **");
      System.out.println("** usage :                                                                    **");
      System.out.println("** java DaqStartup -processName ProcessName                                   **");
      System.out.println("**                 [-c2monProperties c2mon.properties file path]              **");
      System.out.println("**                 [-daqConf common DAQ configuration file]                   **");
      System.out.println("**                 [-log4j logerConfXMLFile]                                  **");
      System.out.println("**                 [-s saveConfXMLFile]                                       **");
      System.out.println("**                 [-c ProcessconfXML]                                        **");
      System.out.println("**                 [-eqLoggers]                                               **");
      System.out.println("**                 [-eqAppendersOnly]                                         **");
      System.out.println("**                 [-t | -testMode]                                           **");
      System.out.println("**                 [-nf | -nofilter]                                          **");
      System.out.println("**                 [-noDeadband] (disables dynamic deadband)                  **");
      System.out.println("********************************************************************************");
      System.exit(-1);
    }

    // set the process name (used in the log4j file name)
    System.setProperty("c2mon.process.name", commandParams.getParamValue("processName"));

    configureLogging(commandParams);

    logger.info("Starting the DAQ process...");

    // ****************************
    // Initialize Spring context *
    // ****************************

    // load command parameters into Spring context (new bean is created - is
    // done this way
    // to allow for log4j initialization BEFORE starting Spring!)
    GenericBeanDefinition commandParamsBean = new GenericBeanDefinition();
    commandParamsBean.setBeanClass(CommandParamsHandler.class);

    ConstructorArgumentValues constructorArgs = new ConstructorArgumentValues();
    constructorArgs.addGenericArgumentValue(args);
    commandParamsBean.setConstructorArgumentValues(constructorArgs);

    // create Java Properties Spring bean from the provided properties file
    // (defaults to .c2mon.properties in user home directory)
    GenericBeanDefinition propertiesFactoryBean = new GenericBeanDefinition();
    propertiesFactoryBean.setBeanClass(PropertiesFactoryBean.class);
    MutablePropertyValues propertyValues = new MutablePropertyValues();
    List<String> propertyList = new ArrayList<String>();

    // ****************** Determine c2mon.properties file location **********************
    if (commandParams.hasParam("c2monProperties")) {
      propertyList.add("file:" + commandParams.getParamValue("c2monProperties"));
      logger.info("Configured C2MON properties from " + commandParams.getParamValue("c2monProperties"));
    }
    else {
      Path propertyFile = Paths.get(getDefaultConfPath() + "/c2mon.properties");

      if (propertyFile.toFile().exists()) {
        propertyList.add("file:" + propertyFile.toString());
        logger.info("Configured C2MON properties from " + propertyFile.toString());
      }
      else {
        String errMsg1 = "Default C2MON configuration file does not exist: " + propertyFile.toString();
        String errMsg2 = "Try instead specifying the property file with the -c2monProperties option";
        logger.fatal(errMsg1);
        logger.info(errMsg2);
        System.err.println(errMsg1);
        System.err.println(errMsg2);
        System.exit(-1);
      }
    }

    // ****************** Determine daq.conf file location **********************
    if (commandParams.hasParam("daqConf")) {
      propertyList.add("file:" + commandParams.getParamValue("daqConf"));
      logger.info("Configured DAQ common properties from " + commandParams.getParamValue("daqConf"));
    }
    else {
      Path daqConfFile = Paths.get(getDefaultConfPath() + "/daq.conf");

      if (daqConfFile.toFile().exists()) {
        propertyList.add("file:" + daqConfFile.toString());
        logger.info("Configured DAQ common properties from " + daqConfFile.toString());
      }
      else {
        String errMsg1 = "DAQ common properties file does not exist: " + daqConfFile.toString();
        String errMsg2 = "Try instead specifying the property file with the -daqConf option";
        logger.fatal(errMsg1);
        logger.info(errMsg2);
        System.err.println(errMsg1);
        System.err.println(errMsg2);
        System.exit(-1);
      }
    }

    String[] propertyLocations = propertyList.toArray(new String[0]);
    propertyValues.addPropertyValue("locations", propertyLocations);

    propertiesFactoryBean.setPropertyValues(propertyValues);

    // start an initial Spring application context and register these beans
    GenericApplicationContext ctx = new GenericApplicationContext();
    ctx.registerBeanDefinition("commandParamsHandler", commandParamsBean);
    ctx.registerBeanDefinition("daqProperties", propertiesFactoryBean);
    ctx.refresh();

    // load Spring XML either from external file or classpath default
    // for shutdown: we do not rely on the Spring shutdown hook, as the EMH
    // disconnect methods need to be called first
    try {
      @SuppressWarnings("unused")
      ApplicationContext xmlContext;

      // If testMode there is a special configuration
      if (commandParams.hasParam("testMode") || commandParams.hasParam("t")) {
        logger.info("The DAQ process is starting in TEST mode (no JMS connections will be opened, no real PIK will be requested from the server)");

        logger.info("Loading testMode Spring context configuration file from classpath");
        xmlContext = new ClassPathXmlApplicationContext(new String[] { "resources/daq-core-service-testmode.xml" }, ctx);
      }
      else {
        if (System.getProperty("c2mon.daq.spring.context") != null) {
          logger.info("Loading Spring context from external configuration file: " + System.getProperty("c2mon.daq.spring.context"));
          xmlContext = new FileSystemXmlApplicationContext(new String[] { System.getProperty("c2mon.daq.spring.context") }, ctx);
        }
        else {
          logger.info("Loading default Spring context configuration file from classpath");
          xmlContext = new ClassPathXmlApplicationContext(new String[] { "resources/daq-core-service.xml" }, ctx);
        }
      }
    }
    catch (Exception e) {
      logger.error("Exception caught during DAQ start up: ", e);
      e.printStackTrace();
    }
  }

  /**
   * Helper method to compute the default conf path location
   * 
   * @return The default conf/ directory path
   */
  private static String getDefaultConfPath() {
    Path confPath = Paths.get(getHomePath().concat("/conf/"));
    String errMsg2 = "Try instead specifying the property files with -c2monProperties and -daqConf";
    
    if (confPath.toFile().exists()) {
      try {
        return confPath.toRealPath().toString();
      }
      catch (IOException e) {
        String errMsg1 = "Could not generate real path to " + confPath.toString();
        if (logger != null) {
          logger.fatal(errMsg1);
          logger.info(errMsg2);
        }
        System.err.println(errMsg1);
        System.err.println(errMsg2);
        System.exit(-1);
      }
    }

    String errMsg1 = "Default configuration directory does not exist: " + confPath.toString();
    if (logger != null) {
      logger.fatal(errMsg1);
      logger.info(errMsg2);
    }
    System.err.println(errMsg1);
    System.err.println(errMsg2);
    System.exit(-1);

    // never reached
    return null;
  }
  
  /**
   * Helper method to compute the default conf path location
   * 
   * @return The default conf/ directory path
   */
  private static String getDefaultLogPath() {
    Path confPath = Paths.get(getHomePath().concat("/log/"));
    String errMsg2 = "Try instead specifying the log path with -Dc2mon.log.dir";
    if (confPath.toFile().exists()) {
      try {
        return confPath.toRealPath().toString();
      }
      catch (IOException e) {
        String errMsg1 = "Could not generate real log/ path from " + confPath.toString();
        System.err.println(errMsg1);
        System.err.println(errMsg2);
        System.exit(-1);
      }
    }

    String errMsg1 = "Default log directory does not exist: " + confPath.toString();
    System.err.println(errMsg1);
    System.err.println(errMsg2);
    System.exit(-1);

    // never reached
    return null;
  }

  /**
   * Helper method to HOME path location of the DAQ process
   * 
   * @return The default conf/ directory path
   */
  private static String getHomePath() {
    URL location = DaqStartup.class.getProtectionDomain().getCodeSource().getLocation();
    String locationPath = location.getPath();
    String osAppropriatePath = System.getProperty("os.name").contains("indow") ? locationPath.substring(1) : locationPath;

    Path daqHomePath = Paths.get(osAppropriatePath.concat("../"));
    if (daqHomePath.toFile().exists()) {
      try {
        return daqHomePath.toRealPath().toString();
      }
      catch (IOException e) {
        String errMsg1 = "Could not generate real path to DAQ HOME folder: " + daqHomePath.toString();
        logger.fatal(errMsg1);
        System.err.println(errMsg1);
      }
    }

    String errMsg1 = "Could not determine the DAQ HOME directory: " + daqHomePath.toString();
    String errMsg2 = "Make sure that your JARs are in the lib/ folder under the DAQ HOME directory";
    logger.fatal(errMsg1);
    logger.info(errMsg2);
    System.err.println(errMsg1);
    System.err.println(errMsg2);

    // never reached
    return daqHomePath.toString();
  }

  /**
   * Configure log4j from the command arguments. This method may need modifying
   * if log4j is already configured elsewhere. It should always initialize the
   * local logger.
   * 
   * @param commandParams the command line arguments object
   */
  private static void configureLogging(final CommandParamsHandler commandParams) {
    String log4jConfFile = null;
    if (commandParams.hasParam("log4j")) {
      log4jConfFile = commandParams.getParamValue("log4j");
    }
    else {
      Path confFile = Paths.get(getDefaultConfPath() + "/log4j.xml");

      if (confFile.toFile().exists()) {
        log4jConfFile = confFile.toString();
      }
      else {
        String errMsg1 = "Log4j default configuration file does not exist: " + confFile.toString();
        String errMsg2 = "Try instead specifying the log4j configuration file with the -log4j option";
        System.err.println(errMsg1);
        System.err.println(errMsg2);
        System.exit(-1);
      }
    }
    System.out.println("Using log4j configuration file from " + log4jConfFile);
    
    if (!System.getProperties().containsKey("c2mon.log.dir")) {
      System.setProperty("c2mon.log.dir", getDefaultLogPath());
    }
    System.out.println("Setting log directory to " + System.getProperty("c2mon.log.dir"));
    
    
    try {
      // Load the log4j configuration
      DOMConfigurator.configureAndWatch(log4jConfFile);
      logger = Logger.getLogger(DaqStartup.class);
      logger.info("Configured log4j from " + log4jConfFile);
    }
    catch (Exception ex) {
      String errMsg = "Unable to load log4j configuration file : ";
      logger.fatal(errMsg, ex);
      System.err.println(errMsg);
      ex.printStackTrace();
      System.exit(-1);
    }
  }

}
