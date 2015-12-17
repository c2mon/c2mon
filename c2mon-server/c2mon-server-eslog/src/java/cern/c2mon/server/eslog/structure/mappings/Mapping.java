package cern.c2mon.server.eslog.structure.mappings;

/**
 * Defines the ElasticSearch arguments for the types and the indices.
 * Permits to have dynamic mappings according to what we want to insert. (dataType)
 * @author Alban Marguet.
 */
public interface Mapping {
  String stringType = "string";
  String longType = "long";
  String intType = "integer";
  String floatType = "float";
  String shortType = "short";
  String doubleType = "double";
  String boolType = "boolean";
  String dateType = "date";
  String indexNotAnalyzed = "not_analyzed";
  String routing = "true";

  String getMapping();
  void setProperties(String tagValueType);

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

    Properties(String tagValueType) {
      this.tagId = new TagId(longType);
      this.tagName = new TagName(stringType, indexNotAnalyzed);
      this.dataType = new DataType(stringType, indexNotAnalyzed);
      this.tagTime = new TagTime(dateType);
      this.tagServerTime = new TagServerTime(dateType);
      this.tagDaqTime = new TagDaqTime(dateType);
      this.tagStatus = new TagStatus(intType);
      this.quality = new Quality(stringType, indexNotAnalyzed);
      this.tagValueDesc = new TagValueDesc(stringType, indexNotAnalyzed);
      this.tagValue = new TagValue(tagValueType);
      this.processName = new ProcessName(stringType, indexNotAnalyzed);
      this.equipmentName = new EquipmentName(stringType, indexNotAnalyzed);
      this.subEquipmentName = new SubEquipmentName(stringType, indexNotAnalyzed);
    }

    public String getValueType() {
      return tagValue.getType();
    }
  }

  class TagId {
    String type;

    public TagId(String type) {
      this.type = type;
    }
  }

  class TagName {
    String type;
    String index;

    public TagName(String type, String indexing) {
      this.type = type;
      this.index = indexing;
    }
  }

  class DataType {
    private String type;
    private String index;

    public DataType(String type, String indexing) {
      this.type = type;
      this.index = indexing;
    }
  }

  class TagTime {
    private String type;

    public TagTime(String type) {
      this.type = type;
    }
  }

  class TagServerTime {
    private String type;

    public TagServerTime(String type) {
      this.type = type;
    }
  }

  class TagDaqTime {
    private String type;

    public TagDaqTime(String type) {
      this.type = type;
    }
  }

  class TagStatus {
    private String type;

    public TagStatus(String type) {
      this.type = type;
    }
  }

  class Quality {
    private String type;
    private String index;

    public Quality(String type, String indexing) {
      this.type = type;
      this.index = indexing;
    }
  }

  class TagValueDesc {
    private String type;
    private String index;

    public TagValueDesc(String type, String indexing) {
      this.type = type;
      this.index = indexing;
    }
  }

  class TagValue {
    private String type;

    public TagValue(String type) {
      this.type = type;
    }

    public String getType() {
      return type;
    }
  }

  class ProcessName {
    private String type;
    private String index;

    public ProcessName(String type, String indexing) {
      this.type = type;
      this.index = indexing;
    }
  }

  class EquipmentName {
    private String type;
    private String index;

    public EquipmentName(String type, String indexing) {
      this.type = type;
      this.index = indexing;
    }
  }

  class SubEquipmentName {
    private String type;
    private String index;

    public SubEquipmentName(String type, String indexing) {
      this.type = type;
      this.index = indexing;
    }
  }
}
