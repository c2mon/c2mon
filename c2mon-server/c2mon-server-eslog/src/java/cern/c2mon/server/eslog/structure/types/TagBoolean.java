package cern.c2mon.server.eslog.structure.types;

/**
 * Represents a Tag in ElasticSearch.
 * This type of TagES contains a boolean tagValue.
 * @author Alban Marguet.
 */
public class TagBoolean extends TagES implements TagESInterface {
  /**
   * Set the tagValue of this TagBoolean to the tagValue of the Tag in C2MON.
   * @param tagValue Object supposed to be a boolean.
   */
  @Override
  public void setTagValue(Object tagValue) {
    if (tagValue instanceof Boolean) {
      this.tagValue = tagValue;
    } else {
      throw new IllegalArgumentException("Must give a boolean object to TagBoolean in ElasticSearch");
    }
  }
}