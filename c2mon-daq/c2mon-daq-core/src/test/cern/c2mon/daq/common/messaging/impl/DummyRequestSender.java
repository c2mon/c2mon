package cern.c2mon.daq.common.messaging.impl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

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
    
//    @Override
//    public Document sendProcessConfigurationRequest() {        
//        return loadConfigLocal(processConfigurationFilePath); 
//    }
//
//    @Override
//    public Document sendProcessConfigurationRequest(String fileToSaveConf) {
//        // TODO Auto-generated method stub
//        return null;
//    }

    @Override
    public void sendProcessDisconnectionRequest() {
        // TODO Auto-generated method stub

    }    
    
    @Override
    public ProcessConnectionResponse sendProcessConnectionRequest() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public ProcessConfigurationResponse sendProcessConfigurationRequest() {
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
