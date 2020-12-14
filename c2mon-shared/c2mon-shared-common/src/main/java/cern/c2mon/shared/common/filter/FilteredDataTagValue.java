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

package cern.c2mon.shared.common.filter;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;

import cern.c2mon.shared.common.datatag.util.SourceDataTagQualityCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * does not allow null values for attributes (empty strings only)
 *
 * Notice these filter classes are all shared between the DAQ layer and the Statistics Consumer
 * process, not the server.
 *
 * @author mbrightw
 *
 */
@AllArgsConstructor
@Data
public class FilteredDataTagValue {

    /**
     * The general logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FilteredDataTagValue.class);

    /**
     * Filter Type enum
     */
    public enum FilterType {
      /*
       * Tag value was filtered out as the previously received
       * value had a similar value, quality flag and quality description.
       */
      REPEATED_INVALID(0),
      /*
       * Tag value was filtered out as the value was in the value deadband.
       */
      VALUE_DEADBAND(1),
      /*
       *  Filtering occurred because the value received was the same as the current value.
       */
      REPEATED_VALUE(2),
      /*
       * Filtering occurred because the value was received during the time deadband.
       */
      TIME_DEADBAND(3),
      /*
       * Filtering occurred while checking current TS vs New TS and current Quality vs new Quality
       *
       * Filter when:
       * - New TS <= Current TS + Current Good Quality
       * - New TS <= Current TS + Current Bad Quality + New Bad Quality
       *
       * No filter when:
       * - New TS <= Current TS + New Good Quality + Current Bad Quality
       * - New TS > Current TS
       */
      OLD_UPDATE(4),
      /*
       * No filtering
       */
      NO_FILTERING(5);

      /**
       * The Filter type name
       */
      private int number;

      /**
       * The Filter type number
       *
       * @param number The Filter type number
       */
      FilterType(final int number) {
        this.number = number;
      }

      /**
       * @return The Filter object number
       */
      public final int getNumber() {
        return this.number;
      }
    }

    /**
     * The log of all tag values sent to the filter module.
     */
    private static final Logger TAGLOGGER = LoggerFactory.getLogger("FilteredDataTagLogger");

    // Constants for creating the XML representation of the object
    /**
     * The root element in the XML encoding of the object.
     */
    public static final String XML_ROOT_ELEMENT = "FilteredDataTag";

    /**
     * The id attribute in the XML encoding of the object.
     */
    private static final String XML_ATTRIBUTE_ID = "id";

    /**
     * The name attribute in the XML encoding of the object.
     */
    private static final String XML_ATTRIBUTE_NAME = "name";

    /** Unique numeric identifier of the tag */
    private Long id;

    /** Unique name of the tag */
    private String name;

    /** Current value of the tag (may be null if invalid) */
    private String value;

    /** Optional value description **/
    private String valueDescription = null;

    /**
     * Current data quality code of the tag. This is set as OK by default.
     * */
    private Integer qualityCode = SourceDataTagQualityCode.OK.getQualityCode();

    /** The textual description of the quality */
    private String qualityDescription = null;

    /** Timestamp of the current value */
    private Timestamp timestamp;

    /**
     * Type of the value
     */
    private String dataType;

    /**
     * Flag indicating that the value was sent here due to dynamic filtering.
     */
    private boolean dynamicFiltered = false;

    /**
     * Indicates the type of filtering that was applied to this value.
     */
    private int filterApplied = 0;

    // CONSTRUCTORS

    /**
     * Constructor.
     *
     * @param pId the tag id
     * @param pName the tag name
     */
    public FilteredDataTagValue(final Long pId, final String pName) {
      this.id = pId;
      this.name = pName;
    }

    /**
     * logs the FilteredDataTag in the appropriate log file
     */
    public final void log() {
        TAGLOGGER.info(this.toString());
    }

    @Override
    public String toString() {
        StringBuffer str = new StringBuffer();

        str.append(this.getId());
        str.append('\t');
        str.append(this.getName());
        str.append('\t');
        str.append(this.getTimestamp());
        str.append('\t');
        str.append(this.getDataType());
        str.append('\t');
        str.append(this.getValue());
        str.append('\t');

        str.append(this.isDynamicFiltered());
        str.append('\t');
        str.append(this.getFilterApplied());
        if (this.getQualityCode() != null || this.getQualityCode() != 0) {
            str.append('\t');
            str.append(this.getQualityCode());

        } else {
            str.append("\tOK");
        }
        if (this.getQualityDescription() != null) {
            str.append('\t');
            str.append(this.getQualityDescription());
        }
        if (this.getValueDescription() != null) {
            str.append('\t');
            str.append(this.getValueDescription());
        }
        return str.toString();
    }

    // METHODS FOR TRANSFORMATION TO AND FROM XML

