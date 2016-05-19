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
 * Configuration object for a AliveTag.
 * Holds the information to create a {@link cern.c2mon.shared.client.configuration.ConfigurationElement}
 * related to an AliveTag.
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
public class AliveTag extends ControlTag {

  /**
   * Address configuration of the datatag (if any)
   */
  private DataTagAddress address;

  // TODO test to set the dataType default final
  @DefaultValue("java.lang.Long")
  private String dataType = null;

  // TODO test to set the isLogged default final
  @DefaultValue("false")
  private Boolean isLogged = null;

  /**
   * Constructor for building a AliveTag with all fields.
   * To build a Alive tag with arbitrary fields use the builder pattern.
   *
   * @param id          Unique id of the tag.
   * @param name        Unique name the tag.
   * @param description Describes the propose of the tag.
   * @param mode        define the mode in which the tag is running.
   * @param alarms      List of configuration objects for this tag. If the argument is null the field will be an empty List as default.
   * @param address     DataTagAddress which belongs to this tag configuration.
   * @param metadata    Arbitrary metadata attached to his tag configuration.
   */
  @Builder
  public AliveTag(Long id, String name, String description, TagMode mode,
                  @Singular List<Alarm> alarms, DataTagAddress address, Metadata metadata) {
    super(id, name, description, mode, alarms, metadata);
    this.address = address;
  }

  public AliveTag(Long id, String description, TagMode mode, DataTagAddress address, Metadata metadata) {
    super(id, null, description, mode, null, metadata);
    this.address = address;
  }

  /**
   * Empty default constructor
   */
  public AliveTag() {
  }

  @Override
  public boolean requiredFieldsGiven() {
    return super.requiredFieldsGiven();
  }

  public static CreateBuilder create(String name) {

    AliveTag iniTag = AliveTag.builder().name(name).build();

    return iniTag.toCreateBuilder(iniTag);
  }

  public CreateBuilder toCreateBuilder(AliveTag initializationTag) {
    return new CreateBuilder(initializationTag);
  }

  public static class CreateBuilder {

    private AliveTag builderTag;

    CreateBuilder(AliveTag initializationTag) {

      initializationTag.setCreate(true);
      this.builderTag = initializationTag;
    }

    public AliveTag.CreateBuilder id(Long id) {
      this.builderTag.setId(id);
      return this;
    }

    public AliveTag.CreateBuilder description(String description) {
      this.builderTag.setDescription(description);
      return this;
    }

    public AliveTag.CreateBuilder mode(TagMode mode) {
      this.builderTag.setMode(mode);
      return this;
    }

    public AliveTag.CreateBuilder address(DataTagAddress address) {
      this.builderTag.setAddress(address);
      return this;
    }

    public AliveTag.CreateBuilder metadata(Metadata metadata) {
      this.builderTag.setMetadata(metadata);
      return this;
    }

    public AliveTag build() {
      return this.builderTag;
    }
  }
}
