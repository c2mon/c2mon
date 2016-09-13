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

import java.util.ArrayList;
import java.util.Collection;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


/**
 * The DataTagValueUpdate class is used for encoding/decoding DataTag value
 * update XML messages sent to the server application by TIM drivers.
 */
@Slf4j @AllArgsConstructor
public final class DataTagValueUpdate {

  private static final String XML_ROOT_ELEMENT= "DataTagValueUpdate";

  private static final String XML_ATTRIBUTE_PROCESS_ID= "process-id";
  
  private static final String XML_ATTRIBUTE_PROCESS_PIK= "process-pik";
  
  // ----------------------------------------------------------------------------
  // MEMBERS
  // ----------------------------------------------------------------------------

  @Getter @Setter
  protected Long processId = null;
  @Getter
  protected Long processPIK = null;
  protected ArrayList<SourceDataTagValue> tagValues = null;

  // ----------------------------------------------------------------------------
  // CONSTRUCTORS
  // ----------------------------------------------------------------------------
  protected DataTagValueUpdate() {
  }

  public DataTagValueUpdate(final Long pProcessId) {
    this.processId = pProcessId;
    this.tagValues = new ArrayList<SourceDataTagValue>(10);
  }

  public DataTagValueUpdate(final Long pProcessId, final ArrayList<SourceDataTagValue> pTagValues) {
    this.processId = pProcessId;
    this.tagValues = pTagValues;
  }
  
  public DataTagValueUpdate(final Long pProcessId, final Long pProcessPIK) {
    this.processId = pProcessId;
    this.processPIK = pProcessPIK;
    this.tagValues = new ArrayList<SourceDataTagValue>(10);
  }

  public void addValue(final SourceDataTagValue pValue) {
    this.tagValues.add(pValue);
  }

  public void setValues(final ArrayList<SourceDataTagValue> pSourceDataTagValues) {
    this.tagValues = pSourceDataTagValues;
  }

  public Collection<SourceDataTagValue> getValues() {
    return this.tagValues;
  }

  @Deprecated
  public static DataTagValueUpdate fromXML(Element domElement) {
    DataTagValueUpdate result = new DataTagValueUpdate();

    /* Only process if the element name is <DataTagValueUpdate> */
    if (domElement.getNodeName().equals(XML_ROOT_ELEMENT)) {

      /* Try to extract the process-id attribute from the XML message. 
       * If this fails, return null. */
      try {
        result.processId = Long.valueOf(domElement.getAttribute(XML_ATTRIBUTE_PROCESS_ID));

        /* Only proceed if the extraction of the process-id was successful */
        if (result.processId != null) {

          try {
          result.processPIK = Long.valueOf(domElement.getAttribute(XML_ATTRIBUTE_PROCESS_PIK));
          } catch (NumberFormatException nfe) {
            log.trace("DataTagValueUpdate - fromXML - No PIK attribute received.");
          }
          
          Node fieldNode = null; 
          NodeList fields = domElement.getChildNodes();
          int fieldsCount = fields.getLength();
        
          result.tagValues = new ArrayList<SourceDataTagValue>(fieldsCount);

          for (int i = 0; i < fieldsCount; i++) {
            fieldNode = fields.item(i);
            if (fieldNode.getNodeType() == Node.ELEMENT_NODE 
                && fieldNode.getNodeName().equals(SourceDataTagValue.XML_ROOT_ELEMENT)
            ) {
              result.tagValues.add(
                  SourceDataTagValue.fromXML((Element) fieldNode));
            }
          } // for
        } // if processId != null

      } catch (NumberFormatException nfe) {
        result = null;
        log.error("DataTagValueUpdate - Cannot extract valid process-id from DataTagValueUpdate message. Returning null.");
      }
    } // if DataTagValueUpdate
    else {
      result = null;
      log.error("DataTagValueUpdate - Cannot decode DataTagValueUpdate message. Root element is not <DataTagValueUpdate>");
    }
    return result;
  }

  public void log() {
    if (this.tagValues != null) {
      int size = tagValues.size();
      for (int i = 0; i != size; i++) {
        ((SourceDataTagValue) tagValues.get(i)).log();
      }
    }
  }
}
