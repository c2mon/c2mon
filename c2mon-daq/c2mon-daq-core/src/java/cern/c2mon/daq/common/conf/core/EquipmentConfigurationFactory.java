/*
 * $Id $
 * 
 * $Date$ $Revision$ $Author$
 * 
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.daq.common.conf.core;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import cern.c2mon.daq.common.ValueChangeMonitorEngine;
import cern.c2mon.daq.common.conf.equipment.IEquipmentConfiguration;
import cern.tim.shared.common.datatag.DataTagAddress;
import cern.tim.shared.common.datatag.ValueChangeMonitor;
import cern.tim.shared.daq.command.SourceCommandTag;
import cern.tim.shared.daq.config.ConfigurationXMLConstants;
import cern.tim.shared.daq.datatag.SourceDataTag;
import cern.tim.util.parser.SimpleXMLParser;


public class EquipmentConfigurationFactory extends XMLTagValueExtractor implements ConfigurationXMLConstants {

    private static EquipmentConfigurationFactory theInstance;
    
    
    SimpleXMLParser parser;

    private EquipmentConfigurationFactory() {
        
    }
    
    public static final EquipmentConfigurationFactory getInstance() {
        if (theInstance == null)
            theInstance = new EquipmentConfigurationFactory();

        return theInstance;
    }

    @Autowired
    public void setParser(SimpleXMLParser parser) {
        this.parser = parser;
    }
    
    /**
     * The logger.
     */
    private static final Logger LOGGER = Logger.getLogger(EquipmentConfigurationFactory.class);

    
    /**
     * Creates the equipment configuration from the matching subelement in the DOM tree.
     * 
     * @param equipmentUnit A EquipmentUnit element from the DOM tree.
     * @return An equipment configuration object.
     */
    public EquipmentConfiguration createEquipmentConfiguration(final String equipmentUnitXml) throws Exception {       
        return this.createEquipmentConfiguration(parser.parse(equipmentUnitXml).getDocumentElement());
    }
    
    /**
     * Creates the equipment configuration from the matching subelement in the DOM tree.
     * 
     * @param equipmentUnit A EquipmentUnit element from the DOM tree.
     * @return An equipment configuration object.
     */
    public EquipmentConfiguration createEquipmentConfiguration(final Element equipmentUnit) throws Exception {
        String eqID = equipmentUnit.getAttribute(ID_ATTRIBUTE);
        LOGGER.debug("EQ ID : " + eqID);
        String eqName = equipmentUnit.getAttribute(NAME_ATTRIBUTE);

        EquipmentConfiguration equipmentConfiguration = new EquipmentConfiguration();

        try {
            equipmentConfiguration.setId(Long.parseLong(eqID));
            equipmentConfiguration.setName(eqName);

            equipmentConfiguration.setHandlerClassName(getTagValue(equipmentUnit, HANDLER_CLASS_NAME_ELEMENT).trim());

            equipmentConfiguration.setCommFaultTagId(Long
                    .parseLong(getTagValue(equipmentUnit, COMMFAULT_TAG_ID_ELEMENT)));

            equipmentConfiguration.setCommFaultTagValue(Boolean.parseBoolean(getTagValue(equipmentUnit,
                    COMMFAULT_TAG_VALUE_ELEMENT)));

            // try and be prepared to catch the exception, because the field is
            // not obligatory and may not exist
            try {
                equipmentConfiguration.setAliveTagId(Long.parseLong(getTagValue(equipmentUnit, ALIVE_TAG_ID_ELEMENT)));
            } catch (NullPointerException ex) {
                LOGGER.debug("Process has no alive Tag id.");
            }
            // try and be prepared to catch the exception, because the field is
            // not obligatory and may not exist
            try {
                equipmentConfiguration.setAliveTagInterval(Long.parseLong(getTagValue(equipmentUnit,
                        ALIVE_INTERVAL_ELEMENT)));
            } catch (NullPointerException ex) {
                LOGGER.debug("Process has no alive Tag interval.");
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
     * Processes all subequipment units of the provided equipment unit and adds their commFault IDs and values to the
     * configuration object.
     * 
     * @param equipmentUnit The equipment unit element to go through.
     * @param equipmentConfiguration The configuration object to fill.
     */
    private void processSubEquipmentUnits(final Element equipmentUnit,
            final IEquipmentConfiguration equipmentConfiguration) {
        NodeList subEquipmentUnitsList = equipmentUnit.getElementsByTagName(SUB_EQUIPMENT_UNITS_ELEMENT);
        Element subEquipmentUnitsElement = null;
        if (subEquipmentUnitsList != null && subEquipmentUnitsList.getLength() > 0) {
            subEquipmentUnitsElement = (Element) subEquipmentUnitsList.item(0);
        }
        if (subEquipmentUnitsElement != null) {
            NodeList subEquipmentUnitsNode = subEquipmentUnitsElement.getElementsByTagName(SUB_EQUIPMENT_UNIT_ELEMENT);
            LOGGER.debug("\t" + subEquipmentUnitsNode.getLength() + " SubEquipments found for current equipment");
            for (int i = 0; i < subEquipmentUnitsNode.getLength(); i++) {
                LOGGER.debug("getting the subequipment's commFaultTag...");
                Element subEquipmentConf = (Element) subEquipmentUnitsNode.item(i);
                String commFaultId = getTagValue(subEquipmentConf, COMMFAULT_TAG_ID_ELEMENT);
                String commFaultValue = getTagValue(subEquipmentConf, COMMFAULT_TAG_VALUE_ELEMENT);
                equipmentConfiguration.getSubEqCommFaultValues().put(Long.parseLong(commFaultId),
                        Boolean.parseBoolean(commFaultValue));
            }
        }
    }

    /**
     * Processes all data tags of this equipment configuration DOM element and adds them to the equipment configuration
     * object.
     * 
     * @param equipmentUnit The DOM element with the data.
     * @param equipmentConfiguration The equipment configuration object.
     */
    private void processDataTags(final Element equipmentUnit, final EquipmentConfiguration equipmentConfiguration) {
        Element dataTagsBlock = (Element) equipmentUnit.getElementsByTagName(DATA_TAGS_ELEMENT).item(0);
        NodeList dataTags = dataTagsBlock.getElementsByTagName(DATA_TAG_ELEMENT);
        LOGGER.debug("\t" + dataTags.getLength() + " DataTags found for current equipment");
        SourceDataTag sourceDataTag = null;
        // for each SourceDataTag defined in the DataTags XML block
        for (int i = 0; i < dataTags.getLength(); i++) {
            sourceDataTag = SourceDataTag.fromConfigXML((Element) dataTags.item(i));
            LOGGER.debug("\tCreating SourceDataTag object for id " + sourceDataTag.getId() + "..");
            if (sourceDataTag.getAddress().getTimeDeadband() > 0) {
                sourceDataTag.getAddress().setStaticTimedeadband(true);
            }
            if (sourceDataTag.getId().longValue() == equipmentConfiguration.getAliveTagId()) {
                if (sourceDataTag.getAddress().getPriority() != DataTagAddress.PRIORITY_HIGH) {
                    LOGGER.warn("\tPriority on equipment alive tag " + sourceDataTag.getId()
                            + " is wrongly configured! Adjusting priority to HIGH (7)");
                    sourceDataTag.getAddress().setPriority(DataTagAddress.PRIORITY_HIGH);
                }
                if (!sourceDataTag.isControlTag()) {
                    LOGGER.warn("\tEquipment alive tag " + sourceDataTag.getId()
                            + " is not configured as control tag! Please correct this in the configuration.");
                }
            }
            equipmentConfiguration.getDataTags().put(sourceDataTag.getId(), sourceDataTag);                       
            
            // register tag in the ValueChangeMonitorEngine if needed
            if (sourceDataTag.hasValueCheckMonitor()) {
                ValueChangeMonitor vcm = sourceDataTag.getValueCheckMonitor();
                ValueChangeMonitorEngine.getInstance().register(equipmentConfiguration.getId(),sourceDataTag, vcm);
            }
                        
        } // for
    }

    /**
     * Processes all command tags in this equipment configuration DOM element and adds them to the configuration object.
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
            LOGGER.debug("creating SourceCommandTag object for id " + sourceCommandTag.getId() + "..");
            equipmentConfiguration.getCommandTags().put(sourceCommandTag.getId(), sourceCommandTag);
        }
    }
}
