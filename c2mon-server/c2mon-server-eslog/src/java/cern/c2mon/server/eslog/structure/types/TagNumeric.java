package cern.c2mon.server.eslog.structure.types;

/**
 * Represents a Tag in ElasticSearch.
 * This type of TagES contains a numeric tagValue.
 * @author Alban Marguet.
 */
public class TagNumeric extends TagES implements TagESInterface {
  /**
   * Set tagValue of TagNumeric to the value of Tag in C2MON if it was numeric.
   * @param tagValue Object supposed to be numeric.
   */
  @Override
  public void setTagValue(Object tagValue) {
    if (tagValue instanceof Integer || tagValue instanceof Double || tagValue instanceof Float || tagValue instanceof Long) {
      this.tagValue = tagValue;
    } else {
      throw new IllegalArgumentException("Must pass a numeric tagValue to TagNumeric in ElasticSearch");
    }
  }
}