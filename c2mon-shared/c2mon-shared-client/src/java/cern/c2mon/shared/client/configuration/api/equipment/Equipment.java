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
import cern.c2mon.shared.client.configuration.api.util.IgnoreProperty;
import lombok.*;

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
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Equipment extends AbstractEquipment {

  /**
   * The id of the overlying Process. This field should never set by the user directly.
   */
  @IgnoreProperty
  private Long parentProcessId;

  /**
   * The name of the overlying Process. This field should never set by the user directly.
   */
  @IgnoreProperty
  private String parentProcessName;

  /**
   * The list of SubEquipments which are attached to the Equipment.
   * Each SubEquipment holds a unique configuration information.
   */
  @IgnoreProperty
  @Singular
  private List<SubEquipment> subEquipments = new ArrayList<>();

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
  @Builder
  public Equipment(boolean deleted, Long id, String name, Integer aliveInterval, String description,
                   String handlerClass, String address, @Singular List<SubEquipment> subEquipments, StatusTag statusTag, CommFaultTag commFaultTag,
                   AliveTag aliveTag, @Singular List<DataTag> dataTags, @Singular List<CommandTag> commandTags) {
    super(deleted, id, name, aliveInterval, description, handlerClass, address,  statusTag, commFaultTag,aliveTag,  dataTags, commandTags);

    this.subEquipments = subEquipments == null ? new ArrayList<SubEquipment>() : subEquipments;
    this.commandTags = commandTags == null ? new ArrayList<CommandTag>() : commandTags;
  }

  public Equipment() {
  }

  public static CreateBuilder create(String name, String handlerClass) {

    Equipment iniEq = Equipment.builder().name(name).handlerClass(handlerClass).build();

    return iniEq.toCreateBuilder(iniEq);
  }

  public static UpdateBuilder update(String name) {

    Equipment iniEq = Equipment.builder().name(name).build();

    return iniEq.toUpdateBuilder(iniEq);
  }

  public static UpdateBuilder update(Long id) {

    Equipment iniEq = Equipment.builder().id(id).build();

    return iniEq.toUpdateBuilder(iniEq);
  }

  private CreateBuilder toCreateBuilder(Equipment initializationEquipment) {
    return new CreateBuilder(initializationEquipment);
  }

  private UpdateBuilder toUpdateBuilder(Equipment initializationEquipment) {
    return new UpdateBuilder(initializationEquipment);
  }

  public static class CreateBuilder {

    private Equipment buildEquipment;

    CreateBuilder(Equipment initializationEquipment) {

      initializationEquipment.setCreate(true);
      this.buildEquipment = initializationEquipment;
    }

    public Equipment.CreateBuilder id(Long id) {
      this.buildEquipment.setId(id);
      return this;
    }

    public Equipment.CreateBuilder description(String description) {
      this.buildEquipment.setDescription(description);
      return this;
    }

    public Equipment.CreateBuilder address(String address) {
      this.buildEquipment.setAddress(address);
      return this;
    }

    public Equipment.CreateBuilder aliveTag(AliveTag aliveTag, Integer aliveInterval) {

      this.buildEquipment.setAliveInterval(aliveInterval);
      this.buildEquipment.setAliveTag(aliveTag);

      if (!aliveTag.isCreate()) {
        buildEquipment.setCreate(false);
      }

      return this;
    }

    public Equipment.CreateBuilder statusTag(StatusTag statusTag) {
      this.buildEquipment.setStatusTag(statusTag);

      if (!statusTag.isCreate()) {
        buildEquipment.setCreate(false);
      }

      return this;
    }

    public Equipment.CreateBuilder commFaultTag(CommFaultTag commFaultTag) {
      this.buildEquipment.setCommFaultTag(commFaultTag);

      if (!commFaultTag.isCreate()) {
        buildEquipment.setCreate(false);
      }

      return this;
    }

    public Equipment build() {
      return this.buildEquipment;
    }
  }

  public static class UpdateBuilder {
    private Equipment buildEquipment;

    UpdateBuilder(Equipment initializationEquipment) {
      buildEquipment = initializationEquipment;
    }

    public Equipment.UpdateBuilder aliveInterval(Integer aliveInterval) {
      this.buildEquipment.setAliveInterval(aliveInterval);
      return this;
    }

    public Equipment.UpdateBuilder description(String description) {
      this.buildEquipment.setDescription(description);
      return this;
    }

    public Equipment.UpdateBuilder handlerClass(String handlerClass) {
      this.buildEquipment.setHandlerClass(handlerClass);
      return this;
    }

    public Equipment.UpdateBuilder address(String address) {
      this.buildEquipment.setAddress(address);
      return this;
    }

    public Equipment build() {
      buildEquipment.setUpdate(true);

      return this.buildEquipment;
    }
  }
}
