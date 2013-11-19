package cern.c2mon.shared.client.process;

import cern.c2mon.shared.client.request.ClientRequestReport;

/**
 * Implementation of Process Name Request, wrapping the
 * process name.
 * 
 * @author Koufakis Emmanouil
 *
 */
public class ProcessNameResponseImpl extends ClientRequestReport implements ProcessNameResponse {

  /**
   * The process name.
   */
  private String processName;
 
  public ProcessNameResponseImpl(String pProcessName) {    
    this.processName = pProcessName;
  }

  /**
   * @return the processName
   */
  @Override
  public String getProcessName() {
    return processName;
  }

  /**
   * @param processName the processName to set
   */
  public void setProcessName(String processName) {
    this.processName = processName;
  }
  
}
