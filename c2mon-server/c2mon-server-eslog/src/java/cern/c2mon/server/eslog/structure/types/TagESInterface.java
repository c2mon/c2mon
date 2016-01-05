package cern.c2mon.server.eslog.structure.types;

import java.io.IOException;

import cern.c2mon.server.eslog.structure.mappings.Mapping.ValueType;

/**
 * Interface for the ES Tag format.
 * @author Alban Marguet.
 */
public interface TagESInterface {
  /**
   * Build the JSON format of the Java TagES object for ElasticSearch to add its data.
   * @return the JSON format.
   * @throws IOException
   */
  String build() throws IOException;

  /**
   * The mapping (as JSON) specified what are the fields present for a type of TagES in the cluster,
   * what are their types and how to deal with them.
   * @return the JSON format of the mapping.
   */
  String getMapping();

  /**
   * The mapping is set according to the dataType: tag_string, tag_numeric or tag_boolean.
   * @param tagValueType dataType of the TagES.
   */
  void setMapping(ValueType tagValueType);

  /**
   * Override the toString() method to display the structure of a TagES.
   */
  String toString();

  /**
   * @return the value of a TagES.
   */
  Object getValue();

  /**
   * Set the Object value but also the valueNumeric, valueBoolean or valueString depending on the class sof the child.
   * @param tagValue to give to a TagES.
   */
  void setValue(Object tagValue);
}