package cern.c2mon.server.eslog.structure.types;

/**
 * Represents a Tag in ElasticSearch.
 * This type of TagES contains a String tagValue.
 * @author Alban Marguet.
 */
public class TagString extends TagES implements TagESInterface {
    public String getMapping() {
        return mapping.getMapping();
    }

    @Override
    public void setTagValue(Object tagValue) {
        if (tagValue instanceof String) {
            this.tagValue = tagValue;
        } else {
            throw new IllegalArgumentException("Must give a String object to TagString in ElasticSearch");
        }
    }
}
