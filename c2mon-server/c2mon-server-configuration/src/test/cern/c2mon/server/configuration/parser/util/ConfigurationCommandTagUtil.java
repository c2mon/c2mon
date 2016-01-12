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

import cern.c2mon.shared.client.configuration.api.tag.CommandTag;
import cern.c2mon.shared.client.configuration.api.util.DataType;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.common.datatag.address.impl.PLCHardwareAddressImpl;

import java.util.Properties;

//@Service
public class ConfigurationCommandTagUtil {


  public static Pair<CommandTag, Properties> buildCommandTagWithId(Long id) {
    return new Pair<>(CommandTag.builder().id(id).build(), new Properties());
  }

  public static Pair<CommandTag, Properties> buildCommandTagWithPrimFields(Long id) {
    CommandTag pro = CommandTag.builder()
        .id(id)
        .name("CommandTag")
        .description("foo")
        .dataType(DataType.STRING)
        .hardwareAddress(new PLCHardwareAddressImpl(1, 1, 1, 1, 1, 1.0f, "testAddress"))
        .clientTimeout(30000)
        .execTimeout(6000)
        .sourceTimeout(200)
        .sourceRetries(2)
        .rbacClass("RBAC class")
        .rbacDevice("RBAC device")
        .rbacProperty("RBAC property")
        .build();

    Properties props = new Properties();
    props.setProperty("name", "CommandTag");
    props.setProperty("description", "foo");
    props.setProperty("mode", String.valueOf(TagMode.TEST.ordinal()));
    props.setProperty("dataType", DataType.STRING.toString());
    props.setProperty("hardwareAddress", new PLCHardwareAddressImpl(1, 1, 1, 1, 1, 1.0f, "testAddress").toConfigXML());
    props.setProperty("equipmentId", String.valueOf(1l));
    props.setProperty("clientTimeout", String.valueOf(30000));
    props.setProperty("execTimeout", String.valueOf(6000));
    props.setProperty("sourceTimeout", String.valueOf(200));
    props.setProperty("sourceRetries", String.valueOf(2));
    props.setProperty("rbacClass", "RBAC class");
    props.setProperty("rbacDevice", "RBAC device");
    props.setProperty("rbacProperty", "RBAC property");
    props.setProperty("equipmentId", String.valueOf(1L));

    return new Pair<>(pro, props);
  }

  public static Pair<CommandTag, Properties> buildCommandTagWithAllFields(Long id) {
    CommandTag pro = CommandTag.builder()
        .id(id)
        .name("CommandTag")
        .description("foo")
        .mode(TagMode.OPERATIONAL)
        .dataType(DataType.STRING)
        .hardwareAddress(new PLCHardwareAddressImpl(1, 1, 1, 1, 1, 1.0f, "testAddress"))
        .clientTimeout(30000)
        .execTimeout(6000)
        .sourceTimeout(200)
        .sourceRetries(2)
        .rbacClass("RBAC class")
        .rbacDevice("RBAC device")
        .rbacProperty("RBAC property")
        .build();

    Properties props = new Properties();
    props.setProperty("name", "CommandTag");
    props.setProperty("description", "foo");
    props.setProperty("mode", String.valueOf(TagMode.OPERATIONAL.ordinal()));
    props.setProperty("dataType", DataType.STRING.toString());
    props.setProperty("hardwareAddress", new PLCHardwareAddressImpl(1, 1, 1, 1, 1, 1.0f, "testAddress").toConfigXML());
    props.setProperty("equipmentId", String.valueOf(1l));
    props.setProperty("clientTimeout", String.valueOf(30000));
    props.setProperty("execTimeout", String.valueOf(6000));
    props.setProperty("sourceTimeout", String.valueOf(200));
    props.setProperty("sourceRetries", String.valueOf(2));
    props.setProperty("rbacClass", "RBAC class");
    props.setProperty("rbacDevice", "RBAC device");
    props.setProperty("rbacProperty", "RBAC property");
    props.setProperty("equipmentId", String.valueOf(1L));

    return new Pair<>(pro, props);
  }

