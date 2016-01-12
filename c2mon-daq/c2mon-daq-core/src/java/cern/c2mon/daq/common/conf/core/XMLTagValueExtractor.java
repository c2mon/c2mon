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
package cern.c2mon.daq.common.conf.core;

import org.w3c.dom.Element;

public class XMLTagValueExtractor {

    /**
     * Gets the tag value of a child element.
     * @param element The element to use.
     * @param tagName The name of the element.
     * @return The String value of the child element with the provided tag name.
     */
    protected final String getTagValue(final Element element, final String tagName) {
        return element.getElementsByTagName(tagName).item(0).getFirstChild().getNodeValue();
    }
}
