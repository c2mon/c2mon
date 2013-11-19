package cern.c2mon.shared.client.request;


/**
 * This interface is used to indicate the progress of a <code>ClientRequest</code>
 * in the server side. 
 * 
 * @author ekoufaki
 */
public interface ClientRequestProgressReport extends ClientRequestResult {
  
  /**
   * Every progress report consists of a number of operations.
   * @return How many operations to expect for this progress report.
   */
  int getTotalOperationsCount();
  
  /**
   * @return The current operation.
   */
  int getCurrentOperation();
  
  /**
   * @return How many parts to expect for this progress report.
   * Refers to the current operation.
   */
  int getTotalProgressParts();
  
  /**
   * @return The current progress
   * Refers to the current operation.
   */
  int getCurrentProgressPart();
  
  /**
   * Optional.
   * @return a description of the current progress stage
   */
  String getProgressDescription();
}
