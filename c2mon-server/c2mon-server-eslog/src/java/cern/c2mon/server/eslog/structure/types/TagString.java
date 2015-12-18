package cern.c2mon.server.eslog.structure.types;

import lombok.extern.slf4j.Slf4j;

/**
 * Represents a Tag in ElasticSearch.
 * This type of TagES contains a String tagValue.
 * @author Alban Marguet.
 */
@Slf4j
public class TagString extends TagES implements TagESInterface {
  /**
   * Set the tagValue as a String for this TagES.
   * @param tagValue Object supposed to be a String.
   */
  @Override
  public void setValue(Object tagValue) {
    if (tagValue == null) {
      log.trace("setValue() TagString - Value is not set (tagValue= " + tagValue + ").");
    }
    else if (tagValue instanceof String) {
      this.value = tagValue;
    } else {
      throw new IllegalArgumentException("setValue() - Cannot instantiate new TagString in ElasticSearch because the tagValue has class=" + tagValue.getClass() + ")");
    }
  }
}