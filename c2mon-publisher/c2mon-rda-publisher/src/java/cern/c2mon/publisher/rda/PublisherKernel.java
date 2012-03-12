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
package cern.c2mon.publisher.rda;

import java.io.File;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.apache.log4j.helpers.FileWatchdog;
import org.apache.log4j.xml.DOMConfigurator;

import cern.c2mon.client.core.C2monServiceGateway;

/**
 * This class contains the main() method for starting the 
 * TIM RDA Publisher. It is reading in all program arguments,
 * initializes the RDA publisher and start the watchdog process
 * which is checking every 60 seconds whether the TID file has
 * changed.
 * 
 * @author Matthias Braeger
 */
public final class PublisherKernel {
   /**  The Log4j's logger  */
   private static Logger logger = null;

   /** The parameter table that contains the startup arguments */
   private static Hashtable<String, String> commandParams = null;
   
   /** Startup parameter for defining log4j*/
   private static final String PARAM_LOG4J = "-log4j";
   /** Startup parameter for defining the tid file location */
   private static final String PARAM_TID_FILE = "-tidfile";
   /** Startup parameter for defining the RDA Server Name */
   private static final String PARAM_RDA_SERVER_NAME = "-server";
   /** Startup parameter for defining the RDA Device Name */
   private static final String PARAM_RDA_DEVICE_NAME = "-device";
   
   /** The JAPC Gateway instance */
   private static Gateway gateway;
   
   /**
    * Checks every 60 seconds, if the tag id file has changed.
    */
   private static FileWatchdog tidWatchdog;
   
   /**
    * Hidden Constructor
    */
   private PublisherKernel() {
     // Do nothing!
   }
   
   /**
    * This method parses the command line array of arguments and tries to 
    * fill the hashtable with (param-name, param-value). 
    * 
    * @param params The arguments passed during startup.
    * @return <code>false</code>, in case of some troubles an inproper number of tokens, else
    *         <code>true</code>
    */
   private static boolean initialize(final String[] params) {
     commandParams = new Hashtable<String, String>(); 
     
     if (params.length % 2 != 0) {
       return false;
     }
     
     for (int i = 0; i < params.length; i++) {
       if (params[i].charAt(0) == '-') {
         if (i < params.length - 1) {
           if (params[i + 1].charAt(0) == '-') {
             return false;     
           }
           else {
             commandParams.put(params[i], params[i + 1]);
           }
         } //if
         else {
           return false;
         }
       } //if
     } //for
     
     return true;
   }


   
   /**
    * The TIM DIPPublisher's main method
    * @param args Must at least contain -log4j and -dataTags
    */
   public static void main(String[] args) {
     // make sure all obligatory parameters are specified on command line
     if (!initialize(args) 
         || !commandParams.containsKey(PARAM_LOG4J) 
         || !commandParams.containsKey(PARAM_TID_FILE)
         || !commandParams.containsKey(PARAM_RDA_SERVER_NAME)
         || !commandParams.containsKey(PARAM_RDA_DEVICE_NAME))  {
        System.out.println("\n****************************************************************");
        System.out.println("**                C2MON RDA Publisher                         **");
        System.out.println("** usage :                                                    **");
        System.out.println("** java PublisherKernel -tidfile tagIDFile                    **");
        System.out.println("**                      -log4j  loggerConfXMLFile             **");
        System.out.println("**                      -server rdaServerName                 **");
        System.out.println("**                      -device rdaDeviceName                 **");
        System.out.println("****************************************************************");
        System.exit(0);
     }
     
     
     try {
      // Load log4j XML file 
      DOMConfigurator.configureAndWatch(commandParams.get(PARAM_LOG4J));
      logger = Logger.getLogger(PublisherKernel.class);
      if (logger.isInfoEnabled()) {
        logger.info("[preDeploy] Configured log4j from " + commandParams.get(PARAM_LOG4J));
      }
     }
     catch (Exception ex) {
       logger.fatal("Unable to load log4j configuration file : " + ex.getMessage());          
       System.exit(-1);
     }
     
     // Initialize C2MON Gateway
     logger.info("Initialiazing C2MON Client API...");
     C2monServiceGateway.startC2monClientSynchronous();
     
     logger.info("Starting Gateway...");
     gateway = new Gateway(commandParams.get(PARAM_RDA_SERVER_NAME), commandParams.get(PARAM_RDA_DEVICE_NAME));
     
     final File tidFile = new File(commandParams.get(PARAM_TID_FILE));
     logger.debug("Subscribing to all data tags which IDs are listed in file " + tidFile.toString());
     if (!gateway.subscribeDataTags(tidFile)) {
       logger.fatal("Unable to successfully parse data tag file " + tidFile.toString());          
       System.exit(-1);
     }
     
     createTidWatchdog(tidFile);
   }
   
   /**
    * Creates a watchdog process which checks every 60 seconds if the file with provides
    * the list of tag ids has changed.
    * @param tidFile The file that contains the list of tag ids.
    */
   private static void createTidWatchdog(final File tidFile) {
     tidWatchdog = new FileWatchdog(tidFile.toString()) {
       @Override
       protected void doOnChange() {
         logger.info("TID file has changed!");
         if (!gateway.subscribeDataTags(tidFile)) {
           logger.error("Unable to successfully parse data tag file " + tidFile.toString());
         }
       }
     };
     tidWatchdog.start();
   }
}