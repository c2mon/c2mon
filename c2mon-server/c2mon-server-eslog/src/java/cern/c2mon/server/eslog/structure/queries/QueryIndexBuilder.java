package cern.c2mon.server.eslog.structure.queries;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;

import java.util.List;

/**
 * @author Alban Marguet.
 */
@Slf4j
public class QueryIndexBuilder extends Query {
  public QueryIndexBuilder(Client client) {
    super(client);
  }

  public QueryIndexBuilder(Client client, List<String> indices, boolean isTypeDefined, List<String> types, List<Long> tagIds, int from, int size, int min, int max) {
    super(client, indices, isTypeDefined, types, tagIds, from, size, min, max);
  }

  public boolean indexNew(String index, Settings settings, String type, String mapping) {
    CreateIndexRequestBuilder createIndexRequestBuilder = client.admin().indices().prepareCreate(index);

    if (settings != null) {
      createIndexRequestBuilder.setSettings(settings);
    }

    if (type != null && mapping != null && mapping.compareTo("") != 0) {
      createIndexRequestBuilder.addMapping(type, mapping);
    }

    CreateIndexResponse response = createIndexRequestBuilder.execute().actionGet();
    log.debug(response.toString());
    return response.isAcknowledged();
  }
}