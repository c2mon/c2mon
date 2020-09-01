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

import javax.xml.parsers.ParserConfigurationException;

import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import lombok.extern.slf4j.Slf4j;

import cern.c2mon.shared.common.command.SourceCommandTag;
import cern.c2mon.shared.common.datatag.SourceDataTag;
import cern.c2mon.shared.common.datatag.util.JmsMessagePriority;
import cern.c2mon.shared.common.process.EquipmentConfiguration;
import cern.c2mon.shared.common.process.IEquipmentConfiguration;
import cern.c2mon.shared.common.process.SubEquipmentConfiguration;
import cern.c2mon.shared.daq.config.ConfigurationXMLConstants;
import cern.c2mon.shared.util.parser.SimpleXMLParser;

@Slf4j
@Component
public class EquipmentConfigurationFactory extends XMLTagValueExtractor implements ConfigurationXMLConstants {

  private final SimpleXMLParser parser;
  
  public EquipmentConfigurationFactory() throws ParserConfigurationException {
	  this.parser = new SimpleXMLParser();
  }
  
  /**
   * Creates the equipment configuration from the matching subelement in the DOM
   * tree.
   *
   * @param equipmentUnit A EquipmentUnit element from the DOM tree.
   * @return An equipment configuration object.
   */
  public EquipmentConfiguration createEquipmentConfiguration(final String equipmentUnitXml) throws Exception {
    return this.createEquipmentConfiguration(parser.parse(equipmentUnitXml).getDocumentElement());
  }

  /**
   * Creates the equipment configuration from the matching subelement in the DOM
   * tree.
   *
   * @param equipmentUnit A EquipmentUnit element from the DOM tree.
   * @return An equipment configuration object.
   */
  public EquipmentConfiguration createEquipmentConfiguration(final Element equipmentUnit) throws Exception {
    String eqID = equipmentUnit.getAttribute(ID_ATTRIBUTE);
    log.debug("EQ ID : " + eqID);
    String eqName = equipmentUnit.getAttribute(NAME_ATTRIBUTE);

    EquipmentConfiguration equipmentConfiguration = new EquipmentConfiguration();

    try {
      equipmentConfiguration.setId(Long.parseLong(eqID));
      equipmentConfiguration.setName(eqName);

      equipmentConfiguration.setHandlerClassName(getTagValue(equipmentUnit, HANDLER_CLASS_NAME_ELEMENT).trim());

      equipmentConfiguration.setCommFaultTagId(Long.parseLong(getTagValue(equipmentUnit, COMMFAULT_TAG_ID_ELEMENT)));

      equipmentConfiguration.setCommFaultTagValue(Boolean.parseBoolean(getTagValue(equipmentUnit, COMMFAULT_TAG_VALUE_ELEMENT)));

      // try and be prepared to catch the exception, because the field is
      // not obligatory and may not exist
      try {
        equipmentConfiguration.setAliveTagId(Long.parseLong(getTagValue(equipmentUnit, ALIVE_TAG_ID_ELEMENT)));
      } catch (NullPointerException ex) {
        log.debug("Equipment {} has no alive Tag id.", equipmentConfiguration.getName());
      }
      // try and be prepared to catch the exception, because the field is
      // not obligatory and may not exist
      try {
        equipmentConfiguration.setAliveTagInterval(Long.parseLong(getTagValue(equipmentUnit, ALIVE_INTERVAL_ELEMENT)));
      } catch (NullPointerException ex) {
        log.debug("Equipment {} has no alive Tag interval.", equipmentConfiguration.getName());
      }

      // try and be prepared to catch the exception, because the field is
      // not obligatory and may not exist
      try {
        equipmentConfiguration.setEquipmentAddress(getTagValue(equipmentUnit, ADDRESS_ELEMENT));
      } catch (NullPointerException ex) {
        equipmentConfiguration.setEquipmentAddress(null);
      }
    } finally {

    }
    // should be one
    processSubEquipmentUnits(equipmentUnit, equipmentConfiguration);
    processDataTags(equipmentUnit, equipmentConfiguration);
    processCommandTags(equipmentUnit, equipmentConfiguration);

    return equipmentConfiguration;
  }

