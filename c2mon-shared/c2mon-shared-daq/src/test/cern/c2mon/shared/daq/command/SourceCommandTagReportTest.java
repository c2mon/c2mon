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
package cern.c2mon.shared.daq.command;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import static junit.framework.Assert.*;
import org.w3c.dom.Document;

import cern.c2mon.shared.util.parser.ParserException;
import cern.c2mon.shared.util.parser.SimpleXMLParser;

public class SourceCommandTagReportTest {
    
    private SimpleXMLParser parser;
    
    @Before
    public void setUp() throws ParserConfigurationException {
        parser = new SimpleXMLParser();
    }
    
    @Test
    public void testToXmlToObjectEmptyDescription() throws ParserException {
       int status = SourceCommandTagReport.STATUS_NOK_CONVERSION_ERROR;
        SourceCommandTagReport report = new SourceCommandTagReport(1L, "name", status, "", null, System.currentTimeMillis());
        String xmlString = report.toXML();
        Document reportDoc = parser.parse(xmlString);
        SourceCommandTagReport recreatedReport = SourceCommandTagReport.fromXML(reportDoc.getDocumentElement());
        assertEquals(status, recreatedReport.getStatus());
        assertEquals("", recreatedReport.getDescription());
    }
    
    @Test
    public void testToXmlToObjectEmptyReturnValue() throws ParserException {
       int status = SourceCommandTagReport.STATUS_NOK_CONVERSION_ERROR;
        SourceCommandTagReport report = new SourceCommandTagReport(1L, "name", status, "asd", "", System.currentTimeMillis());
        String xmlString = report.toXML();
        Document reportDoc = parser.parse(xmlString);
        SourceCommandTagReport recreatedReport = SourceCommandTagReport.fromXML(reportDoc.getDocumentElement());
        assertEquals(status, recreatedReport.getStatus());
        assertEquals("asd", recreatedReport.getDescription());
        assertEquals("", recreatedReport.getReturnValue());
    }
}
