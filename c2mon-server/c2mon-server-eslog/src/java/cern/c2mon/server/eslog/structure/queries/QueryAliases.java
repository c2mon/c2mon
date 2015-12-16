package cern.c2mon.server.eslog.structure.queries;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.AliasesRequest;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.AliasOrIndex;
import org.elasticsearch.cluster.metadata.IndexMetaData;

import java.util.*;

/**
 * @author Alban Marguet.
 */
@Slf4j
public class QueryAliases extends Query {
  public QueryAliases(Client client) {
    super(client);
  }

  public QueryAliases(Client client, List<String> indices, boolean isTypeDefined, List<String> types, List<Long> tagIds, int from, int size, int min, int max) {
    super(client, indices, isTypeDefined, types, tagIds, from, size, min, max);
  }

  public boolean addAlias(String indexMonth, String aliasName) {
    if (client != null) {
      IndicesAliasesResponse response = client.admin().indices().prepareAliases().addAlias(indexMonth, aliasName).execute().actionGet();
      return response.isAcknowledged();

    } else {
      log.info("addAlias() - client has null value.");
    }

    return false;
  }

  public List<String> getListOfAnswer() {
    List<String> result = new ArrayList<>();
    Iterator<ObjectCursor<IndexMetaData>> indicesIt = client.admin().cluster().prepareState().execute().actionGet().getState().getMetaData().indices().values().iterator();

    while(indicesIt.hasNext()) {
      Iterator<ObjectCursor<String>> aliases = indicesIt.next().value.getAliases().keys().iterator();

      while(aliases.hasNext()) {
        result.add(aliases.next().value);
      }
    }

    return result;
  }
}