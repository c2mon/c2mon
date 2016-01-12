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
package cern.c2mon.shared.common;


import static org.junit.Assert.assertTrue;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TestXMLFactory extends AbstractXMLTst {

    @Test
    public void testXMLFactory() throws ParserConfigurationException, DOMException, IllegalArgumentException, IllegalAccessException, InstantiationException, TransformerException {
        DOMFactory factory = new DOMFactory();
        Document document = factory.createDocument();
        TestPojo pojo = new TestPojo();
        Element element = factory.generateSimpleElement(document, pojo, "id", "name");        
        document.appendChild(element);
        assertTrue(element.getChildNodes().getLength() > 0);
        printDocument(document);
    }
}
