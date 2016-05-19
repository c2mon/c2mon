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

import cern.c2mon.shared.client.configuration.api.equipment.SubEquipment;
import cern.c2mon.shared.client.configuration.api.tag.AliveTag;
import cern.c2mon.shared.client.configuration.api.tag.CommFaultTag;
import cern.c2mon.shared.client.configuration.api.tag.StatusTag;

import java.util.Properties;

//@Service
public class ConfigurationSubEquipmentUtil {


  public static Pair<SubEquipment, Properties> buildSubEquipmentWithId(Long id) {
    return new Pair<>(SubEquipment.builder().id(id).build(), new Properties());
  }

  public static Pair<SubEquipment, Properties> buildSubEquipmentWtihPrimFields(Long id) {
    SubEquipment pro = SubEquipment.builder()
        .id(id)
        .name("SubEquipment")
        .description("foo")
        .handlerClass("cern.c2mon.driver.")
        .statusTag(StatusTag.builder().id(0l).name("").description("").build())
        .commFaultTag(CommFaultTag.builder().id(2l).name("").description("").build())
        .build();

    Properties props = new Properties();
    props.setProperty("name", "SubEquipment");
    props.setProperty("description", "foo");
    props.setProperty("stateTagId", String.valueOf(0l));
    props.setProperty("commFaultTagId", String.valueOf(2l));
    props.setProperty("aliveInterval", String.valueOf(60000));
    props.setProperty("equipmentId", String.valueOf(1l));
    props.setProperty("handlerClass", "cern.c2mon.driver.");

    return new Pair<>(pro, props);
  }

  public static Pair<SubEquipment, Properties> buildSubEquipmentWithAllFields(Long id) {
    SubEquipment pro = SubEquipment.builder()
        .id(id)
        .name("SubEquipment")
        .description("foo")
        .statusTag(StatusTag.builder().id(0l).name("").description("").build())
        .aliveInterval(60000)
        .aliveTag(AliveTag.builder().id(1l).name("").description("").build())
        .handlerClass("testHandler")
        .commFaultTag(CommFaultTag.builder().id(2l).name("").description("").build())
        .build();

    Properties props = new Properties();
    props.setProperty("name", "SubEquipment");
    props.setProperty("description", "foo");
    props.setProperty("stateTagId", String.valueOf(0l));
    props.setProperty("aliveInterval", String.valueOf(60000));
    props.setProperty("aliveTagId", String.valueOf(1l));
    props.setProperty("handlerClass", "testHandler");
    props.setProperty("commFaultTagId", String.valueOf(2l));
    props.setProperty("equipmentId", String.valueOf(1l));

    return new Pair<>(pro, props);
  }

  public static Pair<SubEquipment, Properties> buildSubEquipmentWithoutDefaultFields(Long id) {
    SubEquipment pro = SubEquipment.builder()
        .id(id)
        .name("SubEquipment")
        .description("foo")
        .handlerClass("cern.c2mon.driver.")
        .statusTag(StatusTag.builder().id(0l).name("").description("").build())
        .commFaultTag(CommFaultTag.builder().id(2l).name("").description("").build())
        .build();

    Properties props = new Properties();
    props.setProperty("name", "SubEquipment");
    props.setProperty("description", "foo");
    props.setProperty("stateTagId", String.valueOf(0l));
    props.setProperty("commFaultTagId", String.valueOf(2l));
    props.setProperty("aliveInterval", String.valueOf(60000));
    props.setProperty("equipmentId", String.valueOf(1l));
    props.setProperty("handlerClass", "cern.c2mon.driver.");

    return new Pair<>(pro, props);
  }

  public static SubEquipment buildUpdateSubEquipmentNewControlTag(Long id) {
    return SubEquipment.builder()
        .id(0l)
        .aliveTag(AliveTag.builder().id(1l).name("").description("").build())
        .build();
  }

