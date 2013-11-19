package cern.c2mon.shared.daq.datatag;

import static org.junit.Assert.*;

import java.sql.Timestamp;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;

import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.util.parser.SimpleXMLParser;

public class SourceDataTagValueTest {

  @Test
  public void testFromXmlWithNullValue() throws ParserConfigurationException {
    SourceDataTagValue sourceDataTagValue = new SourceDataTagValue(10L, 
        "DataTag name",
        false, null, //null value
        new SourceDataQuality(Short.valueOf("4")), //invalid
        new Timestamp(System.currentTimeMillis()),
        DataTagConstants.PRIORITY_LOW,
        false,
        "test description",
        DataTagAddress.TTL_FOREVER);
    
    String xmlString = sourceDataTagValue.toXML();
    SimpleXMLParser parser = new SimpleXMLParser();
    Document document = parser.parse(xmlString);
    SourceDataTagValue extractedValue = SourceDataTagValue.fromXML(document.getDocumentElement());
    
    assertEquals(sourceDataTagValue, extractedValue);
    
  }
  
}
