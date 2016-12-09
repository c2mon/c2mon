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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.util.Assert;

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

  private Long equipmentId;

  private Long subEquipmentId;

  private Long processId;

  @IgnoreProperty
  private String processName;

  @IgnoreProperty
  private String equipmentName;

  @IgnoreProperty
  private String subEquipmentName;

  /**
   * Address configuration of the datatag (if any)
   */
  private DataTagAddress address;

  @DefaultValue("java.lang.Boolean")
  private String dataType;

  @DefaultValue("true")
  private Boolean isLogged;

  public CommFaultTag() {
  }

  public static CreateBuilder create(String name) {
    Assert.hasText(name, "Comm fault tag name required!");

    return new CreateBuilder(name);
  }

  public static UpdateBuilder update(Long id) {

    return new UpdateBuilder(id);
  }

  public static UpdateBuilder update(String name) {

    return new CommFaultTag.UpdateBuilder(name);
  }

  public static class UpdateBuilder {

    private CommFaultTag tagToBuild = new CommFaultTag();

    private UpdateBuilder(String name) {
      tagToBuild.setName(name);
    }

    private UpdateBuilder(Long id) {
      tagToBuild.setId(id);
    }

    public CommFaultTag.UpdateBuilder id(Long id) {
      this.tagToBuild.setId(id);
      return this;
    }

    public CommFaultTag.UpdateBuilder description(String description) {
      this.tagToBuild.setDescription(description);
      return this;
    }

    public CommFaultTag.UpdateBuilder mode(TagMode mode) {
      this.tagToBuild.setMode(mode);
      return this;
    }

    public CommFaultTag.UpdateBuilder isLogged(Boolean isLogged) {
      this.tagToBuild.setIsLogged(isLogged);
      return this;
    }

    public CommFaultTag.UpdateBuilder address(DataTagAddress address) {
      this.tagToBuild.setAddress(address);
      return this;
    }

    public CommFaultTag.UpdateBuilder metadata(Metadata metadata) {
      this.tagToBuild.setMetadata(metadata);
      return this;
    }

    public CommFaultTag build() {
      this.tagToBuild.setUpdated(true);
      return this.tagToBuild;
    }
  }

  public static class CreateBuilder {

    private CommFaultTag tagToBuild = new CommFaultTag();

    private CreateBuilder(String name) {
      tagToBuild.setName(name);
    }

    public CommFaultTag.CreateBuilder id(Long id) {
      this.tagToBuild.setId(id);
      return this;
    }

    public CommFaultTag.CreateBuilder description(String description) {
      this.tagToBuild.setDescription(description);
      return this;
    }

    public CommFaultTag.CreateBuilder mode(TagMode mode) {
      this.tagToBuild.setMode(mode);
      return this;
    }

    public CommFaultTag.CreateBuilder isLogged(Boolean isLogged) {
      this.tagToBuild.setIsLogged(isLogged);
      return this;
    }

    public CommFaultTag.CreateBuilder address(DataTagAddress address) {
      this.tagToBuild.setAddress(address);
      return this;
    }

    public CommFaultTag.CreateBuilder metadata(Metadata metadata) {
      this.tagToBuild.setMetadata(metadata);
      return this;
    }

    public CommFaultTag build() {
      tagToBuild.setCreated(true);
      return this.tagToBuild;
    }
  }
}
