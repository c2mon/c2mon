package cern.c2mon.server.eslog.structure.mappings;

import lombok.Getter;

/**
 * Defines the ElasticSearch arguments for the types and the indices.
 * Permits to have dynamic mappings according to what we want to insert. (dataType)
 * @author Alban Marguet.
 */
public interface Mapping {
  enum ValueType {
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
    ValueDescription valueDescription;
    ValueBoolean valueBoolean;
    ValueString valueString;
    ValueNumeric valueNumeric;
    ProcessName processName;
    EquipmentName equipmentName;
    SubEquipmentName subEquipmentName;

    Properties(ValueType valueType) {
      this.id = new Id();
      this.name = new Name();
      this.dataType = new DataType();
      this.sourceTimestamp = new SourceTimestamp();
      this.serverTimestamp = new ServerTimestamp();
      this.daqTimestamp = new DaqTimestamp();
      this.status = new Status();
      this.quality = new Quality();
      this.valueDescription = new ValueDescription();

      if (ValueType.isNumeric(valueType)) {
        this.valueNumeric = new ValueNumeric(valueType);
      }
      else if (ValueType.isString(valueType)) {
        this.valueString = new ValueString(valueType);
      }
      else if (ValueType.isBoolean(valueType)){
        this.valueBoolean = new ValueBoolean(valueType);
      }

      this.processName = new ProcessName();
      this.equipmentName = new EquipmentName();
      this.subEquipmentName = new SubEquipmentName();
    }

    public String getValueType() {
      if (valueNumeric != null) {
        return valueNumeric.getType();
      }
      else if (valueBoolean != null) {
        return valueBoolean.getType();
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
      private final String index = indexNotAnalyzed;
    }

    class DataType {
      private String type = ValueType.stringType.toString();
      private final String index = indexNotAnalyzed;
    }

    class SourceTimestamp {
      private final String type = ValueType.dateType.toString();
      private final String format = "epoch_millis";
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
      private final String index = indexNotAnalyzed;
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

    class ProcessName {
      private final String type = ValueType.stringType.toString();
      private final String index = indexNotAnalyzed;
    }

    class EquipmentName {
      private final String type = ValueType.stringType.toString();
      private final String index = indexNotAnalyzed;
    }

    class SubEquipmentName {
      private final String type = ValueType.stringType.toString();
      private final String index = indexNotAnalyzed;
    }
  }
}
