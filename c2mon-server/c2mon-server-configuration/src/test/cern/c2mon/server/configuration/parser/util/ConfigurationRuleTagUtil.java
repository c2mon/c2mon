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
import cern.c2mon.shared.client.configuration.api.util.DataType;
import cern.c2mon.shared.client.tag.TagMode;

import java.util.Properties;

//@Service
public class ConfigurationRuleTagUtil {


  public static Pair<RuleTag, Properties> buildRuleTagWtihId(Long id) {
    return new Pair<>(RuleTag.builder().id(id).build(), new Properties());
  }

  public static Pair<RuleTag, Properties> buildRuleTagWtihPrimFields(Long id) {
    RuleTag pro = RuleTag.builder()
        .id(id)
        .name("RuleTag")
        .description("foo")
        .dataType(DataType.INTEGER)
        .build();

    Properties props = new Properties();
    props.setProperty("name", "RuleTag");
    props.setProperty("description", "foo");
    props.setProperty("mode", String.valueOf(TagMode.TEST.ordinal()));
    props.setProperty("dataType", DataType.INTEGER.toString());
    props.setProperty("isLogged", String.valueOf(true));

    return new Pair<>(pro, props);
  }

  public static Pair<RuleTag, Properties> buildRuleTagWtihAllFields(Long id) {
    RuleTag pro = RuleTag.builder()
        .id(id)
        .name("RuleTag")
        .description("foo")
        .mode(TagMode.OPERATIONAL)
        .dataType(DataType.INTEGER)
        .isLogged(false)
        .ruleText("testRule")
        .build();

    Properties props = new Properties();
    props.setProperty("name", "RuleTag");
    props.setProperty("description", "foo");
    props.setProperty("mode", String.valueOf(TagMode.OPERATIONAL.ordinal()));
    props.setProperty("dataType", DataType.INTEGER.toString());
    props.setProperty("isLogged", String.valueOf(false));
    props.setProperty("ruleText", "testRule");

    return new Pair<>(pro, props);
  }

  public static Pair<RuleTag, Properties> buildRuleTagWtihoutDefaultFields(Long id) {
    RuleTag pro = RuleTag.builder()
        .id(id)
        .name("RuleTag")
        .description("foo")
        .dataType(DataType.INTEGER)
        .build();

    Properties props = new Properties();
    props.setProperty("name", "RuleTag");
    props.setProperty("description", "foo");
    props.setProperty("mode", String.valueOf(TagMode.TEST.ordinal()));
    props.setProperty("dataType", DataType.INTEGER.toString());
    props.setProperty("isLogged", String.valueOf(true));

    return new Pair<>(pro, props);
  }

  public static Pair<RuleTag, Properties> buildUpdateRuleTagWithAllFields(Long id) {
    RuleTag pro = RuleTag.builder()
        .id(id)
        .name("RuleTag_Update")
        .description("foo_Update")
        .mode(TagMode.OPERATIONAL)
        .dataType(DataType.DOUBLE)
        .isLogged(true)
        .ruleText("testRule_Update")
        .build();

    Properties props = new Properties();
    props.setProperty("name", "RuleTag_Update");
    props.setProperty("description", "foo_Update");
    props.setProperty("mode", String.valueOf(TagMode.OPERATIONAL.ordinal()));
    props.setProperty("dataType", DataType.DOUBLE.toString());
    props.setProperty("isLogged", String.valueOf(true));
    props.setProperty("ruleText", "testRule_Update");

    return new Pair<>(pro, props);
  }

  public static Pair<RuleTag, Properties> buildUpdateRuleTagWtihSomeFields(Long id) {
    RuleTag pro = RuleTag.builder()
        .id(id)
        .description("foo_Update")
        .build();

    Properties props = new Properties();
    props.setProperty("description", "foo_Update");

    return new Pair<>(pro, props);
  }

  public static RuleTag buildDeleteRuleTag(Long id) {
    return RuleTag.builder()
        .id(id)
        .deleted(true)
        .build();
  }
  // ##################### Builder #####################

  public static Pair<RuleTag.RuleTagBuilder, Properties> builderRuleTagWithPrimFields(Long id) {
    RuleTag.RuleTagBuilder pro = RuleTag.builder()
        .id(id)
        .name("RuleTag"+id)
        .description("foo")
        .dataType(DataType.INTEGER);

    Properties props = new Properties();
    props.setProperty("name", "RuleTag"+id);
    props.setProperty("description", "foo");
    props.setProperty("mode", String.valueOf(TagMode.TEST.ordinal()));
    props.setProperty("dataType", DataType.INTEGER.toString());
    props.setProperty("isLogged", String.valueOf(true));

    return new Pair<>(pro, props);
  }

  public static Pair<RuleTag.RuleTagBuilder, Properties> builderRuleTagWithAllFields(Long id) {
    RuleTag.RuleTagBuilder pro = RuleTag.builder()
        .id(id)
        .name("RuleTag"+id)
        .description("foo")
        .mode(TagMode.OPERATIONAL)
        .dataType(DataType.INTEGER)
        .isLogged(false)
        .ruleText("testRule");

    Properties props = new Properties();
    props.setProperty("name", "RuleTag"+id);
    props.setProperty("description", "foo");
    props.setProperty("mode", String.valueOf(TagMode.OPERATIONAL.ordinal()));
    props.setProperty("dataType", DataType.INTEGER.toString());
    props.setProperty("isLogged", String.valueOf(false));
    props.setProperty("ruleText", "testRule");

    return new Pair<>(pro, props);
  }

  public static Pair<RuleTag.RuleTagBuilder, Properties> builderRuleTagWithAllFields(Long id, String ruleText) {
    RuleTag.RuleTagBuilder pro = RuleTag.builder()
        .id(id)
        .name("RuleTag"+id)
        .description("foo")
        .mode(TagMode.OPERATIONAL)
        .dataType(DataType.INTEGER)
        .isLogged(false)
        .ruleText(ruleText)
        .dipAddress("testConfigDIPaddress")
        .japcAddress("testConfigJAPCaddress");

    Properties props = new Properties();
    props.setProperty("name", "RuleTag"+id);
    props.setProperty("description", "foo");
    props.setProperty("mode", String.valueOf(TagMode.OPERATIONAL.ordinal()));
    props.setProperty("dataType", DataType.INTEGER.toString());
    props.setProperty("isLogged", String.valueOf(false));
    props.setProperty("ruleText", ruleText);
    props.setProperty("dipAddress", "testConfigDIPaddress");
    props.setProperty("japcAddress", "testConfigJAPCaddress");

    return new Pair<>(pro, props);
  }

  public static Pair<RuleTag.RuleTagBuilder, Properties> builderRuleTagUpdate(Long id, String ruleText) {
    RuleTag.RuleTagBuilder pro = RuleTag.builder()
        .id(id)
        .ruleText(ruleText)
        .japcAddress("testConfigJAPCAddress_Update");

    Properties props = new Properties();
    props.setProperty("ruleText", ruleText);
    props.setProperty("japcAddress", "testConfigJAPCAddress_Update");

    return new Pair<>(pro, props);
  }
}
