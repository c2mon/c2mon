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
package cern.c2mon.daq.common.messaging.impl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import cern.c2mon.shared.common.process.ProcessConfiguration;
import org.apache.xerces.parsers.DOMParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;

import cern.c2mon.daq.common.messaging.ProcessRequestSender;
import cern.c2mon.shared.daq.process.ProcessConfigurationResponse;
import cern.c2mon.shared.daq.process.ProcessConnectionResponse;

public class DummyRequestSender implements ProcessRequestSender {

    private String processConfigurationFilePath;
    
    @Autowired
    private DummyRequestSender(final String processConfigurationFilePath) {
      this.processConfigurationFilePath = processConfigurationFilePath;   
    }

    @Override
    public void sendProcessDisconnectionRequest(ProcessConfiguration processConfiguration, long startupTime) {
        // TODO Auto-generated method stub

    }    
    
    @Override
    public ProcessConnectionResponse sendProcessConnectionRequest(String processName) {
      return new ProcessConnectionResponse("P_NACHO", new Long(12345));
    }

    @Override
    public ProcessConfigurationResponse sendProcessConfigurationRequest(String processName) {
      //return (ProcessConfigurationResponse) loadConfigLocal(processConfigurationFilePath); 
      ProcessConfigurationResponse processConfigurationResponse = new ProcessConfigurationResponse();
      //processConfigurationResponse.setProcessName(processConfigurationRequest.getProcessName());
      
      // We get the configuration XML file (empty by default)
      processConfigurationResponse.setConfigurationXML(readFile(processConfigurationFilePath)); 
      
      return processConfigurationResponse;
    }
    
    private String readFile(String filename)
    {
      String content = null;
      File file = new File(filename); //for ex foo.txt
      try {
        FileReader reader = new FileReader(file);
        char[] chars = new char[(int) file.length()];
        reader.read(chars);
        content = new String(chars);
        reader.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      return content;
    }
    
    public Document loadConfigLocal(final String fileSystemLocation) {
        Document confXMLDoc;

        DOMParser parser = new DOMParser();
        try {
            parser.parse(fileSystemLocation);
            confXMLDoc = parser.getDocument();
        } catch (java.io.IOException ex) {     
            ex.printStackTrace();
            confXMLDoc = null;
        } catch (org.xml.sax.SAXException ex) {            
            confXMLDoc = null;
        }
       
        return confXMLDoc;
    }
}
