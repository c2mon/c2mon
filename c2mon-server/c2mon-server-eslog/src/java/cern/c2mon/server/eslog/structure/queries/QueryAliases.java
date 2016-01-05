package cern.c2mon.server.eslog.structure.queries;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.AliasesRequest;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequestBuilder;
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

  public boolean addAlias(String indexMonth, String aliasName) {
    if (client != null) {
      IndicesAliasesRequestBuilder preparedAliases = prepareAliases();
      IndicesAliasesResponse response = preparedAliases.addAlias(indexMonth, aliasName).execute().actionGet();
      return response.isAcknowledged();
    }
    else {
      log.info("addAlias() - client has null value.");
    }

    return false;
  }

  public List<String> getListOfAnswer() {
    List<String> result = new ArrayList<>();
    Iterator<ObjectCursor<IndexMetaData>> indicesIt = getIndicesWithMetadata();
    addAliasesToResult(indicesIt, result);
    log.info("QueryAliases - got a list of aliases, size=" + result.size());
    return result;
  }

  private void addAliasesToResult(Iterator<ObjectCursor<IndexMetaData>> indicesIt, List<String> result) {
    while(indicesIt.hasNext()) {
      Iterator<ObjectCursor<String>> aliases = indicesIt.next().value.getAliases().keys().iterator();

      while(aliases.hasNext()) {
        result.add(aliases.next().value);
      }
    }
  }
}