package cern.c2mon.daq.common.startup;

import org.apache.log4j.Logger;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;


import cern.c2mon.daq.tools.CommandParamsHandler;


/**
 * The main DAQ start up class, adapted for start up as a web application.
 * 
 * TODO should unify all start up class in a single class, with a start up
 * option passed as command line argument. Will do this once we have a clearer
 * picture of the different start up options that are required. Currently only
 * support the usual and test web. 
 * 
 * Parses the command line arguments and properties file, and loads the Spring context.
 * 
 * The only difference with the usual start up is that log4j is not configured in this class
 * as this is done by the web application. The only difference is that the configureLogging()
 * method only sets the local logger for this class.
 * 
 * @author mbrightw
 *
 */
public final class DaqWebStartup {

  /**
   * The log4j logger.
   */
  private static Logger logger;  
  
  /**
   * Reference to the DAQ Spring context. This is then used by the web application
   * to access the beans (driverKernel in particular). Is set in the main method.
   */
  private static ApplicationContext applicationContext;
  
  /**
   * Override the default public constructor.
   */
  private DaqWebStartup() {    
  }
  
  /**
   * MAIN METHOD SHOULD BE KEPT SAME ACROSS ALL DAQ START UP CLASSES FOR CONSISTENCY!
   * IF NECESSARY, INTRODUCE NEW STATIC METHODS FOR CUSTOMIZATION.
   * 
   * The DAQ main start up method. Accesses the required command line arguments, parses the properties
   * file and loads the Spring context.
   * 
   * The properties are loaded from .tim.properties in the user home directory unless specified otherwise
   * with the -basicConf command line argument.
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
       System.out.println("******************************************************************");
       System.out.println("**                TIM Data Acquisition                          **");
       System.out.println("** usage :                                                      **");
       System.out.println("** java DriverKernel [-basicConf basicConfFilePath]             **");
       System.out.println("**                   (defaults to .c2mon.properties in home dir)**");
       System.out.println("**                   -log4j logerConfXMLFile                    **");
       System.out.println("**                   -processName ProcessName                   **");
       System.out.println("**                   [-s saveConfXMLFile]                       **");
       System.out.println("**                   [-c ProcessconfXML]                        **");
       System.out.println("**                   [-eqLoggers]                               **");
       System.out.println("**                   [-eqAppendersOnly]                         **");
       System.out.println("**                   [-t | -testMode]                           **");
       System.out.println("**                   [-nf | -nofilter]                          **");
       System.out.println("******************************************************************");
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
    //(defaults to .tim.properties in user home directory)
    GenericBeanDefinition propertiesFactoryBean = new GenericBeanDefinition();
    propertiesFactoryBean.setBeanClass(PropertiesFactoryBean.class);
    MutablePropertyValues propertyValues = new MutablePropertyValues();
    if (commandParams.hasParam("-basicConf")) {
      propertyValues.addPropertyValue("location", "file:" + commandParams.getParamValue("-basicConf"));
    } else {
      propertyValues.addPropertyValue("location", "file:" + System.getProperty("user.home") + "/.tim.properties");
    }
    
    propertiesFactoryBean.setPropertyValues(propertyValues);    

    //start an initial Spring application context and register these beans
    GenericApplicationContext ctx = new GenericApplicationContext();
    ctx.registerBeanDefinition("commandParamsHandler", commandParamsBean);
    ctx.registerBeanDefinition("daqProperties", propertiesFactoryBean);        
    ctx.refresh();
    
    //start the DAQ from the Spring XML files (the XML files also specify the packages that need
    //scanning for Bean definitions
    ClassPathXmlApplicationContext xmlContext = new ClassPathXmlApplicationContext(new String[] {"resources/daq-core-service.xml", "resources/daq-activemq.xml"}, ctx);
    applicationContext = xmlContext;
    
  }
  
  
  /**
   * Log4j is already configured in Spring web application, so not needed here.
   * @param commandParams the command line arguments object
   */
  private static void configureLogging(final CommandParamsHandler commandParams) {
    logger = Logger.getLogger(DaqWebStartup.class);
  }

  /**
   * Getter method used in the web application to retrieve the Spring context
   * in which the DAQ is running.
   * @return the applicationContext
   */
  public static ApplicationContext getApplicationContext() {
    return applicationContext;
  }
  
}