  /**
   * Processes all subequipment units of the provided equipment unit and adds
   * their commFault IDs and values to the configuration object.
   *
   * @param equipmentUnit The equipment unit element to go through.
   * @param equipmentConfiguration The configuration object to fill.
   */
  private void processSubEquipmentUnits(final Element equipmentUnit, final IEquipmentConfiguration equipmentConfiguration) {
    NodeList subEquipmentUnitsList = equipmentUnit.getElementsByTagName(SUB_EQUIPMENT_UNITS_ELEMENT);
    Element subEquipmentUnitsElement = null;
    if (subEquipmentUnitsList != null && subEquipmentUnitsList.getLength() > 0) {
      subEquipmentUnitsElement = (Element) subEquipmentUnitsList.item(0);
    }
    if (subEquipmentUnitsElement != null) {
      NodeList subEquipmentUnitsNode = subEquipmentUnitsElement.getElementsByTagName(SUB_EQUIPMENT_UNIT_ELEMENT);
      log.debug("\t" + subEquipmentUnitsNode.getLength() + " SubEquipments found for current equipment");

      for (int i = 0; i < subEquipmentUnitsNode.getLength(); i++) {
        log.debug("Creating a SubEquipment configuration object...");
        Element subEquipmentConf = (Element) subEquipmentUnitsNode.item(i);

        String subEquipmentId = subEquipmentConf.getAttribute(ID_ATTRIBUTE);
        String subEquipmentName = subEquipmentConf.getAttribute(NAME_ATTRIBUTE);

        String commFaultId = getTagValue(subEquipmentConf, COMMFAULT_TAG_ID_ELEMENT);
        String commFaultValue = getTagValue(subEquipmentConf, COMMFAULT_TAG_VALUE_ELEMENT);

        SubEquipmentConfiguration subEquipmentConfiguration = new SubEquipmentConfiguration(Long.parseLong(subEquipmentId), subEquipmentName,
            Long.parseLong(commFaultId), Boolean.parseBoolean(commFaultValue));

        // Also read alive tags for SubEquipments
        if (subEquipmentConf.getElementsByTagName(ALIVE_TAG_ID_ELEMENT).getLength() > 0) {
          String aliveTagId = getTagValue(subEquipmentConf, ALIVE_TAG_ID_ELEMENT);
          String aliveTagInterval = getTagValue(subEquipmentConf, ALIVE_INTERVAL_ELEMENT);
          subEquipmentConfiguration.setAliveTagId(Long.parseLong(aliveTagId));
          subEquipmentConfiguration.setAliveInterval(Long.parseLong(aliveTagInterval));
        }

        equipmentConfiguration.getSubEquipmentConfigurations().put(subEquipmentConfiguration.getId(), subEquipmentConfiguration);
      }
    }
  }

  /**
   * Processes all data tags of this equipment configuration DOM element and
   * adds them to the equipment configuration object.
   *
   * @param equipmentUnit The DOM element with the data.
   * @param equipmentConfiguration The equipment configuration object.
   */
  private void processDataTags(final Element equipmentUnit, final EquipmentConfiguration equipmentConfiguration) {
    Element dataTagsBlock = (Element) equipmentUnit.getElementsByTagName(DATA_TAGS_ELEMENT).item(0);
    NodeList dataTags = dataTagsBlock.getElementsByTagName(DATA_TAG_ELEMENT);
    log.debug("\t" + dataTags.getLength() + " DataTags found for current equipment");
    SourceDataTag sourceDataTag = null;
    // for each SourceDataTag defined in the DataTags XML block
    for (int i = 0; i < dataTags.getLength(); i++) {
      sourceDataTag = SourceDataTag.fromConfigXML((Element) dataTags.item(i));
      log.debug("\tCreating SourceDataTag object for id " + sourceDataTag.getId() + "..");
      if (sourceDataTag.getAddress().isTimeDeadbandEnabled()) {
        sourceDataTag.getAddress().setStaticTimedeadband(true);
      }
      if (sourceDataTag.getId().longValue() == equipmentConfiguration.getAliveTagId()) {
        if (sourceDataTag.getAddress().getPriority() != JmsMessagePriority.PRIORITY_HIGH.getPriority()) {
          log.warn("\tPriority on equipment alive tag " + sourceDataTag.getId() + " is wrongly configured! Adjusting priority to HIGH (7)");
          sourceDataTag.getAddress().setPriority(JmsMessagePriority.PRIORITY_HIGH);
        }
        if (!sourceDataTag.isControl()) {
          log.warn("\tEquipment alive tag " + sourceDataTag.getId() + " is not configured as control tag! Please correct this in the configuration.");
        }
      }
      equipmentConfiguration.getDataTags().put(sourceDataTag.getId(), sourceDataTag);

//      // register tag in the ValueChangeMonitorEngine if needed
//      if (sourceDataTag.hasValueCheckMonitor()) {
//        ValueChangeMonitor vcm = sourceDataTag.getValueCheckMonitor();
//        ValueChangeMonitorEngine.getInstance().register(equipmentConfiguration.getId(), sourceDataTag, vcm);
//      }

    } // for
  }

