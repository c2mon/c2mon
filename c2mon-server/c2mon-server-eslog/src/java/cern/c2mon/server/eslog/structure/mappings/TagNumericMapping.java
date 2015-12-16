package cern.c2mon.server.eslog.structure.mappings;

/**
 * Mapping that a TagNumeric will use to be indexed in the ElasticSearch cluster.
 * @author Alban Marguet.
 */
public class TagNumericMapping extends TagESMapping implements Mapping {

  public TagNumericMapping(String type) {
    super();
    setProperties(type);
  }

  @Override
  public void setProperties(String tagValueType) {
    if (!(tagValueType.compareTo(intType) == 0) && !(tagValueType.compareTo(doubleType) == 0)) {
      throw new IllegalArgumentException("Type for TagNumeric must be integer or double.");
    } else {
      this.properties = new Properties(tagValueType);
    }
  }
}