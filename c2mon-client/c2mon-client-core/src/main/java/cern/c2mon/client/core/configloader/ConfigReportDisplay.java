/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
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
