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
      if (floatType.toString().equalsIgnoreCase(typeAsString)
          || longType.toString().equalsIgnoreCase(typeAsString)
          || shortType.toString().equalsIgnoreCase(typeAsString)
          || doubleType.toString().equalsIgnoreCase(typeAsString)
          || intType.toString().equalsIgnoreCase(typeAsString)) {
        return true;
      }
      
      return false;
    }
    
    public static boolean matches(final String typeAsString) {
      if (isNumeric(typeAsString)
          || boolType.toString().equalsIgnoreCase(typeAsString)
          || dateType.toString().equalsIgnoreCase(typeAsString)
          || stringType.toString().equalsIgnoreCase(typeAsString)) {
        return true;
      }
      
      return false;
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
    SourceTime sourceTime;
    ServerTime serverTime;
    DaqTime daqTime;
    Status status;
    Quality quality;
    ValueDescription valueDescription;
    Value value;
    ProcessName processName;
    EquipmentName equipmentName;
    SubEquipmentName subEquipmentName;

    Properties(ValueType valueType) {
      this.id = new Id();
      this.name = new Name();
      this.dataType = new DataType();
      this.sourceTime = new SourceTime();
      this.serverTime = new ServerTime();
      this.daqTime = new DaqTime();
      this.status = new Status();
      this.quality = new Quality();
      this.valueDescription = new ValueDescription();
      this.value = new Value(valueType);
      this.processName = new ProcessName();
      this.equipmentName = new EquipmentName();
      this.subEquipmentName = new SubEquipmentName();
    }

    public String getValueType() {
      return value.getType();
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

  class SourceTime {
    private final String type = ValueType.dateType.toString();
    private final String format = "epoch_millis";
  }

  class ServerTime extends SourceTime {
  }

  class DaqTime extends SourceTime{
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
  class Value {
    private final String type;

    public Value(ValueType type) {
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
