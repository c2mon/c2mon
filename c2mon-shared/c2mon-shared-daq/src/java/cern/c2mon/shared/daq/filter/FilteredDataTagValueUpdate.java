/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2009 CERN This program is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/

package cern.c2mon.shared.daq.filter;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
    // PRIVATE STATIC MEMBERS
    // ----------------------------------------------------------------------------

    /**
     * Log4j Logger for the FilteredDataTagValueUpdate class.
     */
    private static final Logger LOG = Logger.getLogger(FilteredDataTagValueUpdate.class);

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
    private ArrayList tagValues = null;

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
        this.tagValues = new ArrayList(10);
    }

    /**
     * Constructor.
     * @param pProcessId the unique DAQ process id number
     * @param pTagValues the collection of tag values
     */
    public FilteredDataTagValueUpdate(final Long pProcessId, final ArrayList pTagValues) {
        this.processId = pProcessId;
        this.tagValues = pTagValues;
    }

    /**
     * Sets the process id of the value.
     * @param pProcessId the DAQ process id
     */
    public final void setProcessId(final Long pProcessId) {
        this.processId = pProcessId;
    }

    /**
     * Returns the process id.
     * @return the DAQ process id number
     */
    public final Long getProcessId() {
        return this.processId;
    }

    /**
     * Adds the value to the FilteredDataTagValueUpdate object (collection).
     * @param pValue the value to be added
     */
    public final void addValue(final FilteredDataTagValue pValue) {
        this.tagValues.add(pValue);
    }

    /**
     * Sets the collection of values the FilteredDataTagValueUpdate object is wrapping.
     * @param pFilteredDataTagValues the collection of values
     */
    public final void setValues(final ArrayList pFilteredDataTagValues) {
        this.tagValues = pFilteredDataTagValues;
    }

    /**
     * Returns the collection of values the object contains.
     * @return the collection of filtered tag values
     */
    public final Collection getValues() {
        return this.tagValues;
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
        if (tagValues != null) {
            Iterator it = tagValues.iterator();

            while (it.hasNext()) {
                str.append(((FilteredDataTagValue) it.next()).toXML());
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

                    result.tagValues = new ArrayList(fieldsCount);

                    for (int i = 0; i < fieldsCount; i++) {
                        fieldNode = fields.item(i);
                        if (fieldNode.getNodeType() == Node.ELEMENT_NODE && fieldNode.getNodeName().equals(FilteredDataTagValue.XML_ROOT_ELEMENT)) {
                            result.tagValues.add(FilteredDataTagValue.fromXML((Element) fieldNode));
                        }
                    } // for
                } // if processId != null

            } catch (NumberFormatException nfe) {
                result = null;
                LOG.error("Cannot extract valid process-id from FilteredDataTagValueUpdate message. Returning null.");
            }
        } // if DataTagValueUpdate
        else {
            result = null;
            LOG.error("Cannot decode FilteredDataTagValueUpdate message. Root element is not <FilteredDataTagValueUpdate>");
        }
        return result;
    }

    /**
     * The method for logging a FilteredDataTagValueUpdate object in log4j.
     */
    public final void log() {
        if (this.tagValues != null) {
            int size = tagValues.size();
            for (int i = 0; i != size; i++) {
                ((FilteredDataTagValue) tagValues.get(i)).log();
            }
        }
    }

    /**
     * For testing only.
     * @param args no parameters
     */
    public static void main(final String[] args) {
        FilteredDataTagValue ft1 = new FilteredDataTagValue(new Long(0), "name1", "value1", new Short((short)1), "qdesc1", new Timestamp(System.currentTimeMillis()), "vdesc1", "datatype1", false, (short) 0);

        FilteredDataTagValue ft2 = new FilteredDataTagValue(new Long(2), "name2", "value2", new Short((short)2), "qdesc2", new Timestamp(System.currentTimeMillis()), "vdesc2", "datatype2", false, (short) 0);

        FilteredDataTagValueUpdate fUpdate = new FilteredDataTagValueUpdate(new Long((long)34));
        fUpdate.addValue(ft1);
        fUpdate.addValue(ft2);
        String xmlVersion = fUpdate.toXML();
        System.out.println(xmlVersion);

    }

}
