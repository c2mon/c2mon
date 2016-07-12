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
package cern.c2mon.shared.util.parser;

import static org.junit.Assert.*;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.Map;

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

  @Test
  public void parseMapToXml() throws ParserConfigurationException {
    // test map
    Map<String, String> testMap = new HashMap<>();
    testMap.put("key1", ""+ 1 );
    testMap.put("key2", "text");
    testMap.put("key3", ""+ true);

    // expected result
    String testXML = "            <address-parameters class=\"java.util.HashMap\"><entry key=\"key1\"><![CDATA[1]]></entry><entry key=\"key2\"><![CDATA[text]]></entry><entry key=\"key3\"><![CDATA[true]]></entry></address-parameters>\n";

    // testing
    String value = SimpleXMLParser.mapToXMLString(testMap);
    assertEquals(value, testXML);

    simpleXMLParser = new SimpleXMLParser();
    Document doc = simpleXMLParser.parse(value);
  }

  @Test
  public void parseXmlToMap(){
    // argument for the test method
    String testXML = "            <address-parameters class=\"java.util.HashMap\">" +
        "                <entry key=\"key1\">1</entry>\n" +
        "                <entry key=\"key2\">text</entry>\n" +
        "                <entry key=\"key3\">true</entry>\n" +
        "</address-parameters>\n";
    Document doc = simpleXMLParser.parse(testXML);

    HashMap<String, String> compareMap = new HashMap<>();
    compareMap.put("key1", "1");
    compareMap.put("key2", "text");
    compareMap.put("key3", "true");

    Map<String, String> resultMap = SimpleXMLParser.domNodeToMap(doc.getFirstChild());

    for(Map.Entry<String, String> resultEntry : resultMap.entrySet()){

      // check keys
      assertTrue(compareMap.containsKey(resultEntry.getKey()));

      // check values
      assertEquals(resultEntry.getValue(), compareMap.get(resultEntry.getKey()));
    }

  }

}
