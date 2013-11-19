/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project. See http://ts-project-tim.web.cern.ch
 * Copyright (C) 2005 - 2010 CERN This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA. Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.shared.daq.config;

import java.util.List;
import java.util.Map.Entry;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cern.c2mon.shared.common.DOMFactory;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.daq.command.SourceCommandTag;
import cern.c2mon.shared.daq.datatag.SourceDataTag;

/**
 * Class to create DOM artifacts from configuration objects.
 * 
 * @author Andreas Lang
 */
public class ConfigurationDOMFactory extends DOMFactory implements ConfigurationXMLConstants,
        ConfigurationJavaConstants {

    /**
     * Creates a new configuration dom factory.
     */
    public ConfigurationDOMFactory() {
        super(CONFIGURATION_NAMESPACE);
    }

    /**
     * Creates a reconfiguration XML String.
     * 
     * @param changes The list of changes to be part of the XML.
     * @return The XML reconfiguration String.
     * @throws ParserConfigurationException May throw a ParserConfigurationException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws InstantiationException May throw a InstantiationException.
     * @throws TransformerException May throw a TransformerException.
     */
    public String createConfigurationXMLString(final List<Change> changes) throws ParserConfigurationException,
            IllegalAccessException, InstantiationException, TransformerException {
        Document document = createConfigurationDocument(changes);
        String xmlString = getDocumentString(document);
        return xmlString;
    }

    /**
     * Creates a ConfigurationChangeEventReport XML String.
     * 
     * @param configurationChangeEventReport The object with the values to use.
     * @return The created XML String.
     * @throws TransformerException May throw a TransformerException.
     * @throws ParserConfigurationException May throw a ParserConfigurationException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws InstantiationException May throw a InstantiationException.
     */
    public String createConfigurationChangeEventReportXMLString(
            final ConfigurationChangeEventReport configurationChangeEventReport) throws TransformerException,
            ParserConfigurationException, IllegalAccessException, InstantiationException {
        Document document = createConfigurationChangeEventReportDocument(configurationChangeEventReport);
        return getDocumentString(document);
    }

    /**
     * Creates a configuration change event report DOM document.
     * 
     * @param configurationChangeEventReport The configuration change event report to use.
     * @return The created DOM document.
     * @throws ParserConfigurationException May throw a ParserConfigurationException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws InstantiationException May throw a InstantiationException.
     */
    public Document createConfigurationChangeEventReportDocument(
            final ConfigurationChangeEventReport configurationChangeEventReport) throws ParserConfigurationException,
            IllegalAccessException, InstantiationException {
        Document document = createDocument();
        document.appendChild(createConfigurationChangeReportElement(document, configurationChangeEventReport));
        return document;
    }

    /**
     * Creates a configuration change event report element as part of the provided document. But does not add it to the
     * tree.
     * 
     * @param document The document to create the element.
     * @param configurationChangeEventReport The report class to use the values from.
     * @return The element with all the values.
     * @throws ParserConfigurationException May throw a ParserConfigurationException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws InstantiationException May throw a InstantiationException.
     */
    public Element createConfigurationChangeReportElement(final Document document,
            final ConfigurationChangeEventReport configurationChangeEventReport) throws ParserConfigurationException,
            IllegalAccessException, InstantiationException {
        Element configurationChangeReportElement = generateSimpleElement(document, configurationChangeEventReport);
        // Error field should only be set if all reports failed => only use the list if nothing present
        if (configurationChangeEventReport.getError() == null) {
            for (ChangeReport report : configurationChangeEventReport.getChangeReports()) {
                configurationChangeReportElement.appendChild(createChangeReportElement(document, report));
            }
        }
        return configurationChangeReportElement;
    }

    /**
     * Creates a report element for a single change. But doesn't add it to the document.
     * 
     * @param document The document to create the element.
     * @param report The report to use the values from.
     * @return The new change report element.
     * @throws ParserConfigurationException May throw a ParserConfigurationException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws InstantiationException May throw a InstantiationException.
     */
    private Element createChangeReportElement(final Document document, final ChangeReport report)
            throws ParserConfigurationException, IllegalAccessException, InstantiationException {
        Element reportElement = generateSimpleElement(document, report, CHANGE_ID_FIELD, STATE_FIELD);
        reportElement.setAttribute(STATE_ATTRIBUTE, report.getState().toString());
        return reportElement;
    }

    /**
     * Creates a reconfiguration DOM document.
     * 
     * @param changes The list of changes to be part of the document.
     * @return The DOM document with the changes.
     * @throws ParserConfigurationException May throw a ParserConfigurationException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws InstantiationException May throw a InstantiationException.
     */
    public Document createConfigurationDocument(final List<Change> changes) throws ParserConfigurationException,
            IllegalAccessException, InstantiationException {
        Document newDocument = createDocument();
        newDocument.appendChild(createConfigurationChangeEventElement(newDocument));
        for (Change change : changes) {
            Element element = null;
            if (change instanceof DataTagAdd) {
                element = createDataTagAddElement(newDocument, (DataTagAdd) change);
            } else if (change instanceof DataTagRemove) {
                element = createDataTagRemoveElement(newDocument, (DataTagRemove) change);
            } else if (change instanceof DataTagUpdate) {
                element = createDataTagUpdateElement(newDocument, (DataTagUpdate) change);
            } else if (change instanceof CommandTagAdd) {
                element = createCommandTagAddElement(newDocument, (CommandTagAdd) change);
            } else if (change instanceof CommandTagRemove) {
                element = createCommandTagRemoveElement(newDocument, (CommandTagRemove) change);
            } else if (change instanceof CommandTagUpdate) {
                element = createCommandTagUpdateElement(newDocument, (CommandTagUpdate) change);
            } else if (change instanceof EquipmentConfigurationUpdate) {
                element = createEquipmentUnitUpdateElement(newDocument, (EquipmentConfigurationUpdate) change);
            } else if (change instanceof ProcessConfigurationUpdate) {
                element = createProcessUpdateElement(newDocument, (ProcessConfigurationUpdate) change);
            } else if (change instanceof EquipmentUnitAdd) {
                element = createEquipmentUnitAddElement(newDocument, (EquipmentUnitAdd) change);
            } else if (change instanceof EquipmentUnitRemove) {
                element = createEquipmentUnitRemoveElement(newDocument, (EquipmentUnitRemove) change);
            }

            if (element != null)
                newDocument.getDocumentElement().appendChild(element);
        }
        return newDocument;
    }

    public Element createEquipmentUnitAddElement(final Document document, final EquipmentUnitAdd eqUnitAdd)
            throws ParserConfigurationException, IllegalAccessException, InstantiationException {

        Element eqUnitAddlement = generateSimpleElement(document, eqUnitAdd, CHANGE_ID_FIELD, EQUIPMENT_ID_FIELD);

        return eqUnitAddlement;
    }

    public Element createEquipmentUnitRemoveElement(final Document document, final EquipmentUnitRemove eqUnitRemove)
            throws ParserConfigurationException, IllegalAccessException, InstantiationException {

        Element eqUnitRemoveElement = generateSimpleElement(document, eqUnitRemove, CHANGE_ID_FIELD, EQUIPMENT_ID_FIELD);

        return eqUnitRemoveElement;
    }

    /**
     * Creates a configuration change event element. This is the root element for the reconfiguration message.
     * 
     * @param document The document context to use.
     * @return The configuration change event element.
     */
    public Element createConfigurationChangeEventElement(final Document document) {
        Element configurationChangeEventElement = document.createElementNS(getDefaultNamespace(),
                CONFIGURATION_CHANGE_EVENT_ELEMENT);
        configurationChangeEventElement.setAttribute(XMLConstants.XMLNS_ATTRIBUTE, getDefaultNamespace());
        configurationChangeEventElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        configurationChangeEventElement
                .setAttribute(
                        "xsi:schemaLocation",
                        "http://timweb.cern.ch/schemas/c2mon-daq/Configuration http://timweb.cern.ch/schemas/c2mon-daq/ConfigurationChangeEvent.xsd");
        return configurationChangeEventElement;
    }

    /**
     * Creates a data tag update element.
     * 
     * @param document The document in which context the element should be created.
     * @param dataTagUpdate The data tag update object with the data.
     * @return The data tag update element.
     * @throws ParserConfigurationException May throw a ParserConfigurationException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws InstantiationException May throw a InstantiationException.
     */
    public Element createDataTagUpdateElement(final Document document, final DataTagUpdate dataTagUpdate)
            throws ParserConfigurationException, IllegalAccessException, InstantiationException {
        Element dataTagUpdateElement = generateSimpleElement(document, dataTagUpdate, CHANGE_ID_FIELD,
                DATA_TAG_ID_FIELD, EQUIPMENT_ID_FIELD);
        List<String> removeAttributes = dataTagUpdate.getFieldsToRemove();
        appendRemoveAttributes(document, dataTagUpdateElement, removeAttributes);
        DataTagAddressUpdate dataTagAddressUpdate = dataTagUpdate.getDataTagAddressUpdate();
        if (dataTagAddressUpdate != null) {
            dataTagUpdateElement.appendChild(createDataTagAddressUpdateElement(document, dataTagAddressUpdate));
        }
        if (dataTagUpdate.getMinValue() != null || removeAttributes.contains(MIN_VALUE_FIELD)) {
            setBoundaryType(dataTagUpdateElement, MIN_VALUE_ELEMENT, dataTagUpdate.getDataType());
        }
        if (dataTagUpdate.getMaxValue() != null || removeAttributes.contains(MAX_VALUE_FIELD)) {
            setBoundaryType(dataTagUpdateElement, MAX_VALUE_ELEMENT, dataTagUpdate.getDataType());
        }
        return dataTagUpdateElement;
    }

    /**
     * Creates a command tag update element.
     * 
     * @param document The document in which context the element should be created.
     * @param commandTagUpdate The command tag update object with the data.
     * @return The command tag update element.
     * @throws ParserConfigurationException May throw a ParserConfigurationException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws InstantiationException May throw a InstantiationException.
     */
    public Element createCommandTagUpdateElement(final Document document, final CommandTagUpdate commandTagUpdate)
            throws ParserConfigurationException, IllegalAccessException, InstantiationException {
        Element commandTagUpdateElement = generateSimpleElement(document, commandTagUpdate, CHANGE_ID_FIELD,
                COMMAND_TAG_ID_FIELD, EQUIPMENT_ID_FIELD);
        appendRemoveAttributes(document, commandTagUpdateElement, commandTagUpdate.getFieldsToRemove());
        HardwareAddressUpdate hardwareAddressUpdate = commandTagUpdate.getHardwareAddressUpdate();
        if (hardwareAddressUpdate != null) {
            commandTagUpdateElement.appendChild(createHardwareAddressUpdateElement(document, hardwareAddressUpdate));
        }
        return commandTagUpdateElement;
    }

    /**
     * Creates a equipment unit update.
     * 
     * @param document The document in which context the element should be created.
     * @param equipmentConfigurationUpdate The data object to create the DOM from.
     * @return The equipment unit update element.
     * @throws ParserConfigurationException May throw a ParserConfigurationException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws InstantiationException May throw a InstantiationException.
     */
    public Element createEquipmentUnitUpdateElement(final Document document,
            final EquipmentConfigurationUpdate equipmentConfigurationUpdate) throws ParserConfigurationException,
            IllegalAccessException, InstantiationException {
        Element equipmentUnitUpdateElement = generateSimpleElement(document, equipmentConfigurationUpdate,
                CHANGE_ID_FIELD, EQUIPMENT_ID_FIELD);
        appendRemoveAttributes(document, equipmentUnitUpdateElement, equipmentConfigurationUpdate.getFieldsToRemove());
        // has no complex elements nothing more to do
        return equipmentUnitUpdateElement;
    }

    /**
     * Creates a process update.
     * 
     * @param document The document in which context the element should be created.
     * @param processConfigurationUpdate The data object to create the DOM from.
     * @return The process update element.
     * @throws ParserConfigurationException May throw a ParserConfigurationException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws InstantiationException May throw a InstantiationException.
     */
    public Element createProcessUpdateElement(final Document document,
            final ProcessConfigurationUpdate processConfigurationUpdate) throws ParserConfigurationException,
            IllegalAccessException, InstantiationException {
        Element processUpdateElement = generateSimpleElement(document, processConfigurationUpdate, CHANGE_ID_FIELD,
                PROCESS_ID_FIELD);
        appendRemoveAttributes(document, processUpdateElement, processConfigurationUpdate.getFieldsToRemove());
        // has no complex elements nothing more to do
        return processUpdateElement;
    }

    /**
     * Creates a data tag add element.
     * 
     * @param document The document in which context the element should be created.
     * @param dataTagAdd The data object to create the DOM from.
     * @return The data tag add element.
     * @throws ParserConfigurationException May throw a ParserConfigurationException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws InstantiationException May throw a InstantiationException.
     */
    public Element createDataTagAddElement(final Document document, final DataTagAdd dataTagAdd)
            throws ParserConfigurationException, IllegalAccessException, InstantiationException {
        Element dataTagAddElement = generateSimpleElement(document, dataTagAdd, CHANGE_ID_FIELD, EQUIPMENT_ID_FIELD);
        dataTagAddElement.appendChild(createSourceDataTagElement(document, dataTagAdd.getSourceDataTag()));
        return dataTagAddElement;
    }

    /**
     * Creates a source data tag DOM element.
     * 
     * @param document The document in which context the element should be created.
     * @param sourceDataTag The data object to create the DOM from.
     * @return The new source data tag element.
     * @throws ParserConfigurationException May throw a ParserConfigurationException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws InstantiationException May throw a InstantiationException.
     */
    public Element createSourceDataTagElement(final Document document, final SourceDataTag sourceDataTag)
            throws ParserConfigurationException, IllegalAccessException, InstantiationException {
        Element sourceDataTagElement = generateSimpleElement(document, DATA_TAG_ELEMENT, sourceDataTag, ID_FIELD,
                NAME_FIELD, CONTROL_FIELD);
        DataTagAddress address = sourceDataTag.getAddress();
        if (address != null) {
            sourceDataTagElement.appendChild(createDataTagAddressElement(document, address));
        }
        if (sourceDataTag.getMinValue() != null) {
            setBoundaryType(sourceDataTagElement, MIN_VALUE_ELEMENT, sourceDataTag.getDataType());
        }
        if (sourceDataTag.getMaxValue() != null) {
            setBoundaryType(sourceDataTagElement, MAX_VALUE_ELEMENT, sourceDataTag.getDataType());
        }
        return sourceDataTagElement;
    }

    /**
     * Sets the boundary data type of a min- or max-value element in the provided element.
     * 
     * @param element The element to set the data type.
     * @param minMax The name of the min or max value element.
     * @param dataType The data type to set.
     */
    private void setBoundaryType(final Element element, final String minMax, final String dataType) {
        NodeList elements = element.getChildNodes();
        for (int i = 0; i < elements.getLength(); i++) {
            Node curNode = elements.item(i);
            if (curNode.getNodeType() == Node.ELEMENT_NODE) {
                Element curElement = (Element) curNode;
                if (curElement.getTagName().equals(minMax)) {
                    curElement.setAttribute(DATA_TYPE_ATTRIBUTE, dataType);
                    break;
                }
            }
        }

    }

    /**
     * Creates a data tag address element.
     * 
     * @param document The document in which context the element should be created.
     * @param address The data object to use.
     * @return The data tag address element.
     * @throws ParserConfigurationException May throw a ParserConfigurationException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws InstantiationException May throw a InstantiationException.
     */
    public Element createDataTagAddressElement(final Document document, final DataTagAddress address)
            throws ParserConfigurationException, IllegalAccessException, InstantiationException {
        Element dataTagAddressElement = generateSimpleElement(document, address);
        HardwareAddress hardwareAddress = address.getHardwareAddress();
        if (hardwareAddress != null) {
            dataTagAddressElement.appendChild(createHardwareAddressElement(document, hardwareAddress));
        }
        return dataTagAddressElement;
    }

    /**
     * Creates a new hardware address element.
     * 
     * @param document The document in which context the element should be created.
     * @param hardwareAddress The data object to use.
     * @return The hardware address element.
     * @throws ParserConfigurationException May throw a ParserConfigurationException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws InstantiationException May throw a InstantiationException.
     */
    public Element createHardwareAddressElement(final Document document, final HardwareAddress hardwareAddress)
            throws ParserConfigurationException, IllegalAccessException, InstantiationException {
        Element hardwareAddressElement = generateSimpleElement(document, HARDWARE_ADDRESS_ELEMENT, hardwareAddress);
        hardwareAddressElement.setAttribute(CLASS_ATTRIBUTE, hardwareAddress.getClass().getName());
        return hardwareAddressElement;
    }

    /**
     * Creates a new command tag add element.
     * 
     * @param document The document in which context the element should be created.
     * @param commandTagAdd The data object to use.
     * @return The command tag add element.
     * @throws ParserConfigurationException May throw a ParserConfigurationException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws InstantiationException May throw a InstantiationException.
     */
    public Element createCommandTagAddElement(final Document document, final CommandTagAdd commandTagAdd)
            throws ParserConfigurationException, IllegalAccessException, InstantiationException {
        Element commandTagAddElement = generateSimpleElement(document, commandTagAdd, CHANGE_ID_FIELD,
                EQUIPMENT_ID_FIELD);
        commandTagAddElement.appendChild(createCommandTagElement(document, commandTagAdd.getSourceCommandTag()));
        return commandTagAddElement;
    }

    /**
     * Creates a command tag element.
     * 
     * @param document The document in which context the element should be created.
     * @param sourceCommandTag The data object to use.
     * @return The new command tag element.
     * @throws ParserConfigurationException May throw a ParserConfigurationException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws InstantiationException May throw a InstantiationException.
     */
    public Element createCommandTagElement(final Document document, final SourceCommandTag sourceCommandTag)
            throws ParserConfigurationException, IllegalAccessException, InstantiationException {
        Element commandTagElement = generateSimpleElement(document, COMMAND_TAG_ELEMENT, sourceCommandTag, ID_FIELD,
                NAME_FIELD, MODE_FIELD);
        HardwareAddress hardwareAddress = sourceCommandTag.getHardwareAddress();
        if (hardwareAddress != null) {
            commandTagElement.appendChild(createHardwareAddressElement(document, hardwareAddress));
        }
        return commandTagElement;
    }

    /**
     * Creates a data tag address update.
     * 
     * @param document The document in which context the element should be created.
     * @param dataTagAddressUpdate The data object to use.
     * @return The data tag address update element.
     * @throws ParserConfigurationException May throw a ParserConfigurationException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws InstantiationException May throw a InstantiationException.
     */
    public Element createDataTagAddressUpdateElement(final Document document,
            final DataTagAddressUpdate dataTagAddressUpdate) throws ParserConfigurationException,
            IllegalAccessException, InstantiationException {
        Element dataTagAddressUpdateElement = generateSimpleElement(document, DATA_TAG_ADDRESS_ELEMENT,
                dataTagAddressUpdate);
        appendRemoveAttributes(document, dataTagAddressUpdateElement, dataTagAddressUpdate.getFieldsToRemove());
        HardwareAddressUpdate hardwareAddressUpdate = dataTagAddressUpdate.getHardwareAddressUpdate();
        if (hardwareAddressUpdate != null) {
            dataTagAddressUpdateElement
                    .appendChild(createHardwareAddressUpdateElement(document, hardwareAddressUpdate));
        }
        return dataTagAddressUpdateElement;
    }

    /**
     * Creates a hardware address update.
     * 
     * @param document The document in which context the element should be created.
     * @param hardwareAddressUpdate The data object to use.
     * @return The hardware address update element.
     */
    public Element createHardwareAddressUpdateElement(final Document document,
            final HardwareAddressUpdate hardwareAddressUpdate) {
        Element hardwareAddressUpdateElement = document
                .createElementNS(getDefaultNamespace(), HARDWARE_ADDRESS_ELEMENT);
        appendRemoveAttributes(document, hardwareAddressUpdateElement, hardwareAddressUpdate.getFieldsToRemove());
        hardwareAddressUpdateElement.setAttribute(CLASS_ATTRIBUTE, hardwareAddressUpdate.getClazz());
        for (Entry<String, Object> entry : hardwareAddressUpdate.getChangedValues().entrySet()) {
            // if there's no value for that entry (entry is null) - don't append empty child tag
            if (entry.getValue() != null)
              hardwareAddressUpdateElement.appendChild(createElement(document, entry));
            
        }
        return hardwareAddressUpdateElement;
    }

    /**
     * Creates a data tag remove element.
     * 
     * @param document The document in which context the element should be created.
     * @param dataTagRemove The data object.
     * @return The data tag remove element.
     * @throws ParserConfigurationException May throw a ParserConfigurationException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws InstantiationException May throw a InstantiationException.
     */
    public Element createDataTagRemoveElement(final Document document, final DataTagRemove dataTagRemove)
            throws ParserConfigurationException, IllegalAccessException, InstantiationException {
        Element dataTagRemoveElement = generateSimpleElement(document, dataTagRemove, CHANGE_ID_FIELD,
                DATA_TAG_ID_FIELD, EQUIPMENT_ID_FIELD);
        return dataTagRemoveElement;
    }

    /**
     * Creates a command tag remove element.
     * 
     * @param document The document in which context the element should be created.
     * @param commandTagRemove The data object to use.
     * @return The command tag remove element.
     * @throws ParserConfigurationException May throw a ParserConfigurationException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws InstantiationException May throw a InstantiationException.
     */
    public Element createCommandTagRemoveElement(final Document document, final CommandTagRemove commandTagRemove)
            throws ParserConfigurationException, IllegalAccessException, InstantiationException {
        Element commandTagRemoveElement = generateSimpleElement(document, commandTagRemove, CHANGE_ID_FIELD,
                COMMAND_TAG_ID_FIELD, EQUIPMENT_ID_FIELD);
        return commandTagRemoveElement;
    }

    /**
     * Appends all fields which are removed to the list.
     * 
     * @param document The document in which context the element should be created.
     * @param elementToAppend The element where the remove elements should be appended.
     * @param fieldsToRemove The list of fields to remove.
     */
    private void appendRemoveAttributes(final Document document, final Element elementToAppend,
            final List<String> fieldsToRemove) {
        for (String field : fieldsToRemove) {
            elementToAppend.appendChild(createRemoveElement(document, field, 0));
        }
    }

    /**
     * Creates an element from a map entry
     * 
     * @param document The document in which context the element should be created.
     * @param entry The entry to use. Key as tag name, value as tag value.
     * @return The new element.
     */
    private Element createElement(final Document document, final Entry<String, Object> entry) {
        return createElement(document, entry.getKey(), entry.getValue());
    }

    /**
     * Creates a new element with the provided name (will be converted with javaNametoXMLName()) and value.
     * 
     * @param document The document in which context the element should be created.
     * @param tagName The tagname of the element.
     * @param value The value of the element (uses toString() method)
     * @return The new element.
     */
    private Element createElement(final Document document, final String tagName, final Object value) {
        Element element = document.createElementNS(getDefaultNamespace(), javaNameToXMLName(tagName));
        if (value != null) {
            element.setTextContent(value.toString());
        }
        return element;
    }

    /**
     * Creates a new remove element.
     * 
     * @param document The document in which context the element should be created.
     * @param tagName The tag name.
     * @param value The tag value.
     * @return The new element.
     */
    private Element createRemoveElement(final Document document, final String tagName, final Object value) {
        Element element = createElement(document, tagName, value);
        element.setAttribute(UPDATE_ATTRIBUTE, REMOVE_VALUE);
        return element;
    }

}
