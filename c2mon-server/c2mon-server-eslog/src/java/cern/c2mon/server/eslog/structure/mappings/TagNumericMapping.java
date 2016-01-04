package cern.c2mon.server.eslog.structure.mappings;

/**
 * Mapping that a TagNumeric will use to be indexed in the ElasticSearch cluster.
 * @author Alban Marguet.
 */
public class TagNumericMapping extends TagESMapping implements Mapping {

  public TagNumericMapping(ValueType type) {
    super();
    setProperties(type);
  }

  @Override
  public void setProperties(ValueType tagValueType) {
    if (ValueType.isNumeric(tagValueType)) {
      this.properties = new Properties(tagValueType);
    }
    else {
      throw new IllegalArgumentException("Type for TagNumeric must be integer or double.");
    }
  }
}