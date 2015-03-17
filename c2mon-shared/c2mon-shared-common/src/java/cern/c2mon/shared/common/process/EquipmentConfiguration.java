/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project. See http://ts-project-tim.web.cern.ch
 * Copyright (C) 2010 CERN This program is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details. You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA. Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.shared.common.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.core.Commit;

import cern.c2mon.shared.common.command.ISourceCommandTag;
import cern.c2mon.shared.common.command.SourceCommandTag;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataTag;

/**
 * This class is responsible for keeping equipment configuration parameters
 */
public class EquipmentConfiguration implements IEquipmentConfiguration, Cloneable {

  /**
   * Unique equipment identifier.
   *
   * TODO make this a required attribute!
   */
  @Attribute
  private Long id = 0L;

  /**
   * Equipment name.
   *
   * TODO make this a required attribute!
   */
  @Attribute
  private String name;

  /**
   * Flag if the dynamic timedeadband is enabled
   */
  @Element(required = false)
  private boolean isDynamicTimeDeadbandEnabled = true;

  /**
   * Fully qualified class name of the EquipmentMessageHandler subclass to be
   * used for communication with the monitoring equipment.
   */
  @Element(name = "handler-class-name")
  private String handlerClassName;

  /**
   * Identifier of the data tag that is used to signal a communication fault to
   * the Server.
   */
  @Element(name = "commfault-tag-id")
  private long commfaultTagId;

  /**
   * Value that the communication fault tag has to take if there is a
   * communication problem with the equipment
   */
  @Element(name = "commfault-tag-value")
  private boolean commfaultTagValue;

  /**
   * Identifier of the equipment alive-tag
   */
  @Element(name = "alive-tag-id", required = false)
  private long aliveTagId;

  /**
   * Interval (ms) beetwen 2 equipment alive-tags
   */
  @Element(name = "alive-interval", required = false)
  private long aliveInterval;

  /**
   * Address of the Monitoring equipment. This is the information the
   * EquipmentMessageHandler needs to talk to the piece of equipment in
   * question, e.g. the IP address and parameters for a PLC, a URL for a SCADA
   * ...
   */
  @Element(name = "address")
  private String equipmentAddress;

  /**
   * The map of SubEquipment configurations.
   */
  private Map<Long, SubEquipmentConfiguration> subEquipmentConfigurations = new HashMap<>();

  /**
   * The list of SubEquipment configurations that will be deserialised and
   * converted to the map later.
   */
  @ElementList(name = "SubEquipmentUnits")
  private List<SubEquipmentConfiguration> subEquipmentConfigurationList = new ArrayList<>();

  /**
   * The source data tags of this equipment configuration.
   */
  private Map<Long, SourceDataTag> sourceDataTags = new ConcurrentHashMap<Long, SourceDataTag>();

  /**
   * The list of SourceDataTags that will be deserialised and converted to the
   * map later.
   */
  @ElementList(name = "DataTags")
  private List<SourceDataTag> sourceDataTagList = new ArrayList<>();

  /**
   * The command tags of this equipment configuration.
   */
  private Map<Long, SourceCommandTag> sourceCommandTags = new ConcurrentHashMap<Long, SourceCommandTag>();

  /**
   * The list of SourceCommandTags that will be deserialised and converted to
   * the map later.
   */
  @ElementList(name = "CommandTags")
  private List<SourceCommandTag> sourceCommandTagList = new ArrayList<>();

  /**
   * Since it isn't possible to deserialise the process XML format directly to
   * the maps of subequipment, datatags and commands in this class, we
   * deserialise first to a list and then recreate the maps later.
   *
   * This function is called by SimpleXML after deserialisation in order to
   * recreate the maps of subequipment, datatags and commands from their
   * corresponding lists.
   */
  @Commit
  public void build() {
    for (SubEquipmentConfiguration configuration : subEquipmentConfigurationList) {
      subEquipmentConfigurations.put(configuration.getId(), configuration);
    }

    for (SourceDataTag tag : sourceDataTagList) {
      sourceDataTags.put(tag.getId(), tag);
    }

    for (SourceCommandTag tag : sourceCommandTagList) {
      sourceCommandTags.put(tag.getId(), tag);
    }
  }

