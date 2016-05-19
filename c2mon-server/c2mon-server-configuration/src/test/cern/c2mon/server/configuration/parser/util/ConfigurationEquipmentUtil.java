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

import java.util.Properties;

import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.tag.AliveTag;
import cern.c2mon.shared.client.configuration.api.tag.CommFaultTag;
import cern.c2mon.shared.client.configuration.api.tag.StatusTag;

//@Service
public class ConfigurationEquipmentUtil {


  public static Pair<Equipment, Properties> buildEquipmentWtihId(Long id) {
    return new Pair<>(Equipment.builder().id(id).build(), new Properties());
  }

  public static Pair<Equipment, Properties> buildEquipmentWithPrimFields(Long id) {
    Equipment pro = Equipment.builder()
        .id(id)
        .name("Equipment")
        .description("foo")
        .statusTag(StatusTag.builder().id(0l).name("").description("").build())
        .commFaultTag(CommFaultTag.builder().id(2l).name("").description("").build())
        .handlerClass("cern.c2mon.driver.")
        .build();

    Properties props = new Properties();
    props.setProperty("name", "Equipment");
    props.setProperty("description", "foo");
    props.setProperty("stateTagId", String.valueOf(0l));
    props.setProperty("commFaultTagId", String.valueOf(2l));
    props.setProperty("aliveInterval", String.valueOf(60000));
    props.setProperty("processId", String.valueOf(1l));
    props.setProperty("handlerClass", "cern.c2mon.driver.");

    return new Pair<>(pro, props);
  }

  public static Pair<Equipment, Properties> buildEquipmentWithAllFields(Long id) {
    Equipment pro = Equipment.builder()
        .id(id)
        .name("Equipment")
        .description("foo")
        .statusTag(StatusTag.builder().id(0l).name("").description("").build())
        .aliveInterval(60000)
        .aliveTag(AliveTag.builder().id(1l).name("").description("").build())
        .handlerClass("testHandler")
        .commFaultTag(CommFaultTag.builder().id(2l).name("").description("").build())
        .address("testAddress")
        .build();

    Properties props = new Properties();
    props.setProperty("name", "Equipment");
    props.setProperty("description", "foo");
    props.setProperty("stateTagId", String.valueOf(0l));
    props.setProperty("aliveInterval", String.valueOf(60000));
    props.setProperty("aliveTagId", String.valueOf(1l));
    props.setProperty("handlerClass", "testHandler");
    props.setProperty("commFaultTagId", String.valueOf(2l));
    props.setProperty("address", "testAddress");
    props.setProperty("processId", String.valueOf(1l));
    return new Pair<>(pro, props);
  }

  public static Pair<Equipment, Properties> buildEquipmentWithoutDefaultFields(Long id) {
    Equipment pro = Equipment.builder()
        .id(id)
        .name("Equipment")
        .description("foo")
        .statusTag(StatusTag.builder().id(0l).name("").description("").build())
        .commFaultTag(CommFaultTag.builder().id(2l).name("").description("").build())
        .handlerClass("cern.c2mon.driver.")
        .build();

    Properties props = new Properties();
    props.setProperty("name", "Equipment");
    props.setProperty("description", "foo");
    props.setProperty("stateTagId", String.valueOf(0l));
    props.setProperty("commFaultTagId", String.valueOf(2l));
    props.setProperty("aliveInterval", String.valueOf(60000));
    props.setProperty("processId", String.valueOf(1l));
    props.setProperty("handlerClass", "cern.c2mon.driver.");

    return new Pair<>(pro, props);
  }

  public static Equipment buildUpdateEquipmentNewControlTag(Long id) {
    return Equipment.builder()
        .id(0l)
        .aliveTag(AliveTag.builder().id(1l).name("").description("").build())
        .build();
  }

  public static Pair<Equipment, Properties> buildUpdateEquipmentWithAllFields(Long id) {
    Equipment pro = Equipment.builder()
        .id(id)
        .name("Equipment_Update")
        .description("foo_Update")
        .aliveInterval(100000)
        .handlerClass("testHandler_Update")
        .address("testAddress_Update")
        .build();

    Properties props = new Properties();
    props.setProperty("name", "Equipment_Update");
    props.setProperty("description", "foo_Update");
    props.setProperty("aliveInterval", String.valueOf(100000));
    props.setProperty("handlerClass", "testHandler_Update");
    props.setProperty("address", "testAddress_Update");

    return new Pair<>(pro, props);
  }

  public static Pair<Equipment, Properties> buildUpdateEquipmentWithSomeFields(Long id) {
    Equipment pro = Equipment.builder()
        .id(id)
        .description("foo_Update")
        .address("testAddress_Update")
        .handlerClass("handlerClassName")
        .build();

    Properties props = new Properties();
    props.setProperty("description", "foo_Update");
    props.setProperty("address", "testAddress_Update");
    props.setProperty("handlerClass", "handlerClassName");

    return new Pair<>(pro, props);
  }

  public static Equipment buildDeleteEquipment(Long id) {
    Equipment pro = Equipment.builder()
        .id(id)
        .deleted(true)
        .build();

    return pro;
  }

  // ##################### Builder #####################

  public static Pair<Equipment.EquipmentBuilder, Properties> builderEquipmentWithPrimFields(Long id, Long parentId, Long statusTagId, Long aliveTagId, Long commFaultId) {
    Equipment.EquipmentBuilder pro = Equipment.builder()
        .id(id)
        .name("Equipment")
        .handlerClass("cern.c2mon.driver.")
        .description("foo");

    Properties props = new Properties();
    props.setProperty("name", "Equipment");
    props.setProperty("description", "foo");
    props.setProperty("aliveInterval", String.valueOf(60000));
    props.setProperty("processId", String.valueOf(parentId));
    props.setProperty("stateTagId", String.valueOf(statusTagId));
    props.setProperty("commFaultTagId", String.valueOf(commFaultId));
    props.setProperty("handlerClass", "cern.c2mon.driver.");
    if (aliveTagId != null) {
      props.setProperty("aliveTagId", String.valueOf(aliveTagId));
    }
    return new Pair<>(pro, props);
  }

  public static Pair<Equipment.EquipmentBuilder, Properties> builderEquipmentWithAllFields(Long id, Long parentId, Long statusTagId, Long commFaultId, Long aliveTagId) {
    Equipment.EquipmentBuilder pro = Equipment.builder()
        .id(id)
        .name("Equipment")
        .description("foo")
        .aliveInterval(60000)
        .handlerClass("cern.c2mon.driver.")
        .address("testAddress");

    Properties props = new Properties();
    props.setProperty("name", "Equipment");
    props.setProperty("description", "foo");
    props.setProperty("handlerClass", "cern.c2mon.driver.");
    props.setProperty("aliveInterval", String.valueOf(60000));
    props.setProperty("address", "testAddress");
    props.setProperty("processId", String.valueOf(parentId));
    props.setProperty("stateTagId", String.valueOf(statusTagId));
    props.setProperty("aliveTagId", String.valueOf(aliveTagId));
    props.setProperty("commFaultTagId", String.valueOf(commFaultId));
    return new Pair<>(pro, props);
  }

  public static Pair<Equipment.EquipmentBuilder, Properties> builderEquipmentUpdate(Long id) {
    Equipment.EquipmentBuilder pro = Equipment.builder()
        .id(id)
        .description("foo_update")
        .address("testAddress_update");

    Properties props = new Properties();
    props.setProperty("description", "foo_update");
    props.setProperty("address", "testAddress_update");
    return new Pair<>(pro, props);
  }

}
