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
package cern.c2mon.japc.publisher;

import java.io.File;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import cern.c2mon.client.core.C2monServiceGateway;

/**
 * The TIM JAPCPublisher main class
 * 
 * @author Matthias Braeger
 */
public final class JAPCPublisherKernel {
   /**  The Log4j's logger  */
   private static Logger logger = null;

   /** The parameter table that contains the startup arguments */
   private static Hashtable<String, String> commandParams = null;
   
   
   /**
    * Hidden Constructor
    */
   private JAPCPublisherKernel() {
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
         || !commandParams.containsKey("-log4j") 
         || !commandParams.containsKey("-dataTags"))  {
        System.out.println("\n****************************************************************");
        System.out.println("**                TIM JAPC Publisher ver.0.1b                 **");
        System.out.println("** usage :                                                    **");
        System.out.println("** java JAPCPublisherKernel -dataTags dataTagFile             **");
        System.out.println("**                          -log4j loggerConfXMLFile          **");
        System.out.println("****************************************************************");
        System.exit(0);
     }
     
     
     try {
      // Load log4j XML file 
      DOMConfigurator.configureAndWatch(commandParams.get("-log4j"));
      logger = Logger.getLogger(JAPCPublisherKernel.class);
      if (logger.isInfoEnabled()) {
        logger.info("[preDeploy] Configured log4j from " + commandParams.get("-log4j"));
      }
     }
     catch (Exception ex) {
       logger.fatal("Unable to load log4j configuration file : " + ex.getMessage());          
       System.exit(-1);
     }
     
     // Initialize C2MON Gateway
     C2monServiceGateway.startC2monClient();
     
     logger.info("Starting JAPCGateway...");
     final JAPCGateway gateway = new JAPCGateway();
     
     logger.debug("Subscribe all data tags that which IDs are listed in file " + commandParams.get("-dataTags"));
     if (!gateway.subscribeDataTags(new File(commandParams.get("-dataTags")))) {
       logger.fatal("Unable to successfully parese data tag file " + commandParams.get("-dataTags"));          
       System.exit(-1);
     }
   }
}