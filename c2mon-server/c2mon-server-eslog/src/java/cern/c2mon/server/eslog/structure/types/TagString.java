package cern.c2mon.server.eslog.structure.types;

import lombok.extern.slf4j.Slf4j;

/**
 * Represents a Tag in ElasticSearch.
 * This type of TagES contains a String value.
 * @author Alban Marguet.
 */
@Slf4j
public class TagString extends TagES implements TagESInterface {
  /**
   * Set the value as a String for this TagES.
   * @param value Object supposed to be a String.
   */
  @Override
  public void setValue(Object tagValue) {
    if (tagValue == null) {
      log.trace("setValue() TagString - Value is not set (value= " + tagValue + ").");
    }
    else if (tagValue instanceof String) {
      this.value = tagValue;
    }
    else {
      throw new IllegalArgumentException("setValue() - Cannot instantiate new TagString in ElasticSearch because the value has class=" + tagValue.getClass().getName() + ")");
    }
  }
}