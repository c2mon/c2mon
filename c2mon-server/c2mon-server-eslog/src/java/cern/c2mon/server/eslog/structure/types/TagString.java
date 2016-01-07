package cern.c2mon.server.eslog.structure.types;

import lombok.extern.slf4j.Slf4j;

/**
 * Represents a Tag in ElasticSearch.
 * This type of TagES contains a String value.
 * @author Alban Marguet.
 */
@Slf4j
public class TagString extends TagES implements TagESInterface {

  public TagString() {
    super();
  }
  /**
   * Set the value as a String for this TagES.
   * @param value Object supposed to be a String.
   */
  @Override
  public void setValue(Object value) {
    if (value == null) {
      log.trace("setValue() TagString - Value is not set (value= " + value + ").");
    }
    else if (value instanceof String) {
      this.value = value;
      this.valueString = (String) value;
    }
    else {
      throw new IllegalArgumentException("setValue() - Cannot instantiate new TagString in ElasticSearch because the value has class=" + value.getClass().getName() + ")");
    }
  }
}