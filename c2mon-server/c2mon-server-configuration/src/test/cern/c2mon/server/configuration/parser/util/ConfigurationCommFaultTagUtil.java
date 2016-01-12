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

import cern.c2mon.shared.client.configuration.api.tag.CommFaultTag;
import cern.c2mon.shared.client.configuration.api.util.DataType;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.address.impl.PLCHardwareAddressImpl;

//@Service
public class ConfigurationCommFaultTagUtil {


  public static Pair<CommFaultTag, Properties> buildCommFaultTagWithId(Long id) {
    return new Pair<>(CommFaultTag.builder().id(id).build(), new Properties());
  }

  public static Pair<CommFaultTag, Properties> buildCommFaultTagWithPrimFields(Long id) {
    CommFaultTag pro = CommFaultTag.builder()
        .id(id)
        .name("CommFaultTag")
        .description("foo")
        .build();

    Properties props = new Properties();
    props.setProperty("name", "CommFaultTag");
    props.setProperty("description", "foo");
    props.setProperty("mode", String.valueOf(TagMode.TEST.ordinal()));
    props.setProperty("dataType", DataType.BOOLEAN.toString());
    props.setProperty("isLogged", "true");
    props.setProperty("equipmentId", "1");

    return new Pair<>(pro, props);
  }

  public static Pair<CommFaultTag, Properties> buildCommFaultTagWithAllFields(Long id) {
    CommFaultTag pro = CommFaultTag.builder()
        .id(id)
        .name("CommFaultTag")
        .description("foo")
        .mode(TagMode.TEST)
        .isLogged(false)
        .build();

    Properties props = new Properties();
    props.setProperty("name", "CommFaultTag");
    props.setProperty("description", "foo");
    props.setProperty("mode", String.valueOf(TagMode.TEST.ordinal()));
    props.setProperty("dataType", DataType.BOOLEAN.toString());
    props.setProperty("isLogged", "false");
    props.setProperty("equipmentId", "1");

    return new Pair<>(pro, props);
  }

  public static Pair<CommFaultTag, Properties> buildCommFaultTagWithoutDefaultFields(Long id) {
    CommFaultTag pro = CommFaultTag.builder()
        .id(id)
        .name("CommFaultTag")
        .description("foo")
        .build();

    Properties props = new Properties();
    props.setProperty("name", "CommFaultTag");
    props.setProperty("description", "foo");
    props.setProperty("mode", String.valueOf(TagMode.TEST.ordinal()));
    props.setProperty("dataType", DataType.BOOLEAN.toString());
    props.setProperty("isLogged", "true");
    props.setProperty("equipmentId", "1");

    return new Pair<>(pro, props);
  }

  public static Pair<CommFaultTag, Properties> buildUpdateCommFaultTagWithAllFields(Long id) {
    CommFaultTag pro = CommFaultTag.builder()
        .id(id)
        .name("CommFaultTag_Update")
        .description("foo_Update")
        .mode(TagMode.OPERATIONAL)
        .isLogged(true)
        .build();

    Properties props = new Properties();
    props.setProperty("name", "CommFaultTag_Update");
    props.setProperty("description", "foo_Update");
    props.setProperty("mode", String.valueOf(TagMode.OPERATIONAL.ordinal()));
    props.setProperty("isLogged", "true");

    return new Pair<>(pro, props);
  }

  public static Pair<CommFaultTag, Properties> buildUpdateCommFaultTagWithSomeFields(Long id) {
    CommFaultTag pro = CommFaultTag.builder()
        .id(id)
        .description("foo_Update")
        .build();

    Properties props = new Properties();
    props.setProperty("description", "foo_Update");

    return new Pair<>(pro, props);
  }

  // ##################### Builder #####################

  public static Pair<CommFaultTag.CommFaultTagBuilder, Properties> builderCommFaultTagWithPrimFields(Long id, String parent, Long parentId) {
    CommFaultTag.CommFaultTagBuilder pro = CommFaultTag.builder()
        .id(id)
        .name("CommFaultTag"+parent)
        .description("foo");

    Properties props = new Properties();
    props.setProperty("name", "CommFaultTag"+parent);
    props.setProperty("description", "foo");
    props.setProperty("mode", String.valueOf(TagMode.TEST.ordinal()));
    props.setProperty("dataType", DataType.BOOLEAN.toString());
    props.setProperty("isLogged", String.valueOf(true));
    switch(parent){
      case "process" : props.setProperty("processId", String.valueOf(parentId)); break;
      case "equipment" : props.setProperty("equipmentId", String.valueOf(parentId)); break;
      case "subEquipment" : props.setProperty("subEquipmentId", String.valueOf(parentId)); break;
      default: throw new RuntimeException("not such super class given");
    }

    return new Pair<>(pro, props);
  }

  public static Pair<CommFaultTag.CommFaultTagBuilder, Properties> builderCommFaultTagWithAllFields(Long id, String parent, Long parentId) {
    CommFaultTag.CommFaultTagBuilder pro = CommFaultTag.builder()
        .id(id)
        .name("CommFaultTag"+parent)
        .description("foo")
        .mode(TagMode.TEST)
        .isLogged(false)
        .address(new DataTagAddress(new PLCHardwareAddressImpl(1, 1, 1, 1, 1, 1.0f, "testAddress")));

    Properties props = new Properties();
    props.setProperty("name", "CommFaultTag"+parent);
    props.setProperty("description", "foo");
    props.setProperty("mode", String.valueOf(TagMode.TEST.ordinal()));
    props.setProperty("dataType", DataType.BOOLEAN.toString());
    props.setProperty("isLogged", String.valueOf(false));
    props.setProperty("address", new DataTagAddress(new PLCHardwareAddressImpl(1, 1, 1, 1, 1, 1.0f, "testAddress")).toConfigXML());
    switch(parent){
      case "process" : props.setProperty("processId", String.valueOf(parentId)); break;
      case "equipment" : props.setProperty("equipmentId", String.valueOf(parentId)); break;
      case "subEquipment" : props.setProperty("subEquipmentId", String.valueOf(parentId)); break;
      default: throw new RuntimeException("not such super class given");
    }
    return new Pair<>(pro, props);
  }

  public static Pair<CommFaultTag.CommFaultTagBuilder, Properties> builderCommFaultTagUpdate(Long id) {
    CommFaultTag.CommFaultTagBuilder pro = CommFaultTag.builder()
        .id(id)
        .description("foo_update");

    Properties props = new Properties();
    props.setProperty("description", "foo_update");
    return new Pair<>(pro, props);
  }
}
