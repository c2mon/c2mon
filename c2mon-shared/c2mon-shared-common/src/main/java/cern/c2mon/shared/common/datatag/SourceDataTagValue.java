/******************************************************************************
 * Copyright (C) 2010-2020 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.shared.common.datatag;

import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Objects of the SourceDataTagValue class represent the current value of a
 * SourceDataTag. They are created within the TIM DAQ process whenever a
 * SourceDataTag is updated, usually by a call to the SourceDataTag's update()
 * and invalidate() methods.
 *
 * Subsequently, an XML representation of the SourceDataTagValue object is sent
 * to the application server as an XML message. The application server decodes
 * the contents using the fromXML method and propagates the value update to the
 * server representation of the DataTag.
 *
 * A new SourceDataTagValue object is created for each value update. This is why
 * the class contains no setXXX() method for any of its fields.
 *
 * Please note that the toXML()/fromXML methods of this class disregard any
 * fields that need not be transmitted to the application server. The fields
 * guaranteedDelivery, priority and timeToLive are only needed as message
 * parameters and are therefore not included in the XML. Therefore, a
 * SourceDataTagValue object created via the fromXML method may not be equal to
 * the original object (equals() may return false).
 *
 * @author Jan Stowisek
 */
@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class SourceDataTagValue implements Cloneable {
  // ----------------------------------------------------------------------------
  // PRIVATE STATIC MEMBERS
  // ----------------------------------------------------------------------------
  public static final String XML_ROOT_ELEMENT = "DataTag";

  /**
   * Log4j Logger for logging DataTag values.
   */
  protected static final Logger TAG_LOG = LoggerFactory.getLogger("SourceDataTagLogger");

  // ----------------------------------------------------------------------------
  // MEMBERS
  // ----------------------------------------------------------------------------

  /** Unique numeric identifier of the tag */
  protected Long id;

  /** Unique name of the tag */
  protected String name;

  /** Flag indicating whether the tag is used for system supervision */
  protected boolean controlTag;

  /** Current value of the tag (may be null if invalid) */
  protected Object value;

  /** Optional value description **/
  protected String valueDescription = "";

  /** Current data quality of the tag (may be null if value is valid) */
  protected SourceDataTagQuality quality;

  /** Source timestamp of the current value */
  protected Timestamp timestamp;

  /**
   * Timestamp set locally when the value was last updated (the DAQ server
   * system time at the time the update was performed).
   */
  protected Timestamp daqTimestamp;

  /**
   * Priority with which the tag value has to be sent to the server: either
   * DataTagAddress.PRIORITY_HIGH or DataTagAddress.PRIORITY_LOW
   */
  protected int priority;

  /**
   * Flag indicating whether a guaranteed delivery mechanism has to be used for
   * transmitting the value to the server.
   */
  protected boolean guaranteedDelivery;

  /**
   * Time to live in milliseconds. If the transmission to the server takes
   * longer, the value is discarded.
   */
  protected int timeToLive;

  /**
   * Flag indicating whether the value is the result of a simulation.
   */
  protected boolean simulated;

  // ----------------------------------------------------------------------------
  // CONSTRUCTORS
  // ----------------------------------------------------------------------------

  /**
   * Constructor. The DAQ timestamp is set at object creation.
   */
  public SourceDataTagValue(final Long pId, final String pName, final boolean pControlTag) {
    this.id = pId;
    this.name = pName;
    this.controlTag = pControlTag;
    this.value = null;
    this.valueDescription = "";
    this.quality = null;
    this.timestamp = null;
    this.priority = DataTagAddress.PRIORITY_LOW;
    this.guaranteedDelivery = false;
    this.timeToLive = DataTagAddress.TTL_FOREVER;
    this.simulated = false;
    daqTimestamp = new Timestamp(System.currentTimeMillis());
  }

  /**
   * Copy constructor. Create a deep copy of the SourceDataTagValue object
   * passed as a parameter. Notice the DAQ timestamp is also copied: depending
   * on the context it may make more sense to reset it using the setter method.
   *
   * @param pOld
   *          original to be copied.
   */
  public SourceDataTagValue(final SourceDataTagValue pOld) {
    this.id =  pOld.id;
    this.name =  pOld.name;
    this.controlTag =  pOld.controlTag;
    this.value =  pOld.value;
    this.setValueDescription( pOld.valueDescription);
    this.quality =  pOld.quality;
    this.timestamp =  pOld.timestamp;
    this.priority =  pOld.priority;
    this.guaranteedDelivery =  pOld.guaranteedDelivery;
    this.timeToLive =  pOld.timeToLive;
    this.simulated = pOld.simulated;
    this.daqTimestamp = pOld.daqTimestamp;
  }

  // ----------------------------------------------------------------------------
  // READ-ONLY MEMBER ACCESSORS and UTILITY METHODS
  // ----------------------------------------------------------------------------

  /**
   * Get the data type of the DataTag's current value as a string. This method
   * will return null if the tag's current value is null.
   */
  public String getDataType() {
    if (this.value == null) {
      return null;
    }

    String dataType = value.getClass().getName();
    if (dataType.contains(".")) {
      return dataType.substring(dataType.lastIndexOf('.') + 1);
    }

    return dataType;
  }

  /**
   * Set a free-text description for the tag's current value.
   */
  public void setValueDescription(final String description) {
    // if description is not defined, store empty string instead of null
    if (description == null) {
      this.valueDescription = "";
    } else {
      this.valueDescription = description;
    }
  }

  /**
   * Get the quality of the DataTag's current value. If no quality has been set
   * for this object, we assume that the value is of GOOD quality. Therefore, a
   * new SourceDataTagQuality object will be returned.
   */
  public SourceDataTagQuality getQuality() {
    return this.quality != null ? this.quality : new SourceDataTagQuality();
  }

  /**
   * Check whether the DataTag's current value is valid. A value is considered
   * valid if the associated SourceDataTagQuality object is valid OR if no
   * SourceDataTagQuality object is associated with the value.
   */
  public boolean isValid() {
    return (quality == null || quality.isValid());
  }

  /**
   * Create a SourceDataTagValue object from a DOM element. As this method is
   * only used on the application server, to received tag value updates sent by
   * a driver, this method will initialise the fields priority,
   * guaranteedDelivery and timeToLive.
   *
   * @param domElement
   *          DOM element SourceDataTagValue object
   * @return a SourceDataTagValue object.
   */

  private static final String XML_ATTRIBUTE_ID = "id";
  private static final String XML_ATTRIBUTE_NAME = "name";
  private static final String XML_ATTRIBUTE_CONTROLTAG = "control";

  /**
   * Create a SourceDataTagValue from its XML representation. It is ESSENTIAL to
   * know that the following fields are NOT initialised from the XML:
   * <UL>
   * <LI>guaranteedDelivery</LI>
   * <LI>priority</LI>
   * <LI>timeToLive</LI>
   * </UL>
   *
   * @return
   */

  public static SourceDataTagValue fromXML(Element domElement) {

    /*
     * We assume that the root element really is <DataTag> as this method is
     * only called from DataTagValueUpdate.fromXML if a <DataTag> element is
     * found.
     */
    Long id = null;
    String name;
    boolean control;
    SourceDataTagValue result = null;

    /* Try to extract the datatag id from the XML content */
    try {
      id = Long.valueOf(domElement.getAttribute(XML_ATTRIBUTE_ID));
    } catch (NumberFormatException nfe) {
      log.error("Cannot extract valid id attribute from <DataTag> element.");
      throw nfe;
    }

    /* Only proceed if the id has been extracted successfully */
    if (id != null) {
      name = domElement.getAttribute(XML_ATTRIBUTE_NAME);
      if (domElement.getAttribute(XML_ATTRIBUTE_CONTROLTAG) != null) {
        control = domElement.getAttribute(XML_ATTRIBUTE_CONTROLTAG).equals("true");
      } else {
        throw new RuntimeException("Control tag attribute not set in SourceDataTagValue XML - unable to decode it.");
      }

      result = new SourceDataTagValue(id, name, control);

      NodeList fields = domElement.getChildNodes();
      String fieldName;
      String fieldValueString;
      Node fieldNode;

      int fieldsCount = fields.getLength();

      for (int i = 0; i != fieldsCount; i++) {
        fieldNode = fields.item(i);
        if (fieldNode.getNodeType() == Node.ELEMENT_NODE) {
          fieldName = fieldNode.getNodeName();
          if (fieldNode.getFirstChild() != null) {
            fieldValueString = fieldNode.getFirstChild().getNodeValue();
            if (fieldName.equals("value")) {
              String dataType = fieldNode.getAttributes().item(0).getNodeValue();

              if (dataType.equals("Integer")) {
                result.value = Integer.valueOf(fieldValueString);
              } else if (dataType.equals("Float")) {
                result.value = Float.valueOf(fieldValueString);
              } else if (dataType.equals("Double")) {
                result.value = Double.valueOf(fieldValueString);
              } else if (dataType.equals("Long")) {
                result.value = Long.valueOf(fieldValueString);
              } else if (dataType.equals("Boolean")) {
                result.value = Boolean.valueOf(fieldValueString);
              } else if (dataType.equals("String")) {
                result.value = fieldValueString;
              }
            } else if (fieldName.equals("value-description")) {
              result.valueDescription = fieldNode.getFirstChild().getNodeValue();
            } else if (fieldName.equals("quality")) {
              result.quality = SourceDataTagQuality.fromXML((Element) fieldNode);
            } else if (fieldName.equals("timestamp")) {
              try {
                result.timestamp = new Timestamp(Long.parseLong(fieldValueString));
              } catch (NumberFormatException nfe) {
                log.error("Error during timestamp extraction.");
                result.timestamp = new Timestamp(System.currentTimeMillis());
              }
            } else if (fieldName.equals("daq-timestamp")) {
              try {
                result.daqTimestamp = new Timestamp(Long.parseLong(fieldValueString));
              } catch (NumberFormatException nfe) {
                log.error("Error during DAQ timestamp extraction - leaving null.");
                result.daqTimestamp = null;
              }
            } else if (fieldName.equals("simulated")) {
              result.simulated = true;
            }
          }
        }
      }

      // If no quality was specified in the XML, we assume that the value is
      // valid
      if (result.quality == null) {
        result.quality = new SourceDataTagQuality();
      }
    }
    return result;
  }

  /**
   * Create a clone of this SourceDataTagValue object.
   *
   * @throws CloneNotSupportedException
   */
  @Override
  public SourceDataTagValue clone() {

    SourceDataTagValue clone = null;
    try {
      clone = (SourceDataTagValue) super.clone();

      if (this.daqTimestamp != null) {
        clone.daqTimestamp = (Timestamp) this.daqTimestamp.clone();
      }
      if (this.quality != null) {
        clone.quality = this.quality.clone();
      }
      if (this.timestamp != null) {
        clone.timestamp = (Timestamp) this.timestamp.clone();
      }
      // TODO: We assume that the value field is always a primitve Object. In
      // case this changes in the future
      // you have to make sure that the value field gets cloned as well!
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(
          "Catched CloneNotSupportedException when trying to create a clone from SourceDataTagValue! Please check the code",
          e);
    }

    return clone;
  }

  public void log() {
    if (TAG_LOG.isInfoEnabled()) {
      TAG_LOG.info(this.toString());
    }
  }

  @Override
  public boolean equals(final Object pObj) {
    boolean result = pObj != null && pObj instanceof SourceDataTagValue;
    if (result) {
      SourceDataTagValue copy = (SourceDataTagValue) pObj;
      result = this.controlTag == copy.controlTag && this.guaranteedDelivery == copy.guaranteedDelivery
          && this.simulated == copy.simulated && this.priority == copy.priority;
      if (result) {
        if (this.value != null) {
          result = result && this.value.equals(copy.value);
        }
        if (this.valueDescription != null) {
          result = result && this.valueDescription.equals(copy.valueDescription);
        }
        if (this.id != null) {
          result = result && this.id.equals(copy.id);
        }
        if (this.name != null) {
          result = result && this.name.equals(copy.name);
        }
        if (this.quality != null) {
          result = result && this.quality.equals(copy.quality);
        }
        if (this.timestamp != null) {
          result = result && this.timestamp.equals(copy.timestamp);
        }
        if (this.daqTimestamp != null) {
          result = result && this.daqTimestamp.equals(copy.daqTimestamp);
        }
      }

    }
    return result;
  }

  @Override
  public String toString() {
    StringBuffer str = new StringBuffer();

    str.append(getId());
    str.append('\t');
    str.append(getName());
    str.append('\t');
    str.append(getTimestamp());
    str.append('\t');
    str.append(getValue());
    str.append('\t');
    str.append(getDataType());
    str.append('\t');
    str.append(getQuality().getQualityCode());
    if (getQuality().getDescription() != null) {
      str.append('\t');
      str.append(getQuality().getDescription());
    }
    if (getValueDescription() != null) {
      str.append('\t');
      // remove all \n and replace all \t characters of the value description string
      str.append(getValueDescription().replace("\n", "").replace("\t", "  "));
    }

    return str.toString();
  }

}
