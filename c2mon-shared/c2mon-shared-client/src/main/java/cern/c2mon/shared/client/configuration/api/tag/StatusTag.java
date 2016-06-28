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
import cern.c2mon.shared.common.metadata.Metadata;
import lombok.*;
import org.springframework.util.Assert;

/**
 * Configuration object for a StatusTag.
 * Holds the information to create a {@link cern.c2mon.shared.client.configuration.ConfigurationElement}
 * related to a StatusTag.
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
public class StatusTag extends ControlTag {

  private Long equipmentId;

  private Long subEquipmentId;

  private Long processId;

  @IgnoreProperty
  private String processName;

  @IgnoreProperty
  private String equipmentName;

  @IgnoreProperty
  private String subEquipmentName;

  @DefaultValue("java.lang.String")
  private String dataType;

  @DefaultValue("true")
  private Boolean isLogged;

  public static CreateBuilder create(String name) {
    Assert.hasText(name, "Status tag name required!");

    return new CreateBuilder(name);
  }

  public static UpdateBuilder update(Long id) {
    return new UpdateBuilder(id);
  }

  public static UpdateBuilder update(String name) {
    return new UpdateBuilder(name);
  }

  public static class UpdateBuilder {

    private StatusTag tagToBuild = new StatusTag();

    private UpdateBuilder(String name) {
      tagToBuild.setName(name);
    }

    private UpdateBuilder(Long id) {
      tagToBuild.setId(id);
    }

    public StatusTag.UpdateBuilder description(String description) {
      this.tagToBuild.setDescription(description);
      return this;
    }

    public StatusTag.UpdateBuilder mode(TagMode mode) {
      this.tagToBuild.setMode(mode);
      return this;
    }

    public StatusTag.UpdateBuilder isLogged(Boolean isLogged) {
      this.tagToBuild.setIsLogged(isLogged);
      return this;
    }

    public StatusTag.UpdateBuilder metadata(Metadata metadata) {
      this.tagToBuild.setMetadata(metadata);
      return this;
    }

    public StatusTag build() {
      this.tagToBuild.setUpdated(true);
      return this.tagToBuild;
    }
  }

  public static class CreateBuilder {

    private StatusTag tagToBuild = new StatusTag();

    private CreateBuilder(String name) {
      tagToBuild.setName(name);
    }

    public StatusTag.CreateBuilder id(Long id) {
      this.tagToBuild.setId(id);
      return this;
    }

    public StatusTag.CreateBuilder description(String description) {
      this.tagToBuild.setDescription(description);
      return this;
    }

    public StatusTag.CreateBuilder mode(TagMode mode) {
      this.tagToBuild.setMode(mode);
      return this;
    }

    public StatusTag.CreateBuilder isLogged(Boolean isLogged) {
      this.tagToBuild.setIsLogged(isLogged);
      return this;
    }

    public StatusTag.CreateBuilder metadata(Metadata metadata) {
      this.tagToBuild.setMetadata(metadata);
      return this;
    }

    public StatusTag build() {
      tagToBuild.setCreated(true);
      return this.tagToBuild;
    }
  }
}
