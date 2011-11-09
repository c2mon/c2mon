package cern.c2mon.statistics.consumer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * The main Consumer start up class. Loads the Spring ApplicationContext.
 * 
 * Runs with options: -l log4j XML config file  *                    
 *                    -p the process name
 *                    
 * Can specify c2mon.properties file using -Dc2mon.properties
 * 
 * @author Mark Brightwell
 *
 */
public final class ConsumerStartup {
    
    /**
     * Local static logger. Configured once the log4j config file is located from the command line option.
     */
    private static Logger logger;
    
    /**
     * Private constructor.
     */
    private ConsumerStartup() {      
    }
    
    /**
     * Main method, loading the Spring context. Also parses the command line options.
     * 
     * @param args the command line options
     */
    public static void main(String [] args) {
        
        // the command line options
        Options options = new Options();        
        options.addOption("l", true, "the log4j XML configuration file");
        
        // the command line option Strings
        String loggerConfig = null;
               
        // try to parse the commandline
        try {
            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse(options, args);
            loggerConfig = cmd.getOptionValue("l");            
           
        } catch (ParseException ex) {  // parsing fails
            System.err.print("error in parsing the command line arguments");
            System.err.print("error: " + ex);
            throw new RuntimeException(ex);
        }

        // check command line options are not null
        if (loggerConfig == null) { 
            System.err.print("missing command line options");
            System.err.print("exiting...");
            throw new RuntimeException("Missing command line options.");
        } 
        
        // try to configure the logger
        try {
            // Load log4j xml file
            DOMConfigurator.configureAndWatch(loggerConfig);
            logger = Logger.getLogger(ConsumerStartup.class);
            if (logger.isInfoEnabled()) {
                logger.info("[preDeploy] Configured log4j from " + loggerConfig);
            }
        } catch (Exception ex) {   // logger configuration fails
            logger.fatal("Unable to load log4j configuration file : " + ex.getMessage());
            throw new RuntimeException(ex);            
        }
        
        AbstractApplicationContext context = new ClassPathXmlApplicationContext("resources/consumer-service.xml");
        context.registerShutdownHook();

    }

}