  public static Pair<CommandTag, Properties> buildCommandTagWithoutDefaultFields(Long id) {
    CommandTag pro = CommandTag.builder()
        .id(id)
        .name("CommandTag")
        .description("foo")
        .dataType(DataType.STRING)
        .hardwareAddress(new PLCHardwareAddressImpl(1, 1, 1, 1, 1, 1.0f, "testAddress"))
        .clientTimeout(30000)
        .execTimeout(6000)
        .sourceTimeout(200)
        .sourceRetries(2)
        .rbacClass("RBAC class")
        .rbacDevice("RBAC device")
        .rbacProperty("RBAC property")
        .build();

    Properties props = new Properties();
    props.setProperty("name", "CommandTag");
    props.setProperty("description", "foo");
    props.setProperty("mode", String.valueOf(TagMode.TEST.ordinal()));
    props.setProperty("dataType", DataType.STRING.toString());
    props.setProperty("hardwareAddress", new PLCHardwareAddressImpl(1, 1, 1, 1, 1, 1.0f, "testAddress").toConfigXML());
    props.setProperty("equipmentId", String.valueOf(1l));
    props.setProperty("clientTimeout", String.valueOf(30000));
    props.setProperty("execTimeout", String.valueOf(6000));
    props.setProperty("sourceTimeout", String.valueOf(200));
    props.setProperty("sourceRetries", String.valueOf(2));
    props.setProperty("rbacClass", "RBAC class");
    props.setProperty("rbacDevice", "RBAC device");
    props.setProperty("rbacProperty", "RBAC property");
    props.setProperty("equipmentId", String.valueOf(1L));

    return new Pair<>(pro, props);
  }

  public static Pair<CommandTag, Properties> buildUpdateCommandTagWithAllFields(Long id) {
    CommandTag pro = CommandTag.builder()
        .id(id)
        .name("CommandTag_update")
        .description("foo_update")
        .mode(TagMode.OPERATIONAL)
        .hardwareAddress(new PLCHardwareAddressImpl(1, 1, 1, 1, 1, 1.0f, "testAddress_update"))
        .clientTimeout(40000)
        .execTimeout(7000)
        .sourceTimeout(300)
        .sourceRetries(3)
        .rbacClass("RBAC class_update")
        .rbacDevice("RBAC device_update")
        .rbacProperty("RBAC property_update")
        .build();

    Properties props = new Properties();
    props.setProperty("name", "CommandTag_update");
    props.setProperty("description", "foo_update");
    props.setProperty("mode", String.valueOf(TagMode.OPERATIONAL.ordinal()));
    props.setProperty("hardwareAddress", new PLCHardwareAddressImpl(1, 1, 1, 1, 1, 1.0f, "testAddress_update").toConfigXML());
    props.setProperty("clientTimeout", String.valueOf(40000));
    props.setProperty("execTimeout", String.valueOf(7000));
    props.setProperty("sourceTimeout", String.valueOf(300));
    props.setProperty("sourceRetries", String.valueOf(3));
    props.setProperty("rbacClass", "RBAC class_update");
    props.setProperty("rbacDevice", "RBAC device_update");
    props.setProperty("rbacProperty", "RBAC property_update");

    return new Pair<>(pro, props);
  }

  public static Pair<CommandTag, Properties> buildUpdateCommandTagWithSomeFields(Long id) {
    CommandTag pro = CommandTag.builder()
        .id(id)
        .description("foo_update")
        .mode(TagMode.OPERATIONAL)
        .hardwareAddress(new PLCHardwareAddressImpl(2, 2, 2, 2, 2, 2.0f, "testAddress_update"))
        .clientTimeout(40000)
        .sourceRetries(3)
        .rbacProperty("RBAC property_update")
        .build();

    Properties props = new Properties();
    props.setProperty("description", "foo_update");
    props.setProperty("mode", String.valueOf(TagMode.OPERATIONAL.ordinal()));
    props.setProperty("hardwareAddress", new PLCHardwareAddressImpl(2, 2, 2, 2, 2, 2.0f, "testAddress_update").toConfigXML());
    props.setProperty("clientTimeout", String.valueOf(40000));
    props.setProperty("sourceRetries", String.valueOf(3));
    props.setProperty("rbacProperty", "RBAC property_update");

    return new Pair<>(pro, props);
  }

  public static CommandTag buildDeleteCommandTag(Long id) {
    CommandTag pro = CommandTag.builder()
        .id(id)
        .deleted(true)
        .build();

    return pro;
  }

  // ##################### Builder #####################


