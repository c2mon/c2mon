/******************************************************************************
 * Copyright (C) 2010-2020 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.elasticsearch.util;

import cern.c2mon.server.elasticsearch.client.ElasticsearchClientRest;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
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

/**
 * Class containing Elasticsearch operations used for testing. It builds upon
 * {@link ElasticsearchClientRest}, and thus tests using it are limited to the
 * "rest" client.
 */
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
      AcknowledgedResponse res = esClient
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
