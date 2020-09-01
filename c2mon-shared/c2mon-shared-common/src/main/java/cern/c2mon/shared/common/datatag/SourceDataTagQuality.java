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
package cern.c2mon.shared.common.datatag;

import lombok.AllArgsConstructor;
import lombok.Data;

import cern.c2mon.shared.common.datatag.util.SourceDataTagQualityCode;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The quality object of the {@link SourceDataTag}
 *
 * @author Matthias Braeger
 */
@AllArgsConstructor
@Data
public class SourceDataTagQuality implements Cloneable {

  @Deprecated
  private static final String XML_ELEMENT_QUALITY_CODE = "code";
  @Deprecated
  private static final String XML_ELEMENT_QUALITY_DESC = "desc";

  /** The quality code */
  private SourceDataTagQualityCode qualityCode;
  /** Additional description for the quality reason */
  private String description = "";

  /**
   * The default quality is OK
   */
  public SourceDataTagQuality() {
    this.qualityCode = SourceDataTagQualityCode.OK;
  }

  public SourceDataTagQuality(SourceDataTagQualityCode qualityCode) {
    this.qualityCode = qualityCode;
  }

  /**
   * Check whether this SourceDataQuality object represents a VALID value. The
   * method will return true if the quality code is SourceDataTagQualityCode.OK
   *
   * @return true, if the quality object represents a "valid" object
   */
  public final boolean isValid() {
    return qualityCode == SourceDataTagQualityCode.OK;
  }

  /**
   * Create a SourceDataQuality object from its XML representation. The format
   * of the XML required is determined by the output of the toXML() method.
   *
   * @deprecated Should be removed once XML serialization is not anymore supported
   */
  @Deprecated
  public static SourceDataTagQuality fromXML(Element domElement) {
    NodeList fields = domElement.getChildNodes();
    int fieldsCount = fields.getLength();
    String fieldName;
    String fieldValueString;
    Node fieldNode;

    // Create result object
    SourceDataTagQuality result = new SourceDataTagQuality();

    // Extract information from DOM elements
    for (int i = 0; i != fieldsCount; i++) {
      fieldNode = fields.item(i);
      if (fieldNode.getNodeType() == Node.ELEMENT_NODE) {
        fieldName = fieldNode.getNodeName();
        Node fieldValueNode = fieldNode.getFirstChild();
        if (fieldValueNode != null) {
          fieldValueString = fieldValueNode.getNodeValue();
        }
        else {
          fieldValueString = "";
        }

        if (fieldName.equals(XML_ELEMENT_QUALITY_CODE)) {
          short code = Short.parseShort(fieldValueString);
          result.qualityCode = SourceDataTagQualityCode.getEnum(code);
        }
        else if (fieldName.equals(XML_ELEMENT_QUALITY_DESC)) {
          result.description = fieldValueString;
        }
      }
    }

    return result;
  }

  @Override
  public SourceDataTagQuality clone() {

    SourceDataTagQuality clone = null;
    try {
      clone = (SourceDataTagQuality) super.clone();
    }
    catch (CloneNotSupportedException e) {
      throw new RuntimeException("Catched CloneNotSupportedException when trying to create a clone from SourceDataTagQuality! Please check the code", e);
    }

    return clone;

  }
}