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
