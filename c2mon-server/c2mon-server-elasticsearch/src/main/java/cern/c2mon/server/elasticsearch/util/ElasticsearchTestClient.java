package cern.c2mon.server.elasticsearch.util;

import cern.c2mon.server.elasticsearch.client.ElasticsearchClientRest;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Slf4j
@Component
public class ElasticsearchTestClient {

  private final ElasticsearchClientRest esClient;

  @Autowired
  public ElasticsearchTestClient(ElasticsearchClientRest esClient) {
    this.esClient = esClient;
  }

  public void refreshIndices() {
    try {
      RefreshResponse res = esClient
        .getClient()
        .indices()
        .refresh(new RefreshRequest("_all"), RequestOptions.DEFAULT);

      if (!res.getStatus().equals(RestStatus.OK)) {
        throw new RuntimeException("Request to refresh all indices failed.");
      }
    } catch (IOException e) {
      throw new RuntimeException("Request to refresh all indices failed.", e);
    }
  }

  public void deleteIndex(String index) {
    try {
      DeleteIndexResponse res = esClient
        .getClient()
        .indices()
        .delete(new DeleteIndexRequest(index), RequestOptions.DEFAULT);

      if (!res.isAcknowledged()) {
        throw new RuntimeException("Request to delete index failed.");
      }
    } catch (ElasticsearchStatusException e) {
      if (e.status().equals(RestStatus.NOT_FOUND)) {
        log.warn("Index \"{}\" couldn't be found, thus it wasn't removed.", index);
      } else {
        throw new RuntimeException("Request to delete index failed.", e);
      }
    } catch (IOException e) {
      throw new RuntimeException("Request to delete index failed.", e);
    }
  }

  public List<Map<String, Object>> fetchAllDocuments(String... indices) {
    try {
      SearchResponse res = esClient
        .getClient()
        .search(new SearchRequest(indices), RequestOptions.DEFAULT);

      if (!res.status().equals(RestStatus.OK)) {
        throw new RuntimeException("Request to find documents of all given indices failed.");
      }

      return Stream
        .of(res.getHits().getHits())
        .map(SearchHit::getSourceAsMap)
        .collect(toList());
    } catch (IOException e) {
      throw new RuntimeException("Request to find documents of all given indices failed.", e);
    }
  }
}
