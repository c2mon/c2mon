package cern.c2mon.server.eslog.structure.types;

import lombok.extern.slf4j.Slf4j;

/**
 * Represents a Tag in ElasticSearch.
 * This type of TagES contains a numeric tagValue.
 * @author Alban Marguet.
 */
@Slf4j
public class TagNumeric extends TagES implements TagESInterface {
  /**
   * Set tagValue of TagNumeric to the value of Tag in C2MON if it was numeric.
   * @param tagValue Object supposed to be numeric.
   */
  @Override
  public void setValue(Object tagValue) {
    if (tagValue == null) {
      log.trace("setValue() TagNumeric - Value is not set (tagValue= " + tagValue + ").");
    }
    else if (tagValue instanceof Number) {
      this.value = tagValue;
    }
    else {
      log.trace("setValue() - tagValue has value " + tagValue + ".");
      throw new IllegalArgumentException("setValue() - Cannot instantiate new TagNumeric in ElasticSearch because the tagValue has class=" + tagValue.getClass() + ")");
    }
  }
}