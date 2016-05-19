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
package cern.c2mon.shared.client.configuration.api.process;

import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.tag.AliveTag;
import cern.c2mon.shared.client.configuration.api.tag.StatusTag;
import cern.c2mon.shared.client.configuration.api.util.ConfigurationObject;
import cern.c2mon.shared.client.configuration.api.util.DefaultValue;
import cern.c2mon.shared.client.configuration.api.util.IgnoreProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration object for a Process.
 * Holds the information to create a {@link cern.c2mon.shared.client.configuration.ConfigurationElement}
 * related to a Process.
 * <p/>
 * For further information how to use instances of this for server configurations read <a
 * href="http://c2mon.web.cern.ch/c2mon/docs/#_offline_configuration_via_c2mon_database_test_purpose_only">this</a> documentation.
 * <p/>
 *
 * @author Franz Ritter
 */
@Data
public class Process implements ConfigurationObject {

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
   * <p/>
   * Note: Consider that an update of the name is not provided on the server side.
   */
  private String name;

  /**
   * Interval in milliseconds at which the alive tag is expected to change.
   */
  @DefaultValue("10000")
  private Integer aliveInterval;

  /**
   * A description of the process.
   */
  @DefaultValue("")
  private String description;

  /**
   * Max number of updates in a single message from the DAQ process.
   */
  @DefaultValue("100")
  private Integer maxMessageSize;

  /**
   * Max delay between reception of update by a DAQ and sending it to the
   * server.
   */
  @DefaultValue("1000")
  private Integer maxMessageDelay;

  @IgnoreProperty
  private AliveTag aliveTag;

  @IgnoreProperty
  private StatusTag statusTag;

  @IgnoreProperty
  @Singular
  private List<Equipment> equipments = new ArrayList<>();

  /**
   * Constructor for building a Process with all fields.
   * To build a Process with arbitrary fields use the builder pattern.
   *
   * @param id              Unique id of the Process.
   * @param name            Unique name the Process.
   * @param description     Describes the propose of the Process.
   * @param deleted         Determine if this object apply as deletion.
   * @param aliveInterval   Defines the configuration of the alive interval of the AliveTag which is attached to this Process.
   * @param maxMessageSize  Defines the configuration of the maximum message size of this process.
   * @param maxMessageDelay Defines the configuration of the maximum message delay of this process.
   * @param equipments      List of Equipment configuration objects for this tag. If the argument is null the field will be an empty List as default.
   * @param statusTag       Mandatory configuration object for an StatusTag which is attached to this process. If the configuration is not a delete this
   *                        field have to be null.
   * @param aliveTag        Mandatory configuration object for an AliveTag which is attached to this process. If the configuration is not a delete this field
   *                        have to be null.
   */
  @Builder
  public Process(boolean deleted, Long id, String name, Integer aliveInterval, String description, Integer maxMessageSize,
                    Integer maxMessageDelay, @Singular List<Equipment> equipments, StatusTag statusTag, AliveTag aliveTag) {
    super();
    this.deleted = deleted;
    this.id = id;
    this.name = name;
    this.aliveInterval = aliveInterval;
    this.description = description;
    this.maxMessageSize = maxMessageSize;
    this.maxMessageDelay = maxMessageDelay;
    this.statusTag = statusTag;
    this.aliveTag = aliveTag;
    this.equipments = equipments == null || equipments.isEmpty() ? new ArrayList<Equipment>() : equipments;
  }

  public Process() {
  }

  /**
   * Adds an Equipment to the Process if the Process is already created.
   *
   * @param equipment The configuration object for an Equipment.
   */
  public void addEquipment(Equipment equipment) {
    this.equipments.add(equipment);
  }

  @Override
  public boolean requiredFieldsGiven() {
    return (id != null) && (name != null) ;
  }

  public static CreateBuilder create(String name) {

    Process iniProcess = Process.builder().name(name).build();

    return iniProcess.toCreateBuilder(iniProcess);
  }

  public static UpdateBuilder update(Long id) {

    Process iniProcess = Process.builder().id(id).build();

    return iniProcess.toUpdateBuilder(iniProcess);
  }

  public static UpdateBuilder update(String name) {

    Process iniProcess = Process.builder().name(name).build();

    return iniProcess.toUpdateBuilder(iniProcess);
  }

  private CreateBuilder toCreateBuilder(Process initializationProcess) {
    return new CreateBuilder(initializationProcess);
  }

  private UpdateBuilder toUpdateBuilder(Process initializationProcess) {
    return new UpdateBuilder(initializationProcess);
  }

  public static class CreateBuilder {

    private Process builderProcess;

    CreateBuilder(Process initializationProcess) {

      initializationProcess.setCreate(true);
      this.builderProcess = initializationProcess;
    }

    public Process.CreateBuilder id(Long id) {
      this.builderProcess.setId(id);
      return this;
    }

    public Process.CreateBuilder aliveTag(AliveTag aliveTag, Integer aliveInterval) {

      this.builderProcess.setAliveInterval(aliveInterval);
      this.builderProcess.setAliveTag(aliveTag);

      if(!aliveTag.isCreate()){
        builderProcess.setCreate(false);
      }

      return this;
    }

    public Process.CreateBuilder statusTag(StatusTag statusTag) {
      this.builderProcess.setStatusTag(statusTag);

      if(!statusTag.isCreate()){
        builderProcess.setCreate(false);
      }

      return this;
    }

    public Process.CreateBuilder description(String description) {
      this.builderProcess.setDescription(description);
      return this;
    }

    public Process.CreateBuilder maxMessageSize(Integer maxMessageSize) {
      this.builderProcess.setMaxMessageSize(maxMessageSize);
      return this;
    }

    public Process.CreateBuilder maxMessageDelay(Integer maxMessageDelay) {
      this.builderProcess.setMaxMessageDelay(maxMessageDelay);
      return this;
    }

    public Process build() {
      return this.builderProcess;
    }
  }

  public static class UpdateBuilder {

    private Process builderProcess;

    UpdateBuilder(Process initializationProcess) {
      this.builderProcess = initializationProcess;
    }

    public Process.UpdateBuilder aliveInterval(Integer aliveInterval) {
      this.builderProcess.setAliveInterval(aliveInterval);
      return this;
    }

    public Process.UpdateBuilder description(String description) {
      this.builderProcess.setDescription(description);
      return this;
    }

    public Process.UpdateBuilder maxMessageSize(Integer maxMessageSize) {
      this.builderProcess.setMaxMessageSize(maxMessageSize);
      return this;
    }

    public Process.UpdateBuilder maxMessageDelay(Integer maxMessageDelay) {
      this.builderProcess.setMaxMessageDelay(maxMessageDelay);
      return this;
    }

    public Process build() {
      builderProcess.setUpdate(true);
      return this.builderProcess;
    }
  }
}
