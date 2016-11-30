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
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.common.metadata.Metadata;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.util.Assert;

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
   * Unit of the tag's value. This parameter is defined at configuration time
   * and doesn't change during run-time. It is mainly used for analogue values
   * that may represent e.g. a flow in "m3", a voltage in "kV" etc.
   */
  private String unit;

  /**
   * Indicates whether this tag's value changes shall be logged to the
   * history.
   */
  @DefaultValue("true")
  private Boolean isLogged;

  /**
   * empty default constructor
   */
  public RuleTag() {
  }

  public static CreateBuilder create(String name, Class<?> dataType, String ruleText) {

    Assert.hasText(name, "Rule tag name is required!");
    Assert.notNull(name, "Data type is required!");
    Assert.hasText(ruleText, "Rule expression is required!");

    return new CreateBuilder(name, dataType, ruleText);
  }

  public static UpdateBuilder update(Long id) {
    return new UpdateBuilder(id);
  }

  public static UpdateBuilder update(String name) {
    return new UpdateBuilder(name);
  }

  public static class CreateBuilder {

    private RuleTag tagToBuild = new RuleTag();

    private CreateBuilder(String name, Class<?> dataType, String ruleText) {
      this.tagToBuild.setName(name);
      this.tagToBuild.setDataType(dataType.getName());
      this.tagToBuild.setRuleText(ruleText);
    }

    public RuleTag.CreateBuilder id(Long id) {
      this.tagToBuild.setId(id);
      return this;
    }

    public RuleTag.CreateBuilder description(String description) {
      this.tagToBuild.setDescription(description);
      return this;
    }

    public RuleTag.CreateBuilder mode(TagMode mode) {
      this.tagToBuild.setMode(mode);
      return this;
    }

    public RuleTag.CreateBuilder unit(String unit) {
      this.tagToBuild.setUnit(unit);
      return this;
    }

    public RuleTag.CreateBuilder isLogged(Boolean isLogged) {
      this.tagToBuild.setIsLogged(isLogged);
      return this;
    }

    public RuleTag.CreateBuilder addMetadata(String key, Object value) {
      if (this.tagToBuild.getMetadata() == null) {
        Metadata metadata = new Metadata();
        this.tagToBuild.setMetadata(metadata);
      }
      this.tagToBuild.getMetadata().addMetadata(key, value);
      return this;
    }

    public RuleTag build() {

      tagToBuild.setCreated(true);
      return this.tagToBuild;
    }
  }

  public static class UpdateBuilder {

    private RuleTag tagToBuild = new RuleTag();

    private UpdateBuilder(String name) {
      tagToBuild.setName(name);
    }

    private UpdateBuilder(Long id) {
      tagToBuild.setId(id);
    }

    public RuleTag.UpdateBuilder name(String name) {
      this.tagToBuild.setName(name);
      return this;
    }

    public RuleTag.UpdateBuilder description(String description) {
      this.tagToBuild.setDescription(description);
      return this;
    }

    public RuleTag.UpdateBuilder dataType(Class<?> dataType) {
      this.tagToBuild.setDataType(dataType.getName());
      return this;
    }

    public RuleTag.UpdateBuilder mode(TagMode mode) {
      this.tagToBuild.setMode(mode);
      return this;
    }

    public RuleTag.UpdateBuilder isLogged(Boolean isLogged) {
      this.tagToBuild.setIsLogged(isLogged);
      return this;
    }

    public RuleTag.UpdateBuilder ruleText(String ruleText) {
      this.tagToBuild.setRuleText(ruleText);
      return this;
    }

    public RuleTag.UpdateBuilder updateMetadata(String key, Object value) {
      if (this.tagToBuild.getMetadata() == null) {
        Metadata metadata = new Metadata();
        this.tagToBuild.setMetadata(metadata);
      }
      this.tagToBuild.getMetadata().updateMetadata(key, value);
      return this;
    }

    public RuleTag.UpdateBuilder removeMetadata(String key) {
      if (this.tagToBuild.getMetadata() == null) {
        tagToBuild.setMetadata(new Metadata());
      }
      this.tagToBuild.getMetadata().addToRemoveList(key);
      return this;
    }


    public RuleTag build() {
      this.tagToBuild.setUpdated(true);
      return this.tagToBuild;
    }
  }
}
