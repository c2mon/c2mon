package cern.c2mon.server.eslog.structure.types;

/**
 * Represents a Tag in ElasticSearch.
 * This type of TagES contains a numeric tagValue.
 * @author Alban Marguet.
 */
public class TagNumeric extends TagES implements TagESInterface {

    public String getMapping() {
        return mapping.getMapping();
    }

    @Override
    public void setTagValue(Object tagValue) {
        if (tagValue instanceof Integer || tagValue instanceof Double || tagValue instanceof Float) {
            this.tagValue = tagValue;
        } else {
            throw new IllegalArgumentException("Must pass a numeric tagValue to TagNumeric in ElasticSearch");
        }
    }
}
