package cern.c2mon.server.eslog.structure.types;

import java.io.IOException;

import cern.c2mon.server.eslog.structure.mappings.Mapping.ValueType;

/**
 * Interface for the ES Tag format.
 * @author Alban Marguet.
 */
public interface TagESInterface {
  String build() throws IOException;
  String getMapping();
  void setMapping(ValueType tagValueType);
  String toString();

  Object getTagValue();
  void setTagValue(Object tagValue);
}