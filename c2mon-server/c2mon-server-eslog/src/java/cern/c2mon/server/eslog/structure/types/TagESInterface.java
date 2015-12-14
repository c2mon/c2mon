package cern.c2mon.server.eslog.structure.types;

import java.io.IOException;

/**
 * Interface for the ES Tag format.
 * @author Alban Marguet.
 */
public interface TagESInterface {
	String build() throws IOException;
	String getMapping();
	void setMapping(String tagValueType);
	String toString();

	Object getTagValue();
	void setTagValue(Object tagValue);
}