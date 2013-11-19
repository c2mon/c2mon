package cern.c2mon.shared.client.process;

import cern.c2mon.shared.client.request.ClientRequestResult;

/**
 * Response to DAQ XML request.
 * @author Mark Brightwell
 *
 */
public interface ProcessXmlResponse extends ClientRequestResult {

  /** 
   * returns an error message if the request failed on the server side 
   * @return description of the error
   */
  String getErrorMessage();
  
  /**
   * @return the DAQ XML as String
   */
  String getProcessXML();
 
}
