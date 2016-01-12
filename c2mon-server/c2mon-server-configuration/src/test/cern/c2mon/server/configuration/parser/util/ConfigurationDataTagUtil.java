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

import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import cern.c2mon.shared.client.configuration.api.util.DataType;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.address.impl.PLCHardwareAddressImpl;
import cern.c2mon.shared.common.metadata.Metadata;

//@Service
public class ConfigurationDataTagUtil {


  public static Pair<DataTag<Number>, Properties> buildDataTagWithId(Long id) {
    return new Pair<DataTag<Number>, Properties>(DataTag.<Number>builder().id(id).build(), new Properties());
  }

  public static Pair<DataTag<Number>, Properties> buildDataTagWithPrimFields(Long id) {
    DataTag<Number> pro = DataTag.<Number>builder()
        .id(id)
        .name("DataTag")
        .description("foo")
        .mode(TagMode.OPERATIONAL)
        .dataType(DataType.INTEGER)
        .build();

    Properties props = new Properties();
    props.setProperty("name", "DataTag");
    props.setProperty("description", "foo");
    props.setProperty("mode", String.valueOf(TagMode.OPERATIONAL.ordinal()));
    props.setProperty("dataType", DataType.INTEGER.toString());
    props.setProperty("isLogged", String.valueOf(true));
    props.setProperty("equipmentId", String.valueOf(1l));

    return new Pair<>(pro, props);
  }

  public static Pair<DataTag<Number>, Properties> buildDataTagWithAllFields(Long id) {
    DataTag pro = DataTag.<Number>builder()
        .id(id)
        .name("DataTag")
        .description("foo")
        .mode(TagMode.OPERATIONAL)
        .dataType(DataType.INTEGER)
        .isLogged(false)
        .minValue(0)
        .maxValue(10)
        .address(new DataTagAddress(new PLCHardwareAddressImpl(1, 1, 1, 1, 1, 1.0f, "testAddress")))
        .unit("testUnit")
        .metadata(Metadata.builder().addMetadata("testMetadata",11).build())
        .build();

    Properties props = new Properties();
    props.setProperty("name", "DataTag");
    props.setProperty("description", "foo");
    props.setProperty("mode", String.valueOf(TagMode.OPERATIONAL.ordinal()));
    props.setProperty("dataType", DataType.INTEGER.toString());
    props.setProperty("isLogged", String.valueOf(false));
    props.setProperty("minValue", String.valueOf(0));
    props.setProperty("maxValue", String.valueOf(10));
    props.setProperty("address", new DataTagAddress(new PLCHardwareAddressImpl(1, 1, 1, 1, 1, 1.0f, "testAddress")).toConfigXML());
    props.setProperty("equipmentId", String.valueOf(1l));
    props.setProperty("metadata", Metadata.toJSON(Metadata.builder().addMetadata("testMetadata",11).build()));
    props.setProperty("unit", "testUnit");

    return new Pair<DataTag<Number>, Properties>(pro, props);
  }

  public static Pair<DataTag<Number>, Properties> buildDataTagWithoutDefaultFields(Long id) {
    DataTag<Number> pro = DataTag.<Number>builder()
        .id(id)
        .name("DataTag")
        .description("foo")
        .dataType(DataType.INTEGER)
        .build();

    Properties props = new Properties();
    props.setProperty("name", "DataTag");
    props.setProperty("description", "foo");
    props.setProperty("mode", String.valueOf(TagMode.TEST.ordinal()));
    props.setProperty("dataType", DataType.INTEGER.toString());
    props.setProperty("isLogged", String.valueOf(true));
    props.setProperty("equipmentId", String.valueOf(1l));

    return new Pair<>(pro, props);
  }

  public static Pair<DataTag<Number>, Properties> buildUpdateDataTagWithAllFields(Long id) {
    DataTag<Number> pro = DataTag.<Number>builder()
        .id(id)
        .name("DataTag_Update")
        .description("foo_Update")
        .mode(TagMode.OPERATIONAL)
        .dataType(DataType.DOUBLE)
        .isLogged(true)
        .minValue(1)
        .maxValue(11)
        .address(new DataTagAddress(new PLCHardwareAddressImpl(2, 2, 2, 2, 2, 2.0f, "testAddress_Update")))
        .unit("testUnit_Update")
        .metadata(Metadata.builder().addMetadata("testMetadata_Update",true).build())
        .build();

    Properties props = new Properties();
    props.setProperty("name", "DataTag_Update");
    props.setProperty("description", "foo_Update");
    props.setProperty("mode", String.valueOf(TagMode.OPERATIONAL.ordinal()));
    props.setProperty("dataType", DataType.DOUBLE.toString());
    props.setProperty("isLogged", String.valueOf(true));
    props.setProperty("minValue", String.valueOf(1));
    props.setProperty("maxValue", String.valueOf(11));
    props.setProperty("address", new DataTagAddress(new PLCHardwareAddressImpl(2, 2, 2, 2, 2, 2.0f, "testAddress_Update")).toConfigXML());
    props.setProperty("unit", "testUnit_Update");
    props.setProperty("metadata", Metadata.toJSON(Metadata.builder().addMetadata("testMetadata_Update",true).build()));

    return new Pair<>(pro, props);
  }

