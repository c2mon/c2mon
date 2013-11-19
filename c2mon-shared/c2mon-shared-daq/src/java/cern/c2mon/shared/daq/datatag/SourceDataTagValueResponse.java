// TIM. CERN. All rights reserved.
//  
// T Nick:           Date:       Info:
// -------------------------------------------------------------------------
// D wbuczak       08/Dec/2006   First implementation
//
//
//
// -------------------------------------------------------------------------


package cern.c2mon.shared.daq.datatag;


import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cern.c2mon.shared.daq.exception.ProcessRequestException;
import cern.c2mon.util.parser.SimpleXMLParser;


/**
 * This is a wrapper class for DataTagValueUpdate messages used inside reponse 
 * messages for the DataTagValueUpdate requests
 * 
 * imported as-is from TIM1
 */
public class SourceDataTagValueResponse implements java.io.Serializable {

  private static final Logger LOG = Logger.getLogger(SourceDataTagValueRequest.class);

  private String status;
  
  private String errorMessage;
  
  private ArrayList dataTagValueUpdates = new ArrayList();
  
  public  static final String XML_ROOT_ELEMENT = "DataTagValueUpdateResponse";  
  private static final String XML_ELEMENT_STATUS = "status";
  private static final String XML_ELEMENT_ERROR_MSG = "error";  
  private static final String XML_ELEMENT_UPDATE = "DataTagValueUpdate";
  
  public static final String STATUS_OK = "OK";
  public static final String STATUS_EXECUTION_FAILED = "FAILED";


  public SourceDataTagValueResponse(final DataTagValueUpdate pDataTagVAlueUpdate) {
    this.dataTagValueUpdates.add(pDataTagVAlueUpdate);
    this.status = STATUS_OK;
  }
  
  
  public SourceDataTagValueResponse(final Collection pDataTagVAlueUpdates) {
    this.dataTagValueUpdates = new ArrayList(pDataTagVAlueUpdates);
    this.status = STATUS_OK;
  }
  
  
  public SourceDataTagValueResponse(final String pExecutionErrorMessage) {
    this.status= STATUS_EXECUTION_FAILED;
    this.errorMessage = pExecutionErrorMessage;
  }
  
  public void addDataTagValueUpdate(final DataTagValueUpdate pDataTagValueUpdate) 
  {
    
  }
  
  public boolean isStatusOK() 
  {
    if (this.status.equals(STATUS_OK)) 
     return true;
    else 
     return false;
  }
  
  
  public String getErrorMessage() {
    return this.errorMessage;
  }
  
  public DataTagValueUpdate getDataTagValueUpdate(int index) {
    DataTagValueUpdate dtvUpdate = null;
    try 
    {
      dtvUpdate = (DataTagValueUpdate)this.dataTagValueUpdates.get(index);
    }
    catch (Exception ex) {}
    
    return dtvUpdate;
  }

