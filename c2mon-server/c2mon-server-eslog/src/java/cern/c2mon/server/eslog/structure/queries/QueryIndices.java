package cern.c2mon.server.eslog.structure.queries;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Query to be launched against ElasticSearch to retrieve all the indices present in the cluster.
 * @author Alban Marguet.
 */
@Slf4j
public class QueryIndices extends Query {

  public QueryIndices(Client client) {
    super(client);
  }

  public QueryIndices(Client client, List<String> indices, boolean isTypeDefined, List<String> types, List<Long> tagIds, int from, int size, int min, int max) {
    super(client, indices, isTypeDefined, types, tagIds, from, size, min, max);
  }
  /**
   * Query to get all the entries where the tagId is present.
   * @return SearchResponse
   */
  public SearchResponse getResponse() {
    SearchRequestBuilder requestBuilder = client.prepareSearch();
    requestBuilder.setSearchType(SearchType.DEFAULT)
        .setIndices(indices())
        .setFrom(from())
        .setSize(size());

    if (tagIds() != null) {
      requestBuilder.setQuery(QueryBuilders.boolQuery()
          .filter(QueryBuilders.termsQuery("tagId", tagIds())));
    }

    return requestBuilder.execute().actionGet();
  }

  /**
   * Simple query to get all the indices in the cluster.
   * @return List<String>: names of the indices.
   */
  public List<String> getListOfAnswer() {
    if (client != null) {
      String[] indices = client.admin().indices().prepareGetIndex().get().indices();
      log.info("QueryIndices - got a list of indices, size=" + indices.length);

      if (indices.length > 0) {
        log.info("index  " + indices[0]);
      }
      return Arrays.asList(indices);

    } else {

      log.warn("getListOFAnswer() - Warning: client has value " + client + ".");
      return new ArrayList<>();
    }
  }

  public boolean initTest() {
    try {
      List<String> indices = getListOfAnswer();
      log.info("initTest() - Indices present in the cluster:");
      for (String s : indices) {
        log.info(s);
      }

      return true;

    } catch(NoNodeAvailableException e) {
      log.error("initTest() - Error while creating client, could not find a connection to the ElasticSearch cluster, is it running?");
      return false;
    }
  }
}