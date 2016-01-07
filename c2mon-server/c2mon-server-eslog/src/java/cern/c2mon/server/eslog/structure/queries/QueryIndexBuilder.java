package cern.c2mon.server.eslog.structure.queries;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;

/**
 * @author Alban Marguet.
 */
@Slf4j
public class QueryIndexBuilder extends Query {

  public QueryIndexBuilder(Client client) {
    super(client);
  }

  public boolean indexNew(String index, Settings settings, String type, String mapping) {
    if (type == null && mapping == null) {
      return handleAddingIndex(index, settings);
    }
    else {
      return handleAddingMapping(index, type, mapping);
    }
  }

  private boolean handleAddingIndex(String index, Settings settings) {
    CreateIndexRequestBuilder createIndexRequestBuilder = prepareCreateIndexRequestBuilder(index);

    if (settings != null && settings != Settings.EMPTY) {
      createIndexRequestBuilder.setSettings(settings);
    }

    CreateIndexResponse response = createIndexRequestBuilder.execute().actionGet();
    return response.isAcknowledged();
  }

  private boolean handleAddingMapping(String index, String type, String mapping) {
    PutMappingResponse response = client.admin().indices().preparePutMapping(index).setType(type).setSource(mapping).execute().actionGet();
    return response.isAcknowledged();
  }
}