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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cern.c2mon.shared.common.NoSimpleValueParseException;
import cern.c2mon.shared.common.ObjectFactory;
import cern.c2mon.shared.common.type.TypeConverter;
import cern.c2mon.shared.daq.command.SourceCommandTag;
import cern.c2mon.shared.daq.datatag.SourceDataTag;

/**
 * Class to create configuration objects from parts of the DOM tree. If you extend/change this class and still want to
 * use the generic way to create the change objects. Be sure to match with your creation methods the name convention
 * 'CREATE_METHOD_PREFIX + methodName(Element domElement)'.
 * 
 * @author alang
 */
public class ConfigurationObjectFactory extends ObjectFactory implements ConfigurationXMLConstants {

    /**
     * The prefix of all object creation methods.
     */
    private static final String CREATE_METHOD_PREFIX = "create";

    /**
     * Generates a list of changes for the elements in this configuration change event.
     * 
     * @param configurationChangeEventElement The configuration change event element.
     * @return The list of generated changes.
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws NoSuchMethodException May throw a NoSuchMethodException.
     * @throws InvocationTargetException May throw a InvocationTargetException.
     */
    public List<Change> generateChanges(final Element configurationChangeEventElement) throws NoSuchFieldException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        NodeList childNodes = configurationChangeEventElement.getChildNodes();
        List<Change> changes = new ArrayList<Change>(childNodes.getLength());
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element changeElement = (Element) node;
                String name = changeElement.getTagName();
                name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
                Method method = getClass().getMethod(CREATE_METHOD_PREFIX + name, Element.class);
                if (method != null) {
                    changes.add((Change) method.invoke(this, changeElement));
                }
            }
        }
        return changes;
    }

    /**
     * Creates a new configuration change event report and fills it with the values in the provided DOM element.
     * 
     * @param configurationChangeEventReportElement The element with the values to fill in.
     * @return The new and filled ConfigurationChangeEventReport object.
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws NoSimpleValueParseException This exception is thrown if the converted xml name matches a complex java
     *             field which cannot be converted.
     */
    public ConfigurationChangeEventReport createConfigurationChangeEventReport(
            final Element configurationChangeEventReportElement) throws NoSuchFieldException, IllegalAccessException,
            NoSimpleValueParseException {
        ConfigurationChangeEventReport configurationChangeEventReport = new ConfigurationChangeEventReport();
        List<Element> complexElements = fillConfigurationChangeEventReport(configurationChangeEventReportElement,
                configurationChangeEventReport);
        for (Element element : complexElements) {
            if (element.getTagName().equals(CHANGE_REPORT_ELEMENT)) {
                configurationChangeEventReport.appendChangeReport(createChangeReport(element));
            }
        }
        return configurationChangeEventReport;
    }

    /**
     * Fills a configuration change event report with the values in the provided DOM element.
     * 
     * @param configurationChangeEventReportElement The element with the values.
     * @param configurationChangeEventReport The report to fill.
     * @return Returns a list of elements which could not be processed automatically.
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws NoSimpleValueParseException This exception is thrown if the converted xml name matches a complex java
     *             field which cannot be converted.
     */
    public List<Element> fillConfigurationChangeEventReport(final Element configurationChangeEventReportElement,
            final ConfigurationChangeEventReport configurationChangeEventReport) throws NoSuchFieldException,
            IllegalAccessException, NoSimpleValueParseException {
        return initSimpleFields(configurationChangeEventReport, configurationChangeEventReportElement);
    }

    /**
     * Creates a new ChangeReport object.
     * 
     * @param changeReportElement The element to use to fill the new object.
     * @return The created and filled object.
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws NoSimpleValueParseException This exception is thrown if the converted xml name matches a complex java
     *             field which cannot be converted.
     */
    private ChangeReport createChangeReport(final Element changeReportElement) throws NoSuchFieldException,
            IllegalAccessException, NoSimpleValueParseException {
        ChangeReport changeReport = new ChangeReport(0);
        fillChangeReport(changeReportElement, changeReport);
        return changeReport;
    }

    /**
     * Fills a change report with the values from the provided element.
     * 
     * @param changeReportElement The change report element to use.
     * @param changeReport The object to fill.
     * @return Returns a List of Element which where not automatically processed.
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws NoSimpleValueParseException This exception is thrown if the converted xml name matches a complex java
     *             field which cannot be converted.
     */
    public List<Element> fillChangeReport(final Element changeReportElement, final ChangeReport changeReport)
            throws NoSuchFieldException, IllegalAccessException, NoSimpleValueParseException {
        return initSimpleFields(changeReport, changeReportElement);
    }

    /**
     * Creates a new data tag update event object.
     * 
     * @param element The part of the DOM tree to use.
     * @return The new data tag update event.
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws InstantiationException May throw a InstantiationException.
     * @throws ClassNotFoundException May throw a ClassNotFoundException.
     * @throws NoSuchMethodException May throw a NoSuchMethodException.
     * @throws InvocationTargetException May throw a InvocationTargetException.
     * @throws NoSimpleValueParseException This exception is thrown if the converted xml name matches a complex java
     *             field which cannot be converted.
     */
    public DataTagUpdate createDataTagUpdate(final Element element) throws NoSuchFieldException,
            IllegalAccessException, InstantiationException, ClassNotFoundException, InvocationTargetException,
            NoSuchMethodException, NoSimpleValueParseException {
        DataTagUpdate dataTagUpdate = new DataTagUpdate();
        List<Element> complexElements = fillDataTagUpdate(element, dataTagUpdate);
        for (Element child : complexElements) {
            if (child.getTagName().equals(DATA_TAG_ADDRESS_ELEMENT)) {
                dataTagUpdate.setDataTagAddressUpdate(createDataTagAddressUpdate(child));
            }
        }
        return dataTagUpdate;
    }

    /**
     * Fills the data tag update with values derived from the DOM element.
     * 
     * @param element The DOM element to use.
     * @param dataTagUpdate The data tag update to fill.
     * @return Returns all elements which could not be processed automatically.
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws InstantiationException May throw a InstantiationException.
     * @throws ClassNotFoundException May throw a ClassNotFoundException.
     * @throws NoSuchMethodException May throw a NoSuchMethodException.
     * @throws InvocationTargetException May throw a InvocationTargetException.
     * @throws NoSimpleValueParseException This exception is thrown if the converted xml name matches a complex java
     *             field which cannot be converted.
     */
    public List<Element> fillDataTagUpdate(final Element element, final DataTagUpdate dataTagUpdate)
            throws NoSuchFieldException, IllegalAccessException, InstantiationException, ClassNotFoundException,
            InvocationTargetException, NoSuchMethodException, NoSimpleValueParseException {
        List<Element> complexChildElements = initSimpleFields(dataTagUpdate, element);
        List<Element> notProcessedElements = new ArrayList<Element>();
        for (Element child : complexChildElements) {
            if (child.getTagName().equals(MIN_VALUE_ELEMENT) || child.getTagName().equals((MAX_VALUE_ELEMENT))) {
                evaluateBoundaryValue(dataTagUpdate, child.getAttribute(DATA_TYPE_ATTRIBUTE), child.getTextContent(),
                        child);
            } else if (child.hasAttribute(UPDATE_ATTRIBUTE)) {
                evaluateUpdateAttribute(dataTagUpdate, child);
            } else {
                notProcessedElements.add(child);
            }
        }
        return notProcessedElements;
    }

    /**
     * Creates a new command tag update event object.
     * 
     * @param element The part of the DOM tree to use.
     * @return The new command tag update event.
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws InstantiationException May throw a InstantiationException.
     * @throws ClassNotFoundException May throw a ClassNotFoundException.
     * @throws NoSuchMethodException May throw a NoSuchMethodException.
     * @throws InvocationTargetException May throw a InvocationTargetException.
     * @throws NoSimpleValueParseException This exception is thrown if the converted xml name matches a complex java
     *             field which cannot be converted.
     */
    public CommandTagUpdate createCommandTagUpdate(final Element element) throws NoSuchFieldException,
            IllegalAccessException, InstantiationException, ClassNotFoundException, InvocationTargetException,
            NoSuchMethodException, NoSimpleValueParseException {
        CommandTagUpdate commandTagUpdate = new CommandTagUpdate();
        List<Element> unprocessedElements = fillCommandTagUpdate(element, commandTagUpdate);
        for (Element childElement : unprocessedElements) {
            if (childElement.getTagName().equals(HARDWARE_ADDRESS_ELEMENT)) {
                commandTagUpdate.setHardwareAddressUpdate(createHardwareAddressUpdate(childElement));
            }
        }
        return commandTagUpdate;
    }

    /**
     * Fills the command tag update with values derived from the DOM element.
     * 
     * @param element The DOM element to use.
     * @param commandTagUpdate The command tag update to fill.
     * @return List of Elements which could not be processed automatically.
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws InstantiationException May throw a InstantiationException.
     * @throws ClassNotFoundException May throw a ClassNotFoundException.
     * @throws NoSuchMethodException May throw a NoSuchMethodException.
     * @throws InvocationTargetException May throw a InvocationTargetException.
     * @throws NoSimpleValueParseException This exception is thrown if the converted xml name matches a complex java
     *             field which cannot be converted.
     */
    public List<Element> fillCommandTagUpdate(final Element element, final CommandTagUpdate commandTagUpdate)
            throws NoSuchFieldException, IllegalAccessException, InstantiationException, ClassNotFoundException,
            InvocationTargetException, NoSuchMethodException, NoSimpleValueParseException {
        List<Element> complexChildElements = initSimpleFields(commandTagUpdate, element);
        List<Element> unprocessedElements = new ArrayList<Element>();
        for (Element child : complexChildElements) {
            if (child.hasAttribute(UPDATE_ATTRIBUTE)) {
                evaluateUpdateAttribute(commandTagUpdate, child);
            } else {
                unprocessedElements.add(child);
            }
        }
        return unprocessedElements;
    }

    /**
     * Creates a new Equipment unit update.
     * 
     * @param element The element to use to create the update.
     * @return The new EquipmentConfigurationUpdate.
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws NoSimpleValueParseException This exception is thrown if the converted xml name matches a complex java
     *             field which cannot be converted.
     */
    public EquipmentConfigurationUpdate createEquipmentConfigurationUpdate(final Element element)
            throws NoSuchFieldException, IllegalAccessException, NoSimpleValueParseException {
        EquipmentConfigurationUpdate equipmentConfigurationUpdate = new EquipmentConfigurationUpdate();
        fillEquipmentConfigurationUpdate(element, equipmentConfigurationUpdate);
        return equipmentConfigurationUpdate;
    }

    /**
     * Fills the provided Equipment unit update.
     * 
     * @param element The element to use to create the update.
     * @param equipmentConfigurationUpdate The EquipmentConfigurationUpdate to fill.
     * @return All elements which could not be processed automatically.
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws NoSimpleValueParseException This exception is thrown if the converted xml name matches a complex java
     *             field which cannot be converted.
     */
    public List<Element> fillEquipmentConfigurationUpdate(final Element element,
            final EquipmentConfigurationUpdate equipmentConfigurationUpdate) throws NoSuchFieldException,
            IllegalAccessException, NoSimpleValueParseException {
        List<Element> complexChildElements = initSimpleFields(equipmentConfigurationUpdate, element);
        return evaluateUpdateAttributes(complexChildElements, equipmentConfigurationUpdate);
    }

    /**
     * Creates a new process update.
     * 
     * @param element The element to use to create the update.
     * @return The new process update object.
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws NoSimpleValueParseException This exception is thrown if the converted xml name matches a complex java
     *             field which cannot be converted.
     */
    public ProcessConfigurationUpdate createProcessConfigurationUpdate(final Element element)
            throws NoSuchFieldException, IllegalAccessException, NoSimpleValueParseException {
        ProcessConfigurationUpdate processConfigurationUpdate = new ProcessConfigurationUpdate();
        fillProcessConfigurationUpdate(element, processConfigurationUpdate);
        return processConfigurationUpdate;
    }

    /**
     * Fills a process update.
     * 
     * @param element The element to use to create the update.
     * @param processConfigurationUpdate The process update object to fill.
     * @return All elements which could not be processed automatically.
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws NoSimpleValueParseException This exception is thrown if the converted xml name matches a complex java
     *             field which cannot be converted.
     */
    public List<Element> fillProcessConfigurationUpdate(final Element element,
            final ProcessConfigurationUpdate processConfigurationUpdate) throws NoSuchFieldException,
            IllegalAccessException, NoSimpleValueParseException {
        List<Element> complexChildElements = initSimpleFields(processConfigurationUpdate, element);
        return evaluateUpdateAttributes(complexChildElements, processConfigurationUpdate);
    }

    /**
     * Creates a data tag add event.
     * 
     * @param element The DOM element to use.
     * @return The new data tag add event
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws NoSimpleValueParseException This exception is thrown if the converted xml name matches a complex java
     *             field which cannot be converted.
     */
    public DataTagAdd createDataTagAdd(final Element element) throws NoSuchFieldException, IllegalAccessException,
            NoSimpleValueParseException {
        DataTagAdd dataTagAdd = new DataTagAdd();
        List<Element> complexChildElements = fillDataTagAdd(element, dataTagAdd);
        for (Element child : complexChildElements) {
            if (child.getTagName().equals(DATA_TAG_ELEMENT)) {
                SourceDataTag dataTag = SourceDataTag.fromConfigXML(child);
                dataTagAdd.setSourceDataTag(dataTag);
            }
        }
        return dataTagAdd;
    }

    /**
     * Fills a data tag add event.
     * 
     * @param element The DOM element to use.
     * @param dataTagAdd The data tag add event to fill.
     * @return Returns all elements which could not be processed automatically.
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws NoSimpleValueParseException This exception is thrown if the converted xml name matches a complex java
     *             field which cannot be converted.
     */
    public List<Element> fillDataTagAdd(final Element element, final DataTagAdd dataTagAdd)
            throws NoSuchFieldException, IllegalAccessException, NoSimpleValueParseException {
        return initSimpleFields(dataTagAdd, element);
    }

    /**
     * Fills a equipment unit add event.
     * 
     * @param element The DOM element to use.
     * @param eqUnitAdd The equipment unit add event to fill.
     * @return Returns all elements which could not be processed automatically.
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws NoSimpleValueParseException This exception is thrown if the converted xml name matches a complex java
     *             field which cannot be converted.
     */
    public List<Element> fillEquipmentUnitAdd(final Element element, final EquipmentUnitAdd eqUnitAdd)
            throws NoSuchFieldException, IllegalAccessException, NoSimpleValueParseException {
        return initSimpleFields(eqUnitAdd, element);
    }

    /**
     * Fills a equipment unit remove event.
     * 
     * @param element The DOM element to use.
     * @param eqUnitRem The equipment unit remove event to fill.
     * @return Returns all elements which could not be processed automatically.
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws NoSimpleValueParseException This exception is thrown if the converted xml name matches a complex java
     *             field which cannot be converted.
     */
    public List<Element> fillEquipmentUnitRemove(final Element element, final EquipmentUnitRemove eqUnitRem)
            throws NoSuchFieldException, IllegalAccessException, NoSimpleValueParseException {
        return initSimpleFields(eqUnitRem, element);
    }

    /**
     * Creates a command tag add event.
     * 
     * @param element The DOM element to use.
     * @return The new command tag add event
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws NoSimpleValueParseException This exception is thrown if the converted xml name matches a complex java
     *             field which cannot be converted.
     */
    public CommandTagAdd createCommandTagAdd(final Element element) throws NoSuchFieldException,
            IllegalAccessException, NoSimpleValueParseException {
        CommandTagAdd commandTagAdd = new CommandTagAdd();
        List<Element> complexChildElements = fillCommandTagAdd(element, commandTagAdd);
        for (Element child : complexChildElements) {
            if (child.getTagName().equals(COMMAND_TAG_ELEMENT)) {
                SourceCommandTag commandTag = SourceCommandTag.fromConfigXML(child);
                commandTagAdd.setSourceCommandTag(commandTag);
            }
        }
        return commandTagAdd;
    }

    /**
     * Fills a command tag add event.
     * 
     * @param element The DOM element to use.
     * @param commandTagAdd The command tag add event to fill.
     * @return Returns a List of Element which where not automatically processed.
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws NoSimpleValueParseException This exception is thrown if the converted xml name matches a complex java
     *             field which cannot be converted.
     */
    public List<Element> fillCommandTagAdd(final Element element, final CommandTagAdd commandTagAdd)
            throws NoSuchFieldException, IllegalAccessException, NoSimpleValueParseException {
        return initSimpleFields(commandTagAdd, element);
    }

    /**
     * Creates a data tag remove event.
     * 
     * @param element The DOM element with the data.
     * @return A new data tag remove event.
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws NoSimpleValueParseException This exception is thrown if the converted xml name matches a complex java
     *             field which cannot be converted.
     */
    public DataTagRemove createDataTagRemove(final Element element) throws NoSuchFieldException,
            IllegalAccessException, NoSimpleValueParseException {
        DataTagRemove dataTagRemove = new DataTagRemove();
        fillDataTagRemove(element, dataTagRemove);
        return dataTagRemove;
    }

    /**
     * Fills a data tag remove event.
     * 
     * @param element The DOM element with the data.
     * @param dataTagRemove The data tag remove event to fill.
     * @return A List of not processed Elements.s
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws NoSimpleValueParseException This exception is thrown if the converted xml name matches a complex java
     *             field which cannot be converted.
     */
    public List<Element> fillDataTagRemove(final Element element, final DataTagRemove dataTagRemove)
            throws NoSuchFieldException, IllegalAccessException, NoSimpleValueParseException {
        return initSimpleFields(dataTagRemove, element);
    }

    /**
     * Creates a command tag remove event.
     * 
     * @param element The DOM element with the data.
     * @return A new command tag remove event.
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws NoSimpleValueParseException This exception is thrown if the converted xml name matches a complex java
     *             field which cannot be converted.
     */
    public CommandTagRemove createCommandTagRemove(final Element element) throws NoSuchFieldException,
            IllegalAccessException, NoSimpleValueParseException {
        CommandTagRemove commandTagRemove = new CommandTagRemove();
        fillCommandTagRemove(element, commandTagRemove);
        return commandTagRemove;
    }

    /**
     * Fills a command tag remove event.
     * 
     * @param element The DOM element with the data.
     * @param commandTagRemove The command tag remove event to fill.
     * @return Returns a List of Element which where not automatically processed.
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws NoSimpleValueParseException This exception is thrown if the converted xml name matches a complex java
     *             field which cannot be converted.
     */
    public List<Element> fillCommandTagRemove(final Element element, final CommandTagRemove commandTagRemove)
            throws NoSuchFieldException, IllegalAccessException, NoSimpleValueParseException {
        return initSimpleFields(commandTagRemove, element);
    }

    /**
     * Creates a data tag address update.
     * 
     * @param element The DOM element with the data.
     * @return The new data tag address update.
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws InstantiationException May throw a InstantiationException.
     * @throws ClassNotFoundException May throw a ClassNotFoundException.
     * @throws NoSuchMethodException May throw a NoSuchMethodException.
     * @throws InvocationTargetException May throw a InvocationTargetException.
     * @throws NoSimpleValueParseException This exception is thrown if the converted xml name matches a complex java
     *             field which cannot be converted.
     */
    public DataTagAddressUpdate createDataTagAddressUpdate(final Element element) throws NoSuchFieldException,
            IllegalAccessException, InstantiationException, ClassNotFoundException, InvocationTargetException,
            NoSuchMethodException, NoSimpleValueParseException {
        DataTagAddressUpdate dataTagAddressUpdate = new DataTagAddressUpdate();
        List<Element> elements = fillDataTagAddressUpdate(element, dataTagAddressUpdate);
        for (Element complexChild : elements) {
            if (complexChild.getTagName().equals(HARDWARE_ADDRESS_ELEMENT)) {
                HardwareAddressUpdate hardwareAddressUpdate = createHardwareAddressUpdate(complexChild);
                dataTagAddressUpdate.setHardwareAddressUpdate(hardwareAddressUpdate);
            }
        }
        return dataTagAddressUpdate;
    }

    /**
     * Fills a data tag address update.
     * 
     * @param element The DOM element with the data.
     * @param dataTagAddressUpdate The data tag address update to fill.
     * @return List of not processed Elements.
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws InstantiationException May throw a InstantiationException.
     * @throws ClassNotFoundException May throw a ClassNotFoundException.
     * @throws NoSuchMethodException May throw a NoSuchMethodException.
     * @throws InvocationTargetException May throw a InvocationTargetException.
     * @throws NoSimpleValueParseException This exception is thrown if the converted xml name matches a complex java
     *             field which cannot be converted.
     */
    public List<Element> fillDataTagAddressUpdate(final Element element, final DataTagAddressUpdate dataTagAddressUpdate)
            throws NoSuchFieldException, IllegalAccessException, InstantiationException, ClassNotFoundException,
            InvocationTargetException, NoSuchMethodException, NoSimpleValueParseException {
        List<Element> complexChildElements = initSimpleFields(dataTagAddressUpdate, element);
        List<Element> notProcessedElements = new ArrayList<Element>();
        for (Element complexChild : complexChildElements) {
            if (complexChild.hasAttribute(UPDATE_ATTRIBUTE)) {
                evaluateUpdateAttribute(dataTagAddressUpdate, complexChild);
            } else {
                notProcessedElements.add(complexChild);
            }
        }
        return notProcessedElements;
    }

    /**
     * Creates a hardware address update.
     * 
     * @param element The DOM element with the data.
     * @return The new hardware address update.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws InstantiationException May throw a InstantiationException.
     * @throws ClassNotFoundException May throw a ClassNotFoundException.
     * @throws NoSuchMethodException May throw a NoSuchMethodException.
     * @throws InvocationTargetException May throw a InvocationTargetException.
     * @throws NoSimpleValueParseException This exception is thrown if the converted xml name matches a complex java
     *             field which cannot be converted.
     * @throws NoSuchFieldException Throws a no such field exception if a tag name in the DOM element cannot be found in
     *             the hardware address object. (After the name of the tag was converted by name conventions).
     */
    public HardwareAddressUpdate createHardwareAddressUpdate(final Element element) throws InstantiationException,
            IllegalAccessException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException,
            NoSimpleValueParseException, NoSuchFieldException {
        HardwareAddressUpdate hardwareAddressUpdate = new HardwareAddressUpdate();
        fillHardwareAddressUpdate(element, hardwareAddressUpdate);
        return hardwareAddressUpdate;
    }

    /**
     * Fills a hardware address update.
     * 
     * @param element The DOM element with the data.
     * @param hardwareAddressUpdate The hardware address update to fill.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws InstantiationException May throw a InstantiationException.
     * @throws ClassNotFoundException May throw a ClassNotFoundException.
     * @throws NoSuchMethodException May throw a NoSuchMethodException.
     * @throws InvocationTargetException May throw a InvocationTargetException.
     * @throws NoSimpleValueParseException This exception is thrown if the converted xml name matches a complex java
     *             field which cannot be converted.
     * @throws NoSuchFieldException Throws a no such field exception if a tag name in the DOM element cannot be found in
     *             the hardware address object. (After the name of the tag was converted by name conventions).
     */
    public void fillHardwareAddressUpdate(final Element element, final HardwareAddressUpdate hardwareAddressUpdate)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException, InvocationTargetException,
            NoSuchMethodException, NoSimpleValueParseException, NoSuchFieldException {
        String hwClass = element.getAttribute(HW_CLASS_ATTRIBUTE);
        Constructor<?> constructor = Class.forName(hwClass).getDeclaredConstructor();
        constructor.setAccessible(true);
        Object hardwareAddress = constructor.newInstance();
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Attr attribute = (Attr) attributes.item(i);
            String attributeName = attribute.getName();
            if (!attributeName.equals(HW_CLASS_ATTRIBUTE)) {
                String javaFieldName = xmlNameToJavaName(attributeName);
                hardwareAddressUpdate.getChangedValues().put(javaFieldName,
                        parse(attribute.getTextContent(), javaFieldName, hardwareAddress.getClass()));
            } else {
                hardwareAddressUpdate.setClazz(attribute.getTextContent());
            }
        }
        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) node;
                String elementName = childElement.getTagName();
                if (childElement.hasAttribute(UPDATE_ATTRIBUTE)
                        && childElement.getAttribute(UPDATE_ATTRIBUTE).equals(REMOVE_VALUE)) {
                    hardwareAddressUpdate.addFieldToRemove(xmlNameToJavaName(elementName));
                } else {
                    String javaFieldName = xmlNameToJavaName(elementName);
                    hardwareAddressUpdate.getChangedValues().put(javaFieldName,
                            parse(childElement.getTextContent(), javaFieldName, hardwareAddress.getClass()));
                }
            }
        }
    }

    /**
     * Evaluates boundary values (min-value and max-value) and adds them to the update object.
     * 
     * @param dataTagUpdate The data tag update to add the boundary value.
     * @param dataType The data type of the value.
     * @param value The value to set.
     * @param tag The element to identify min or max.
     */
    private void evaluateBoundaryValue(final DataTagUpdate dataTagUpdate, final String dataType, final String value,
            final Element tag) {
        if (tag.hasAttribute(UPDATE_ATTRIBUTE) && tag.getAttribute(UPDATE_ATTRIBUTE).equals(REMOVE_VALUE)) {
            dataTagUpdate.addFieldToRemove(xmlNameToJavaName(tag.getTagName()));
        } else {
            Object comparable = TypeConverter.cast(value, dataType);
            if (comparable != null && comparable instanceof Comparable<?>) {
                if (tag.getTagName().equals(MIN_VALUE_ELEMENT)) {
                    dataTagUpdate.setMinValue((Comparable<?>) comparable);
                } else {
                    dataTagUpdate.setMaxValue((Comparable<?>) comparable);
                }
            }
        }
    }

    /**
     * Evaluates all elements in the provided list concerning their update attribute.
     * 
     * @param complexChildElements The elements to evaluate.
     * @param changePart The change part to add the values.
     * @return All elements without an update attribute.
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws NoSimpleValueParseException This exception is thrown if the converted xml name matches a complex java
     *             field which cannot be converted.
     */
    private List<Element> evaluateUpdateAttributes(final List<Element> complexChildElements, final ChangePart changePart)
            throws NoSuchFieldException, IllegalAccessException, NoSimpleValueParseException {
        List<Element> notProcessedElements = new ArrayList<Element>();
        for (Element element : complexChildElements) {
            if (element.hasAttribute(UPDATE_ATTRIBUTE))
                evaluateUpdateAttribute(changePart, element);
            else
                notProcessedElements.add(element);
        }
        return notProcessedElements;
    }

    /**
     * Evaluates the elements update attribute and updates the change part. Only use this with elements which actually
     * have an update attribute.
     * 
     * @param child The element to evaluate.
     * @param changePart The change part to add the value.
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws NoSimpleValueParseException This exception is thrown if the converted xml name matches a complex java
     *             field which cannot be converted.
     */
    private void evaluateUpdateAttribute(final ChangePart changePart, final Element child) throws NoSuchFieldException,
            IllegalAccessException, NoSimpleValueParseException {
        String updateAttributeValue = child.getAttribute(UPDATE_ATTRIBUTE);
        String javaFieldName = xmlNameToJavaName(child.getTagName());
        if (updateAttributeValue.equals(REMOVE_VALUE)) {
            changePart.addFieldToRemove(javaFieldName);
        } else {
            setSimpleFieldForTag(changePart, javaFieldName, child.getTextContent());
        }
    }

    /**
     * Creates a equipment unit add event.
     * 
     * @param element The DOM element to use.
     * @return The new equipment add event
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws NoSimpleValueParseException This exception is thrown if the converted xml name matches a complex java
     *             field which cannot be converted.
     */
    public EquipmentUnitAdd createEquipmentUnitAdd(final Element element) throws NoSuchFieldException,
            IllegalAccessException, NoSimpleValueParseException {
        EquipmentUnitAdd eqUnitAdd = new EquipmentUnitAdd();

        List<Element> complexChildElements = fillEquipmentUnitAdd(element, eqUnitAdd);

        for (Element child : complexChildElements) {
            if (child.getTagName().equals(EQUIPMENT_UNIT_XML)) {
                eqUnitAdd.setEquipmentUnitXml(child.getFirstChild().getTextContent());
            }
        }
        return eqUnitAdd;
    }

    /**
     * Creates a equipment unit remove event.
     * 
     * @param element The DOM element to use.
     * @return The new equipment remove event
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws NoSimpleValueParseException This exception is thrown if the converted xml name matches a complex java
     *             field which cannot be converted.
     */
    public EquipmentUnitRemove createEquipmentUnitRemove(final Element element) throws NoSuchFieldException,
            IllegalAccessException, NoSimpleValueParseException {
        EquipmentUnitRemove eqUnitRem = new EquipmentUnitRemove();

        fillEquipmentUnitRemove(element, eqUnitRem);

        return eqUnitRem;
    }

}
