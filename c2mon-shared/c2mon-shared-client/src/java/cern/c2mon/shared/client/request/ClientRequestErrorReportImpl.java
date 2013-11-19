package cern.c2mon.shared.client.request;

import javax.validation.constraints.NotNull;

import cern.c2mon.shared.util.json.GsonFactory;

import com.google.gson.Gson;

/**
 * This class implements the <code>ClientRequestErrorReport</code> interface 
 * which is used to indicate whether the <code>ClientRequest</code>
 * was executed successfully or not.
 *
 * @author ekoufaki
 */
public class ClientRequestErrorReportImpl implements ClientRequestErrorReport {
  
  /** The error Message */
  private final String errorMessage;  

  /** The RequestExecutionStatus */
  @NotNull
  private final RequestExecutionStatus requestStatus;
  
  /**
   * Hidden constructor for Json
   */
  @SuppressWarnings("unused")
  private ClientRequestErrorReportImpl() {
    requestStatus = null;
    errorMessage = "";
  }
  
  /**
   * Default Constructor needs specifying whether the request executed successfully or not.
   * @param pExecutedSuccessfully True if the client request was executed successfully,
   * false otherwise. 
   * @param pErrorMessage Describes the error that occured in the server side. 
   * In case the execution was successfull, the error message can be left null.
   * @see RequestExecutionStatus
   */
  public ClientRequestErrorReportImpl(final boolean pExecutedSuccessfully, final String pErrorMessage) {
    
    if (pExecutedSuccessfully)
      this.requestStatus = RequestExecutionStatus.REQUEST_EXECUTED_SUCCESSFULLY;
    else
      this.requestStatus = RequestExecutionStatus.REQUEST_FAILED;
    
    this.errorMessage = pErrorMessage;
  }

  @Override
  public boolean executedSuccessfully() {
    
    return getRequestExecutionStatus() == RequestExecutionStatus.REQUEST_EXECUTED_SUCCESSFULLY;
  }

  @Override
  public String getErrorMessage() {
    return errorMessage;
  }

  @Override
  public RequestExecutionStatus getRequestExecutionStatus() {
    
    return requestStatus;
  }
}
