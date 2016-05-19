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
package cern.c2mon.shared.client.configuration.api.equipment;

import cern.c2mon.shared.client.configuration.api.tag.*;
import cern.c2mon.shared.client.configuration.api.util.ConfigurationObject;
import cern.c2mon.shared.client.configuration.api.util.DefaultValue;
import cern.c2mon.shared.client.configuration.api.util.IgnoreProperty;
import lombok.Data;
import lombok.Singular;

import java.util.ArrayList;
import java.util.List;


/**
 * Configuration object for a Equipment.
 * Holds the information to create a {@link cern.c2mon.shared.client.configuration.ConfigurationElement}
 * related to an Equipment.
 * <p/>
 * For further information how to use instances of this for server configurations read <a
 * href="http://c2mon.web.cern.ch/c2mon/docs/#_offline_configuration_via_c2mon_database_test_purpose_only">this</a> documentation.
 * <p/>
 *
 * @author Franz Ritter
 */
@Data
public abstract class AbstractEquipment implements ConfigurationObject {

  @IgnoreProperty
  private boolean update = false;

  @IgnoreProperty
  private boolean create = false;

  /**
   * Determine if the instance of this class defines a DELETE command
   */
  @IgnoreProperty
  private boolean deleted;

  /**
   * Unique identifier of the equipment.
   */
  @IgnoreProperty
  private Long id;

  /**
   * Unique name of the equipment.
   */
  private String name;

  /**
   * Interval in milliseconds at which the alive tag is expected to change.
   */
  @DefaultValue("60000")
  private Integer aliveInterval;

  /**
   * Free-text description of the equipment.
   */
  @DefaultValue("")
  private String description;

  /**
   * Fully qualified name of the EquipmentMessageHandler subclass to be used by the DAQ to connect to the equipment.
   * Make Sure that the name of the class matches with the full EquipmentMessageHandler class name.
   */
  private String handlerClass;

  /**
   * Address parameters used by the handler class to connect to the equipment.
   */
  private String address;

  /**
   * The StatusTag is one of three mandatory control tags which are needed to create an Equipment.
   * For further information of the tag read the documentation of {@link StatusTag}.
   */
  @IgnoreProperty
  private StatusTag statusTag;

  /**
   * The AliveTag is one of three mandatory control tags which are needed to create an Equipment.
   * For further information of the tag read the documentation of {@link AliveTag}.
   */
  @IgnoreProperty
  private AliveTag aliveTag;

  /**
   * The CommFaultTag is one of three mandatory control tags which are needed to create an Equipment.
   * For further information of the tag read the documentation of {@link CommFaultTag}.
   */
  @IgnoreProperty
  private CommFaultTag commFaultTag;

  /**
   * The list of DataTags which are attached to the Equipment.
   * Each DataTag holds a unique configuration information.
   */
  @IgnoreProperty
  @Singular
  private List<DataTag> dataTags = new ArrayList<>();

  /**
   * The list of CommandTags which are attached to the Equipment.
   * Each CommandTag holds a unique configuration information.
   */
  @IgnoreProperty
  @Singular
  private List<CommandTag> commandTags = new ArrayList<>();

  /**
   * Constructor for building a Equipment with all fields.
   * To build a Equipment with arbitrary fields use the builder pattern.
   *
   * @param id            Unique id of the Equipment.
   * @param name          Unique name the Equipment.
   * @param description   Describes the propose of the Equipment.
   * @param deleted       Determine if this object apply as deletion.
   * @param aliveInterval Defines the configuration of the alive interval of the AliveTag which is attached to this Process.
   * @param equipments    List of SubEquipment configuration objects for this tag. If the argument is null the field will be an empty List as default.
   * @param statusTag     Mandatory configuration object for an StatusTag which is attached to this process. If the configuration is not a 'delete' this
   *                      field has to be null.
   * @param aliveTag      Mandatory configuration object for an AliveTag which is attached to this process. If the configuration is not a 'delete' this field
   *                      has to be null.
   * @param handlerClass  Full path-class name of the handler class of the this equipemnt.
   * @param address       Address parameter used by the handler class to connect to the equipment.
   * @param commFaultTag  Mandatory configuration object for an CommFaultTag which is attached to this process. If the configuration is not a 'delete' this
   *                      field has to be null.
   * @param dataTags      Optional list of DataTag configurations which are attached to this Equipment. If the argument is null the field will be an empty
   *                      List as default.
   * @param commandTags   Optional list of CommandTag configurations which are attached to this Equipment. If the argument is null the field will be an empty
   *                      List as default.
   */
  public AbstractEquipment(boolean deleted, Long id, String name, Integer aliveInterval, String description,
                           String handlerClass, String address,StatusTag statusTag, CommFaultTag commFaultTag,
                           AliveTag aliveTag, @Singular List<DataTag> dataTags, @Singular List<CommandTag> commandTags) {
    this.deleted = deleted;
    this.id = id;
    this.name = name;
    this.aliveInterval = aliveInterval;
    this.description = description;
    this.handlerClass = handlerClass;
    this.address = address;

    // metadata of the equipment:
    this.statusTag = statusTag;
    this.commFaultTag = commFaultTag;
    this.aliveTag = aliveTag;

    // Because of lombok the default values of the collections are set here
    this.commandTags = commandTags == null ? new ArrayList<CommandTag>() : commandTags;
    this.dataTags = dataTags;
  }

  public AbstractEquipment() {
  }

  @Override
  public boolean requiredFieldsGiven() {
    return (id != null) && (name != null) && (handlerClass != null);
  }

}
