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
package cern.c2mon.shared.daq.config;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import cern.c2mon.shared.common.NoSimpleValueParseException;

import static org.junit.Assert.*;

public class XMLJavaLoopTest extends AbstractXMLTest {
    
    private static final String TEST_UPDATE_XML = "/TestUpdateConfigurationChangeEvent.xml";
    private static final String TEST_REPORT_XML = "/TestConfigurationChangeEventReport.xml";
    private ConfigurationObjectFactory objectFactory = new ConfigurationObjectFactory();
    private ConfigurationDOMFactory domFactory = new ConfigurationDOMFactory();
    private DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    private Document documentChanges;
    private Document documentReport;
    
    @Before
    public void setUp() throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
        documentChanges = builder.parse(getClass().getResource(TEST_UPDATE_XML).openStream());
        documentReport = builder.parse(getClass().getResource(TEST_REPORT_XML).openStream());
    }
    
    @Test
    public void testReConfigurationLoop() throws NoSuchFieldException, IllegalAccessException, 
            NoSuchMethodException, InvocationTargetException, ParserConfigurationException, InstantiationException, TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException {
        List<Change> changes = objectFactory.generateChanges(documentChanges.getDocumentElement());
        Document newDocument = domFactory.createConfigurationDocument(changes);
//        System.out.println(getDocumentString(newDocument).replaceAll("\\s", ""));
//        System.out.println(getDocumentString(documentChanges).replaceAll("\\s", ""));
        assertTrue(getDocumentString(documentChanges).replaceAll("\\s", "").length() == getDocumentString(newDocument).replaceAll("\\s", "").length());
    }
    
    @Test
    public void testConfigurationReportLoop() throws NoSuchFieldException, IllegalAccessException, 
            NoSuchMethodException, InvocationTargetException, ParserConfigurationException, InstantiationException, TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException, NoSimpleValueParseException {
        ConfigurationChangeEventReport configurationChangeEventReport = objectFactory.createConfigurationChangeEventReport(documentReport.getDocumentElement());
        domFactory.setDefaultNamespace("http://timweb.cern.ch/schemas/c2mon-daq/ConfigurationReport");
        Document newDocument = domFactory.createConfigurationChangeEventReportDocument(configurationChangeEventReport);
//        System.out.println(getDocumentString(newDocument).replaceAll("\\s", ""));
//        System.out.println(getDocumentString(documentReport).replaceAll("\\s", ""));
        assertTrue(getDocumentString(documentReport).replaceAll("\\s", "").equals(getDocumentString(newDocument).replaceAll("\\s", "")));
    }

}
