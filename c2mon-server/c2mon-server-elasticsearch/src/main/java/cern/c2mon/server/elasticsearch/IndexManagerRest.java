/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.server.elasticsearch;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import cern.c2mon.server.elasticsearch.client.ElasticsearchClientRest;
import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;


/**
 * Rest-based (check
 * <a href="https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/index.html>
 * Elasticsearch Documentation</a> for more details) supported index-related operations manager.
 *
 * @author James Hamilton
 * @author Serhiy Boychenko
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "c2mon.server.elasticsearch.rest", havingValue = "true")
public class IndexManagerRest implements IndexManager {

  private final List<String> indexCache = new CopyOnWriteArrayList<>();

  private final ElasticsearchClientRest client;
  private final ElasticsearchProperties properties;

  /**
   * @param client {@link ElasticsearchClientRest} client instance.
   */
  @Autowired
  public IndexManagerRest(ElasticsearchClientRest client) {
    this.client = client;
    this.properties = client.getProperties();
  }

  @Override
  public boolean create(String indexName, String mapping) {
    synchronized (IndexManager.class) {
      if (exists(indexName)) {
        return true;
      }

      CreateIndexRequest request = new CreateIndexRequest(indexName);

      request.settings(Settings.builder()
          .put("index.number_of_shards", properties.getShardsPerIndex())
          .put("index.number_of_replicas", properties.getReplicasPerShard())
      );

      if (properties.isAutoTemplateMapping()) {
        request.mapping(TYPE, mapping, XContentType.JSON);
      }

      boolean created = false;
      try {
        CreateIndexResponse createIndexResponse = client.getClient().indices().create(request, RequestOptions.DEFAULT);
        created = createIndexResponse.isAcknowledged();
      } catch (IOException e) {
        log.error("Error creating '{}' index on Elasticsearch.", indexName, e);
      }

      client.waitForYellowStatus();

      if (created) {
        indexCache.add(indexName);
      }

      return created;
    }
  }

  @Override
  public boolean index(String indexName, String source, String routing) {
    return index(indexName, source, "", routing);
  }

  @Override
  public boolean index(String indexName, String source, String id, String routing) {
    synchronized (IndexManagerRest.class) {
      IndexRequest indexRequest = new IndexRequest(indexName, TYPE);
      if (id != null && !id.isEmpty()) {
        indexRequest.id(id);
      }
      indexRequest.source(source, XContentType.JSON);
      indexRequest.routing(routing);

      boolean indexed = false;
      try {
        IndexResponse indexResponse = client.getClient().index(indexRequest, RequestOptions.DEFAULT);
        indexed = indexResponse.status().equals(RestStatus.CREATED) || indexResponse.status().equals(RestStatus.OK);
      } catch (IOException e) {
        log.error("Could not index '#{}' to index '{}'.", routing, indexName, e);
      }

      client.waitForYellowStatus();

      return indexed;
    }
  }

  @Override
  public boolean exists(String indexName) {
    return exists(indexName, "");
  }

  @Override
  public boolean exists(String indexName, String routing) {
    synchronized (IndexManager.class) {
      if (indexCache.contains(indexName)) {
        return true;
      }
      SearchRequest searchRequest = new SearchRequest(indexName);
      searchRequest.types(TYPE);
      searchRequest.routing(routing);

      boolean exists = false;
      try {
        SearchResponse searchResponse = client.getClient().search(searchRequest, RequestOptions.DEFAULT);
        exists = searchResponse.status().equals(RestStatus.OK);
      } catch (ElasticsearchStatusException e) {
        if (!RestStatus.NOT_FOUND.equals(e.status())) {
          log.error("Error checking '{}' index existence on Elasticsearch, unexpected status: ", e);
        }
      } catch (IOException e) {
        log.error("Error checking '{}' index existence on Elasticsearch.", indexName, e);
      }

      if (exists) {
        indexCache.add(indexName);
      }

      return exists;
    }
  }

  @Override
  public boolean update(String indexName, String source, String id) {
    synchronized (IndexManagerRest.class) {
      UpdateRequest updateRequest = new UpdateRequest(indexName, TYPE, id);
      updateRequest.doc(source, XContentType.JSON);
      updateRequest.routing(id);

      IndexRequest indexRequest = new IndexRequest(indexName, TYPE);
      if (id != null && !id.isEmpty()) {
        indexRequest.id(id);
      }
      indexRequest.source(source, XContentType.JSON);

      updateRequest.upsert(indexRequest);

      boolean updated = false;
      try {
        UpdateResponse updateResponse = client.getClient().update(updateRequest, RequestOptions.DEFAULT);
        updated = updateResponse.status().equals(RestStatus.OK);
      } catch (IOException e) {
        log.error("Error updating index '{}'.", indexName, e);
      }

      client.waitForYellowStatus();

      return updated;
    }
  }

  @Override
  public boolean delete(String indexName, String id, String routing) {
    synchronized (IndexManagerRest.class) {
      boolean deleted = false;
      try {
        indexCache.remove(indexName);

        DeleteRequest deleteRequest = new DeleteRequest(indexName, TYPE, id);
        deleteRequest.routing(routing);

        DeleteResponse deleteResponse = client.getClient().delete(deleteRequest, RequestOptions.DEFAULT);
        deleted = deleteResponse.status().equals(RestStatus.OK);
      } catch (IOException e) {
        log.error("Error deleting '{}' index from ElasticSearch.", indexName, e);
      }

      client.waitForYellowStatus();

      return deleted;
    }
  }

  @Override
  public void purgeIndexCache() {
    synchronized (IndexManagerRest.class) {
      indexCache.clear();
    }
  }
}
