package cern.c2mon.shared.client.process;

import cern.c2mon.shared.client.request.ClientRequestResult;

/**
 * Response to Process Name Request.
 * @author Koufakis Emmanouil
 *
 */
public interface ProcessNameResponse extends ClientRequestResult {

  /** 
   * returns an error message if the request failed on the server side 
   */
  String getErrorMessage();
  
  /**
   * @return the process name as String
   */
  String getProcessName();
}
