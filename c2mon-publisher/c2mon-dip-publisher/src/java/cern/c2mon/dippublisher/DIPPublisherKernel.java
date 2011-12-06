//Source file: F:\\DEVELOPMENT\\TIM.DIPPUBLISHER\\src\\ch\\cern\\tim\\dippublisher\\DIPPublisherKernel.java

// TIM DIPPublisher. CERN. All rights reserved.
//  
// T Nick:           Date:       Info:
// -------------------------------------------------------------------------
// D wbuczak    19/Sep/2004       Class generation from model
// P wbuczak    20/Sep/2004       First implementation
// -------------------------------------------------------------------------

package cern.c2mon.dippublisher;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.xerces.parsers.DOMParser;

import cern.c2mon.dippublisher.tools.CommandParamsHandler;


/**
The TIM DIPPublisher main class
 */
public class DIPPublisherKernel {
   /**
   The Log4j's logger
    */
   public static Logger logger = null;

   /**
   A reference to the DIPGateway object, responsible for data transmission via DIP
    */
   public DipGateway dipGateway = null;

   
   /**
   @roseuid 414DA10A016A
    */
   public DIPPublisherKernel(final String processName)  {
     dipGateway = new DipGateway(processName);
   }


   
   /**
   The TIM DIPPublisher's main method
   @param args
   @roseuid 414D9FFD0371
    */
   public static void main(final String[] args) {
     logger = Logger.getLogger(DIPPublisherKernel.class);

     CommandParamsHandler commandParams = new CommandParamsHandler();    
     commandParams.initialise(args);
    
     // make sure all obligatory parameters are specified on command line
     if (!commandParams.hasParam("-log4j") || !commandParams.hasParam("-dataTags") ||
         !commandParams.hasParam("-processName")) {
        System.out.println();
        System.out.println("****************************************************************");
        System.out.println("**                TIM DIP.Publisher ver.0.1b                  **");
        System.out.println("** usage :                                                    **");
        System.out.println("** java DIPPublisherKernel -dataTags dataTagXMLFile           **");
        System.out.println("**                   -log4j logerConfXMLFile                  **");
        System.out.println("**                   -processName publisherName               **");
        System.out.println("****************************************************************");
        System.exit(0);
     }

     try {
      // Load log4j xml file 
      DOMConfigurator.configureAndWatch(commandParams.getParamValue("-log4j"));
      if (logger.isInfoEnabled()){
        logger.info("[preDeploy] Configured log4j from " + commandParams.getParamValue("-log4j"));
      }
     }
     catch (Exception ex) {
       logger.fatal("Unable to load log4j configuration file : " + ex.getMessage());          
       System.exit(-1);
     }
     
     logger.info("Starting TIMDipPublisher...");

     DIPPublisherKernel dpk = new DIPPublisherKernel(commandParams.getParamValue("-processName"));
     
     logger.debug("trying to parse data tag definition xml. file=" + commandParams.getParamValue("-dataTags"));

     DOMParser parser = new DOMParser();
     try {
       parser.parse(commandParams.getParamValue("-dataTags"));
       dpk.dipGateway.parseTags(parser.getDocument());
     }
     catch (java.io.IOException ex) {
       logger.error("Could not open data-tag configuration XML file : " + commandParams.getParamValue("-dataTags"));
       System.exit(-1);
     }
     catch (org.xml.sax.SAXException ex) {
       logger.error("Could not parse data-tag configuration XML file : " + commandParams.getParamValue("-dataTags"));
       logger.error(ex);
       System.exit(-1);
     }

     try {
       logger.debug("trying to create DIP publications");
       dpk.dipGateway.startPublications();
       logger.debug("trying to create TIM.client datatags");
       dpk.dipGateway.createClientTags();
     }
     catch (Exception ex) {
       System.exit(-1);       
     }
     
   }
   
}
