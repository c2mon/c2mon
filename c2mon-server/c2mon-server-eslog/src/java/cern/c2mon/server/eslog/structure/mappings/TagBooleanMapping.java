package cern.c2mon.server.eslog.structure.mappings;

/**
 * Mapping that a TagBoolean will use in the ElasticSearch cluster.
 * @author Alban Marguet.
 */
public class TagBooleanMapping extends TagESMapping implements Mapping {
  public TagBooleanMapping(ValueType type) {
    super();
    setProperties(type);
  }

  @Override
  public void setProperties(ValueType tagValueType) {
    if (tagValueType.equals(ValueType.boolType)) {
      properties = new Properties(tagValueType);
    } else {
      throw new IllegalArgumentException("Type for TagBoolean must be boolean.");
    }
  }
}