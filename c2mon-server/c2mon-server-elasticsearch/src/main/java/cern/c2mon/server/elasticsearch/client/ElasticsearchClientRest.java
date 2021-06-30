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
package cern.c2mon.server.elasticsearch.client;

import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;
import cern.c2mon.server.elasticsearch.domain.IndexMetadata;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;


/**
 * Wrapper around {@link RestHighLevelClient}. Connects asynchronously, but also provides
 * methods to block until a healthy connection is established.
 *
 * @author Serhiy Boychenko
 */
@Slf4j
public final class ElasticsearchClientRest implements ElasticsearchClient {

  private final ElasticsearchProperties properties;

  private RestHighLevelClient client;

  /**
   * Elasticsearch REST client constructor
   *
   * @param properties to initialize REST client.
   */
  @Autowired
  public ElasticsearchClientRest(ElasticsearchProperties properties) {
    this.properties = properties;

    setup();

    connectAsynchronously();
  }

  @Override
  public BulkProcessor getBulkProcessor(BulkProcessor.Listener listener) {
    BiConsumer<BulkRequest, ActionListener<BulkResponse>> bulkConsumer =
        (request, bulkListener) -> client.bulkAsync(request, RequestOptions.DEFAULT, bulkListener);

    return BulkProcessor.builder(bulkConsumer, listener)
        .setBulkActions(properties.getBulkActions())
        .setBulkSize(new ByteSizeValue(properties.getBulkSize(), ByteSizeUnit.MB))
        .setFlushInterval(TimeValue.timeValueSeconds(properties.getBulkFlushInterval()))
        .setConcurrentRequests(properties.getConcurrentRequests())
        .build();
  }

  @Override
  public boolean createIndex(IndexMetadata indexMetadata, String mapping) {
    CreateIndexRequest request = new CreateIndexRequest(indexMetadata.getName());

    request.settings(Settings.builder()
        .put("index.number_of_shards", properties.getShardsPerIndex())
        .put("index.number_of_replicas", properties.getReplicasPerShard())
    );

    if (properties.isAutoTemplateMapping()) {
      request.mapping(mapping, XContentType.JSON);
    }

    try {
      CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
      return createIndexResponse.isAcknowledged();
    } catch (IOException e) {
      log.error("Error creating '{}' index on Elasticsearch.", indexMetadata.getName(), e);
    }

    return false;
  }

  @Override
  public boolean indexData(IndexMetadata indexMetadata, String data) {
    IndexRequest indexRequest = new IndexRequest(indexMetadata.getName());
    if (indexMetadata.getId() != null && !indexMetadata.getId().isEmpty()) {
      indexRequest.id(indexMetadata.getId());
    }
    indexRequest.source(data, XContentType.JSON);
    indexRequest.routing(indexMetadata.getRouting());

    try {
      IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
      return indexResponse.status().equals(RestStatus.CREATED) || indexResponse.status().equals(RestStatus.OK);
    } catch (IOException e) {
      log.error("Could not index '#{}' to index '{}'.", indexMetadata.getRouting(), indexMetadata.getName(), e);
    }
    return false;
  }

  @Override
  public boolean isIndexExisting(IndexMetadata indexMetadata) {
    SearchRequest searchRequest = new SearchRequest(indexMetadata.getName());
    searchRequest.routing(indexMetadata.getName());

    try {
      SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
      return searchResponse.status().equals(RestStatus.OK);
    } catch (ElasticsearchStatusException e) {
      if (!RestStatus.NOT_FOUND.equals(e.status())) {
        log.warn("Error checking '{}' index existence on Elasticsearch, unexpected status: ", indexMetadata.getName(), e);
      }
      log.debug("Exception checking '{}' index existence on Elasticsearch: ", indexMetadata.getName(), e);
    } catch (IOException e) {
      log.error("Error checking '{}' index existence on Elasticsearch.", indexMetadata.getName(), e);
    }

    return false;
  }

  @Override
  public boolean updateIndex(IndexMetadata indexMetadata, String data) {
    UpdateRequest updateRequest = new UpdateRequest(indexMetadata.getName(), indexMetadata.getId());
    updateRequest.doc(data, XContentType.JSON);
    updateRequest.routing(indexMetadata.getId());

    IndexRequest indexRequest = new IndexRequest(indexMetadata.getName());
    if (indexMetadata.getId() != null && !indexMetadata.getId().isEmpty()) {
      indexRequest.id(indexMetadata.getId());
    }
    indexRequest.source(data, XContentType.JSON);

    updateRequest.upsert(indexRequest);

    try {
      UpdateResponse updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
      return updateResponse.status().equals(RestStatus.OK);
    } catch (IOException e) {
      log.error("Error updating index '{}'.", indexMetadata.getName(), e);
    }
    return false;
  }

