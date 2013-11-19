package cern.c2mon.shared.daq.datatag;

import java.io.ByteArrayInputStream;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;


/**
 * The DataTagValueUpdate class is used for encoding/decoding DataTag value
 * update XML messages sent to the server application by TIM drivers.
 */

public final class DataTagValueUpdate {
  // ----------------------------------------------------------------------------
  // CONSTANT DEFINITIONS
  // ----------------------------------------------------------------------------
  public static final String XSD_URL = "http://ts-project-tim.web.cern.ch/ts-project-tim/xml/DataTagValueUpdate.xsd";

  public static final String XML_ROOT_ELEMENT= "DataTagValueUpdate";

  public static final String XML_ATTRIBUTE_PROCESS_ID= "process-id";
  
  public static final String XML_ATTRIBUTE_PROCESS_PIK= "process-pik";
  // ----------------------------------------------------------------------------
  // PRIVATE STATIC MEMBERS
  // ----------------------------------------------------------------------------

  
  /**
   * Log4j Logger for the DataTagValueUpdate class.
   */
  protected static final Logger log = Logger.getLogger(DataTagValueUpdate.class);
  
  // ----------------------------------------------------------------------------
  // MEMBERS
  // ----------------------------------------------------------------------------

  protected Long processId = null;
  protected Long processPIK = null;
  protected ArrayList<SourceDataTagValue> tagValues = null;

  // ----------------------------------------------------------------------------
  // CONSTRUCTORS
  // ----------------------------------------------------------------------------
  protected DataTagValueUpdate() {
  }

  public DataTagValueUpdate(final Long pProcessId) {
    this.processId = pProcessId;
    this.tagValues = new ArrayList<SourceDataTagValue>(10);
  }

  public DataTagValueUpdate(final Long pProcessId, final ArrayList<SourceDataTagValue> pTagValues) {
    this.processId = pProcessId;
    this.tagValues = pTagValues;
  }
  
  public DataTagValueUpdate(final Long pProcessId, final Long pProcessPIK) {
    this.processId = pProcessId;
    this.processPIK = pProcessPIK;
    this.tagValues = new ArrayList<SourceDataTagValue>(10);
  }

  public DataTagValueUpdate(final Long pProcessId, final Long pProcessPIK, final ArrayList<SourceDataTagValue> pTagValues) {
    this.processId = pProcessId;
    this.processPIK = pProcessPIK;
    this.tagValues = pTagValues;
  }

  public void setProcessId(final Long pProcessId) {
    this.processId = pProcessId;
  }

  public Long getProcessId() {
    return this.processId;
  }
  
  public void setProcessPIK(final Long pProcessPIK) {
    this.processPIK = pProcessPIK;
  }

  public Long getProcessPIK() {
    return this.processPIK;
  }

  public void addValue(final SourceDataTagValue pValue) {
    this.tagValues.add(pValue);
  }

  public void setValues(final ArrayList<SourceDataTagValue> pSourceDataTagValues) {
    this.tagValues = pSourceDataTagValues;
  }

  public Collection<SourceDataTagValue> getValues() {
    return this.tagValues;
  }

  // ----------------------------------------------------------------------------
  // METHODS FOR XML-IFICATION and DE-XML-IFICATION 
  // ----------------------------------------------------------------------------

  public String toXML() {

    StringBuffer str = new StringBuffer();

    /* Open <DataTagValueUpdate> tag with reference to XSD definition and process-id */
    str.append('<');
    str.append(XML_ROOT_ELEMENT);
    str.append(' ');
    str.append(XML_ATTRIBUTE_PROCESS_ID);
    str.append("=\"");
    str.append(processId);
    // Add process PIK
    if (this.processPIK != null) {
      str.append("\" ");
      str.append(XML_ATTRIBUTE_PROCESS_PIK);
      str.append("=\"");
      str.append(this.processPIK);
    }
    str.append("\">\n");

    /* Add a <DataTag> section for each SourceDataTagValue in the collection */
    if (tagValues != null) {
      Iterator it = tagValues.iterator();

      while (it.hasNext()) {
        str.append(((SourceDataTagValue) it.next()).toXML());
      }
    }

    /* Close <DataTagValueUpdate> tag */
    str.append("</");
    str.append(XML_ROOT_ELEMENT);
    str.append('>');

    /* Return contents of the buffer as a String */
    return str.toString();
  }

  //Jira: [TIMS-811] Adding PIK to deserialization process
  public static DataTagValueUpdate fromXML(Element domElement) {
    DataTagValueUpdate result = new DataTagValueUpdate();

    /* Only process if the element name is <DataTagValueUpdate> */
    if (domElement.getNodeName().equals(XML_ROOT_ELEMENT)) {

      /* Try to extract the process-id attribute from the XML message. 
       * If this fails, return null. */
      try {
        result.processId = Long.valueOf(domElement.getAttribute(XML_ATTRIBUTE_PROCESS_ID));

        /* Only proceed if the extraction of the process-id was successful */
        if (result.processId != null) {

          try {
          result.processPIK = Long.valueOf(domElement.getAttribute(XML_ATTRIBUTE_PROCESS_PIK));
          } catch (NumberFormatException nfe) {
            log.trace("DataTagValueUpdate - fromXML - No PIK attribute received.");
          }
          
          Node fieldNode = null; 
          NodeList fields = domElement.getChildNodes();
          int fieldsCount = fields.getLength();
        
          result.tagValues = new ArrayList<SourceDataTagValue>(fieldsCount);

          for (int i = 0; i < fieldsCount; i++) {
            fieldNode = fields.item(i);
            if (fieldNode.getNodeType() == Node.ELEMENT_NODE 
                && fieldNode.getNodeName().equals(SourceDataTagValue.XML_ROOT_ELEMENT)
            ) {
              result.tagValues.add(
                  SourceDataTagValue.fromXML((Element) fieldNode));
            }
          } // for
        } // if processId != null

      } catch (NumberFormatException nfe) {
        result = null;
        log.error("DataTagValueUpdate - Cannot extract valid process-id from DataTagValueUpdate message. Returning null.");
      }
    } // if DataTagValueUpdate
    else {
      result = null;
      log.error("DataTagValueUpdate - Cannot decode DataTagValueUpdate message. Root element is not <DataTagValueUpdate>");
    }
    return result;
  }

  public void log() {
    if (this.tagValues != null) {
      int size = tagValues.size();
      for (int i = 0; i != size; i++) {
        ((SourceDataTagValue) tagValues.get(i)).log();
      }
    }
  }
}