  /**
   * The default constructor
   */
  public EquipmentConfiguration() {
  }

  /**
   * This method encodes the equipment configuration object into XML
   *
   * @return String Not yet implemented.
   */
  public String encode2XML() {
    return null;
  }

  /**
   * This method sets the Equipment identifier
   *
   * @param id Equipment id
   */
  public void setId(final long id) {
    this.id = id;
  }

  /**
   * This method gets the Equipment identifier
   *
   * @return long
   */
  @Override
  public long getId() {
    return id;
  }

  /**
   * This method sets the Equipment's CommFaultTag identifier
   *
   * @param id CommFaultTag id
   */
  public void setCommFaultTagId(final long id) {
    commfaultTagId = id;
  }

  /**
   * Sets the id of the alive tag.
   *
   * @param id The new id of the alive tag.
   */
  public void setAliveTagId(final long id) {
    aliveTagId = id;
  }

  /**
   * Returns the id of the alive tag.
   *
   * @return The id of the alive tag.
   */
  @Override
  public long getAliveTagId() {
    return aliveTagId;
  }

  /**
   * Sets the alive tag interval of this equipment.
   *
   * @param interval The new alive tag interval.
   */
  public void setAliveTagInterval(final long interval) {
    aliveInterval = interval;
  }

  /**
   * Returns the alive tag interval.
   *
   * @return The alive tag interval of this equipment.
   */
  @Override
  public long getAliveTagInterval() {
    return aliveInterval;
  }

  /**
   * This method sets the Equipment's CommFaultTag identifier
   *
   * @return The communication fault tag id.
   */
  public long getCommFaultTagId() {
    return commfaultTagId;
  }

  /**
   * This method sets the Equipment unit's name
   *
   * @param name Equipment Unit's name
   */
  public void setName(final String name) {
    this.name = name;
  }

  /**
   * This method gets the Equipment Unit's name.
   *
   * @return The name of this equipment unit.
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * This method sets the Equipment unit's supervising handler class name
   *
   * @param className the supervising handler class name
   */
  public void setHandlerClassName(final String className) {
    handlerClassName = className;
  }

  /**
   * This method gets the Equipment unit's name
   *
   * @return The class name of the handler implementation.
   */
  public String getHandlerClassName() {
    return handlerClassName;
  }

  /**
   * This method sets the Equipment unit's CommFaultTag value
   *
   * @param value new CommFaultTag value
   */
  public void setCommFaultTagValue(final boolean value) {
    commfaultTagValue = value;
  }

  /**
   * This method gets the Equipment unit's CommFaultTag value
   *
   * @return The value to send if there was an communication fault.
   */
  public Boolean getCommFaultTagValue() {
    return Boolean.valueOf(commfaultTagValue);
  }

  /**
   * This method sets the Equipment unit's address
   *
   * @deprecated this method is kept for backward compatibility reasons. For new
   *             development please use method
   *             {@link #setEquipmentAddress(String)}
   * @param addr equipment's address
   */
  @Deprecated
  public void setAddress(final String addr) {
    equipmentAddress = addr;
  }

  public void setEquipmentAddress(final String addr) {
    equipmentAddress = addr;
  }

  /**
   * This method sets the Equipment unit's address
   *
   * @deprecated this method is kept for backward compatibility reasons. For new
   *             development please use method {@link #getEquipmentAddress()}
   * @return The address of this equipment. (key value pairs separated with ';')
   */
  @Override
  @Deprecated
  public String getAddress() {
    return equipmentAddress;
  }

  public String getEquipmentAddress() {
    return equipmentAddress;
  }

  /**
   * Checks if this equipment configuration contains the source data tag with
   * the provided id.
   *
   * @param tagID The tag id to check.
   * @return True if the source data tag is part of this equipment unit else
   *         false.
   */
  @Override
  public boolean hasSourceDataTag(final Long tagID) {
    return sourceDataTags.get(tagID) != null;
  }

  /**
   * Checks if this equipment configuration contains the command tag with the
   * provided id.
   *
   * @param tagID The tag id to check.
   * @return True if the command tag is part of this equipment unit else false.
   */
  @Override
  public boolean hasSourceCommandTag(final Long tagID) {
    return sourceCommandTags.get(tagID) != null;
  }

