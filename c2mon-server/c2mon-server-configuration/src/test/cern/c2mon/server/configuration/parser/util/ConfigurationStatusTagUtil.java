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

import cern.c2mon.shared.client.configuration.api.tag.StatusTag;
import cern.c2mon.shared.client.configuration.api.util.DataType;
import cern.c2mon.shared.client.tag.TagMode;

import java.util.Properties;

//@Service
public class ConfigurationStatusTagUtil {


  public static Pair<StatusTag, Properties> buildStatusTagWithId(Long id) {
    return new Pair<>(StatusTag.builder().id(id).build(), new Properties());
  }

  public static Pair<StatusTag, Properties> buildStatusTagWithPrimFields(Long id) {
    StatusTag pro = StatusTag.builder()
        .id(id)
        .name("StatusTag")
        .description("foo")
        .build();

    Properties props = new Properties();
    props.setProperty("name", "StatusTag");
    props.setProperty("description", "foo");
    props.setProperty("mode", String.valueOf(TagMode.TEST.ordinal()));
    props.setProperty("dataType", DataType.STRING.toString());
    props.setProperty("isLogged", String.valueOf(true));
    props.setProperty("processId", String.valueOf(1l));

    return new Pair<>(pro, props);
  }

  public static Pair<StatusTag, Properties> buildStatusTagWithAllFields(Long id) {
    StatusTag pro = StatusTag.builder()
        .id(id)
        .name("StatusTag")
        .description("foo")
        .mode(TagMode.TEST)
        .isLogged(false)
        .build();

    Properties props = new Properties();
    props.setProperty("name", "StatusTag");
    props.setProperty("description", "foo");
    props.setProperty("mode", String.valueOf(TagMode.TEST.ordinal()));
    props.setProperty("dataType", DataType.STRING.toString());
    props.setProperty("isLogged", String.valueOf(false));
    props.setProperty("processId", String.valueOf(1l));

    return new Pair<>(pro, props);
  }

  public static Pair<StatusTag, Properties> buildStatusTagWithoutDefaultFields(Long id) {
    StatusTag pro = StatusTag.builder()
        .id(id)
        .name("StatusTag")
        .description("foo")
        .build();

    Properties props = new Properties();
    props.setProperty("name", "StatusTag");
    props.setProperty("description", "foo");
    props.setProperty("mode", String.valueOf(TagMode.TEST.ordinal()));
    props.setProperty("dataType", DataType.STRING.toString());
    props.setProperty("isLogged", String.valueOf(true));
    props.setProperty("processId", String.valueOf(1l));

    return new Pair<>(pro, props);
  }

  public static Pair<StatusTag, Properties> buildUpdateStatusTagWithAllFields(Long id) {
    StatusTag pro = StatusTag.builder()
        .id(id)
        .name("StatusTag_Update")
        .description("foo_Update")
        .mode(TagMode.OPERATIONAL)
        .isLogged(true)
        .build();

    Properties props = new Properties();
    props.setProperty("name", "StatusTag_Update");
    props.setProperty("description", "foo_Update");
    props.setProperty("mode", String.valueOf(TagMode.OPERATIONAL.ordinal()));
    props.setProperty("isLogged", String.valueOf(true));

    return new Pair<>(pro, props);
  }

  public static Pair<StatusTag, Properties> buildUpdateStatusTagWithSomeFields(Long id) {
    StatusTag pro = StatusTag.builder()
        .id(id)
        .description("foo_Update")
        .build();

    Properties props = new Properties();
    props.setProperty("description", "foo_Update");

    return new Pair<>(pro, props);
  }

  // ##################### Builder #####################

  public static Pair<StatusTag.StatusTagBuilder, Properties> builderStatusTagWithPrimFields(Long id, String parent, Long parentId) {
    StatusTag.StatusTagBuilder pro = StatusTag.builder()
        .id(id)
        .name("StatusTag"+parent)
        .description("foo");

    Properties props = new Properties();
    props.setProperty("name", "StatusTag"+parent);
    props.setProperty("description", "foo");
    props.setProperty("mode", String.valueOf(TagMode.TEST.ordinal()));
    props.setProperty("dataType", DataType.STRING.toString());
    props.setProperty("isLogged", String.valueOf(true));
    switch(parent){
      case "process" : props.setProperty("processId", String.valueOf(parentId)); break;
      case "equipment" : props.setProperty("equipmentId", String.valueOf(parentId)); break;
      case "subEquipment" : props.setProperty("subEquipmentId", String.valueOf(parentId)); break;
    }

    return new Pair<>(pro, props);
  }

  public static Pair<StatusTag.StatusTagBuilder, Properties> builderStatusTagWithAllFields(Long id, String parent, Long parentId) {
    StatusTag.StatusTagBuilder pro = StatusTag.builder()
        .id(id)
        .name("StatusTag"+parent)
        .description("foo")
        .mode(TagMode.TEST)
        .isLogged(false);

    Properties props = new Properties();
    props.setProperty("name", "StatusTag"+parent);
    props.setProperty("description", "foo");
    props.setProperty("mode", String.valueOf(TagMode.TEST.ordinal()));
    props.setProperty("dataType", DataType.STRING.toString());
    props.setProperty("isLogged", String.valueOf(false));
    switch(parent){
      case "process" : props.setProperty("processId", String.valueOf(parentId)); break;
      case "equipment" : props.setProperty("equipmentId", String.valueOf(parentId)); break;
      case "subEquipment" : props.setProperty("subEquipmentId", String.valueOf(parentId)); break;
    }

    return new Pair<>(pro, props);
  }

  public static Pair<StatusTag.StatusTagBuilder, Properties> builderStatusTagUpdate(Long id) {
    StatusTag.StatusTagBuilder pro = StatusTag.builder()
        .id(id)
        .description("foo_update")
        .isLogged(true);

    Properties props = new Properties();
    props.setProperty("description", "foo_update");
    props.setProperty("isLogged", String.valueOf(true));

    return new Pair<>(pro, props);
  }
}
