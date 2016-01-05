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

  public boolean indexNew(String index, Settings settings, String type, String mapping) {
    CreateIndexRequestBuilder createIndexRequestBuilder = prepareCreateIndexRequestBuilder(index);

    if (settings != null) {
      createIndexRequestBuilder.setSettings(settings);
    }

    boolean parametersAreInitialized = type != null && mapping != null && !mapping.equals("");
    if (parametersAreInitialized) {
      createIndexRequestBuilder.addMapping(type, mapping);
    }

    CreateIndexResponse response = createIndexRequestBuilder.execute().actionGet();
    log.debug(response.toString());
    return response.isAcknowledged();
  }
}