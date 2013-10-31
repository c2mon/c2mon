/*
 * $Id $
 *
 * $Date$
 * $Revision$
 * $Author$
 *
 * Copyright CERN ${year}, All Rights Reserved.
 */
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
