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

package cern.c2mon.shared.daq.filter;

import java.util.ArrayList;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cern.c2mon.shared.common.filter.FilteredDataTagValue;

/**
 * Contains a collection of FilteredDataTagValue's.
 *
 * This class is identical to the DataTagValueUpdate class, but collects
 * FilteredDataTagValue's instead of SourceDataTagValue's.
 *
 * Also provides methods for encoding and decoding in XML.
 *
 * @author mbrightw
 *
 */
@Data
@Slf4j
public class FilteredDataTagValueUpdate {

    // ----------------------------------------------------------------------------
    // CONSTANT DEFINITIONS
    // ----------------------------------------------------------------------------

    /**
     * The root element of the XML encoding of the object.
     */
    private static final String XML_ROOT_ELEMENT = "FilteredDataTagValueUpdate";

    /**
     * The process-id attribute name of the XML document.
     */
    private static final String XML_ATTRIBUTE_PROCESS_ID = "process-id";

    // ----------------------------------------------------------------------------
    // MEMBERS
    // ----------------------------------------------------------------------------

    /**
     * The unique DAQ process identification number.
     */
    private Long processId = null;

    /**
     * The collection of tag values.
     */
    private ArrayList<FilteredDataTagValue> values = null;

    // ----------------------------------------------------------------------------
    // CONSTRUCTORS
    // ----------------------------------------------------------------------------

    /**
     * Constructor.
     */
    public FilteredDataTagValueUpdate() {
    }

    /**
     * Constructor.
     * @param pProcessId the DAQ process id number
     */
    public FilteredDataTagValueUpdate(final Long pProcessId) {
        this.processId = pProcessId;
        this.values = new ArrayList<>(10);
    }

    /**
     * Constructor.
     * @param pProcessId the unique DAQ process id number
     * @param pTagValues the collection of tag values
     */
    public FilteredDataTagValueUpdate(final Long pProcessId, final ArrayList<FilteredDataTagValue> pTagValues) {
        this.processId = pProcessId;
        this.values = pTagValues;
    }

    /**
     * Adds the value to the FilteredDataTagValueUpdate object (collection).
     * @param pValue the value to be added
     */
    public final void addValue(final FilteredDataTagValue pValue) {
        this.values.add(pValue);
    }


    // ----------------------------------------------------------------------------
    // METHODS FOR XML-IFICATION and DE-XML-IFICATION
    // ----------------------------------------------------------------------------

    /**
     * Encodes the object as an XML document.
     * @return the XML document as a String
     */
    public final String toXML() {

        StringBuffer str = new StringBuffer();

        /*
         * Open <FilteredDataTagValueUpdate> tag with reference to XSD
         * process-id
         */
        str.append('<');
        str.append(XML_ROOT_ELEMENT);
        str.append(' ');
        str.append(XML_ATTRIBUTE_PROCESS_ID);
        str.append("=\"");
        str.append(processId);
        str.append("\">\n");

        /*
         * Add a <DataTag> section for each FilteredDataTagValue in the
         * collection
         */
        if (values != null) {
            for (FilteredDataTagValue value : values) {
                str.append(value.toXML());
            }
        }

        /* Close <DataTagValueUpdate> tag */
        str.append("</");
        str.append(XML_ROOT_ELEMENT);
        str.append('>');

        /* Return contents of the buffer as a String */
        return str.toString();
    }

    /**
     * Gets the object from it's XML encoded form.
     * @return the object containing the filtered values
     * @param domElement the document element of the XML document
     */
    public static FilteredDataTagValueUpdate fromXML(final Element domElement) {
        FilteredDataTagValueUpdate result = new FilteredDataTagValueUpdate();

        /* Only process if the element name is <DataTagValueUpdate> */
        if (domElement.getNodeName().equals(XML_ROOT_ELEMENT)) {

            /*
             * Try to extract the process-id attribute from the XML message. If
             * this fails, return null.
             */
            try {
                result.processId = Long.valueOf(domElement.getAttribute(XML_ATTRIBUTE_PROCESS_ID));

                /*
                 * Only proceed if the extraction of the process-id was
                 * successful
                 */
                if (result.processId != null) {

                    Node fieldNode = null;
                    NodeList fields = domElement.getChildNodes();
                    int fieldsCount = fields.getLength();

                    result.values = new ArrayList<>(fieldsCount);

                    for (int i = 0; i < fieldsCount; i++) {
                        fieldNode = fields.item(i);
                        if (fieldNode.getNodeType() == Node.ELEMENT_NODE && fieldNode.getNodeName().equals(FilteredDataTagValue.XML_ROOT_ELEMENT)) {
                            result.values.add(FilteredDataTagValue.fromXML((Element) fieldNode));
                        }
                    } // for
                } // if processId != null

            } catch (NumberFormatException nfe) {
                result = null;
                log.error("Cannot extract valid process-id from FilteredDataTagValueUpdate message. Returning null.");
            }
        } // if DataTagValueUpdate
        else {
            result = null;
            log.error("Cannot decode FilteredDataTagValueUpdate message. Root element is not <FilteredDataTagValueUpdate>");
        }
        return result;
    }

  /**
   * The method for logging a FilteredDataTagValueUpdate object in Slf4j.
   */
  public final void log() {
    if (this.values != null) {
      for (FilteredDataTagValue tagValue : values) {
        tagValue.log();
      }
    }
  }
}
