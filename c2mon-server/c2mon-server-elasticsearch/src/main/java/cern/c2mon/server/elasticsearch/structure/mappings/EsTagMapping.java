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
package cern.c2mon.server.elasticsearch.structure.mappings;

import java.util.HashMap;
import java.util.Map;

import cern.c2mon.server.elasticsearch.structure.types.tag.EsTag;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import cern.c2mon.shared.common.type.TypeConverter;

/**
 * Allows to create dynamic mappings for the different types that exist in Elasticsearch.
 * Take care of the basic structure requiring the routing for faster retrieval and the body of the properties.
 *
 * @author Alban Marguet.
 */
@Slf4j
@Getter
public class EsTagMapping implements EsMapping {
  protected Routing _routing;
  protected final Properties properties;
  private static transient Gson gson = new GsonBuilder().setPrettyPrinting().create();

  /**
   * Instantiate a new EsTagMapping by putting a routing required.
   * @param esTagType The ES tag type set in {@link EsTag}
   * @param dataType The data type to the corresponding tag
   */
  public  EsTagMapping(String esTagType, String dataType) {
    _routing = new Routing();
    properties = new Properties(esTagType, dataType);
  }

  /**
   * @return the Mapping as JSON.
   */
  @Override
  public String getMapping() {
    String json = gson.toJson(this);
    log.trace("getMapping() - Created the mapping : " + json);
    return json;
  }


  @Getter
  private class Routing {
    final String required = "true";
  }


  /**
   * Properties for a {@link EsTag}
   */
  @Getter
  class Properties {
    Id id;
    Name name;

    /** Numeric value, also used for boolean type */
    Value value;
    /** Field used in addition when data type is Long */
    ValueLong valueLong;
    /** Only used when data type is Boolean */
    ValueBoolean valueBoolean;
    /** Only used when data type is String */
    ValueString valueString;
    /** Used to analyse nested objects */
    ValueObject valueObject;

    /** Valid types are: "number", "boolean", "string", "object" */
    Type type;

    ValueDescription valueDescription;
    Unit unit;
    Quality quality;

    Timestamp timestamp;

    C2monMetadata c2mon;
    Metadata metadata;

    /**
     * @param esTagType The ES tag type set in {@link EsTag}
     * @param dataType The data type to the corresponding tag
     */
    Properties(String esTagType, String dataType) {
      this.id = new Id();
      this.name = new Name();

      switch(esTagType) {
      case EsTag.TYPE_BOOLEAN:
        this.valueBoolean = new ValueBoolean();
        this.value = new Value();
        break;
      case EsTag.TYPE_NUMBER:
        this.value = new Value();
        if (Long.class.isAssignableFrom(TypeConverter.getType(dataType))) {
          this.valueLong = new ValueLong();
        }
        break;
      case EsTag.TYPE_STRING:
        this.valueString = new ValueString();
        break;
      default:
        this.valueObject = new ValueObject();
        break;
      }

      this.type = new Type();
      this.valueDescription = new ValueDescription();
      this.unit = new Unit();
      this.quality = new Quality();

      this.timestamp = new Timestamp();

      this.c2mon = new C2monMetadata();
      this.metadata = new Metadata();
    }

    class Id {
      private final String type = ValueType.LONG.toString();
    }

    class Name {
      private final String type = ValueType.STRING.toString();
      private final String index = indexNotAnalyzed;
    }

    class DataType {
      private String type = ValueType.STRING.toString();
      private final String index = indexNotAnalyzed;
    }

    class Timestamp {
      private final String type = ValueType.DATE.toString();
      private final String format = epochMillisFormat;
    }

    class SourceTimestamp extends Timestamp {
    }

    class ServerTimestamp extends Timestamp {
    }

    class DaqTimestamp extends Timestamp {
    }


    class Quality {
      private final String dynamic = "false";
      private final String type = ValueType.OBJECT.toString();

      @SuppressWarnings("serial")
      private final Map<String, Object> properties = new HashMap<String, Object>() {{
        put("status", new Status());
        put("valid", new Valid());
        put("statusInfo", new StatusInfo());
      }};
    }

    class Type {
      private String type = ValueType.STRING.toString();
      private final String index = indexNotAnalyzed;
    }


    class Status {
      private final String type = ValueType.INTEGER.toString();
    }

    class Valid {
      private final String type = ValueType.BOOLEAN.toString();
    }

    class Unit {
      private final String type = ValueType.STRING.toString();
      private final String index = indexNotAnalyzed;
    }

    class ValueDescription {
      private final String type = ValueType.STRING.toString();
      private final String index = indexNotAnalyzed;
    }

    class StatusInfo {
      private final String type = ValueType.STRING.toString();
      private final String index = indexNotAnalyzed;
    }

    @Getter
    class ValueBoolean {
      private final String type = ValueType.BOOLEAN.toString();
    }

    @Getter
    class ValueString {
      private final String type = ValueType.STRING.toString();
      private final String index = indexNotAnalyzed;
    }

    @Getter
    class ValueObject {
      private final String type = ValueType.NESTED.toString();
      private final String index = indexAnalyzed;
    }

    @Getter
    class Value {
      private final String type = ValueType.DOUBLE.toString();
    }

    @Getter
    class ValueLong {
      private final String type = ValueType.LONG.toString();
    }

    //c2mon goes here
    class C2monMetadata {
      private final String dynamic = "false";
      private final String type = ValueType.OBJECT.toString();

      private final Map<String, Object> properties = new HashMap<String, Object>(){{
        put("process", new Process());
        put("equipment", new Equipment());
        put("subEquipment", new SubEquipment());

        put("dataType", new DataType());

        put("serverTimestamp", new ServerTimestamp());
        put("sourceTimestamp", new SourceTimestamp());
        put("daqTimestamp", new DaqTimestamp());
      }};
    }

    class Process {
      private final String type = ValueType.STRING.toString();
      private final String index = indexNotAnalyzed;
    }

    class Equipment {
      private final String type = ValueType.STRING.toString();
      private final String index = indexNotAnalyzed;
    }

    class SubEquipment {
      private final String type = ValueType.STRING.toString();
      private final String index = indexNotAnalyzed;
    }

    class Metadata {
      private final String dynamic = "true";
      private final String type = ValueType.NESTED.toString();
    }
  }
}
