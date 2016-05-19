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
import cern.c2mon.shared.common.metadata.Metadata;
import lombok.*;

import java.util.List;

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

  @DefaultValue("java.lang.String")
  private String dataType = null;

  @DefaultValue("true")
  private Boolean isLogged = true;

  /**
   * Constructor for building a StatusTag with all fields.
   * To build a StatusTag with arbitrary fields use the builder pattern.
   *
   * @param id          Unique id of the tag.
   * @param name        Unique name the tag.
   * @param description Describes the propose of the tag.
   * @param mode        define the mode in which the tag is running.
   * @param alarms      List of configuration PObjects for this tag. If the argument is null the field will be an empty List as default.
   * @param metadata    Arbitrary metadata attached to his tag configuration.
   * @param isLogged    Defines if the tag which belongs to this configuration should be logged.
   * @param metadata    Arbitrary metadata attached to his tag configuration.
   */
  @Builder
  public StatusTag(Long id, String name, String description, TagMode mode, @Singular List<Alarm> alarms,
                   Boolean isLogged, Metadata metadata) {

    super(id, name, description, mode, alarms, metadata);

    this.isLogged = isLogged;
  }

  public StatusTag() {
  }


  @Override
  public boolean requiredFieldsGiven() {
    return super.requiredFieldsGiven();
  }

  public static CreateBuilder create(String name) {

    StatusTag iniTag = StatusTag.builder().name(name).build();

    return iniTag.toCreateBuilder(iniTag);
  }

  private StatusTag.CreateBuilder toCreateBuilder(StatusTag initializationTag) {
    return new StatusTag.CreateBuilder(initializationTag);
  }

  public static class CreateBuilder {

    private StatusTag builderTag;

    CreateBuilder(StatusTag initializationTag) {

      initializationTag.setCreate(true);
      this.builderTag = initializationTag;
    }

    public StatusTag.CreateBuilder id(Long id) {
      this.builderTag.setId(id);
      return this;
    }

    public StatusTag.CreateBuilder description(String description) {
      this.builderTag.setDescription(description);
      return this;
    }

    public StatusTag.CreateBuilder mode(TagMode mode) {
      this.builderTag.setMode(mode);
      return this;
    }

    public StatusTag.CreateBuilder isLogged(Boolean isLogged) {
      this.builderTag.setIsLogged(isLogged);
      return this;
    }

    public StatusTag.CreateBuilder metadata(Metadata metadata) {
      this.builderTag.setMetadata(metadata);
      return this;
    }

    public StatusTag build() {
      return this.builderTag;
    }
  }
}
