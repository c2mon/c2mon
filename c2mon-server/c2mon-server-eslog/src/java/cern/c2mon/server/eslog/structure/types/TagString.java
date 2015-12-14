package cern.c2mon.server.eslog.structure.types;

/**
 * Represents a Tag in ElasticSearch.
 * This type of TagES contains a String tagValue.
 * @author Alban Marguet.
 */
public class TagString extends TagES implements TagESInterface {
  /**
   * Set the tagValue as a String for this TagES.
   * @param tagValue Object supposed to be a String.
   */
  @Override
  public void setTagValue(Object tagValue) {
    if (tagValue instanceof String) {
      this.tagValue = tagValue;
    } else {
      throw new IllegalArgumentException("Must give a String object to TagString in ElasticSearch");
    }
  }
}