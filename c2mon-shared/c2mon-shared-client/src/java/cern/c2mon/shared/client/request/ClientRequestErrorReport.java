package cern.c2mon.shared.client.request;


/**
 * This interface is used to indicate whether the <code>ClientRequest</code>
 * was executed successfully or not in the server side. Therefore it 
 * supports the following types:
 * <li> <b>REQUEST_EXECUTED_SUCCESSFULLY</b>: In case the request
 * was executed in the server without any problems.
 * <li> <b>REQUEST_FAILED</b>: In case the request failed.
 *
 * @author ekoufaki
 */
public interface ClientRequestErrorReport {

  /**
   * Enumeration for specifying the RequestExecutionStatus:
   *
   * @author ekoufaki
   */
  enum RequestExecutionStatus {
    /** 
     * In case the request was executed in
     * the server without any problems.
     */
    REQUEST_EXECUTED_SUCCESSFULLY,
    /** 
     * In case the request failed.
     */
    REQUEST_FAILED
  };
  
  /**
   * Returns a description of the Error that occured in the server,
   * while executing the ClientRequest.
   * 
   * @return Error description.
   */
  String getErrorMessage();
  
  /**
   * 
   * @return True if the command was executed successfully.
   * @see RequestExecutionStatus
   */  
  boolean executedSuccessfully();

  /**
   * This method returns the Request Execution Status
   * @return Request Execution Status
   * @see RequestExecutionStatus
   */
  RequestExecutionStatus getRequestExecutionStatus();
}
