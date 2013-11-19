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
