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
public class SubEquipment extends AbstractEquipment {

  private Long equipmentId;

  @IgnoreProperty
  private String parentEquipmentName;

  public SubEquipment() {
  }

  public static CreateBuilder create(String name) {
    Assert.hasText(name, "SubEquipment name is required!");
    return new CreateBuilder(name);
  }

  public static UpdateBuilder update(String name) {
    return new UpdateBuilder(name);
  }

  public static UpdateBuilder update(Long id) {
    return new UpdateBuilder(id);
  }

  public static class CreateBuilder {

    private SubEquipment subEquipmentToBuild = new SubEquipment();

    private CreateBuilder(String name) {
      subEquipmentToBuild.setName(name);
      subEquipmentToBuild.setCreated(false);
    }

    public SubEquipment.CreateBuilder id(Long id) {
      this.subEquipmentToBuild.setId(id);
      return this;
    }

    public SubEquipment.CreateBuilder description(String description) {
      this.subEquipmentToBuild.setDescription(description);
      return this;
    }

    public SubEquipment.CreateBuilder address(String address) {
      this.subEquipmentToBuild.setAddress(address);
      return this;
    }

    public SubEquipment.CreateBuilder aliveTag(AliveTag aliveTag, Integer aliveInterval) {

      this.subEquipmentToBuild.setAliveInterval(aliveInterval);
      this.subEquipmentToBuild.setAliveTag(aliveTag);

      if (!aliveTag.isCreated()) {
        subEquipmentToBuild.setCreated(false);
      }

      return this;
    }

    public SubEquipment.CreateBuilder statusTag(StatusTag statusTag) {
      this.subEquipmentToBuild.setStatusTag(statusTag);

      if (!statusTag.isCreated()) {
        subEquipmentToBuild.setCreated(false);
      }

      return this;
    }

    public SubEquipment.CreateBuilder commFaultTag(CommFaultTag commFaultTag) {
      this.subEquipmentToBuild.setCommFaultTag(commFaultTag);

      if (!commFaultTag.isCreated()) {
        subEquipmentToBuild.setCreated(false);
      }
      return this;
    }

    public SubEquipment build() {
      this.subEquipmentToBuild.setCreated(true);
      return this.subEquipmentToBuild;
    }
  }

  public static class UpdateBuilder {
    private SubEquipment subEquipmentToBuild = new SubEquipment();

    private UpdateBuilder(String name) {
      subEquipmentToBuild.setName(name);
    }

    private UpdateBuilder(Long id) {
      subEquipmentToBuild.setId(id);
    }

    public SubEquipment.UpdateBuilder name(String name) {
      this.subEquipmentToBuild.setName(name);
      return this;
    }

    public SubEquipment.UpdateBuilder aliveInterval(Integer aliveInterval) {
      this.subEquipmentToBuild.setAliveInterval(aliveInterval);
      return this;
    }

    public SubEquipment.UpdateBuilder description(String description) {
      this.subEquipmentToBuild.setDescription(description);
      return this;
    }

    public SubEquipment.UpdateBuilder address(String address) {
      this.subEquipmentToBuild.setAddress(address);
      return this;
    }

    public SubEquipment build() {
      subEquipmentToBuild.setUpdated(true);
      return this.subEquipmentToBuild;
    }
  }
}
