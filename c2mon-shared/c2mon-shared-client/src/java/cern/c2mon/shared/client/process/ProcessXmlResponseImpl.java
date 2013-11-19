package cern.c2mon.shared.client.process;

import cern.c2mon.shared.client.request.ClientRequestReport;

/**
 * Implementation of Process XML request, wrapping the
 * DAQ XML.
 * 
 * @author Mark Brightwell
 *
 */
public class ProcessXmlResponseImpl extends ClientRequestReport implements ProcessXmlResponse {

  /**
   * The DAQ XML.
   */
  private String processXML;
 
  /**
   * @return the processXML
   */
  @Override
  public String getProcessXML() {
    return processXML;
  }

  /**
   * @param processXML the processXML to set
   */
  public void setProcessXML(String processXML) {
    this.processXML = processXML;
  }

  public ProcessXmlResponseImpl() {
    super();
  }

  public ProcessXmlResponseImpl(boolean pExecutedSuccessfully, String pErrorMessage) {
    super(pExecutedSuccessfully, pErrorMessage);   
  }
  
  
  
}
