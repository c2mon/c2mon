package cern.c2mon.server.eslog.structure.types;

import cern.c2mon.server.eslog.structure.mappings.TagESMapping;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;

/**
 * Interface for the ES Tag format.
 * @author Alban Marguet.
 */
public interface TagESInterface {

    String build() throws IOException;
    String getMapping();
    String toString();
    Object getTagValue();
    void setTagValue(Object tagValue);
    void setMapping(String tagValueType, TagESMapping mapping);
}
