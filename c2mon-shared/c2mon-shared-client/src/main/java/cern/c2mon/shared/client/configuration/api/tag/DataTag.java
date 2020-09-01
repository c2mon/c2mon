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

import cern.c2mon.shared.client.configuration.api.util.DefaultValue;
import cern.c2mon.shared.client.configuration.api.util.IgnoreProperty;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.client.metadata.Metadata;
import lombok.*;
import org.springframework.util.Assert;

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

  private Long equipmentId;

  private Long subEquipmentId;

  @IgnoreProperty
  private String equipmentName;

  @IgnoreProperty
  private String subEquipmentName;

  /**
   * Minimum value for range checks. If the system receives a tag value that is
   * less than the authorized minimum value, it will flag the new tag value as
   * invalid.
   */
  private Number minValue;

  /**
   * Maximum value for range checks. If the system receives a tag value that is
   * less than the authorized minimum value, it will flag the new tag value as
   * invalid.
   */
  private Number maxValue;

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
   * history.
   */
  @DefaultValue("true")
  private Boolean isLogged;

  public DataTag() {
  }

  public static CreateBuilder create(String name, Class<?> dataType, DataTagAddress address) {
    Assert.hasText(name, "Data tag name is required!");
    Assert.notNull(dataType, "Data type is required!");
    Assert.notNull(address, "Data tag address is required!");

    return new CreateBuilder(name, dataType, address);
  }

  public static UpdateBuilder update(Long id) {
    return new UpdateBuilder(id);
  }

  public static UpdateBuilder update(String name) {
    return new UpdateBuilder(name);
  }

  public static class CreateBuilder {

    private DataTag tagToBuild = new DataTag();

    private CreateBuilder(String name, Class<?> dataType, DataTagAddress address) {
      tagToBuild.setName(name);
      tagToBuild.setDataType(dataType.getName());
      tagToBuild.setAddress(address);
    }

    public DataTag.CreateBuilder id(Long id) {
      this.tagToBuild.setId(id);
      return this;
    }

    public DataTag.CreateBuilder equipmentId(Long equipmentId) {
      this.tagToBuild.setEquipmentId(equipmentId);
      return this;
    }

    public DataTag.CreateBuilder subEquipmentId(Long subEquipmentId) {
      this.tagToBuild.setSubEquipmentId(subEquipmentId);
      return this;
    }

    public DataTag.CreateBuilder description(String description) {
      this.tagToBuild.setDescription(description);
      return this;
    }

    public DataTag.CreateBuilder unit(String unit) {
      this.tagToBuild.setUnit(unit);
      return this;
    }

    public DataTag.CreateBuilder mode(TagMode mode) {
      this.tagToBuild.setMode(mode);
      return this;
    }

    public DataTag.CreateBuilder isLogged(Boolean isLogged) {
      this.tagToBuild.setIsLogged(isLogged);
      return this;
    }

    public DataTag.CreateBuilder minValue(Number minValue) {
      this.tagToBuild.setMinValue(minValue);
      return this;
    }

    public DataTag.CreateBuilder maxValue(Number maxValue) {
      this.tagToBuild.setMaxValue(maxValue);
      return this;
    }

    public DataTag.CreateBuilder addMetadata(String key, Object value) {
      if (this.tagToBuild.getMetadata() == null) {
        Metadata metadata = new Metadata();
        this.tagToBuild.setMetadata(metadata);
      }
      this.tagToBuild.getMetadata().addMetadata(key, value);
      return this;
    }

    public DataTag build() {

      tagToBuild.setCreated(true);
      return this.tagToBuild;
    }

  }

  public static class UpdateBuilder {

    private DataTag tagToBuild = new DataTag();

    private UpdateBuilder(String name) {
      this.tagToBuild.setName(name);
    }

    private UpdateBuilder(long id) {
      this.tagToBuild.setId(id);
    }

    public DataTag.UpdateBuilder name(String name) {
      this.tagToBuild.setName(name);
      return this;
    }

    public DataTag.UpdateBuilder dataType(Class<?> dataType) {
      this.tagToBuild.setDataType(dataType.getName());
      return this;
    }

    public DataTag.UpdateBuilder unit(String unit) {
      this.tagToBuild.setUnit(unit);
      return this;
    }

    public DataTag.UpdateBuilder mode(TagMode mode) {
      this.tagToBuild.setMode(mode);
      return this;
    }

    public DataTag.UpdateBuilder isLogged(Boolean isLogged) {
      this.tagToBuild.setIsLogged(isLogged);
      return this;
    }

    public DataTag.UpdateBuilder minValue(Number minValue) {
      this.tagToBuild.setMinValue(minValue);
      return this;
    }

    public DataTag.UpdateBuilder maxValue(Number maxValue) {
      this.tagToBuild.setMaxValue(maxValue);
      return this;
    }

    public DataTag.UpdateBuilder address(DataTagAddress address) {
      this.tagToBuild.setAddress(address);
      return this;
    }

    public DataTag.UpdateBuilder updateMetadata(String key, Object value) {
      if (this.tagToBuild.getMetadata() == null) {
        Metadata metadata = new Metadata();
        this.tagToBuild.setMetadata(metadata);
      }
      this.tagToBuild.getMetadata().updateMetadata(key, value);
      return this;
    }

    public DataTag.UpdateBuilder removeMetadata(String key) {
      if (this.tagToBuild.getMetadata() == null) {
        tagToBuild.setMetadata(new Metadata());
      }
      this.tagToBuild.getMetadata().addToRemoveList(key);
      return this;
    }

    public DataTag.UpdateBuilder description(String description) {
      this.tagToBuild.setDescription(description);
      return this;
    }

    public DataTag build() {
      tagToBuild.setUpdated(true);
      return this.tagToBuild;
    }
  }
}
