package cern.c2mon.server.eslog.structure.queries;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.common.settings.Settings;

/**
 * @author Alban Marguet.
 */
public class QueryIndexBuilder extends Query {
    public boolean indexNew(String indexName, Settings.Builder settings, String type, String mapping) {
        CreateIndexRequestBuilder createIndexRequestBuilder = client.admin().indices().prepareCreate(indexName);
        createIndexRequestBuilder.setSettings(settings);
        createIndexRequestBuilder.addMapping(type, mapping);
        CreateIndexResponse response = createIndexRequestBuilder.execute().actionGet();
        return response.isAcknowledged();
    }
}
