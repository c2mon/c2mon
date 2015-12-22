package cern.c2mon.server.eslog.structure.types;

import lombok.extern.slf4j.Slf4j;

/**
 * Represents a Tag in ElasticSearch.
 * This type of TagES contains a boolean value.
 * @author Alban Marguet.
 */
@Slf4j
public class TagBoolean extends TagES implements TagESInterface {
  /**
   * Set the value of this TagBoolean to the value of the Tag in C2MON.
   * @param value Object supposed to be a boolean.
   */
  @Override
  public void setValue(Object tagValue) {
    if (tagValue == null) {
      log.trace("setValue() TagBoolean - Value is not set (value= " + tagValue + ").");
    }
    else if (tagValue instanceof Boolean) {
      this.value = tagValue;
    }
    else {
      throw new IllegalArgumentException("setValue() - Cannot instantiate new TagBoolean in ElasticSearch because the value has class=" + tagValue.getClass().getName() + ")");
    }
  }
}