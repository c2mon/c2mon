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
package cern.c2mon.shared.daq.config;

/**
 * Interface containing common constants to deal with configuration messages.
 *
 * @author alang
 *
 * @deprecated
 */
public interface ConfigurationXMLConstants {

  /**
   * The namespace of the configuration
   */
  String CONFIGURATION_NAMESPACE = "http://timweb.cern.ch/schemas/c2mon-daq/Configuration";
  /**
   * The configuration change event element.
   */
  String CONFIGURATION_CHANGE_EVENT_ELEMENT = "ConfigurationChangeEvent";
  /**
   * The ChangeReport element.
   */
  String CHANGE_REPORT_ELEMENT = "ChangeReport";
  /**
   * The data tag element.
   */
  String DATA_TAG_ELEMENT = "DataTag";
  /**
   * The data tag element.
   */
  String COMMAND_TAG_ELEMENT = "CommandTag";
  /**
   * The name of a data tag address element.
   */
  String DATA_TAG_ADDRESS_ELEMENT = "DataTagAddress";
  /**
   * The name of a hardware address element.
   */
  String HARDWARE_ADDRESS_ELEMENT = "HardwareAddress";
  /**
   * The name of a min-value element.
   */
  String MIN_VALUE_ELEMENT = "min-value";
  /**
   * The name of a max-value element.
   */
  String MAX_VALUE_ELEMENT = "max-value";
  /**
   * The name of the HardwareAdress class attribute.
   */
  String CLASS_ATTRIBUTE = "class";
  /**
   * The name of the update type attribute.
   */
  String UPDATE_ATTRIBUTE = "update-type";
  /**
   * The remove value of the update type attribute.
   */
  String REMOVE_VALUE = "remove";
  /**
   * The data type attribute.
   */
  String DATA_TYPE_ATTRIBUTE = "data-type";
  /**
   * The hardware class attribute.
   */
  String HW_CLASS_ATTRIBUTE = "class";
  /**
   * The alive tag id element.
   */
  String ALIVE_TAG_ID_ELEMENT = "alive-tag-id";
  /**
   * The alive tag id element.
   */
  String ALIVE_INTERVAL_ELEMENT = "alive-interval";
  /**
   * The max message size element.
   */
  String MAX_MESSAGE_SIZE_ELEMENT = "max-message-size";
  /**
   * The max message delay element.
   */
  String MAX_MESSAGE_DELAY_ELEMENT = "max-message-delay";
  /**
   * The EquipmentUnits Element. (Top level element for all equipment units)
   */
  String EQUIPMENT_UNITS_ELEMENT = "EquipmentUnits";
  /**
   * The EquipmentUnit Element.
   */
  String EQUIPMENT_UNIT_ELEMENT = "EquipmentUnit";
  /**
   * Process configuration type attribute.
   */
  String TYPE_ATTRIBUTE = "type";
  /**
   * Process configuration type attribute value rejected.
   */
  String TYPE_ATTRIBUTE_VALUE_REJECTED = "rejected";
  /**
   * Process configuration type attribute value unknown.
   */
  String TYPE_ATTRIBUTE_VALUE_UNKNOWN = "unknown";
  /**
   * The process id attribute.
   */
  String PROCESS_ID_ATTRIBUTE = "process-id";
  /**
   * The ID attribute.
   */
  String ID_ATTRIBUTE = "id";
  /**
   * The name attribute.
   */
  String NAME_ATTRIBUTE = "name";
  /**
   * The state attribute of a change.
   */
  String STATE_ATTRIBUTE = "state";
  /**
   * The handler class name element.
   */
  String HANDLER_CLASS_NAME_ELEMENT = "handler-class-name";
  /**
   * The commFault tag id element.
   */
  String COMMFAULT_TAG_ID_ELEMENT = "commfault-tag-id";
  /**
   * The commFault tag value element.
   */
  String COMMFAULT_TAG_VALUE_ELEMENT = "commfault-tag-value";
  /**
   * The Address element.
   */
  String ADDRESS_ELEMENT = "address";
  /**
   * The command tags element.
   */
  String COMMAND_TAGS_ELEMENT = "CommandTags";
  /**
   * The data tags element.
   */
  String DATA_TAGS_ELEMENT = "DataTags";
  /**
   * The sub equipment units element.
   */
  String SUB_EQUIPMENT_UNITS_ELEMENT = "SubEquipmentUnits";
  /**
   * The sub equipment unit element.
   */
  String SUB_EQUIPMENT_UNIT_ELEMENT = "SubEquipmentUnit";

  String EQUIPMENT_UNIT_XML = "equipment-unit-xml";

  String SUB_EQUIPMENT_UNIT_XML = "sub-equipment-unit-xml";
}
