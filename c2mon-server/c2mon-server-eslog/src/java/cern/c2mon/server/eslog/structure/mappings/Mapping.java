package cern.c2mon.server.eslog.structure.mappings;

/**
 * Defines the ElasticSearch arguments for the types and the indices.
 * Permits to have dynamic mappings according to what we want to insert. (dataType)
 * @author Alban Marguet.
 */
public interface Mapping {
  public enum ValueType {
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
    TagId tagId;
    TagName tagName;
    DataType dataType;
    TagTime tagTime;
    TagServerTime tagServerTime;
    TagDaqTime tagDaqTime;
    TagStatus tagStatus;
    Quality quality;
    TagValueDesc tagValueDesc;
    TagValue tagValue;
    ProcessName processName;
    EquipmentName equipmentName;
    SubEquipmentName subEquipmentName;

    Properties(ValueType tagValueType) {
      this.tagId = new TagId();
      this.tagName = new TagName();
      this.dataType = new DataType();
      this.tagTime = new TagTime();
      this.tagServerTime = new TagServerTime();
      this.tagDaqTime = new TagDaqTime();
      this.tagStatus = new TagStatus();
      this.quality = new Quality();
      this.tagValueDesc = new TagValueDesc();
      this.tagValue = new TagValue(tagValueType);
      this.processName = new ProcessName();
      this.equipmentName = new EquipmentName();
      this.subEquipmentName = new SubEquipmentName();
    }

    public String getValueType() {
      return tagValue.getType();
    }
  }

  class TagId {
    private final String type = ValueType.longType.toString();
  }

  class TagName {
    private final String type = ValueType.stringType.toString();
    private final String index = indexNotAnalyzed;
  }

  class DataType {
    private String type = ValueType.stringType.toString();
    private final String index = indexNotAnalyzed;
  }

  class TagTime {
    private final String type = ValueType.dateType.toString();
  }

  class TagServerTime {
    private final String type = ValueType.dateType.toString();
  }

  class TagDaqTime {
    private final String type = ValueType.dateType.toString();;
  }

  class TagStatus {
    private final String type = ValueType.intType.toString();
  }

  class Quality {
    private final String type = ValueType.stringType.toString();
    private final String index = indexNotAnalyzed;
  }

  class TagValueDesc {
    private final String type = ValueType.stringType.toString();
    private final String index = indexNotAnalyzed;
  }

  class TagValue {
    private final String type;

    public TagValue(ValueType type) {
      this.type = type.toString();
    }

    public String getType() {
      return type;
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
