package cern.c2mon.server.eslog.structure.types;

import cern.c2mon.server.eslog.structure.mappings.TagNumericMapping;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents a Tag in ElasticSearch.
 * This type of TagES contains a numeric value.
 * @author Alban Marguet.
 */
@Slf4j
public class TagNumeric extends TagES implements TagESInterface {
  @Override
  public void setValue(Object value) {
    if (value == null) {
      log.trace("setValue() TagNumeric - Value is not set (value= " + value + ").");
    }
    else if (value instanceof Number) {
      this.value = value;
      this.valueNumeric = (Number) value;
    }
    else {
      log.trace("setValue() - value has value " + value + ".");
      throw new IllegalArgumentException("setValue() - Cannot instantiate new TagNumeric in ElasticSearch because the value has class=" + value.getClass().getName() + ")");
    }
  }
}