  public static Pair<DataTag<Number>, Properties> buildUpdateDataTagWithSomeFields(Long id) {
    DataTag<Number> pro = DataTag.<Number>builder()
        .id(id)
        .name("DataTag_Update")
        .description("foo_Update")
        .minValue(1)
        .maxValue(11)
        .build();

    Properties props = new Properties();
    props.setProperty("name", "DataTag_Update");
    props.setProperty("description", "foo_Update");
    props.setProperty("minValue", String.valueOf(1));
    props.setProperty("maxValue", String.valueOf(11));

    return new Pair<>(pro, props);
  }

  public static DataTag<Number> buildDeleteDataTag(Long id) {
    DataTag<Number> pro = DataTag.<Number>builder()
        .id(id)
        .deleted(true)
        .build();

    return pro;
  }

  // ##################### Builder #####################


  public static Pair<DataTag.DataTagBuilder, Properties> builderDataTagWithPrimFields(Long id, String parent, Long parentId) {
    DataTag.DataTagBuilder pro = DataTag.<Number>builder()
        .id(id)
        .name("DataTag")
        .description("foo")
        .mode(TagMode.OPERATIONAL)
        .dataType(DataType.INTEGER);

    Properties props = new Properties();
    props.setProperty("name", "DataTag");
    props.setProperty("description", "foo");
    props.setProperty("mode", String.valueOf(TagMode.OPERATIONAL.ordinal()));
    props.setProperty("dataType", DataType.INTEGER.toString());
    props.setProperty("isLogged", String.valueOf(true));
    switch(parent){
      case "equipment" : props.setProperty("equipmentId", String.valueOf(parentId)); break;
      case "subEquipment" : props.setProperty("subEquipmentId", String.valueOf(parentId)); break;
      default: throw new RuntimeException("not such super class given");
    }

    return new Pair<>(pro, props);
  }

  public static Pair<DataTag.DataTagBuilder, Properties> builderDataTagWithAllFields(Long id, String parent, Long parentId) {
    DataTag.DataTagBuilder pro = DataTag.<Integer>builder()
        .id(id)
        .name("DataTag")
        .description("foo")
        .mode(TagMode.OPERATIONAL)
        .dataType(DataType.INTEGER)
        .isLogged(false)
        .minValue(0)
        .maxValue(10)
        .address(new DataTagAddress(new PLCHardwareAddressImpl(1, 1, 1, 1, 1, 1.0f, "testAddress")))
        .unit("testUnit")
        .dipAddress("testConfigDIPaddress")
        .japcAddress("testConfigJAPCaddress")
        .metadata(Metadata.builder().addMetadata("testMetadata",11).build());


    Properties props = new Properties();
    props.setProperty("name", "DataTag");
    props.setProperty("description", "foo");
    props.setProperty("mode", String.valueOf(TagMode.OPERATIONAL.ordinal()));
    props.setProperty("dataType", DataType.INTEGER.toString());
    props.setProperty("isLogged", String.valueOf(false));
    props.setProperty("minValue", String.valueOf(0));
    props.setProperty("maxValue", String.valueOf(10));
    props.setProperty("address", new DataTagAddress(new PLCHardwareAddressImpl(1, 1, 1, 1, 1, 1.0f, "testAddress")).toConfigXML());
    props.setProperty("unit", "testUnit");
    props.setProperty("dipAddress", "testConfigDIPaddress");
    props.setProperty("japcAddress", "testConfigJAPCaddress");
    props.setProperty("metadata", Metadata.toJSON(Metadata.builder().addMetadata("testMetadata",11).build()));
    switch(parent){
      case "equipment" : props.setProperty("equipmentId", String.valueOf(parentId)); break;
      case "subEquipment" : props.setProperty("subEquipmentId", String.valueOf(parentId)); break;
      default: throw new RuntimeException("not such super class given");
    }

    return new Pair<>(pro, props);
  }

  public static Pair<DataTag.DataTagBuilder, Properties> builderDataTagUpdate(Long id) {
    DataTag.DataTagBuilder pro = DataTag.<Integer>builder()
        .id(id)
        .description("foo_update")
        .maxValue(20)
        .address(new DataTagAddress(new PLCHardwareAddressImpl(1, 1, 1, 1, 1, 1.0f, "testAddress_update")))
        .japcAddress("testConfigJAPCaddress_update")
        .metadata(Metadata.builder().addMetadata("testMetadata_update",true).build());;


    Properties props = new Properties();
    props.setProperty("description", "foo_update");
    props.setProperty("maxValue", String.valueOf(20));
    props.setProperty("address", new DataTagAddress(new PLCHardwareAddressImpl(1, 1, 1, 1, 1, 1.0f, "testAddress_update")).toConfigXML());
    props.setProperty("japcAddress", "testConfigJAPCaddress_update");
    props.setProperty("metadata", Metadata.toJSON(Metadata.builder().addMetadata("testMetadata_update",true).build()));

    return new Pair<>(pro, props);
  }

}