  /**
   * Retrieve all SubEquipment configurations.
   *
   * @return the map of SubEquipment configurations
   */
  @Override
  public Map<Long, SubEquipmentConfiguration> getSubEquipmentConfigurations() {
    return this.subEquipmentConfigurations;
  }

  /**
   * Retrieve a single SubEquipment configuration.
   *
   * @param subEquipmentId the id of the SubEquipment configuration
   * @return the of SubEquipment configuration
   */
  @Override
  public SubEquipmentConfiguration getSubEquipmentConfiguration(Long subEquipmentId) {
    return this.subEquipmentConfigurations.get(subEquipmentId);
  }

  public void addSubEquipmentConfiguration(SubEquipmentConfiguration subEquipmentConfiguration) {
    this.subEquipmentConfigurations.put(subEquipmentConfiguration.getId(), subEquipmentConfiguration);
  }

  /**
   * Returns a copy of the map of data tags. Adding tags to this map will not
   * affect the rest of the application.
   *
   * @return SourceCommandTag map (dataTagId -> SourceCommandTag)
   */
  @Override
  public Map<Long, ISourceDataTag> getSourceDataTags() {
    return new HashMap<Long, ISourceDataTag>(sourceDataTags);
  }

  /**
   * Returns the live map of equipment unit data tags keys and values. All
   * changes will (add/remove...) be made to the real map. It is never null.
   *
   * @return The live map of sub equipment commFault keys and values.
   */
  public Map<Long, SourceDataTag> getDataTags() {
    return sourceDataTags;
  }

  /**
   * Returns a copy of the map of command tags. Adding tags to this map will not
   * affect the rest of the application.
   *
   * @return SourceCommandTag map (commandTagId -> SourceCommandTag)
   */
  @Override
  public Map<Long, ISourceCommandTag> getSourceCommandTags() {
    return new HashMap<Long, ISourceCommandTag>(sourceCommandTags);
  }

  /**
   * Returns the live map of equipment command tags keys and values. All changes
   * will (add/remove/clear...) be made to the real map. It is never null.
   *
   * @return The live map of sub equipment commFault keys and values.
   */
  public Map<Long, SourceCommandTag> getCommandTags() {
    return sourceCommandTags;
  }

  /**
   * Gets the SourceDataTag with the specified id.
   *
   * @param dataTagId The id of the data tag to look for.
   * @return The searched data tag or null if there is no data tag for the id.
   */
  @Override
  public ISourceDataTag getSourceDataTag(final Long dataTagId) {
    return getSourceDataTags().get(dataTagId);
  }

  /**
   * Gets the SourceCommandTag with the specified id.
   *
   * @param commandTagId The id of the command tag to look for.
   * @return The searched data tag or null if there is no command tag for the
   *         id.
   */
  @Override
  public ISourceCommandTag getSourceCommandTag(final Long commandTagId) {
    return getSourceCommandTags().get(commandTagId);
  }

  /**
   * Clones the equipment configuration. Be careful The contained maps will NOT
   * be cloned. So if you try to access for example the contained data tags the
   * references to them will still be the same.
   *
   * @return The clone of the equipment configuration.
   */
  @Override
  public EquipmentConfiguration clone() {
    EquipmentConfiguration equipmentConfiguration = null;
    try {
      equipmentConfiguration = (EquipmentConfiguration) super.clone();
    } catch (CloneNotSupportedException ex) {
      // This should not happen
      ex.printStackTrace();
    }
    return equipmentConfiguration;
  }

  /**
   * @param isDynamicTimeDeadbandEnabled the isDynamicTimeDeadbandEnabled to set
   */
  public void setDynamicTimeDeadbandEnabled(final boolean isDynamicTimeDeadbandEnabled) {
    this.isDynamicTimeDeadbandEnabled = isDynamicTimeDeadbandEnabled;
  }

  /**
   * @return the isDynamicTimeDeadbandEnabled
   */
  public boolean isDynamicTimeDeadbandEnabled() {
    return isDynamicTimeDeadbandEnabled;
  }
}
