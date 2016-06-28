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
import org.springframework.util.Assert;


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

  private Long processId;

  @IgnoreProperty
  private String parentProcessName;

  /**
   * Fully qualified name of the EquipmentMessageHandler subclass to be used by the DAQ to connect to the equipment.
   * Make Sure that the name of the class matches with the full EquipmentMessageHandler class name.
   */
  private String handlerClass;

  public Equipment() {
  }

  public static CreateBuilder create(String name, String handlerClass) {
    Assert.hasText(name, "Equipment name is required!");
    Assert.hasText(handlerClass, "Handler class is required!");
    return new CreateBuilder(name,handlerClass);
  }

  public static UpdateBuilder update(String name) {
    return new UpdateBuilder(name);
  }

  public static UpdateBuilder update(Long id) {
    return new UpdateBuilder(id);
  }

  public static class CreateBuilder {

    private Equipment equipmentToBuild = new Equipment();

    private CreateBuilder(String name, String handlerClass) {
      equipmentToBuild.setName(name);
      equipmentToBuild.setHandlerClass(handlerClass);
      equipmentToBuild.setCreated(true);
    }

    public Equipment.CreateBuilder id(Long id) {
      this.equipmentToBuild.setId(id);
      return this;
    }

    public Equipment.CreateBuilder description(String description) {
      this.equipmentToBuild.setDescription(description);
      return this;
    }

    public Equipment.CreateBuilder address(String address) {
      this.equipmentToBuild.setAddress(address);
      return this;
    }

    public Equipment.CreateBuilder aliveTag(AliveTag aliveTag, Integer aliveInterval) {

      this.equipmentToBuild.setAliveInterval(aliveInterval);
      this.equipmentToBuild.setAliveTag(aliveTag);

      if (!aliveTag.isCreated()) {
        equipmentToBuild.setCreated(false);
      }
      return this;
    }

    public Equipment.CreateBuilder statusTag(StatusTag statusTag) {
      this.equipmentToBuild.setStatusTag(statusTag);

      if (!statusTag.isCreated()) {
        equipmentToBuild.setCreated(false);
      }

      return this;
    }

    public Equipment.CreateBuilder commFaultTag(CommFaultTag commFaultTag) {
      this.equipmentToBuild.setCommFaultTag(commFaultTag);

      if (!commFaultTag.isCreated()) {
        equipmentToBuild.setCreated(false);
      }

      return this;
    }

    public Equipment build() {
      return this.equipmentToBuild;
    }
  }

  public static class UpdateBuilder {
    private Equipment equipmentToBuild = new Equipment();

    private UpdateBuilder(String name) {
      equipmentToBuild.setName(name);
    }

    private UpdateBuilder(Long id) {
      equipmentToBuild.setId(id);
    }

    public Equipment.UpdateBuilder aliveInterval(Integer aliveInterval) {
      this.equipmentToBuild.setAliveInterval(aliveInterval);
      return this;
    }

    public Equipment.UpdateBuilder name(String name) {
      this.equipmentToBuild.setName(name);
      return this;
    }

    public Equipment.UpdateBuilder description(String description) {
      this.equipmentToBuild.setDescription(description);
      return this;
    }

    public Equipment.UpdateBuilder handlerClass(String handlerClass) {
      this.equipmentToBuild.setHandlerClass(handlerClass);
      return this;
    }

    public Equipment.UpdateBuilder address(String address) {
      this.equipmentToBuild.setAddress(address);
      return this;
    }

    public Equipment build() {
      equipmentToBuild.setUpdated(true);

      return this.equipmentToBuild;
    }
  }
}
