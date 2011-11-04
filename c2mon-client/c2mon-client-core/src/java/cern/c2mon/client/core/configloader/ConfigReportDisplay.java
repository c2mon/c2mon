package cern.c2mon.client.core.configloader;

/**
 * Contains the XML report that needs displaying on the
 * web page. The report is formatted into a suitable form
 * by the set method. The get method is then called by
 * the JSP when displaying the XML content.
 * 
 * @author Mark Brightwell
 *
 */
public class ConfigReportDisplay {

  /**
   * The configuration XML report.
   */
  private String xmlReport;

  /**
   * Simple getter.
   * @return the xmlReport
   */
  public String getXmlReport() {
    return xmlReport;
  }

  /**
   * Modifies the XML so that it is ready for
   * use in the JSP.
   * 
   * @param xmlReport the xmlReport to set
   */
  public void setXmlReport(final String xmlReport) {
    if (xmlReport == null) {
      this.xmlReport = null;
    } else {
      String tmpReport = xmlReport.replace('"', '\'');
      this.xmlReport = tmpReport;//.replaceAll(">", "/>");
    }    
  }
  
}
