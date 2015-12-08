package cern.c2mon.server.eslog.structure.queries;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;

/**
 * @author Alban Marguet.
 */
public class QueryIndexBuilder extends Query {

    public QueryIndexBuilder(Client client, String[] indices, boolean isTypeDefined, String[] types, long[] tagIds, int from, int size, int min, int max) {
        super(client, indices, isTypeDefined, types, tagIds, from, size, min, max);
    }

    public boolean indexNew(String indexName, Settings.Builder settings, String type, String mapping) {
        CreateIndexRequestBuilder createIndexRequestBuilder = client.admin().indices().prepareCreate(indexName);
        createIndexRequestBuilder.setSettings(settings);
        createIndexRequestBuilder.addMapping(type, mapping);
        CreateIndexResponse response = createIndexRequestBuilder.execute().actionGet();
        return response.isAcknowledged();
    }
}
