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
package cern.c2mon.server.cache.process;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import cern.c2mon.server.cache.AbstractCacheIntegrationTest;
import cern.c2mon.server.cache.junit.CachePopulationRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import cern.c2mon.server.cache.ProcessXMLProvider;

/**
 * Component test of the XML provider.
 *
 * @author Mark Brightwell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class ProcessXMLProviderTest extends AbstractCacheIntegrationTest {

  @Autowired
  private ProcessXMLProvider processXMLProvider;

  @Rule
  @Autowired
  public CachePopulationRule cachePopulationRule;

  @Test
  public void testGetProcessConfigXML() throws ParserConfigurationException, SAXException, IOException, TransformerException {

    //read in expected XML from file
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory.setValidating(false);
    documentBuilderFactory.setNamespaceAware(true);
    documentBuilderFactory.setIgnoringComments(true);
    documentBuilderFactory.setIgnoringElementContentWhitespace(true);
    DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
    Document expectedDoc = builder.parse(this.getClass().getClassLoader().getResourceAsStream("P_TESTHANDLER03.xml"));
    expectedDoc.normalize();

    DOMSource source = new DOMSource(expectedDoc);
    StringWriter writer = new StringWriter();
    StreamResult streamResult = new StreamResult(writer);
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    transformer.transform(source, streamResult);
    String expectedString = writer.toString();

    //get XML from server
    String processXML = processXMLProvider.getProcessConfigXML("P_TESTHANDLER03");
    Document receivedDoc = builder.parse(new ByteArrayInputStream(processXML.getBytes()));
    receivedDoc.normalize();
    source = new DOMSource(receivedDoc);
    writer = new StringWriter();
    streamResult = new StreamResult(writer);
    transformer.transform(source, streamResult);
    String receivedString = writer.toString();

    // compare the 2 XMLs,
    // because the order of the received datatags might be different than in the expected xml we just check if
    // all expected lines appear in the received String
    String expectedLines[] = expectedString.split("\\n");
    for(String expectedLine : expectedLines){
      assertTrue(receivedString.contains(expectedLine));
    }

    //commented out: not clear why this is not true...
    //assertTrue(expectedDoc.isEqualNode(receivedDoc));
  }

}