  /**
   * returns a collection of DataTagValueUpdate objects
   * @return 
   */
  public Collection getAllDataTagValueUpdatesObjects() {
    return this.dataTagValueUpdates;
  }
  
    
  /**
   * returns the number of DataTagValueUpdate objects iside the wrapper
   * @return 
   */
  public int getDataTagValueUpdatesCount() {
    return dataTagValueUpdates.size();    
  }
  
  
  /**
   * returns the overal number of DataTagValue objects in the wrapper
   * @return 
   */
  public int getDataTagValueUpdateTagsCount() 
  {
    int counter = 0;
    
    for (int i=0; i<this.dataTagValueUpdates.size();i++)
      counter+=((DataTagValueUpdate)dataTagValueUpdates.get(i)).getValues().size();
    
    return counter;
  }
  
  
  /**
   * returns a collection of DataTagValue objects
   * @return 
   */
  public Collection getAllDataTagValueObjects() 
  {
    Collection result = new ArrayList();
  
    for (int i=0; i<this.dataTagValueUpdates.size();i++) {
      result.addAll(((DataTagValueUpdate)dataTagValueUpdates.get(i)).getValues());
    }
    
    return result;    
  }
   
   
  
  
  public String toXML() {
    StringBuffer str = new StringBuffer(500);
    str.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    str.append("<").append(XML_ROOT_ELEMENT).append(">\n");
    str.append("<").append(XML_ELEMENT_STATUS).append(">");
    str.append(this.status);
    str.append("</").append(XML_ELEMENT_STATUS).append(">\n");
   
    if (this.errorMessage != null) 
    {
      str.append("<").append(XML_ELEMENT_ERROR_MSG).append(">");
      str.append(this.errorMessage);
      str.append("</").append(XML_ELEMENT_ERROR_MSG).append(">\n");
    }
    if (this.status.equals(STATUS_OK))
    {
      for (int i=0;i<this.dataTagValueUpdates.size();i++) {       
        str.append(((DataTagValueUpdate)this.dataTagValueUpdates.get(i)).toXML()).append("\n");       
      }
    }
    str.append("</").append(XML_ROOT_ELEMENT).append(">\n");
    
    return str.toString();
  }
  
  
  public synchronized static SourceDataTagValueResponse fromXML(Element domElement) {
    String status = null;
    String error = null;
    ArrayList dtValueUpdates = new ArrayList();
     
    NodeList fields = domElement.getChildNodes();
    int fieldsCount = fields.getLength();
    String fieldName;
    String fieldValueString;
    Node fieldNode;

    // Extract information from DOM elements
    for (int i = 0; i < fieldsCount; i++) {
      fieldNode = fields.item(i);
           
      if (fieldNode.getNodeType() == Node.ELEMENT_NODE) {
        fieldName = fieldNode.getNodeName();
        fieldValueString = fieldNode.getFirstChild().getNodeValue();
   
        if (fieldName.equals(XML_ELEMENT_STATUS)) {
          status =  fieldValueString;            
        }
        else if (fieldName.equals(XML_ELEMENT_ERROR_MSG)) {
          error = fieldValueString;               
        }
        else if (fieldName.equals(XML_ELEMENT_UPDATE))
        {  
          try {
            dtValueUpdates.add(DataTagValueUpdate.fromXML((Element)fieldNode));
          }
          catch (Exception ex) 
          {             
            LOG.error("could not parse DataTagValueUpdate XML block from SourceDataTagValueResponse message",ex);
          }                           
        }
      }
      
    }//for

    if (status.equals(STATUS_OK))
      return new SourceDataTagValueResponse(dtValueUpdates);
    else 
      //return new SourceDataTagValueResponse(error);   removed and used new exception instead
      throw new ProcessRequestException(error);
      
  }


  
  public String toString() {
    return toXML();  
  }



  public static void main(String[] args) {
    String xml;
    
    org.w3c.dom.Document doc = null;
    
    DataTagValueUpdate u1 = new DataTagValueUpdate(new Long(5555555));
    DataTagValueUpdate u2 = new DataTagValueUpdate(new Long(66666));
    
    ArrayList a1 = new ArrayList(2);
    a1.add(u1);
    a1.add(u2);
    
    //SourceDataTagValueResponse sourceDataTagValueResponse = new SourceDataTagValueResponse("some error");
    SourceDataTagValueResponse sourceDataTagValueResponse = new SourceDataTagValueResponse(a1);
    
    SourceDataTagValueResponse sourceDataTagValueResponse2 = null;
    
    xml = sourceDataTagValueResponse.toString();
    
    System.out.println(xml);
    
    System.out.println();
    System.out.println();
    
    try {     
      SimpleXMLParser p = new SimpleXMLParser();
  
      doc = p.parse(xml);
     
      sourceDataTagValueResponse2 = SourceDataTagValueResponse.fromXML(doc.getDocumentElement());
      System.out.println(sourceDataTagValueResponse2.toXML());
      
      if (xml.equals(sourceDataTagValueResponse2.toXML())) {
        System.out.println("[ OK ]");
      }
    
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    
  
  }



}
