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

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cern.c2mon.shared.daq.datatag.SourceDataQuality;

/**
 * does not allow null values for attributes (empty strings only)
 * 
 * Notice these filter classes are all shared between the DAQ layer and the Statistics Consumer
 * process, not the server.
 * 
 * @author mbrightw
 * 
 */
public class FilteredDataTagValue {

    /**
     * The general logger.
     */
    private static final Logger LOGGER = Logger.getLogger(FilteredDataTagValue.class);
    
    /**
     * Filter Type enum
     */
    public enum FilterType {
      /*
       * Tag value was filtered out as the previously received
       * value had a similar value, quality flag and quality description.
       */
      REPEATED_INVALID((short)0), 
      /*
       * Tag value was filtered out as the value was in the value deadband.
       */
      VALUE_DEADBAND((short)1), 
      /*
       *  Filtering occurred because the value received was the same as the current value.
       */
      REPEATED_VALUE((short)2), 
      /*
       * Filtering occurred because the value was received during the time deadband.
       */
      TIME_DEADBAND((short)3),
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
      OLD_UPDATE((short)4),
      /*
       * No filtering
       */
      NO_FILTERING((short)5);

      /**
       * The Filter type name
       */
      private short number;

      /**
       * The Filter type number
       * 
       * @param number The Filter type number
       */
      FilterType(final short number) {
        this.number = number;
      }

      /**
       * @return The Filter object number
       */
      public final short getNumber() {
        return this.number;
      }
    }

    /**
     * The log of all tag values sent to the filter module.
     */
    private static final Logger TAGLOGGER = Logger.getLogger("FilteredDataTagLogger");

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
    private Short qualityCode = new Short(SourceDataQuality.OK);

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
    private boolean dynamicFiltered;

    /**
     * Indicates the type of filtering that was applied to this value.
     */
    private short filterApplied;

    // CONSTRUCTORS

    /**
     * Constructor.
     * 
     * @param pId the tag id
     * @param pName the tag name
     */
    public FilteredDataTagValue(final Long pId, final String pName) {
        this(pId, pName, null, // value
                null, // quality code
                null, // quality description
                null, // timestamp
                null, // value description
                null, // datatype
                false, // dynamic filtered
                (short) 0 // filtering type set to 0 (set correctly at a later
                          // stage)
        );
    }

    /**
     * Constructor.
     * 
     * @param pId the tag id
     * @param pName the tag name
     * @param pValue the tag value
     * @param pQualityCode the tag quality
     * @param pQualityDescription the tag quality description
     * @param pTimestamp the timestamp of the value
     * @param pValueDescription the value description
     * @param pDataType the value datatype
     * @param pDynamicFiltered flag indicating if the value was dynamically filtered
     * @param pFilterApplied the filtering applied
     */
    public FilteredDataTagValue(Long pId, String pName, String pValue, Short pQualityCode, String pQualityDescription, Timestamp pTimestamp, String pValueDescription, String pDataType, boolean pDynamicFiltered, short pFilterApplied) {
        this.id = pId;
        this.name = pName;
        this.value = pValue;
        this.qualityCode = pQualityCode;
        this.qualityDescription = pQualityDescription;
        this.timestamp = pTimestamp;
        this.valueDescription = pValueDescription;
        this.dataType = pDataType;
        this.dynamicFiltered = pDynamicFiltered;
        this.filterApplied = pFilterApplied;
    }

    /**
     * logs the FilteredDataTag in the appropriate log file
     */
    public final void log() {
        TAGLOGGER.info(this);
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
                        result.qualityCode = Short.valueOf(fieldValueString);
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

    // GETTERS AND SETTERS

    /**
     * Get the data type of the DataTag's current value as a string. This method
     * will return null if the tag's current value is null.
     * 
     * @return the datatype of the value
     */
    public final String getDataType() {
        return dataType;
    }

    /**
     * Sets the value description of the value object.
     * 
     * @param pValueDescription the description
     */
    public final void setValueDescription(final String pValueDescription) {
        this.valueDescription = pValueDescription;
    }

    /**
     * Set the quality description of the object.
     * 
     * @param pQualityDescription the quality description
     */
    public final void setQualityDescription(final String pQualityDescription) {
        this.qualityDescription = pQualityDescription;
    }

    /**
     * Gets the tag id.
     * 
     * @return the id
     */
    public final Long getId() {
        return id;
    }

    /**
     * Gets the tag name.
     * @return the name
     */
    public final String getName() {
        return name;
    }

    /**
     * Gets the value.
     * @return the value
     */
    public final String getValue() {
        return value;
    }

    /**
     * Returns the description of the value.
     * @return the value description
     */
    public final String getValueDescription() {
        return valueDescription;
    }

    /**
     * Returns the quality code.
     * @return the quality code
     */
    public final Short getQualityCode() {
        return qualityCode;
    }

    /**
     * Returns the quality description.
     * @return the quality description
     */
    public final String getQualityDescription() {
        return qualityDescription;
    }

    /**
     * Returns the timestamp of the value.
     * @return the timestamp
     */
    public final Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the flag indicating if the value was filtered dynamically.
     * @return the dynamic filtered flag
     */
    public final boolean isDynamicFiltered() {
        return dynamicFiltered;
    }

    /**
     * Returns the type of filtering that was applied.
     * @return the filter type
     */
    public final short getFilterApplied() {
        return filterApplied;
    }

}
