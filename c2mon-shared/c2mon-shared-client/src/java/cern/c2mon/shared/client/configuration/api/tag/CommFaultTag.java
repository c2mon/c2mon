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
 * Configuration object for a CommFaultTag.
 * Holds the information to create a {@link cern.c2mon.shared.client.configuration.ConfigurationElement}
 * related to a CommFaultTag.
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
public class CommFaultTag extends ControlTag {

  /**
   * Address configuration of the datatag (if any)
   */
  private DataTagAddress address;

  @DefaultValue("java.lang.Boolean")
  private String dataType = null;

  @DefaultValue("true")
  private Boolean isLogged = true;

  /**
   * Constructor for building a CommFaultTag with all fields.
   * To build a CommFaultTag with arbitrary fields use the builder pattern.
   *
   * @param id Unique id of the tag.
   * @param name Unique name the tag.
   * @param description Describes the propose of the tag.
   * @param mode define the mode in which the tag is running.
   * @param alarms List of configuration PObjects for this tag. If the argument is null the field will be an empty List as default.
   * @param address DataTagAddress which belongs to this tag configuration.
   * @param metadata Arbitrary metadata attached to his tag configuration.
   * @param isLogged Defines if the tag which belongs to this configuration should be logged.
   * @param address DataTagAddress which belongs to this tag configuration.
   * @param metadata Arbitrary metadata attached to his tag configuration.
   */
  @Builder
  public CommFaultTag(Long id, String name, String description, TagMode mode, @Singular List<Alarm> alarms,
                      Boolean isLogged, DataTagAddress address, Metadata metadata) {
    super(id, name, description, mode, alarms, metadata);
    this.address = address;
    this.isLogged = isLogged;
  }

  /**
   * Empty default constructor
   */
  public CommFaultTag() {
  }

  @Override
  public boolean requiredFieldsGiven() {
    return super.requiredFieldsGiven();
  }

  public static CreateBuilder create(String name) {

    CommFaultTag iniTag = CommFaultTag.builder().name(name).build();

    return iniTag.toCreateBuilder(iniTag);
  }

  private CommFaultTag.CreateBuilder toCreateBuilder(CommFaultTag initializationTag) {
    return new CommFaultTag.CreateBuilder(initializationTag);
  }

  public static class CreateBuilder {

    private CommFaultTag builderTag;

    CreateBuilder(CommFaultTag initializationTag) {

      initializationTag.setCreate(true);
      this.builderTag = initializationTag;
    }

    public CommFaultTag.CreateBuilder id(Long id) {
      this.builderTag.setId(id);
      return this;
    }

    public CommFaultTag.CreateBuilder description(String description) {
      this.builderTag.setDescription(description);
      return this;
    }

    public CommFaultTag.CreateBuilder mode(TagMode mode) {
      this.builderTag.setMode(mode);
      return this;
    }

    public CommFaultTag.CreateBuilder isLogged(Boolean isLogged) {
      this.builderTag.setIsLogged(isLogged);
      return this;
    }

    public CommFaultTag.CreateBuilder address(DataTagAddress address) {
      this.builderTag.setAddress(address);
      return this;
    }

    public CommFaultTag.CreateBuilder metadata(Metadata metadata) {
      this.builderTag.setMetadata(metadata);
      return this;
    }

    public CommFaultTag build() {
      return this.builderTag;
    }
  }
}
