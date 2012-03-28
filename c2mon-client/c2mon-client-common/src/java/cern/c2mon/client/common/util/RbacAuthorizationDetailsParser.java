package cern.c2mon.client.common.util;

import java.io.IOException;

import org.apache.log4j.Logger;
import cern.tim.shared.client.command.RbacAuthorizationDetails;

/**
 * Helper class.
 * Provides a method that parses a String
 * and returns an RbacAuthorizationDetails object.
 * @author ekoufaki
 */
public final class RbacAuthorizationDetailsParser {
  
  /** Private Constructor! */
  private RbacAuthorizationDetailsParser() {
  }
  
  /**
   * RbacAuthorizationDetailsParser logger
   * */
  private static Logger logger = Logger.getLogger(RbacAuthorizationDetailsParser.class);
  
  /**
   * RbacAuthorizationDetails are provided as one string for convenience (instead of 3).
   * Given that string this method returns an RbacAuthorizationDetails Object.
   * @param encodedDetails a String that contains the RbacAuthorizationDetails. 
   * The details should be provided as 3 comma seperated strings in the following order: "Class,Device,Property"
   * Example: "TIM_APPLICATIONS,TIM_WEBCONFIG,RUN" 
   * @return an RbacAuthorizationDetails Object
   * @throws IOException In case the encodedDetails are null or encoded in a non-supported format.
   */
  public static RbacAuthorizationDetails parseRbacDetails(final String encodedDetails) throws IOException {
    
    if (encodedDetails == null)  {
      logger.error(new Error("parseRbacDetails(): RbacAuthorizationDetails == null!"));
      throw new IOException("Not able to fetch RbacAuthorizationDetails.");
    }

    String[] splitedDetails = encodedDetails.replace(" ", "").split( ",\\s*" ); // split on commas
    RbacAuthorizationDetails authDetails = null;

    if (splitedDetails.length != 3) { // RbacAuthorizationDetails should be provided as 3 comma seperated strings
      logger.error(new Error("parseRbacDetails(): error splitting details!:"
          + encodedDetails + ". Splitted in:" + splitedDetails
      ));
      throw new IOException("Not able to fetch RbacAuthorizationDetails.");
    }
    else {
      
      authDetails = new RbacAuthorizationDetails();
      authDetails.setRbacClass(splitedDetails[0]);
      authDetails.setRbacDevice(splitedDetails[1]);
      authDetails.setRbacProperty(splitedDetails[2]);
    }

    return authDetails;
  }
}
