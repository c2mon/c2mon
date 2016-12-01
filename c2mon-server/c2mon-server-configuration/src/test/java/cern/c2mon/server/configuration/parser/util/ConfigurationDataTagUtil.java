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

import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.address.impl.PLCHardwareAddressImpl;
import cern.c2mon.shared.common.metadata.Metadata;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import javax.xml.crypto.Data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigurationDataTagUtil {

  private static ObjectMapper mapper = new ObjectMapper();

  /**
   * Expected generated id is 100.
   * Expected parent id is 10.
   */
  public static DataTag buildCreateBasicDataTag(Properties properties) {
    if (properties == null) {
      properties = new Properties();
    }

    DataTag dataTag = DataTag.create("DataTag", Integer.class, new DataTagAddress()).build();
    dataTag.setEquipmentId(10L);

    properties.setProperty("name", "DataTag");
    properties.setProperty("description", "<no description provided>");
    properties.setProperty("mode", String.valueOf(TagMode.OPERATIONAL.ordinal()));
    properties.setProperty("dataType", Integer.class.getName());
    properties.setProperty("isLogged", String.valueOf(true));
    properties.setProperty("equipmentId", String.valueOf(10l));
    properties.setProperty("address", new DataTagAddress().toConfigXML());

    return dataTag;
  }

  /**
   * Expected parent id is 10.
   */
  public static DataTag buildCreateAllFieldsDataTag(Long id, Properties properties) {
    if (properties == null) {
      properties = new Properties();
    }

    DataTag dataTag = DataTag.create("DataTag" + id, Integer.class, new DataTagAddress())
        .id(id)
        .description("foo")
        .mode(TagMode.OPERATIONAL)
        .isLogged(false)
        .minValue(0)
        .maxValue(10)
        .unit("testUnit")
        .addMetadata("testMetadata", 11)
        .build();
    dataTag.setEquipmentId(10L);

    properties.setProperty("name", "DataTag" + id);
    properties.setProperty("description", "foo");
    properties.setProperty("mode", String.valueOf(TagMode.OPERATIONAL.ordinal()));
    properties.setProperty("dataType", Integer.class.getName());
    properties.setProperty("isLogged", String.valueOf(false));
    properties.setProperty("minValue", String.valueOf(0));
    properties.setProperty("maxValue", String.valueOf(10));
    properties.setProperty("address", new DataTagAddress().toConfigXML());
    properties.setProperty("equipmentId", String.valueOf(10l));
    properties.setProperty("unit", "testUnit");
    Metadata metadata = new Metadata();
    metadata.addMetadata("testMetadata", 11);
    properties.setProperty("metadata", getJsonMetadata(metadata));

    return dataTag;
  }

  public static DataTag buildCreateMultiMetaDataTag(Properties properties) {
    if (properties == null) {
      properties = new Properties();
    }

    DataTag dataTag = DataTag.create("DataTag", Integer.class, new DataTagAddress())
        .addMetadata("testMetadata1", 66)
        .addMetadata("testMetadata1", 11)
        .addMetadata("testMetadata2", 22)
        .build();
    dataTag.setEquipmentId(10L);

    properties.setProperty("name", "DataTag");
    properties.setProperty("description", "<no description provided>");
    properties.setProperty("mode", String.valueOf(TagMode.OPERATIONAL.ordinal()));
    properties.setProperty("dataType", Integer.class.getName());
    properties.setProperty("isLogged", String.valueOf(true));
    properties.setProperty("equipmentId", String.valueOf(10L));
    properties.setProperty("address", new DataTagAddress().toConfigXML());
    Metadata metadata = new Metadata();
    metadata.addMetadata("testMetadata1", 11);
    metadata.addMetadata("testMetadata2", 22);
    properties.setProperty("metadata", getJsonMetadata(metadata));

    return dataTag;
  }

  public static DataTag buildCreateSingleMetaDataTag(Long id, Properties properties) {
    if (properties == null) {
      properties = new Properties();
    }

    DataTag dataTag = DataTag.create("DataTag" + id, Integer.class, new DataTagAddress())
            .addMetadata("testMetadata", 11)
            .build();
    dataTag.setEquipmentId(10L);

    properties.setProperty("name", "DataTag" + id);
    properties.setProperty("description", "<no description provided>");
    properties.setProperty("mode", String.valueOf(TagMode.OPERATIONAL.ordinal()));
    properties.setProperty("dataType", Integer.class.getName());
    properties.setProperty("isLogged", String.valueOf(true));
    properties.setProperty("equipmentId", String.valueOf(10l));
    properties.setProperty("address", new DataTagAddress().toConfigXML());
    Metadata metadata = new Metadata();
    metadata.addMetadata("testMetadata", 11);
    properties.setProperty("metadata", getJsonMetadata(metadata));

    return dataTag;
  }

  public static DataTag buildAddSingleMetaDataTag(Long id, Properties properties) {
    if (properties == null) {
      properties = new Properties();
    }

    DataTag dataTag = DataTag.update(id)
        .updateMetadata("testMetadata", 11)
        .build();
    dataTag.setEquipmentId(10L);

    properties.setProperty("equipmentId", String.valueOf(10l));
    Metadata metadata = new Metadata();
    metadata.addMetadata("testMetadata", 11);
    metadata.setUpdate(true);
    properties.setProperty("metadata", getJsonMetadata(metadata));

    return dataTag;
  }

  public static DataTag buildAddMultiMetaDataTag(Long id, Properties properties) {
    if (properties == null) {
      properties = new Properties();
    }

    DataTag dataTag = DataTag.update(id)
        .updateMetadata("testMetadata1", 33)
        .updateMetadata("testMetadata2", 22)
        .updateMetadata("testMetadata1", 11)
        .build();

    Metadata metadata = new Metadata();
    metadata.addMetadata("testMetadata1", 11);
    metadata.addMetadata("testMetadata2", 22);
    metadata.setUpdate(true);
    properties.setProperty("metadata", getJsonMetadata(metadata));

    return dataTag;
  }

  public static DataTag buildRemoveSingleMetaDataTag(Long id, Properties properties) {
    if (properties == null) {
      properties = new Properties();
    }

    DataTag dataTag = DataTag.update(id)
        .removeMetadata("testMetadata")
        .build();

    Metadata metadata = new Metadata();
    metadata.setRemoveList(Collections.singletonList("testMetadata"));
    properties.setProperty("metadata", getJsonMetadata(metadata));

    return dataTag;
  }

  public static DataTag buildRemoveMultiMetaDataTag(Long id, Properties properties) {
    if (properties == null) {
      properties = new Properties();
    }
    DataTag dataTag = DataTag.update(id)
        .removeMetadata("testMetadata1")
        .removeMetadata("testMetadata2")
        .build();

    Metadata metadata = new Metadata();
    metadata.setRemoveList(Arrays.asList("testMetadata1", "testMetadata2"));
    properties.setProperty("metadata", getJsonMetadata(metadata));

    return dataTag;
  }

  public static DataTag buildUpdateDataTagWithAllFields(Long id, Properties properties) {
    if (properties == null) {
      properties = new Properties();
    }

    DataTag dataTag = DataTag.update(id)
        .unit("updateUnit")
        .name("updateName")
        .description("foo_Update")
        .mode(TagMode.OPERATIONAL)
        .dataType(Double.class)
        .isLogged(true)
        .minValue(1)
        .maxValue(11)
        .address(new DataTagAddress(new PLCHardwareAddressImpl(2, 2, 2, 2, 2, 2.0f, "testAddress_Update")))
        .updateMetadata("testMetadata_Update", true)
        .build();

    properties.setProperty("name", "updateName");
    properties.setProperty("unit", "updateUnit");
    properties.setProperty("description", "foo_Update");
    properties.setProperty("mode", String.valueOf(TagMode.OPERATIONAL.ordinal()));
    properties.setProperty("dataType", Double.class.getName());
    properties.setProperty("isLogged", String.valueOf(true));
    properties.setProperty("minValue", String.valueOf(1));
    properties.setProperty("maxValue", String.valueOf(11));
    properties.setProperty("address", new DataTagAddress(new PLCHardwareAddressImpl(2, 2, 2, 2, 2, 2.0f, "testAddress_Update")).toConfigXML());
    Metadata metadata = new Metadata();
    metadata.addMetadata("testMetadata_Update", true);
    metadata.setUpdate(true);
    properties.setProperty("metadata", getJsonMetadata(metadata));

    return dataTag;
  }

  public static DataTag buildUpdateDataTagWithSomeFields(Long id, Properties properties) {
    if (properties == null) {
      properties = new Properties();
    }

    DataTag dataTag = DataTag.update(id)
        .description("foo_Update")
        .minValue(1)
        .maxValue(11)
        .build();

    properties.setProperty("description", "foo_Update");
    properties.setProperty("minValue", String.valueOf(1));
    properties.setProperty("maxValue", String.valueOf(11));

    return dataTag;
  }

  public static DataTag buildUpdateDataTagWithSomeFields(String name, Properties properties) {
    if (properties == null) {
      properties = new Properties();
    }
    DataTag dataTag = DataTag.update(name)
        .description("foo_Update")
        .minValue(1)
        .maxValue(11)
        .build();

    properties.setProperty("name", name);
    properties.setProperty("description", "foo_Update");
    properties.setProperty("minValue", String.valueOf(1));
    properties.setProperty("maxValue", String.valueOf(11));

    return dataTag;
  }

  public static DataTag buildDeleteDataTag(Long id) {
    DataTag deleteTag = new DataTag();
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