  public static Pair<SubEquipment, Properties> buildUpdateSubEquipmentWithAllFields(Long id) {
    SubEquipment pro = SubEquipment.builder()
        .id(id)
        .name("SubEquipment_Update")
        .description("foo_Update")
        .aliveInterval(100000)
        .handlerClass("cern.c2mon.driver.")
        .build();

    Properties props = new Properties();
    props.setProperty("name", "SubEquipment_Update");
    props.setProperty("description", "foo_Update");
    props.setProperty("aliveInterval", String.valueOf(100000));
    props.setProperty("handlerClass", "cern.c2mon.driver.");

    return new Pair<>(pro, props);
  }

  public static Pair<SubEquipment, Properties> buildUpdateSubEquipmentWithSomeFields(Long id) {
    SubEquipment pro = SubEquipment.builder()
        .id(id)
        .description("foo_Update")
        .handlerClass("cern.c2mon.driver.")
        .build();

    Properties props = new Properties();
    props.setProperty("description", "foo_Update");
    props.setProperty("handlerClass", "cern.c2mon.driver.");

    return new Pair<>(pro, props);
  }

  public static SubEquipment buildDeleteSubEquipment(Long id) {
    SubEquipment pro = SubEquipment.builder()
        .id(id)
        .deleted(true)
        .build();

    return pro;
  }
  // ##################### Builder #####################

  public static Pair<SubEquipment.SubEquipmentBuilder, Properties> builderSubEquipmentWithPrimFields(Long id, Long parentId, Long statusTagId, Long aliveTagId, Long commFaultId) {
    SubEquipment.SubEquipmentBuilder pro = SubEquipment.builder()
        .id(id)
        .name("SubEquipment")
        .description("foo")
        .handlerClass("cern.c2mon.driver.")
        .statusTag(StatusTag.builder().id(0l).name("").description("").build())
        .commFaultTag(CommFaultTag.builder().id(1l).name("").description("").build());

    Properties props = new Properties();
    props.setProperty("name", "SubEquipment");
    props.setProperty("description", "foo");
    props.setProperty("aliveInterval", String.valueOf(60000));
    props.setProperty("equipmentId", String.valueOf(parentId));
    props.setProperty("stateTagId", String.valueOf(statusTagId));
    props.setProperty("commFaultTagId", String.valueOf(commFaultId));
    props.setProperty("handlerClass", "cern.c2mon.driver.");
    if (aliveTagId != null) {
      props.setProperty("aliveTagId", String.valueOf(aliveTagId));
    }
    return new Pair<>(pro, props);
  }

  public static Pair<SubEquipment.SubEquipmentBuilder, Properties> builderSubEquipmentWithAllFields(Long id, Long parentId, Long statusTagId, Long commFaultId, Long aliveTagId ) {
    SubEquipment.SubEquipmentBuilder pro = SubEquipment.builder()
        .id(id)
        .name("SubEquipment")
        .description("foo")
        .aliveInterval(60000)
        .handlerClass("cern.c2mon.driver.");

    Properties props = new Properties();
    props.setProperty("name", "SubEquipment");
    props.setProperty("description", "foo");
    props.setProperty("aliveInterval", String.valueOf(60000));
    props.setProperty("handlerClass", "cern.c2mon.driver.");
    props.setProperty("equipmentId", String.valueOf(parentId));
    props.setProperty("stateTagId", String.valueOf(statusTagId));
    props.setProperty("aliveTagId", String.valueOf(aliveTagId));
    props.setProperty("commFaultTagId", String.valueOf(commFaultId));

    return new Pair<>(pro, props);
  }

  public static Pair<SubEquipment.SubEquipmentBuilder, Properties> builderSubEquipmentUpdate(Long id) {
    SubEquipment.SubEquipmentBuilder pro = SubEquipment.builder()
        .id(id)
        .description("foo_update");

    Properties props = new Properties();
    props.setProperty("description", "foo_update");

    return new Pair<>(pro, props);
  }
}
