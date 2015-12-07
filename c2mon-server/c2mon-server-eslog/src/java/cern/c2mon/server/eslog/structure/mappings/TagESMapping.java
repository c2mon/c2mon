package cern.c2mon.server.eslog.structure.mappings;

/**
 * Allows to create dynamic mappings for the different types that exist in ElasticSearch.
 * Look at the Mapping Interface for more details.
 * @author Alban Marguet.
 */
public class TagESMapping implements Mapping {
    String _routing;

    public TagESMapping() {
        _routing = routing;
    }

    @Override
    public String getMapping() {
        return null;
    }

    @Override
    public void setProperties(String tagValueType) {
    }
}
