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
package cern.c2mon.server.configuration.parser.util;

import cern.c2mon.shared.client.configuration.api.tag.RuleTag;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.common.metadata.Metadata;

import java.util.Properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigurationRuleTagUtil {

  private static ObjectMapper mapper = new ObjectMapper();

  /**
   * Expected generated id is 100.
   */
  public static RuleTag buildCreateBasicRuleTag(Properties properties) {
    if (properties == null) {
      properties = new Properties();
    }

    RuleTag ruleTag = RuleTag.create("RuleTag", Integer.class, "ruleExpression").build();

    properties.setProperty("name", "RuleTag");
    properties.setProperty("description", "<no description provided>");
    properties.setProperty("mode", String.valueOf(TagMode.OPERATIONAL.ordinal()));
    properties.setProperty("dataType", Integer.class.getName());
    properties.setProperty("isLogged", String.valueOf(true));
    properties.setProperty("ruleText", "ruleExpression");

    return ruleTag;
  }

  public static RuleTag buildCreateAllFieldsRuleTag(Long id, Properties properties) {
    if (properties == null) {
      properties = new Properties();
    }

    RuleTag ruleTag = RuleTag.create("RuleTag" + id, Integer.class, "(#1000 < 0)|(#1000 > 200)[1],true[0]")
        .description("foo")
        .isLogged(false)
        .mode(TagMode.OPERATIONAL)
        .id(id)
        .addMetadata("testMetadata", 11)
        .build();

    properties.setProperty("name", "RuleTag" + id);
    properties.setProperty("description", "foo");
    properties.setProperty("mode", String.valueOf(TagMode.OPERATIONAL.ordinal()));
    properties.setProperty("dataType", Integer.class.getName());
    properties.setProperty("isLogged", String.valueOf(false));
    properties.setProperty("ruleText", "(#1000 < 0)|(#1000 > 200)[1],true[0]");
    Metadata metadata = new Metadata();
    metadata.addMetadata("testMetadata", 11);
    properties.setProperty("metadata", getJsonMetadata(metadata));

    return ruleTag;
  }

  public static RuleTag buildUpdateRuleTagWithAllFields(Long id, Properties properties) {
    if (properties == null) {
      properties = new Properties();
    }

    RuleTag ruleTag = RuleTag.update(id)
        .name("updateName")
        .description("foo_Update")
        .mode(TagMode.OPERATIONAL)
        .dataType(Double.class)
        .isLogged(true)
        .updateMetadata("testMetadata_Update", true)
        .ruleText("(#1000 < 20)|(#1000 > 200)[1],true[0]")
        .build();

    properties.setProperty("name", "updateName");
    properties.setProperty("description", "foo_Update");
    properties.setProperty("mode", String.valueOf(TagMode.OPERATIONAL.ordinal()));
    properties.setProperty("dataType", Double.class.getName());
    properties.setProperty("isLogged", String.valueOf(true));
    properties.setProperty("ruleText", "(#1000 < 20)|(#1000 > 200)[1],true[0]");
    Metadata metadata = new Metadata();
    metadata.addMetadata("testMetadata_Update", true);
    metadata.setUpdate(true);
    properties.setProperty("metadata", getJsonMetadata(metadata));

    return ruleTag;
  }

  public static RuleTag buildUpdateRuleTagWithSomeFields(Long id, Properties properties) {
    if (properties == null) {
      properties = new Properties();
    }

    RuleTag ruleTag = RuleTag.update(id)
        .description("foo_Update")
        .ruleText("update ruleExpression")
        .build();

    properties.setProperty("description", "foo_Update");
    properties.setProperty("ruleText", "update ruleExpression");

    return ruleTag;
  }

  public static RuleTag buildUpdateRuleTagWithSomeFields(String name, Properties properties) {
    if (properties == null) {
      properties = new Properties();
    }

    RuleTag ruleTag = RuleTag.update(name)
        .description("foo_Update")
        .ruleText("update ruleExpression")
        .build();

    properties.setProperty("name", name);
    properties.setProperty("description", "foo_Update");
    properties.setProperty("ruleText", "update ruleExpression");

    return ruleTag;
  }

  public static RuleTag buildDeleteRuleTag(Long id) {
    RuleTag deleteTag = new RuleTag();
    deleteTag.setId(id);
    deleteTag.setDeleted(true);

    return deleteTag;
  }

  private static String getJsonMetadata(Metadata metadata) {
    String jsonMetadata = null;
    try {
      jsonMetadata = mapper.writeValueAsString(metadata);
    }
    catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return jsonMetadata;
  }
}
