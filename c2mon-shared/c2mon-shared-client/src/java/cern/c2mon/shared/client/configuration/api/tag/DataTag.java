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
package cern.c2mon.shared.client.configuration.api.tag;

import cern.c2mon.shared.client.configuration.api.alarm.Alarm;
import cern.c2mon.shared.client.configuration.api.util.DefaultValue;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.metadata.Metadata;
import lombok.*;

import java.util.List;

/**
 * Configuration object for a DataTag.
 * Holds the information to create a {@link cern.c2mon.shared.client.configuration.ConfigurationElement}
 * related to a DataTag.
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
public class DataTag extends Tag {

  /**
   * Minimum value for range checks. If the system receives a tag value that is
   * less than the authorized minimum value, it will flag the new tag value as
   * invalid.
   */
  private Number minValue = null;

  /**
   * Maximum value for range checks. If the system receives a tag value that is
   * less than the authorized minimum value, it will flag the new tag value as
   * invalid.
   */
  private Number maxValue = null;

  /**
   * DIP address for tags published on DIP
   */
  private String dipAddress;

  /**
   * JAPC address for tags published on JAPC
   */
  private String japcAddress;

  /**
   * Address configuration of the datatag (if any)
   */
  private DataTagAddress address;

  /**
   * Unit of the tag's value. This parameter is defined at configuration time
   * and doesn't change during run-time. It is mainly used for analogue values
   * that may represent e.g. a flow in "m3", a voltage in "kV" etc.
   */
  private String unit;

  /**
   * Expected data type for the tag's value
   */
  private String dataType;

  /**
   * Indicates whether this tag's value changes shall be logged to the
   * short-term log.
   */
  @DefaultValue("true")
  private Boolean isLogged = true;

  /**
   * Constructor for building a DataTag with all fields.
   * To build a DataTag with arbitrary fields use the builder pattern.
   *
   * @param id          Unique id of the tag.
   * @param name        Unique name the tag.
   * @param description Describes the propose of the tag.
   * @param mode        define the mode in which the tag is running.
   * @param alarms      List of configuration PObjects for this tag. If the argument is null the field will be an empty List as default.
   * @param metadata    Arbitrary metadata attached to this tag configuration.
   * @param isLogged    Defines if the tag which belongs to this configuration should be logged.
   * @param deleted     Determine if this object apply as deletion.
   * @param dataType    Determine the data type of the DataTag which belongs to this configuration.
   * @param unit        Unit of the tag's value.
   * @param minValue    Minimum value of the DataTag which belongs to this configuration.
   * @param maxValue    Maximum value of the DataTag which belongs to this configuration.
   * @param address     DataTagAddress which belongs to this tag configuration.
   * @param dipAddress  Defines the dipAddress of the DataTag which belongs to this configuration.
   * @param japcAddress Defines the japcAddress of the DataTag which belongs to this configuration.
   */
  @Builder
  public DataTag(boolean deleted, Long id, String name, String description, Class<?> dataType, TagMode mode, @Singular List<Alarm> alarms,
                 Boolean isLogged, String unit, Number minValue, Number maxValue, DataTagAddress address, String dipAddress, String japcAddress, Metadata metadata) {
    super(deleted, id, name, description, mode, alarms, metadata);
    this.dataType = dataType != null ? dataType.getName() : null;
    this.minValue = minValue;
    this.maxValue = maxValue;
    this.address = address;
    this.unit = unit;
    this.dipAddress = dipAddress;
    this.japcAddress = japcAddress;
    this.isLogged = isLogged;
  }

  public DataTag() {
  }

  @Override
  public boolean requiredFieldsGiven() {
    return super.requiredFieldsGiven() && (dataType != null);
  }

  public static CreateBuilder create(String name, Class dataType, DataTagAddress address) {

    DataTag iniTag = DataTag.builder().name(name).dataType(dataType).address(address).build();

    return iniTag.toCreateBuilder(iniTag);
  }

  public static UpdateBuilder update(Long id) {

    DataTag iniTag = DataTag.builder().id(id).build();

    return iniTag.toUpdateBuilder(iniTag);
  }

  public static UpdateBuilder update(String name) {

    DataTag iniTag = DataTag.builder().name(name).build();

    return iniTag.toUpdateBuilder(iniTag);
  }

  private DataTag.CreateBuilder toCreateBuilder(DataTag initializationTag) {
    return new CreateBuilder(initializationTag);
  }

  private UpdateBuilder toUpdateBuilder(DataTag initializationTag) {
    return new UpdateBuilder(initializationTag);
  }


  public static class CreateBuilder {

    private DataTag builderTag;

    CreateBuilder(DataTag initializationTag) {
      this.builderTag = initializationTag;
    }

    public DataTag.CreateBuilder id(Long id) {
      this.builderTag.setId(id);
      return this;
    }

    public DataTag.CreateBuilder description(String description) {
      this.builderTag.setDescription(description);
      return this;
    }

    public DataTag.CreateBuilder unit(String unit) {
      this.builderTag.setUnit(unit);
      return this;
    }

    public DataTag.CreateBuilder mode(TagMode mode) {
      this.builderTag.setMode(mode);
      return this;
    }

    public DataTag.CreateBuilder isLogged(Boolean isLogged) {
      this.builderTag.setIsLogged(isLogged);
      return this;
    }

    public DataTag.CreateBuilder minValue(Number minValue) {
      this.builderTag.setMinValue(minValue);
      return this;
    }

    public DataTag.CreateBuilder maxValue(Number maxValue) {
      this.builderTag.setMaxValue(maxValue);
      return this;
    }

    public DataTag.CreateBuilder metadata(Metadata metadata) {
      this.builderTag.setMetadata(metadata);
      return this;
    }

    public DataTag build() {

      builderTag.setCreate(true);
      return  this.builderTag;
    }

  }

  public static class UpdateBuilder {

    private DataTag builderTag;

    UpdateBuilder(DataTag initializationTag) {
      this.builderTag = initializationTag;
    }

    public DataTag.UpdateBuilder name(String name) {
      this.builderTag.setName(name);
      return this;
    }

    public DataTag.UpdateBuilder mode(TagMode mode) {
      this.builderTag.setMode(mode);
      return this;
    }

    public DataTag.UpdateBuilder isLogged(Boolean isLogged) {
      this.builderTag.setIsLogged(isLogged);
      return this;
    }

    public DataTag.UpdateBuilder minValue(Number minValue) {
      this.builderTag.setMinValue(minValue);
      return this;
    }

    public DataTag.UpdateBuilder maxValue(Number maxValue) {
      this.builderTag.setMaxValue(maxValue);
      return this;
    }

    public DataTag.UpdateBuilder address(DataTagAddress address) {
      this.builderTag.setAddress(address);
      return this;
    }

    public DataTag.UpdateBuilder metadata(Metadata metadata) {
      this.builderTag.setMetadata(metadata);
      return this;
    }

    public DataTag.UpdateBuilder description(String description) {
      this.builderTag.setDescription(description);
      return this;
    }

    public DataTag build() {

      builderTag.setUpdate(true);
      return this.builderTag;
    }
  }
}