  public static Pair<CommandTag.CommandTagBuilder, Properties> builderCommandTagWithPrimFields(Long id, Long parentId) {
    CommandTag.CommandTagBuilder pro = CommandTag.builder()
        .id(id)
        .name("CommandTag")
        .description("foo")
        .dataType(DataType.STRING)
        .hardwareAddress(new PLCHardwareAddressImpl(1, 1, 1, 1, 1, 1.0f, "testAddress"))
        .clientTimeout(30000)
        .execTimeout(6000)
        .sourceTimeout(200)
        .sourceRetries(2)
        .rbacClass("RBAC class")
        .rbacDevice("RBAC device")
        .rbacProperty("RBAC property");

    Properties props = new Properties();
    props.setProperty("name", "CommandTag");
    props.setProperty("description", "foo");
    props.setProperty("mode", String.valueOf(TagMode.TEST.ordinal()));
    props.setProperty("dataType", DataType.STRING.toString());
    props.setProperty("hardwareAddress", new PLCHardwareAddressImpl(1, 1, 1, 1, 1, 1.0f, "testAddress").toConfigXML());
    props.setProperty("equipmentId", String.valueOf(parentId));
    props.setProperty("clientTimeout", String.valueOf(30000));
    props.setProperty("execTimeout", String.valueOf(6000));
    props.setProperty("sourceTimeout", String.valueOf(200));
    props.setProperty("sourceRetries", String.valueOf(2));
    props.setProperty("rbacClass", "RBAC class");
    props.setProperty("rbacDevice", "RBAC device");
    props.setProperty("rbacProperty", "RBAC property");

    return new Pair<>(pro, props);
  }

  public static Pair<CommandTag.CommandTagBuilder, Properties> builderCommandTagWithAllFields(Long id, Long parentId) {
    CommandTag.CommandTagBuilder pro = CommandTag.builder()
        .id(id)
        .name("CommandTag")
        .description("foo")
        .mode(TagMode.OPERATIONAL)
        .dataType(DataType.STRING)
        .hardwareAddress(new PLCHardwareAddressImpl(1, 1, 1, 1, 1, 1.0f, "testAddress"))
        .clientTimeout(30000)
        .execTimeout(6000)
        .sourceTimeout(200)
        .sourceRetries(2)
        .rbacClass("RBAC class")
        .rbacDevice("RBAC device")
        .rbacProperty("RBAC property");

    Properties props = new Properties();
    props.setProperty("name", "CommandTag");
    props.setProperty("description", "foo");
    props.setProperty("mode", String.valueOf(TagMode.OPERATIONAL.ordinal()));
    props.setProperty("dataType", DataType.STRING.toString());
    props.setProperty("hardwareAddress", new PLCHardwareAddressImpl(1, 1, 1, 1, 1, 1.0f, "testAddress").toConfigXML());
    props.setProperty("equipmentId", String.valueOf(parentId));
    props.setProperty("clientTimeout", String.valueOf(30000));
    props.setProperty("execTimeout", String.valueOf(6000));
    props.setProperty("sourceTimeout", String.valueOf(200));
    props.setProperty("sourceRetries", String.valueOf(2));
    props.setProperty("rbacClass", "RBAC class");
    props.setProperty("rbacDevice", "RBAC device");
    props.setProperty("rbacProperty", "RBAC property");

    return new Pair<>(pro, props);
  }

  public static Pair<CommandTag.CommandTagBuilder, Properties> builderCommandTagUpdate(Long id) {
    CommandTag.CommandTagBuilder pro = CommandTag.builder()
        .id(id)
        .description("foo_update")
        .mode(TagMode.OPERATIONAL)
        .hardwareAddress(new PLCHardwareAddressImpl(2, 2, 2, 2, 2, 2.0f, "testAddress_update"))
        .clientTimeout(40000)
        .sourceRetries(3)
        .rbacProperty("RBAC property_update");

    Properties props = new Properties();
    props.setProperty("description", "foo_update");
    props.setProperty("mode", String.valueOf(TagMode.OPERATIONAL.ordinal()));
    props.setProperty("hardwareAddress", new PLCHardwareAddressImpl(2, 2, 2, 2, 2, 2.0f, "testAddress_update").toConfigXML());
    props.setProperty("clientTimeout", String.valueOf(40000));
    props.setProperty("sourceRetries", String.valueOf(3));
    props.setProperty("rbacProperty", "RBAC property_update");

    return new Pair<>(pro, props);
  }

}
