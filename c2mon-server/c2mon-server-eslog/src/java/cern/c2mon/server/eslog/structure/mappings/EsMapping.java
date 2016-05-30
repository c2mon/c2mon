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

import cern.c2mon.server.eslog.structure.types.AbstractEsTag;
import lombok.Getter;

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
    INT("integer"),
    FLOAT("float"),
    SHORT("short"),
    DOUBLE("double"),
    BOOLEAN("boolean"),
    DATE("date"),
    NESTED("nested");

    private final String type;

    ValueType(final String type) {
      this.type = type;
    }

    @Override
    public String toString() {
      return this.type;
    }

    public static boolean isAlarm(ValueType type) {
      return ALARM.equals(type);
    }

    public static boolean isAlarm(String typeAsString) {
      return ALARM.toString().equalsIgnoreCase(typeAsString);
    }

    public static boolean isSupervision(ValueType type) {
      return SUPERVISION.equals(type);
    }

    public static boolean isSupervision(String typeAsString) {
      return SUPERVISION.toString().equalsIgnoreCase(typeAsString);
    }

    public static boolean isNumeric(ValueType type) {
      switch(type) {
        case FLOAT:
        case LONG:
        case SHORT:
        case DOUBLE:
        case INT:
          return true;
        default:
          return false;
      }
    }

    public static boolean isNumeric(final String typeAsString) {
      return (isLong(typeAsString)
              || isFloat(typeAsString)
              || isShort(typeAsString)
              || isDouble(typeAsString)
              || isInt(typeAsString));
    }

    public static boolean isLong(final String typeAsString) {
      return LONG.toString().equalsIgnoreCase(typeAsString);
    }

    public static boolean isFloat(final String typeAsString) {
      return FLOAT.toString().equalsIgnoreCase(typeAsString);
    }

    public static boolean isShort(final String typeAsString) {
      return SHORT.toString().equalsIgnoreCase(typeAsString);
    }

    public static boolean isDouble(final String typeAsString) {
      return DOUBLE.toString().equalsIgnoreCase(typeAsString);
    }

    public static boolean isInt(final String typeAsString) {
      return INT.toString().equalsIgnoreCase(typeAsString);
    }

    public static boolean isBoolean(final ValueType type) {
      return BOOLEAN.equals(type);
    }

    public static boolean isBoolean(final String typeAsString) {
      return BOOLEAN.toString().equalsIgnoreCase(typeAsString);
    }

    public static boolean isString(final ValueType type) {
      return STRING.equals(type);
    }

    public static boolean isString(final String typeAsString) {
      return STRING.toString().equalsIgnoreCase(typeAsString);
    }

    public static boolean isDate(final String typeAsString) {
      return DATE.toString().equalsIgnoreCase(typeAsString);
    }

    public static boolean matches(final String typeAsString) {
      return (isNumeric(typeAsString)
              || isBoolean(typeAsString)
              || isDate(typeAsString)
              || isString(typeAsString));
    }
  }

  String indexNotAnalyzed = "not_analyzed";
  String routing = "true";
  String epochMillisFormat = "epoch_millis";

  String getMapping();

  void setProperties(ValueType tagValueType);

  class Routing {
    String required;

    Routing() {
      this.required = routing;
    }

    public String getRequired() {
      return required;
    }
  }

  /**
   * Properties for a {@link AbstractEsTag}
   */
  class Properties {
    Id id;
    Name name;
    DataType dataType;
    SourceTimestamp sourceTimestamp;
    ServerTimestamp serverTimestamp;
    DaqTimestamp daqTimestamp;
    Status status;
    Quality quality;
    Valid valid;
    ValueDescription valueDescription;
    ValueBoolean valueBoolean;
    ValueString valueString;
    ValueNumeric valueNumeric;
    Process process;
    Equipment equipment;
    SubEquipment subEquipment;
    Metadata metadata;

    Properties(ValueType valueType) {
      this.id = new Id();
      this.name = new Name();
      this.dataType = new DataType();
      this.sourceTimestamp = new SourceTimestamp();
      this.serverTimestamp = new ServerTimestamp();
      this.daqTimestamp = new DaqTimestamp();
      this.status = new Status();
      this.quality = new Quality();
      this.valid = new Valid();
      this.valueDescription = new ValueDescription();

      if (ValueType.isNumeric(valueType)) {
        this.valueNumeric = new ValueNumeric(valueType);
      } else if (ValueType.isString(valueType)) {
        this.valueString = new ValueString(valueType);
      } else if (ValueType.isBoolean(valueType)) {
        this.valueBoolean = new ValueBoolean(valueType);
        this.valueNumeric = new ValueNumeric(ValueType.DOUBLE);
      }

      this.process = new Process();
      this.equipment = new Equipment();
      this.subEquipment = new SubEquipment();
      this.metadata = new Metadata();
    }

    public String getValueType() {
      if (valueBoolean != null) {
        return valueBoolean.getType();
      } else if (valueNumeric != null) {
        return valueNumeric.getType();
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

    class SourceTimestamp {
      private final String type = ValueType.DATE.toString();
      private final String format = epochMillisFormat;
    }

    class ServerTimestamp extends SourceTimestamp {
    }

    class DaqTimestamp extends SourceTimestamp {
    }

    class Status {
      private final String type = ValueType.INT.toString();
    }

    class Quality {
      private final String type = ValueType.STRING.toString();
      private final String index = indexNotAnalyzed;
    }

    class Valid {
      private final String type = ValueType.BOOLEAN.toString();
    }

    class ValueDescription {
      private final String type = ValueType.STRING.toString();
      private final String index = indexNotAnalyzed;
    }

    @Getter
    class ValueBoolean {
      private final String type;

      public ValueBoolean(ValueType type) {
        this.type = type.toString();
      }
    }

    @Getter
    class ValueString {
      private final String type;

      public ValueString(ValueType type) {
        this.type = type.toString();
      }
    }

    @Getter
    class ValueNumeric {
      private final String type;

      public ValueNumeric(ValueType type) {
        this.type = type.toString();
      }
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
          private final String type = ValueType.INT.toString();
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
          private final String type = ValueType.INT.toString();
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