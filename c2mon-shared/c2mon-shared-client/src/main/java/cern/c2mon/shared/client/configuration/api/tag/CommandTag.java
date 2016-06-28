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

import cern.c2mon.shared.client.configuration.api.util.IgnoreProperty;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.common.metadata.Metadata;
import lombok.*;
import org.springframework.util.Assert;

/**
 * Configuration object for a CommandTag.
 * Holds the information to create a {@link cern.c2mon.shared.client.configuration.ConfigurationElement}
 * related to a CommandTag.
 * <p/>
 * For further information how to use instances of this for server configurations read <a
 * href="http://c2mon.web.cern.ch/c2mon/docs/#_offline_configuration_via_c2mon_database_test_purpose_only">this</a> documentation.
 * <p/>
 * The class uses the lombok builder annotation.
 * Therefore to create instances of this class you need to use the builder pattern which is provided by lombok.
 *
 * @author Franz Ritter
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CommandTag extends Tag {

  private Long equipmentId;

  @IgnoreProperty
  private String equipmentName;

  /**
   * Client timeout of the CommandTag
   */
  private Integer clientTimeout;

  /**
   * Execution timeout of the CommandTag
   */
  private Integer execTimeout;

  /**
   * Source timeout of the CommandTag
   */
  private Integer sourceTimeout;

  /**
   * Number of times a data source should retry to execute a command in case an attempted execution fails.
   */
  private Integer sourceRetries;

  // TODO remove CERN specific rbac fields;
  /**
   * RBAC class.
   */
  private String rbacClass;

  /**
   * RBAC device.
   */
  private String rbacDevice;

  /**
   * RBAC property.
   */
  private String rbacProperty;

  /**
   * Expected data type for the tag's value
   */
  private String dataType;

  /**
   * Minimum value for the command value.
   */
  private Number minValue;

  /**
   * Maximum value for the command value.
   */
  private Number maxValue;

  /**
   * Hardware address of the CommandTag. The Hardware address is required by the data source to actually execute the
   * command.
   */
  private HardwareAddress hardwareAddress;

  /**
   * Empty default constructor
   */
  public CommandTag() {
  }

  public static CreateBuilder create(String name, Class<?> dataType, HardwareAddress hardwareAddress, Integer clientTimeout, Integer execTimeout, Integer sourceTimeout, Integer sourceRetries,
                                     String rbacClass, String rbacDevice, String rbacProperty) {
    Assert.hasText(name, "Command tag name is required!");
    Assert.notNull(hardwareAddress, "Hardware address is required!");
    Assert.notNull(dataType, "Command tag data type is required!");
    Assert.notNull(clientTimeout, "Client timeout is required!");
    Assert.notNull(execTimeout, "Execution timeout is required!");
    Assert.notNull(sourceTimeout, "Source timeout is required!");
    Assert.notNull(sourceRetries, "Source retries is required!");
    Assert.hasText(rbacClass, "RBAC class is required!");
    Assert.hasText(rbacDevice, "RBAC device is required!");
    Assert.hasText(rbacProperty, "RBAC property is required!");

    return new CreateBuilder(name, dataType, hardwareAddress, clientTimeout, execTimeout, sourceTimeout, sourceRetries, rbacClass, rbacDevice, rbacProperty);
  }

  public static UpdateBuilder update(Long id) {
    return new UpdateBuilder(id);
  }

  public static UpdateBuilder update(String name) {
    return new UpdateBuilder(name);
  }


  public static class CreateBuilder {
    private CommandTag tagToBuild = new CommandTag();

    private CreateBuilder(String name, Class<?> dataType, HardwareAddress hardwareAddress, Integer clientTimeout, Integer execTimeout, Integer sourceTimeout, Integer sourceRetries,
                          String rbacClass, String rbacDevice, String rbacProperty) {
      this.tagToBuild.setName(name);
      this.tagToBuild.setDataType(dataType.getName());
      this.tagToBuild.setHardwareAddress(hardwareAddress);
      this.tagToBuild.setClientTimeout(clientTimeout);
      this.tagToBuild.setExecTimeout(execTimeout);
      this.tagToBuild.setSourceRetries(sourceRetries);
      this.tagToBuild.setSourceTimeout(sourceTimeout);
      this.tagToBuild.setRbacClass(rbacClass);
      this.tagToBuild.setRbacDevice(rbacDevice);
      this.tagToBuild.setRbacProperty(rbacProperty);

      this.tagToBuild.setCreated(true);
    }

    public CommandTag.CreateBuilder id(Long id) {
      this.tagToBuild.setId(id);
      return this;
    }

    public CommandTag.CreateBuilder equipmentId(Long equipmentId) {
      this.tagToBuild.setEquipmentId(equipmentId);
      return this;
    }

    public CommandTag.CreateBuilder description(String description) {
      this.tagToBuild.setDescription(description);
      return this;
    }

    public CommandTag.CreateBuilder mode(TagMode mode) {
      this.tagToBuild.setMode(mode);
      return this;
    }

    public CommandTag.CreateBuilder minimum(Number minimum) {
      this.tagToBuild.setMinValue(minimum);
      return this;
    }

    public CommandTag.CreateBuilder maximum(Number maximum) {
      this.tagToBuild.setMaxValue(maximum);
      return this;
    }

    public CommandTag.CreateBuilder metadata(Metadata metadata) {
      this.tagToBuild.setMetadata(metadata);
      return this;
    }

    public CommandTag build() {
      tagToBuild.setCreated(true);
      return this.tagToBuild;
    }

  }

  public static class UpdateBuilder {
    private CommandTag tagToBuild = new CommandTag();

    private UpdateBuilder(Long id) {
      tagToBuild.setId(id);
    }

    private UpdateBuilder(String name) {
      tagToBuild.setName(name);
    }

    public CommandTag.UpdateBuilder name(String name) {
      this.tagToBuild.setName(name);
      return this;
    }

    public CommandTag.UpdateBuilder description(String description) {
      this.tagToBuild.setDescription(description);
      return this;
    }

    public CommandTag.UpdateBuilder dataType(Class<?> dataType) {
      this.tagToBuild.setDataType(dataType.getName());
      return this;
    }

    public CommandTag.UpdateBuilder mode(TagMode mode) {
      this.tagToBuild.setMode(mode);
      return this;
    }

    public CommandTag.UpdateBuilder clientTimeout(Integer clientTimeout) {
      this.tagToBuild.setClientTimeout(clientTimeout);
      return this;
    }

    public CommandTag.UpdateBuilder execTimeout(Integer execTimeout) {
      this.tagToBuild.setExecTimeout(execTimeout);
      return this;
    }

    public CommandTag.UpdateBuilder sourceTimeout(Integer sourceTimeout) {
      this.tagToBuild.setSourceTimeout(sourceTimeout);
      return this;
    }

    public CommandTag.UpdateBuilder sourceRetries(Integer sourceRetries) {
      this.tagToBuild.setSourceRetries(sourceRetries);
      return this;
    }

    public CommandTag.UpdateBuilder rbacClass(String rbacClass) {
      this.tagToBuild.setRbacClass(rbacClass);
      return this;
    }

    public CommandTag.UpdateBuilder rbacDevice(String rbacDevice) {
      this.tagToBuild.setRbacDevice(rbacDevice);
      return this;
    }

    public CommandTag.UpdateBuilder rbacProperty(String rbacProperty) {
      this.tagToBuild.setRbacProperty(rbacProperty);
      return this;
    }

    public CommandTag.UpdateBuilder minimum(Number minimum) {
      this.tagToBuild.setMinValue(minimum);
      return this;
    }

    public CommandTag.UpdateBuilder maximum(Number maximum) {
      this.tagToBuild.setMaxValue(maximum);
      return this;
    }

    public CommandTag.UpdateBuilder hardwareAddress(HardwareAddress hardwareAddress) {
      this.tagToBuild.setHardwareAddress(hardwareAddress);
      return this;
    }

    public CommandTag.UpdateBuilder metadata(Metadata metadata) {
      this.tagToBuild.setMetadata(metadata);
      return this;
    }

    public CommandTag build() {
      this.tagToBuild.setUpdated(true);
      return this.tagToBuild;
    }
  }
}
