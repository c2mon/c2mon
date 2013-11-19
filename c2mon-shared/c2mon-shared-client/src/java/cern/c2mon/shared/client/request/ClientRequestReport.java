package cern.c2mon.shared.client.request;

/**
 * Abstract class implementing the <code>ClientRequestProgressReport</code> interface 
 * which is used to indicate the progress of a<code>ClientRequest</code>,
 * and the <code>ClientRequestErrorReport</code> interface
 * which is used to indicate whether the <code>ClientRequest</code>
 * was executed successfully or not in the server side.
 * 
 * <p>When designing a class for transmitting the response to client requests,
 * extend this class to add error and progress reporting.
 *
 * @author ekoufaki
 */
public abstract class ClientRequestReport implements ClientRequestProgressReport
   , ClientRequestErrorReport {
  
  /**
   * Type of the report: is it a progress report, error report or the final result.
   * @author Mark Brightwell
   *
   */
  private enum ReportType {PROGRESS, ERROR, RESULT};
  
  /**
   * The report type.
   */
  private ReportType reportType;
  
  /** 
   * Every progress report consists of a number of operations.
   * Used only in case of a <code>ClientRequestProgressReport</code>. 
   */
  private final int totalOperations;  
  
  /** 
   * The current operation.
   * Used only in case of a <code>ClientRequestProgressReport</code>. 
   */
  private final int currentOperation;  
  
  /** 
   * A description of what is happening in the current operation.
   * Used only in case of a <code>ClientRequestProgressReport</code>. 
   */
  private final String progressDescription;

  /** 
   * How many parts to expect for this progress report.
   * Used only in case of a <code>ClientRequestProgressReport</code>. 
   */
  private final int totalParts;  

  /** 
   * The current progress 
   * Used only in case of a <code>ClientRequestProgressReport</code>. 
   */
  private final int currentPart;
  
  /** 
   * The error Message. 
   * Used only in case of an <code>ClientRequestErrorReport</code>. 
   */
  private final String errorMessage;  

  /** 
   * The RequestExecutionStatus. 
   * Used only in case of an <code>ClientRequestErrorReport</code>. 
   */
  private final RequestExecutionStatus requestStatus;
  
  /**
   * If this is neither a <code>ClientRequestProgressReport</code>
   * nor a <code>ClientRequestErrorReport</code>.
   */
  public ClientRequestReport() {
    
    reportType = ReportType.RESULT;
    
    this.requestStatus = null;
    this.errorMessage = null;
    
    this.currentPart = 0;
    this.totalParts = 0;
    this.totalOperations = 0;
    this.currentOperation = 0;
    this.progressDescription = null;
  }
  
  /**
   * Constructs a <code>ClientRequestProgressReport</code>.
   * @param pTotalOperations How many operations to expect for this progress report.
   * @param pCurrentOperation The current operation
   * @param pTotalParts How many parts to expect for this progress report.
   * @param pCurrentPart The current progress
   * @param pDescription a description of what is happening
   */
  public ClientRequestReport(
      final int pTotalOperations,
      final int pCurrentOperation,
      final int pTotalParts, 
      final int pCurrentPart, 
      final String pDescription) {
    
    reportType = ReportType.PROGRESS;
    
    this.currentPart = pCurrentPart;
    this.totalParts = pTotalParts;
    this.progressDescription = pDescription;
    this.totalOperations = pTotalOperations;
    this.currentOperation = pCurrentOperation;
    
    // In case of a Progress Report these fields are not used.
    this.requestStatus = null;
    this.errorMessage = null;
  }
  
  /**
   * Constructs a <code>ClientRequestErrorReport</code>.
   * This constructor needs specifying whether the request executed successfully or not.
   * @param pExecutedSuccessfully True if the client request was executed successfully,
   * false otherwise. 
   * @param pErrorMessage Describes the error that occured in the server side. 
   * In case the execution was successfull, the error message can be left null.
   * @see RequestExecutionStatus
   */
  public ClientRequestReport(final boolean pExecutedSuccessfully, final String pErrorMessage) {
    
    reportType = ReportType.ERROR;
    
    if (pExecutedSuccessfully)
      this.requestStatus = RequestExecutionStatus.REQUEST_EXECUTED_SUCCESSFULLY;
    else
      this.requestStatus = RequestExecutionStatus.REQUEST_FAILED;
    
    this.errorMessage = pErrorMessage;
    
    // In case of a ClientRequestErrorReport these fields are not used.
    this.currentPart = 0;
    this.totalParts = 0;    
    this.totalOperations = 0;
    this.currentOperation = 0;
    this.progressDescription = null;
  }
  
  @Override
  public int getTotalProgressParts() {
    if (!reportType.equals(ReportType.PROGRESS))
      throw new UnsupportedOperationException("This method is only supported for progress reports!");
    return totalParts;
  }

  @Override
  public int getCurrentProgressPart() {
    if (!reportType.equals(ReportType.PROGRESS))
      throw new UnsupportedOperationException("This method is only supported for progress reports!");
    return currentPart;
  }

  @Override
  public String getProgressDescription() {
    if (!reportType.equals(ReportType.PROGRESS))
      throw new UnsupportedOperationException("This method is only supported for progress reports!");
    return progressDescription;
  }

  @Override
  public boolean executedSuccessfully() {
    if (!reportType.equals(ReportType.ERROR))
      throw new UnsupportedOperationException("This method is only supported for error reports!");
    return getRequestExecutionStatus() == RequestExecutionStatus.REQUEST_EXECUTED_SUCCESSFULLY;
  }

  @Override
  public String getErrorMessage() {
    if (!reportType.equals(ReportType.ERROR))
      throw new UnsupportedOperationException("This method is only supported for error reports!");
    return errorMessage;
  }

  @Override
  public RequestExecutionStatus getRequestExecutionStatus() {
    if (!reportType.equals(ReportType.ERROR))
      throw new UnsupportedOperationException("This method is only supported for error reports!");
    return requestStatus;
  }
  
  @Override
  public int getCurrentOperation() {
    if (!reportType.equals(ReportType.PROGRESS))
      throw new UnsupportedOperationException("This method is only supported for progress reports!");
    return currentOperation;
  }
  
  @Override
  public int getTotalOperationsCount() {
    if (!reportType.equals(ReportType.PROGRESS))
      throw new UnsupportedOperationException("This method is only supported for progress reports!");
    return totalOperations;
  }
  
  /**
   * @return True if this is a <code>ClientRequestProgressReport</code>.
   * False otherwise (for example in case of <code>ClientRequestErrorReport</code>).
   */
  public boolean isProgressReport() {
    return reportType.equals(ReportType.PROGRESS);
  }
  
  /**
   * @return True if this is a <code>ClientRequestErrorReport</code>.
   * False otherwise (for example in case of <code>ClientRequestProgressReport</code>).
   */
  public boolean isErrorReport() {    
    return reportType.equals(ReportType.ERROR);
  }

  /**
   * @return True if this is the result of the request. 
   * False otherwise (for example in the cases of a <code>ClientRequestProgressReport</code> 
   * or a <code>ClientRequestErrorReport</code>).
   * 
   * @see ClientRequestProgressReport
   * @see ClientRequestErrorReport
   * @see ClientRequestReport
   */
  public boolean isResult() {      
    return reportType.equals(ReportType.RESULT);
  }
}
