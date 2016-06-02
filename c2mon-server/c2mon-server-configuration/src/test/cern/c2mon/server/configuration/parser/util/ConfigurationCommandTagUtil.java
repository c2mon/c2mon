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
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.common.datatag.address.impl.PLCHardwareAddressImpl;
import cern.c2mon.shared.common.datatag.address.impl.SimpleHardwareAddressImpl;
import cern.c2mon.shared.common.metadata.Metadata;

import java.util.Properties;

public class ConfigurationCommandTagUtil {

  /**
   * Expected generated id is 500.
   * Expected parent id is 10.
   */
  public static CommandTag buildCreateBasicCommandTag(Properties properties) {
    if (properties == null) {
      properties = new Properties();
    }

    CommandTag commandTag = CommandTag.create("CommandTag", Integer.class, new SimpleHardwareAddressImpl("testAddress"),
        30000, 6000, 200, 2, "RBAC class", "RBAC device", "RBAC property").equipmentId(10L).build();

    properties.setProperty("name", "CommandTag");
    properties.setProperty("description", "<no description provided>");
    properties.setProperty("mode", String.valueOf(TagMode.TEST.ordinal()));
    properties.setProperty("dataType", Integer.class.getName());
    properties.setProperty("hardwareAddress", new SimpleHardwareAddressImpl("testAddress").toConfigXML());
    properties.setProperty("equipmentId", String.valueOf(10l));
    properties.setProperty("clientTimeout", String.valueOf(30000));
    properties.setProperty("execTimeout", String.valueOf(6000));
    properties.setProperty("sourceTimeout", String.valueOf(200));
    properties.setProperty("sourceRetries", String.valueOf(2));
    properties.setProperty("rbacClass", "RBAC class");
    properties.setProperty("rbacDevice", "RBAC device");
    properties.setProperty("rbacProperty", "RBAC property");

    return commandTag;
  }

  /**
   * Expected parent id is 10.
   */
  public static CommandTag buildCreateAllFieldsCommandTag(Long id, Properties properties) {
    if (properties == null) {
      properties = new Properties();
    }

    CommandTag commandTag = CommandTag.create("CommandTag" + id, Integer.class, new SimpleHardwareAddressImpl("testAddress"),
        30000 ,6000, 200, 2, "RBAC class", "RBAC device", "RBAC property")
        .id(id)
        .equipmentId(10L)
        .description("foo")
        .mode(TagMode.OPERATIONAL)
        .metadata(Metadata.builder().addMetadata("testMetadata", 11).build())
        .maximum(100)
        .minimum(0)
        .build();

    properties.setProperty("name", "CommandTag" + id);
    properties.setProperty("description", "foo");
    properties.setProperty("mode", String.valueOf(TagMode.OPERATIONAL.ordinal()));
    properties.setProperty("dataType", Integer.class.getName());
    properties.setProperty("metadata", Metadata.toJSON(Metadata.builder().addMetadata("testMetadata", 11).build()));
    properties.setProperty("hardwareAddress", new SimpleHardwareAddressImpl("testAddress").toConfigXML());
    properties.setProperty("equipmentId", String.valueOf(10l));
    properties.setProperty("clientTimeout", String.valueOf(30000));
    properties.setProperty("execTimeout", String.valueOf(6000));
    properties.setProperty("sourceTimeout", String.valueOf(200));
    properties.setProperty("sourceRetries", String.valueOf(2));
    properties.setProperty("rbacClass", "RBAC class");
    properties.setProperty("rbacDevice", "RBAC device");
    properties.setProperty("rbacProperty", "RBAC property");
    properties.setProperty("maxValue", "100");
    properties.setProperty("minValue", "0");

    return commandTag;
  }

  public static CommandTag buildUpdateCommandTagWithAllFields(Long id, Properties properties) {
    if (properties == null) {
      properties = new Properties();
    }

    CommandTag commandTag = CommandTag.update(id)
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
        .maximum(200)
        .minimum(20)
        .build();

    properties.setProperty("name", "CommandTag_update");
    properties.setProperty("description", "foo_update");
    properties.setProperty("mode", String.valueOf(TagMode.OPERATIONAL.ordinal()));
    properties.setProperty("hardwareAddress", new PLCHardwareAddressImpl(1, 1, 1, 1, 1, 1.0f, "testAddress_update").toConfigXML());
    properties.setProperty("clientTimeout", String.valueOf(40000));
    properties.setProperty("execTimeout", String.valueOf(7000));
    properties.setProperty("sourceTimeout", String.valueOf(300));
    properties.setProperty("sourceRetries", String.valueOf(3));
    properties.setProperty("rbacClass", "RBAC class_update");
    properties.setProperty("rbacDevice", "RBAC device_update");
    properties.setProperty("rbacProperty", "RBAC property_update");
    properties.setProperty("maxValue", "200");
    properties.setProperty("minValue", "20");

    return commandTag;
  }

  public static CommandTag buildUpdateCommandTagWithSomeFields(Long id, Properties properties) {
    if (properties == null) {
      properties = new Properties();
    }

    CommandTag commandTag = CommandTag.update(id)
        .description("foo_update")
        .mode(TagMode.OPERATIONAL)
        .hardwareAddress(new PLCHardwareAddressImpl(2, 2, 2, 2, 2, 2.0f, "testAddress_update"))
        .clientTimeout(40000)
        .sourceRetries(3)
        .rbacProperty("RBAC property_update")
        .build();

    properties.setProperty("description", "foo_update");
    properties.setProperty("mode", String.valueOf(TagMode.OPERATIONAL.ordinal()));
    properties.setProperty("hardwareAddress", new PLCHardwareAddressImpl(2, 2, 2, 2, 2, 2.0f, "testAddress_update").toConfigXML());
    properties.setProperty("clientTimeout", String.valueOf(40000));
    properties.setProperty("sourceRetries", String.valueOf(3));
    properties.setProperty("rbacProperty", "RBAC property_update");

    return commandTag;
  }

  public static CommandTag buildUpdateCommandTagWithSomeFields(String name, Properties properties) {
    if (properties == null) {
      properties = new Properties();
    }
    CommandTag commandTag = CommandTag.update(name)
        .description("foo_update")
        .mode(TagMode.OPERATIONAL)
        .hardwareAddress(new PLCHardwareAddressImpl(2, 2, 2, 2, 2, 2.0f, "testAddress_update"))
        .clientTimeout(40000)
        .sourceRetries(3)
        .rbacProperty("RBAC property_update")
        .minimum(20)
        .build();

    properties.setProperty("description", "foo_update");
    properties.setProperty("mode", String.valueOf(TagMode.OPERATIONAL.ordinal()));
    properties.setProperty("hardwareAddress", new PLCHardwareAddressImpl(2, 2, 2, 2, 2, 2.0f, "testAddress_update").toConfigXML());
    properties.setProperty("clientTimeout", String.valueOf(40000));
    properties.setProperty("sourceRetries", String.valueOf(3));
    properties.setProperty("rbacProperty", "RBAC property_update");
    properties.setProperty("minValue", "20");

    return commandTag;
  }

  public static CommandTag buildDeleteCommandTag(Long id) {
    CommandTag deleteTag = new CommandTag();
    deleteTag.setId(id);
    deleteTag.setDeleted(true);

    return deleteTag;
  }

}
