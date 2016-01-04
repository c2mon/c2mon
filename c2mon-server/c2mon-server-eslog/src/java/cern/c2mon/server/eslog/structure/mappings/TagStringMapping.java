package cern.c2mon.server.eslog.structure.mappings;

/**
 * Mapping that a TagString will use to be indexed in the ElasticSearch cluster.
 * @author Alban Marguet.
 */
public class TagStringMapping extends TagESMapping implements Mapping {
  public TagStringMapping(ValueType type) {
    super();
    setProperties(type);
  }

  @Override
  public void setProperties(ValueType tagValueType) {
    if (tagValueType.equals(ValueType.stringType)) {
      this.properties = new Properties(tagValueType);
    }
    else {
      throw new IllegalArgumentException("Type for TagString must be string.");
    }
  }
}