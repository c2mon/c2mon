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
package cern.c2mon.server.eslog.structure.mappings;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

import cern.c2mon.server.eslog.structure.types.tag.AbstractEsTag;
import cern.c2mon.server.eslog.structure.types.tag.EsValueType;

/**
 * Defines the ElasticSearch arguments for the types and the indices.
 * Permits to have mappings according to what we want to insert. (dataType)
 * Also contains the Mapping for routing, tags, alarms and supervisionevents.
 *
 * @author Alban Marguet.
 */
public interface EsMapping {
  enum ValueType {
    ALARM("alarm"),
    SUPERVISION("supervision"),
    STRING("string"),
    LONG("long"),
    INTEGER("integer"),
    DOUBLE("double"),
    BOOLEAN("boolean"),
    DATE("date"),
    OBJECT("object"),
    NESTED("nested");

    private final String type;

    ValueType(final String type) {
      this.type = type;
    }

    @Override
    public String toString() {
      return this.type;
    }
  }

  String indexNotAnalyzed = "not_analyzed";
  String routing = "true";
  String epochMillisFormat = "epoch_millis";

  String getMapping();

  @Getter
  class Routing {
    String required;

    Routing() {
      this.required = routing;
    }
  }

  /**
   * Properties for a {@link AbstractEsTag}
   */
  class Properties {
    Id id;
    Name name;

    Value value;
    ValueBoolean valueBoolean;
    ValueString valueString;

    Type type;

    ValueDescription valueDescription;
    Unit unit;
    Quality quality;

    Timestamp timestamp;

    C2monMetadata c2mon;
    Metadata metadata;

    Properties(EsValueType valueType) {
      this.id = new Id();
      this.name = new Name();

      switch (valueType) {
      case NUMERIC:
        this.value = new Value(ValueType.DOUBLE);
        break;
      case BOOLEAN:
        this.valueBoolean = new ValueBoolean();
        this.value = new Value(ValueType.DOUBLE);
        break;
      case OBJECT:
        this.valueString = new ValueString(ValueType.NESTED);
        break;
      case STRING:
      default:
        this.valueString = new ValueString(ValueType.STRING);
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

    public String getValueType() {
      if (valueBoolean != null) {
        return valueBoolean.getType();
      } else if (value != null) {
        return value.getType();
      } else {
        return valueString.getType();
      }
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
      private final String type;

      public ValueString(ValueType type) {
        this.type = type.toString();
      }
    }

    @Getter
    class Value {
      private final String type;

      public Value(ValueType type) {
        this.type = type.toString();
      }
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

  /**
   * Properties for a {@link cern.c2mon.server.eslog.structure.types.EsSupervisionEvent}.
   */
  class SupervisionProperties {
    private Supervision supervision;

    SupervisionProperties() {
      this.supervision = new Supervision();
    }

    class Supervision {
      private Properties properties;

      Supervision() {
        properties = new Properties();
      }

      class Properties {
        private Id id;
        private Timestamp timestamp;
        private Message message;
        private Status status;

        Properties() {
          this.id = new Id();
          this.timestamp = new Timestamp();
          this.message = new Message();
          this.status = new Status();
        }

        class Id {
          private final String type = ValueType.LONG.toString();
        }

        class Timestamp {
          private final String type = ValueType.DATE.toString();
          private final String format = epochMillisFormat;
        }

        class Message {
          private final String type = ValueType.STRING.toString();
          private final String index = indexNotAnalyzed;
        }

        class Status {
          private final String type = ValueType.STRING.toString();
          private final String index = indexNotAnalyzed;
        }
      }
    }
  }

  /**
   * Properties for a {@link cern.c2mon.server.eslog.structure.types.EsAlarm}.
   */
  class AlarmProperties {
    private Alarm alarm;

    AlarmProperties() {
      this.alarm = new Alarm();
    }

    class Alarm {
      private final Properties properties;

      Alarm() {
        properties = new Properties();
      }

      class Properties {
        private final TagId tagId;
        private final AlarmId alarmId;
        private final FaultFamily faultFamily;
        private final FaultMember faultMember;
        private final FaultCode faultCode;
        private final Active active;
        private final Activity activity;
        private final ActiveNumeric activeNumeric;
        private final Priority priority;
        private final Info info;
        private final ServerTimestamp serverTimestamp;
        private final TimeZone timeZone;

        private final Metadata metadata;


        Properties() {
          this.tagId = new TagId();
          this.alarmId = new AlarmId();
          this.faultFamily = new FaultFamily();
          this.faultMember = new FaultMember();
          this.faultCode = new FaultCode();
          this.active = new Active();
          this.activity = new Activity();
          this.activeNumeric = new ActiveNumeric();
          this.priority = new Priority();
          this.info = new Info();
          this.serverTimestamp = new ServerTimestamp();
          this.timeZone = new TimeZone();
          this.metadata = new Metadata();
        }

        class TagId {
          private final String type = ValueType.LONG.toString();
        }

        class AlarmId {
          private final String type = ValueType.LONG.toString();
        }

        class FaultFamily {
          private final String type = ValueType.STRING.toString();
          private final String index = indexNotAnalyzed;
        }

        class FaultMember {
          private final String type = ValueType.STRING.toString();
          private final String index = indexNotAnalyzed;
        }

        class FaultCode {
          private final String type = ValueType.INTEGER.toString();
        }

        class Active {
          private final String type = ValueType.BOOLEAN.toString();
        }

        class Activity {
          private final String type = ValueType.STRING.toString();
          private final String index = indexNotAnalyzed;
        }

        class ActiveNumeric {
          private final String type = ValueType.DOUBLE.toString();
        }

        class Priority {
          private final String type = ValueType.INTEGER.toString();
        }

        class Info {
          private final String type = ValueType.STRING.toString();
          private final String index = indexNotAnalyzed;
        }

        class ServerTimestamp {
          private final String type = ValueType.DATE.toString();
          private final String format = epochMillisFormat;
        }

        class TimeZone {
          private final String type = ValueType.STRING.toString();
        }

        class Metadata {
          private final String type = ValueType.NESTED.toString();
          private final String dynamic = "true";
        }
      }
    }
  }
}