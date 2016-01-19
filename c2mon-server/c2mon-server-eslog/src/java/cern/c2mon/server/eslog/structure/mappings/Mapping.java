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

import lombok.Getter;

/**
 * Defines the ElasticSearch arguments for the types and the indices.
 * Permits to have dynamic mappings according to what we want to insert. (dataType)
 * @author Alban Marguet.
 */
public interface Mapping {
  enum ValueType {
    alarmType("alarm"),
    supervisionType("supervision"),
    stringType("string"),
    longType("long"),
    intType("integer"),
    floatType("float"),
    shortType("short"),
    doubleType("double"),
    boolType("boolean"),
    dateType("date");
    
    ValueType(final String type) {
      this.type = type;
    }
    
    private final String type;
    
    @Override
    public String toString() {
      return this.type;
    }

    public static boolean isAlarm(ValueType type) {
      return alarmType.equals(type);
    }

    public static boolean isAlarm(String typeAsString) {
      return alarmType.toString().equalsIgnoreCase(typeAsString);
    }

    public static boolean isSupervision(ValueType type) {
      return supervisionType.equals(type);
    }

    public static boolean isSupervision(String typeAsString) {
      return supervisionType.toString().equalsIgnoreCase(typeAsString);
    }
    
    public static boolean isNumeric(ValueType type) {
      switch (type) {
        case floatType:
        case longType:
        case shortType:
        case doubleType:
        case intType:
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
      return longType.toString().equalsIgnoreCase(typeAsString);
    }

    public static boolean isFloat(final String typeAsString) {
      return floatType.toString().equalsIgnoreCase(typeAsString);
    }

    public static boolean isShort(final String typeAsString) {
      return shortType.toString().equalsIgnoreCase(typeAsString);
    }

    public static boolean isDouble(final String typeAsString) {
      return doubleType.toString().equalsIgnoreCase(typeAsString);
    }

    public static boolean isInt(final String typeAsString) {
      return intType.toString().equalsIgnoreCase(typeAsString);
    }

    public static boolean isBoolean(final ValueType type) {
      return boolType.equals(type);
    }

    public static boolean isBoolean(final String typeAsString) {
      return boolType.toString().equalsIgnoreCase(typeAsString);
    }

    public static boolean isString(final ValueType type) {
      return stringType.equals(type);
    }

    public static boolean isString(final String typeAsString) {
      return stringType.toString().equalsIgnoreCase(typeAsString);
    }

    public static boolean isDate(final String typeAsString) {
      return dateType.toString().equalsIgnoreCase(typeAsString);
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
      }
      else if (ValueType.isString(valueType)) {
        this.valueString = new ValueString(valueType);
      }
      else if (ValueType.isBoolean(valueType)){
        this.valueBoolean = new ValueBoolean(valueType);
        this.valueNumeric = new ValueNumeric(ValueType.doubleType);
      }

      this.process = new Process();
      this.equipment = new Equipment();
      this.subEquipment = new SubEquipment();
    }

    public String getValueType() {
      if (valueBoolean != null) {
        return valueBoolean.getType();
      }
      else if (valueNumeric != null) {
        return valueNumeric.getType();
      }
      else {
        return valueString.getType();
      }
    }

    class Id {
      private final String type = ValueType.longType.toString();
    }

    class Name {
      private final String type = ValueType.stringType.toString();
      //private final String index = indexNotAnalyzed;
    }

    class DataType {
      private String type = ValueType.stringType.toString();
      private final String index = indexNotAnalyzed;
    }

    class SourceTimestamp {
      private final String type = ValueType.dateType.toString();
      private final String format = epochMillisFormat;
    }

    class ServerTimestamp extends SourceTimestamp {
    }

    class DaqTimestamp extends SourceTimestamp {
    }

    class Status {
      private final String type = ValueType.intType.toString();
    }

    class Quality {
      private final String type = ValueType.stringType.toString();
      //private final String index = indexNotAnalyzed;
    }

    class Valid {
      private final String type = ValueType.boolType.toString();
    }

    class ValueDescription {
      private final String type = ValueType.stringType.toString();
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
      private final String type = ValueType.stringType.toString();
      //private final String index = indexNotAnalyzed;
    }

    class Equipment {
      private final String type = ValueType.stringType.toString();
      //private final String index = indexNotAnalyzed;
    }

    class SubEquipment {
      private final String type = ValueType.stringType.toString();
      //private final String index = indexNotAnalyzed;
    }
  }

  class Settings {
    private final int number_of_shards;
    private final int number_of_replicas;

    Settings(int shards, int replica) {
      this.number_of_shards = shards;
      this.number_of_replicas = replica;
    }
  }

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
          private final String type = ValueType.longType.toString();
        }

        class Timestamp {
          private final String type = ValueType.dateType.toString();
          private final String format = epochMillisFormat;
        }

        class Message {
          private final String type = ValueType.stringType.toString();
        }

        class Status {
          private final String type = ValueType.stringType.toString();
        }
      }
    }
  }

  class AlarmProperties {
    private Alarm alarm;

    AlarmProperties() {
      this.alarm = new Alarm();
    }

    class Alarm {
      private Properties properties;

      Alarm() {
        properties = new Properties();
      }

      class Properties {
        private TagId tagId;
        private AlarmId alarmId;
        private FaultFamily faultFamily;
        private FaultMember faultMember;
        private FaultCode faultCode;
        private Active active;
        private Activity activity;
        private ActiveNumeric activeNumeric;
        private Priority priority;
        private Info info;
        private ServerTimestamp serverTimestamp;
        private TimeZone timeZone;


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
        }

        class TagId {
          private final String type = ValueType.longType.toString();
        }

        class AlarmId {
          private final String type = ValueType.longType.toString();
        }

        class FaultFamily {
          private final String type = ValueType.stringType.toString();
        }

        class FaultMember {
          private final String type = ValueType.stringType.toString();
        }

        class FaultCode {
          private final String type = ValueType.intType.toString();
        }

        class Active {
          private final String type = ValueType.boolType.toString();
        }

        class Activity {
          private final String type = ValueType.stringType.toString();
        }

        class ActiveNumeric {
          private final String type = ValueType.doubleType.toString();
        }

        class Priority {
          private final String type = ValueType.intType.toString();
        }

        class Info {
          private final String type = ValueType.stringType.toString();
        }

        class ServerTimestamp {
          private final String type = ValueType.dateType.toString();
          private final String format = epochMillisFormat;
        }

        class TimeZone {
          private final String type = ValueType.stringType.toString();
        }
      }
    }
  }
}