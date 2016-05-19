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
 * Configuration object for a RuleTag.
 * Holds the information to create a {@link cern.c2mon.shared.client.configuration.ConfigurationElement}
 * related to a RuleTag.
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
public class RuleTag extends Tag {

  /**
   * The rule as a String. Should never be null for a RuleTag (set as empty
   * String if necessary).
   */
  private String ruleText;

  /**
   * DIP address for tags published on DIP
   */
  private String dipAddress;

  /**
   * JAPC address for tags published on JAPC
   */
  private String japcAddress;

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
   * Constructor for building a RuleTag with all fields.
   * To build a RuleTag with arbitrary fields use the builder pattern.
   *
   * @param id          Unique id of the tag.
   * @param name        Unique name the tag.
   * @param description Describes the propose of the tag.
   * @param mode        define the mode in which the tag is running.
   * @param alarms      List of configuration PObjects for this tag. If the argument is null the field will be an empty List as default.
   * @param isLogged    Defines if the tag which belongs to this configuration should be logged.
   * @param metadata    Arbitrary metadata attached to his tag configuration.
   * @param deleted     Determine if this object apply as deletion.
   * @param dataType    Determine the data type of the DataTag which belongs to this configuration.
   * @param dipAddress  Defines the dipAddress of the DataTag which belongs to this configuration.
   * @param japcAddress Defines the japcAddress of the DataTag which belongs to this configuration.
   * @param ruleText    The rule which will be set to the rule through this configuration.
   */
  @Builder
  protected RuleTag(boolean deleted, Long id, String name, String description, Class<?> dataType, TagMode mode, @Singular List<Alarm> alarms, Boolean isLogged,
                    String ruleText, String dipAddress, String japcAddress, Metadata metadata) {
    super(deleted, id, name, description, mode, alarms, metadata);
    this.dataType = dataType!= null ? dataType.getName() : null;
    this.ruleText = ruleText;
    this.dipAddress = dipAddress;
    this.japcAddress = japcAddress;
    this.isLogged = isLogged;
  }

  /**
   * empty default constructor
   */
  public RuleTag() {
  }

  @Override
  public boolean requiredFieldsGiven() {
    return super.requiredFieldsGiven() && (getDataType() != null);
  }

  public static CreateBuilder create(String name, Class<?> dataType, String ruleText) {

    RuleTag iniTag = RuleTag.builder().name(name).dataType(dataType).ruleText(ruleText).build();

    return iniTag.toCreateBuilder(iniTag);
  }

  public static UpdateBuilder update(Long id) {

    RuleTag iniTag = RuleTag.builder().id(id).build();

    return iniTag.toUpdateBuilder(iniTag);
  }

  public static UpdateBuilder update(String name) {

    RuleTag iniTag = RuleTag.builder().name(name).build();

    return iniTag.toUpdateBuilder(iniTag);
  }

  private CreateBuilder toCreateBuilder(RuleTag initializationTag) {
    return new CreateBuilder(initializationTag);
  }

  private UpdateBuilder toUpdateBuilder(RuleTag initializationTag) {
    return new UpdateBuilder(initializationTag);
  }

  public static class CreateBuilder {

    RuleTag ruleTagBuild;

    CreateBuilder(RuleTag initializationTag) {
      this.ruleTagBuild = initializationTag;
    }

    public RuleTag.CreateBuilder id(Long id) {
      this.ruleTagBuild.setId(id);
      return this;
    }

    public RuleTag.CreateBuilder description(String description) {
      this.ruleTagBuild.setDescription(description);
      return this;
    }

    public RuleTag.CreateBuilder mode(TagMode mode) {
      this.ruleTagBuild.setMode(mode);
      return this;
    }

    public RuleTag.CreateBuilder isLogged(Boolean isLogged) {
      this.ruleTagBuild.setIsLogged(true);
      return this;
    }

    public RuleTag.CreateBuilder metadata(Metadata metadata) {
      this.ruleTagBuild.setMetadata(metadata);
      return this;
    }

    public RuleTag build() {

      ruleTagBuild.setCreate(true);
      return this.ruleTagBuild;
    }
  }

  public static class UpdateBuilder {

    private RuleTag builderTag;

    UpdateBuilder(RuleTag initializationTag) {
      this.builderTag = initializationTag;
    }

    public UpdateBuilder description(String description) {
      this.builderTag.setDescription(description);
      return this;
    }

    public UpdateBuilder dataType(Class<?> dataType) {
      this.builderTag.setDataType(dataType.getName());
      return this;
    }

    public UpdateBuilder mode(TagMode mode) {
      this.builderTag.setMode(mode);
      return this;
    }

    public UpdateBuilder isLogged(Boolean isLogged) {
      this.builderTag.setIsLogged(isLogged);
      return this;
    }

    public UpdateBuilder ruleText(String ruleText) {
      this.builderTag.setRuleText(ruleText);
      return this;
    }

    public UpdateBuilder metadata(Metadata metadata) {
      this.builderTag.setMetadata(metadata);
      return this;
    }

    public RuleTag build() {
      this.builderTag.setUpdate(true);
      return this.builderTag;
    }
  }
}