    /**
     * Encodes the FilteredDataTag value as XML. This encoding is used for
     * sending the data tag value to the Statistics module via JMS.
     *
     * @return the XML as string
     */
    public final String toXML() {
        /* Open <FilteredDataTag> tag with all its attributes */
        StringBuffer str = new StringBuffer();

        str.append('<');
        str.append(XML_ROOT_ELEMENT);
        str.append(' ');
        str.append(XML_ATTRIBUTE_ID);
        str.append("=\"");
        str.append(id);
        str.append("\" ");
        str.append(XML_ATTRIBUTE_NAME);
        str.append("=\"");
        str.append(name);
        str.append("\">\n");
        // str.append(XML_ATTRIBUTE_CONTROLTAG);
        // str.append("=\"");
        // str.append(Boolean.toString(this.controlTag));
        // str.append("\">\n");

        /* If the value isn't null, add <value></value> tag */
        if (value != null) {
            str.append("<value data-type=\"");

            /*
             * extract data-type information from the value itself (cutting off
             * the java.lang part of the class name)
             */
            str.append(getDataType());
            str.append("\">");
            str.append(value.toString());
            str.append("</value>\n");
        }

        if (valueDescription != null) {
            str.append("<value-description><![CDATA[");
            str.append(valueDescription);
            str.append("]]></value-description>\n");
        }

        /* If the value is invalid, add a <quality></quality> tag */
        // if (quality != null && !quality.isValid()) {
        // str.append(quality.toXML());
        // }
        /* Add the String quality code (default is 0=ok) */
        str.append("<quality-code>");
        str.append(qualityCode.toString());
        str.append("</quality-code>\n");

        /* Add the Quality description */
        if (qualityDescription != null) {
            str.append("<quality-description>");
            str.append(qualityDescription);
            str.append("</quality-description>\n");
        }

        /* Add the dynamic filtering flag */
        str.append("<dynamic-filtered>");
        str.append(dynamicFiltered);
        str.append("</dynamic-filtered>");

        // Add the filtering type that was applied
        str.append("<filter-applied>");
        str.append(filterApplied);
        str.append("</filter-applied>");

        /* Add <timestamp></timestamp> tag */
        str.append("<timestamp>");
        str.append(timestamp.getTime());
        str.append("</timestamp>\n");

        // if (simulated) {
        // str.append("<simulated>true</simulated>\n");
        // }

        /* Close <DataTag> tag */
        str.append("</");
        str.append(XML_ROOT_ELEMENT);
        str.append(">\n");
        return str.toString();

    }

    /**
     * Decodes the XML into the FilteredDataTagValue object.
     *
     * @param domElement the XML document element
     * @return the tag value object
     */
    public static FilteredDataTagValue fromXML(final Element domElement) {
        Long id = null;
        String name;
        // boolean control;
        FilteredDataTagValue result = null;

        /* Try to extract the datatag id from the XML content */
        try {
            id = Long.valueOf(domElement.getAttribute(XML_ATTRIBUTE_ID));
        } catch (NumberFormatException nfe) {
            LOGGER.error("Cannot extract valid id attribute from <DataTag> element.");
            id = null;
        }

        /* Only proceed if the id has been extracted successfully */
        if (id != null) {
            name = domElement.getAttribute(XML_ATTRIBUTE_NAME);
            // control =
            // domElement.getAttribute(XML_ATTRIBUTE_CONTROLTAG).equals("true");

            result = new FilteredDataTagValue(id, name);

            NodeList fields = domElement.getChildNodes();
            String fieldName;
            String fieldValueString;
            Node fieldNode;

            int fieldsCount = fields.getLength();

            for (int i = 0; i != fieldsCount; i++) {
                fieldNode = fields.item(i);
                if (fieldNode.getNodeType() == Node.ELEMENT_NODE && fieldNode.getFirstChild() != null) {
                    fieldName = fieldNode.getNodeName();
                    fieldValueString = fieldNode.getFirstChild().getNodeValue();

                    if (fieldName.equals("value")) {
                        String dataType = fieldNode.getAttributes().item(0).getNodeValue();
                        result.value = fieldValueString;
                        result.dataType = dataType;
                    } else if (fieldName.equals("value-description")) {
                        result.valueDescription = fieldValueString;
                    }
                    // quality code is set as OK by default
                    else if (fieldName.equals("quality-code")) {
                        result.qualityCode = Integer.valueOf(fieldValueString);
                    } else if (fieldName.equals("quality-description")) {
                        result.qualityDescription = fieldValueString;
                    } else if (fieldName.equals("timestamp")) {
                        try {
                            result.timestamp = new Timestamp(Long.parseLong(fieldValueString));
                        } catch (NumberFormatException nfe) {
                            LOGGER.error("Error during timestamp extraction. Taking current time as timestamp.");
                            result.timestamp = new Timestamp(System.currentTimeMillis());
                        }
                    } else if (fieldName.equals("dynamic-filtered")) {
                        result.dynamicFiltered = Boolean.valueOf(fieldValueString).booleanValue();
                    } else if (fieldName.equals("filter-applied")) {
                        result.filterApplied = Short.valueOf(fieldValueString).shortValue();
                    }
                    // else if (fieldName.equals("simulated")) {
                    // result.simulated = true;
                    // }
                }
            }
        }
        return result;
    }
}
