package cern.c2mon.server.configuration.parser.util;

import java.util.Properties;

import cern.c2mon.shared.client.configuration.api.tag.AliveTag;
import cern.c2mon.shared.client.configuration.api.util.DataType;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.address.impl.PLCHardwareAddressImpl;

//@Service
public class ConfigurationAliveTagUtil {


  public static Pair<AliveTag, Properties> buildAliveTagWtihId(Long id) {
    return new Pair<>(AliveTag.builder().id(id).build(), new Properties());
  }

  public static Pair<AliveTag, Properties> buildAliveTagWithPrimFields(Long id) {
    AliveTag pro = AliveTag.builder()
        .id(id)
        .name("AliveTag")
        .description("foo")
        .build();

    Properties props = new Properties();
    props.setProperty("name", "AliveTag");
    props.setProperty("description", "foo");
    props.setProperty("mode", String.valueOf(TagMode.TEST.ordinal()));
    props.setProperty("dataType", DataType.LONG.toString());
    props.setProperty("isLogged", "false");
    props.setProperty("processId", "1");

    return new Pair<>(pro, props);
  }

  public static Pair<AliveTag, Properties> buildAliveTagWithAllFields(Long id) {
    AliveTag pro = AliveTag.builder()
        .id(id)
        .name("AliveTag")
        .description("foo")
        .mode(TagMode.TEST)
        .address(new DataTagAddress(new PLCHardwareAddressImpl(1, 1, 1, 1, 1, 1.0f, "testAddress")))
        .build();

    Properties props = new Properties();
    props.setProperty("name", "AliveTag");
    props.setProperty("description", "foo");
    props.setProperty("mode", String.valueOf(TagMode.TEST.ordinal()));
    props.setProperty("dataType", DataType.LONG.toString());
    props.setProperty("isLogged", "false");
    props.setProperty("address", new DataTagAddress(new PLCHardwareAddressImpl(1, 1, 1, 1, 1, 1.0f, "testAddress")).toConfigXML());
    props.setProperty("processId", "1");

    return new Pair<>(pro, props);
  }

  public static Pair<AliveTag, Properties> buildAliveTagWithoutDefaultFields(Long id) {
    AliveTag pro = AliveTag.builder()
        .id(id)
        .name("AliveTag")
        .description("foo")
        .build();

    Properties props = new Properties();
    props.setProperty("name", "AliveTag");
    props.setProperty("description", "foo");
    props.setProperty("mode", String.valueOf(TagMode.TEST.ordinal()));
    props.setProperty("dataType", DataType.LONG.toString());
    props.setProperty("isLogged", "false");
    props.setProperty("processId", "1");

    return new Pair<>(pro, props);
  }

  public static Pair<AliveTag, Properties> buildUpdateAliveTagWithAllFields(Long id) {
    AliveTag pro = AliveTag.builder()
        .id(id)
        .name("AliveTag_Update")
        .description("foo_Update")
        .mode(TagMode.OPERATIONAL)
        .address(new DataTagAddress(new PLCHardwareAddressImpl(2, 2, 2, 2, 2, 2.0f, "testAddress_Update")))
        .build();

    Properties props = new Properties();
    props.setProperty("name", "AliveTag_Update");
    props.setProperty("description", "foo_Update");
    props.setProperty("mode", String.valueOf(TagMode.OPERATIONAL.ordinal()));
    props.setProperty("address", new DataTagAddress(new PLCHardwareAddressImpl(2, 2, 2, 2, 2, 2.0f, "testAddress_Update")).toConfigXML());

    return new Pair<>(pro, props);
  }

  public static Pair<AliveTag, Properties> buildUpdateAliveTagWithSomeFields(Long id) {
    AliveTag pro = AliveTag.builder()
        .id(id)
        .description("foo_Update")
        .build();

    Properties props = new Properties();
    props.setProperty("description", "foo_Update");

    return new Pair<>(pro, props);
  }

  // ##################### Builder #####################

  public static Pair<AliveTag.AliveTagBuilder, Properties> builderAliveTagWithPrimFields(Long id, String parent,Long parentId) {
    AliveTag.AliveTagBuilder pro = AliveTag.builder()
        .id(id)
        .name("AliveTag"+parent)
        .description("foo");

    Properties props = new Properties();
    props.setProperty("name", "AliveTag"+parent);
    props.setProperty("description", "foo");
    props.setProperty("mode", String.valueOf(TagMode.TEST.ordinal()));
    props.setProperty("dataType", DataType.LONG.toString());
    props.setProperty("isLogged", "false");
    switch(parent){
      case "process" : props.setProperty("processId", Long.toString(parentId)); break;
      case "equipment" : props.setProperty("equipmentId", Long.toString(parentId)); break;
      case "subEquipment" : props.setProperty("subEquipmentId", Long.toString(parentId)); break;
    }

    return new Pair<>(pro, props);
  }

  public static Pair<AliveTag.AliveTagBuilder, Properties> builderAliveTagWithAllFields(Long id, String parent, Long parentId) {
    AliveTag.AliveTagBuilder pro = AliveTag.builder()
        .id(id)
        .name("AliveTag"+parent)
        .description("foo")
        .mode(TagMode.TEST)
        .address(new DataTagAddress(new PLCHardwareAddressImpl(1, 1, 1, 1, 1, 1.0f, "testAddress")));

    Properties props = new Properties();
    props.setProperty("name", "AliveTag"+parent);
    props.setProperty("description", "foo");
    props.setProperty("mode", String.valueOf(TagMode.TEST.ordinal()));
    props.setProperty("dataType", DataType.LONG.toString());
    props.setProperty("isLogged", "false");
    props.setProperty("address", new DataTagAddress(new PLCHardwareAddressImpl(1, 1, 1, 1, 1, 1.0f, "testAddress")).toConfigXML());
    switch(parent){
      case "process" : props.setProperty("processId", Long.toString(parentId)); break;
      case "equipment" : props.setProperty("equipmentId", Long.toString(parentId)); break;
      case "subEquipment" : props.setProperty("subEquipmentId", Long.toString(parentId)); break;
    }

    return new Pair<>(pro, props);
  }

  public static Pair<AliveTag.AliveTagBuilder, Properties> builderAliveTagUpdate(Long id) {
    AliveTag.AliveTagBuilder pro = AliveTag.builder()
        .id(id)
        .description("foo_update");

    Properties props = new Properties();
    props.setProperty("description", "foo_update");

    return new Pair<>(pro, props);
  }
}
