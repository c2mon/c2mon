package cern.c2mon.util.parser;

import static org.junit.Assert.*;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Unit test of SimpleXMLParser
 * 
 * @author Mark Brightwell
 *
 */
public class SimpleXMLParserTest {

  private SimpleXMLParser simpleXMLParser;
  
  @Before
  public void init() throws ParserConfigurationException {
    simpleXMLParser = new SimpleXMLParser();
  }
  
  @Test
  public void testParsing() {
    String xmlString = "<DataTagValueUpdate process-id=\"4151\"><DataTag id=\"129568\" name=\"UV.L08.UAUT-01831:TEMP_INLET\" control=\"false\"><value data-type=\"Float\">21.25</value><quality><code>4</code><desc><![CDATA[Name of host2 is empty]]></desc></quality><timestamp>1357548158484</timestamp><daq-timestamp>1357548158484</daq-timestamp></DataTag><DataTag id=\"162386\" name=\"UV.L06.UOWC-00630:ALIVE_COUNTER\" control=\"false\"><value data-type=\"Integer\">33</value><quality><code>4</code><desc><![CDATA[Name of host2 is empty]]></desc></quality><timestamp>1357548158492</timestamp><daq-timestamp>1357548158492</daq-timestamp></DataTag></DataTagValueUpdate>";
    Document doc = simpleXMLParser.parse(xmlString);
    assertNotNull(doc);
    
    String xmlString2 = "<DataTagValueUpdate process-id=\"4152\"><DataTag id=\"129568\" name=\"UV.L08.UAUT-01831:TEMP_INLET\" control=\"false\"><value data-type=\"Float\">21.25</value><quality><code>4</code><desc><![CDATA[Name of host2 is empty]]></desc></quality><timestamp>1357548158484</timestamp><daq-timestamp>1357548158484</daq-timestamp></DataTag><DataTag id=\"162386\" name=\"UV.L06.UOWC-00630:ALIVE_COUNTER\" control=\"false\"><value data-type=\"Integer\">33</value><quality><code>4</code><desc><![CDATA[Name of host2 is empty]]></desc></quality><timestamp>1357548158492</timestamp><daq-timestamp>1357548158492</daq-timestamp></DataTag></DataTagValueUpdate>";
    Document doc2 = simpleXMLParser.parse(xmlString);
    assertNotNull(doc2);
  }
  
  @Test(expected=ParserException.class)
  public void testFailedParsing() {
    //contains &#x01 character
    String invalidXmlString = "<DataTagValueUpdate process-id=\"4151\"><DataTag id=\"129568\" name=\"UV.L08.UAUT-01831:TEMP_INLET&#x01;\" control=\"false\"><value data-type=\"Float\">21.25</value><quality><code>4</code><desc><![CDATA[Name of host2 is empty ]]></desc></quality><timestamp>1357548158484</timestamp><daq-timestamp>1357548158484</daq-timestamp></DataTag><DataTag id=\"162386\" name=\"UV.L06.UOWC-00630:ALIVE_COUNTER\" control=\"false\"><value data-type=\"Integer\">33</value><quality><code>4</code><desc><![CDATA[Name of host2 is empty]]></desc></quality><timestamp>1357548158492</timestamp><daq-timestamp>1357548158492</daq-timestamp></DataTag></DataTagValueUpdate>";
    Document doc = simpleXMLParser.parse(invalidXmlString);   
  }
  
}
