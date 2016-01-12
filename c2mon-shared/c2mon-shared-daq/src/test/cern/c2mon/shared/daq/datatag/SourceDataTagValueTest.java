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
package cern.c2mon.shared.daq.datatag;

import static org.junit.Assert.*;

import java.sql.Timestamp;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;

import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.common.datatag.SourceDataQuality;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;
import cern.c2mon.shared.util.parser.SimpleXMLParser;

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