  @Override
  public boolean deleteIndex(IndexMetadata indexMetadata) {
    try {
      DeleteRequest deleteRequest = new DeleteRequest(indexMetadata.getName(), indexMetadata.getId());
      deleteRequest.routing(indexMetadata.getRouting());

      DeleteResponse deleteResponse = client.delete(deleteRequest, RequestOptions.DEFAULT);
      return deleteResponse.status().equals(RestStatus.OK);
    } catch (IOException e) {
      log.error("Error deleting '{}' index from ElasticSearch.", indexMetadata.getName(), e);
    }
    return false;
  }

  /**
   * Removes a JSON document from the specified index. (Used for testing)
   * @param indexMetadata Allows to defined index metadata
   * @return true on success
   */
  public boolean deleteDocumentByIndex(IndexMetadata indexMetadata) {
    try {
      DeleteIndexRequest deleteRequest = new DeleteIndexRequest(indexMetadata.getName());
      AcknowledgedResponse deleteIndexResponse = client.indices().delete(deleteRequest, RequestOptions.DEFAULT);
      return deleteIndexResponse.isAcknowledged();
    } catch (IOException e) {
      log.error("Error deleting '{}' index from ElasticSearch.", indexMetadata.getName(), e);
    }
    return false;
  }

  private void connectAsynchronously() {
    log.info("Trying to connect to Elasticsearch cluster {} at {}:{}",
        properties.getClusterName(), properties.getHost(), properties.getPort());

    new Thread(() -> {
      log.info("Connecting to Elasticsearch cluster {}", properties.getClusterName());
      waitForYellowStatus();
    }, "EsClusterFinder").start();
  }

  @Override
  public void waitForYellowStatus() {
    try {
      CompletableFuture<Void> nodeReady = CompletableFuture.runAsync(() -> {
        while (true) {
          log.debug("Waiting for yellow status of Elasticsearch cluster...");

          if (isClusterYellow()) {
            break;
          }

          sleep(1000L);
        }
        log.debug("Elasticsearch cluster is yellow");
      });
      nodeReady.get(ElasticsearchClientConfiguration.CLIENT_SETUP_TIMEOUT, TimeUnit.MILLISECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      log.error("Exception when waiting for yellow status", e);
      throw new IllegalStateException("Exception when waiting for Elasticsearch yellow status!", e);
    }
  }

  @SuppressWarnings("squid:S2142")
  private void sleep(long time) {
    try {
      Thread.sleep(time);
    } catch (InterruptedException e) {
      log.debug("Waiting for yellow status interrupted", e);
    }
  }

  private ClusterHealthResponse getClusterHealth() throws IOException {
    ClusterHealthRequest request = new ClusterHealthRequest();
    request.timeout("60s");
    request.waitForYellowStatus();

    return client.cluster().health(request, RequestOptions.DEFAULT);
  }


  @Override
  public boolean isClusterYellow() {
    try {
      byte status = getClusterHealth().getStatus().value();
      return status == ClusterHealthStatus.YELLOW.value() || status == ClusterHealthStatus.GREEN.value();
    } catch (IOException e) {
      log.info("Elasticsearch cluster not yet ready: {}", e.getMessage());
      log.trace("Elasticsearch cluster not yet ready: ", e);
    }
    return false;
  }

  @Override
  public void setup() {
    RestClientBuilder restClientBuilder =
        RestClient.builder(new HttpHost(properties.getHost(), properties.getPort(), properties.getScheme()));

    if(StringUtils.isNotEmpty(properties.getPathPrefix())){
      restClientBuilder.setPathPrefix(properties.getPathPrefix());
    }

    if (StringUtils.isNotEmpty(properties.getUsername()) && StringUtils.isNotEmpty(properties.getPassword())) {
      UsernamePasswordCredentials credentials =
          new UsernamePasswordCredentials(properties.getUsername(), properties.getPassword());

      CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
      credentialsProvider.setCredentials(AuthScope.ANY, credentials);

      restClientBuilder.setHttpClientConfigCallback(
          httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
    } else {
      if (StringUtils.isNotEmpty(properties.getUsername()) || StringUtils.isNotEmpty(properties.getPassword())) {
        log.warn("Both username and password must be configured to setup ES authentication.");
      }
    }

    client = new RestHighLevelClient(restClientBuilder);
  }

  /**
   * Refreshes all indices (Used in testing)
   */
  public void refreshIndices(){
    try {
      client.indices().refresh(new RefreshRequest(), RequestOptions.DEFAULT);
    } catch (IOException e) {
      log.warn("An error occurred refreshing the indices ", e);
    }
  }

  @Override
  public boolean isClientHealthy() {
    try {
      getClusterHealth();
      return true;
    } catch (Exception e) {
      log.error("An error occurred checking cluster health: ", e);
    }
    return false;
  }

  @Override
  public void close() {
    if (client != null) {
      try {
        client.close();
        log.info("Closed Elasticsearch client.");
      } catch (IOException e) {
        log.error("Error closing Elasticsearch client.", e);
      }
    }
  }
}