  /**
   * Processes all command tags in this equipment configuration DOM element and
   * adds them to the configuration object.
   *
   * @param equipmentUnit A equipment unit DOM element.
   * @param equipmentConfiguration The equipment configuration object.
   */
  private void processCommandTags(final Element equipmentUnit, final EquipmentConfiguration equipmentConfiguration) {
    Element commandTagsBlock = (Element) equipmentUnit.getElementsByTagName(COMMAND_TAGS_ELEMENT).item(0);
    NodeList commandTags = commandTagsBlock.getElementsByTagName(COMMAND_TAG_ELEMENT);
    SourceCommandTag sourceCommandTag = null;
    // for each SourceDataTag defined in the DataTags XML block
    for (int i = 0; i < commandTags.getLength(); i++) {
      sourceCommandTag = SourceCommandTag.fromConfigXML((Element) commandTags.item(i));
      log.debug("creating SourceCommandTag object for id " + sourceCommandTag.getId() + "..");
      equipmentConfiguration.getCommandTags().put(sourceCommandTag.getId(), sourceCommandTag);
    }
  }

  /**
   * Creates a SubEquipment configuration from the matching subelement in the DOM tree.
   *
   * @param subEquipmentUnitXml A SubEquipmentUnit XML string
   * @return A SubEquipment configuration object.
   */
  public SubEquipmentConfiguration createSubEquipmentConfiguration(String subEquipmentUnitXml) {
    Element subEquipmentElement = parser.parse(subEquipmentUnitXml).getDocumentElement();

    Long subEquipmentId = Long.parseLong(subEquipmentElement.getAttribute(ID_ATTRIBUTE));
    String subEquipmentName = subEquipmentElement.getAttribute(NAME_ATTRIBUTE);
    log.debug("Creating SubEquipment configuration: id=" + subEquipmentId + " name=" + subEquipmentName);

    Long commFaultTagId = Long.parseLong(getTagValue(subEquipmentElement, COMMFAULT_TAG_ID_ELEMENT));
    Boolean commFaultTagValue = Boolean.parseBoolean(getTagValue(subEquipmentElement, COMMFAULT_TAG_VALUE_ELEMENT));

    SubEquipmentConfiguration subEquipmentConfiguration = new SubEquipmentConfiguration(subEquipmentId, subEquipmentName, commFaultTagId, commFaultTagValue);

    try {
      subEquipmentConfiguration.setAliveTagId(Long.parseLong(getTagValue(subEquipmentElement, ALIVE_TAG_ID_ELEMENT)));
    } catch (NullPointerException e) {
      log.debug("SubEquipment has no alive tag id.");
    }

    try {
      subEquipmentConfiguration.setAliveInterval(Long.parseLong(getTagValue(subEquipmentElement, ALIVE_INTERVAL_ELEMENT)));
    } catch (NullPointerException e) {
      log.debug("SubEquipment has no alive tag interval.");
    }

    return subEquipmentConfiguration;
  }
}
