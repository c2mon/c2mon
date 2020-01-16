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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ResourceAlreadyExistsException;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;
import cern.c2mon.server.elasticsearch.domain.IndexMetadata;

import static cern.c2mon.server.elasticsearch.config.ElasticsearchProperties.*;

/**
 * Wrapper around {@link Client}. Connects asynchronously, but also provides
 * methods to block until a healthy connection is established.
 *
 * @author Alban Marguet
 * @author Justin Lewis Salmon
 * @author James Hamilton
 * @author Serhiy Boychenko
 */
@Slf4j
public final class ElasticsearchClientTransport implements ElasticsearchClient {

  private final ElasticsearchProperties properties;

  private final Client client;

  /**
   * @param properties to initialize Transport client.
   */
  @Autowired
  public ElasticsearchClientTransport(ElasticsearchProperties properties) {
    this.properties = properties;
    this.client = createClient();

    connectAsynchronously();
  }

  @Override
  public BulkProcessor getBulkProcessor(BulkProcessor.Listener listener) {
    return BulkProcessor.builder(client, listener)
        .setBulkActions(properties.getBulkActions())
        .setBulkSize(new ByteSizeValue(properties.getBulkSize(), ByteSizeUnit.MB))
        .setFlushInterval(TimeValue.timeValueSeconds(properties.getBulkFlushInterval()))
        .setConcurrentRequests(properties.getConcurrentRequests())
        .build();
  }

  @Override
  public boolean createIndex(IndexMetadata indexMetadata, String mapping) {
    CreateIndexRequestBuilder builder = client.admin().indices().prepareCreate(indexMetadata.getName());
    builder.setSettings(Settings.builder()
        .put("number_of_shards", properties.getShardsPerIndex())
        .put("number_of_replicas", properties.getReplicasPerShard())
        .build());

    if (mapping != null) {
      builder.addMapping(TYPE, mapping, XContentType.JSON);
    }

    log.debug("Creating new index with name {}", indexMetadata.getName());

    try {
      CreateIndexResponse response = builder.get();
      return response.isAcknowledged();
    } catch (ResourceAlreadyExistsException e) {
      log.debug("Index already exists.", e);
    }

    return false;
  }

  @Override
  public boolean indexData(IndexMetadata indexMetadata, String data) {
    IndexRequest indexRequest = new IndexRequest(indexMetadata.getName(), TYPE);
    if (indexMetadata.getId() != null && !indexMetadata.getId().isEmpty()) {
      indexRequest.id(indexMetadata.getId());
    }
    indexRequest.source(data, XContentType.JSON);
    indexRequest.routing(indexMetadata.getRouting());

    try {
      IndexResponse indexResponse = client.index(indexRequest).get();
      return indexResponse.status().equals(RestStatus.CREATED) || indexResponse.status().equals(RestStatus.OK);
    } catch (InterruptedException | ExecutionException e) {
      log.error("Error indexing '#{}' to '{}'.", indexMetadata.getId(), indexMetadata.getName(), e);
    }

    return false;
  }

  @Override
  public boolean isIndexExisting(IndexMetadata indexMetadata) {
    SearchRequest searchRequest = new SearchRequest(indexMetadata.getName());
    searchRequest.types(TYPE);
    searchRequest.routing(indexMetadata.getRouting());

    try {
      SearchResponse response = client.search(searchRequest).get();
      return response.status().equals(RestStatus.OK);
    } catch (InterruptedException | ExecutionException e) {
      log.error("Error searching index '{}'.", indexMetadata.getName(), e);
    }

    return false;
  }

  @Override
  public boolean updateIndex(IndexMetadata indexMetadata, String data) {
    UpdateRequest updateRequest = new UpdateRequest(indexMetadata.getName(), TYPE, indexMetadata.getId());
    updateRequest.doc(data, XContentType.JSON);
    updateRequest.routing(indexMetadata.getId());

    IndexRequest indexRequest = new IndexRequest(indexMetadata.getName(), TYPE, indexMetadata.getId());
    indexRequest.source(data, XContentType.JSON);
    indexRequest.routing(indexMetadata.getId());

    updateRequest.upsert(indexRequest);

    try {
      UpdateResponse updateResponse = client.update(updateRequest).get();
      return updateResponse.status().equals(RestStatus.OK);
    } catch (InterruptedException | ExecutionException e) {
      log.error("Error updating index '{}'.", indexMetadata.getName(), e);
    }

    return false;
  }

  @Override
  public boolean deleteIndex(IndexMetadata indexMetadata) {
    DeleteRequest deleteRequest = new DeleteRequest(indexMetadata.getName(), TYPE, indexMetadata.getId());
    deleteRequest.routing(indexMetadata.getRouting());

    try {
      DeleteResponse deleteResponse = client.delete(deleteRequest).get();
      return deleteResponse.status().equals(RestStatus.OK);
    } catch (InterruptedException | ExecutionException e) {
      log.error("Error while deleting index", e);
    }

    return false;
  }

  /**
   * Creates a {@link Client} to communicate with the Elasticsearch cluster.
   *
   * @return the {@link Client} instance
   */
  @SuppressWarnings("squid:S2095")
  private Client createClient() {
    final Settings.Builder settingsBuilder = Settings.builder();

    settingsBuilder.put("node.name", properties.getNodeName())
        .put("cluster.name", properties.getClusterName())
        .put("http.enabled", properties.isHttpEnabled());

    TransportClient transportClient = new PreBuiltTransportClient(settingsBuilder.build());
    try {
      transportClient.addTransportAddress(new TransportAddress(InetAddress.getByName(properties.getHost()), properties.getPort()));
    } catch (UnknownHostException e) {
      log.error("Error connecting to the Elasticsearch cluster at {}:{}", properties.getHost(), properties.getPort(), e);
      return null;
    }

    return transportClient;
  }

  /**
   * Connect to the cluster in a separate thread.
   */
  private void connectAsynchronously() {
    log.info("Trying to connect to Elasticsearch cluster {} at {}:{}",
        properties.getClusterName(), properties.getHost(), properties.getPort());

    new Thread(() -> {
      log.info("Connected to Elasticsearch cluster {}", properties.getClusterName());
      waitForYellowStatus();
    }, "EsClusterFinder").start();
  }

  @Override
  @SuppressWarnings("squid:S2142")
  public void waitForYellowStatus() {
    try {
      CompletableFuture<Void> nodeReady = CompletableFuture.runAsync(() -> {
            while (true) {
              log.info("Waiting for yellow status of Elasticsearch cluster...");

              if (isClusterYellow()) {
                break;
              }

              sleep(100L);
            }
            log.info("Elasticsearch cluster is yellow");
          }
      );
      nodeReady.get(120, TimeUnit.SECONDS);
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

  private ClusterHealthResponse getClusterHealth() {
    return client.admin().cluster().prepareHealth()
        .setWaitForYellowStatus()
        .setTimeout(TimeValue.timeValueMillis(100))
        .get();
  }

  public boolean isClusterYellow() {
    try {
      ClusterHealthStatus status = getClusterHealth().getStatus();
      return status.equals(ClusterHealthStatus.YELLOW) || status.equals(ClusterHealthStatus.GREEN);
    } catch (NoNodeAvailableException e) {
      log.info("Elasticsearch cluster not yet ready: {}", e.getMessage());
      log.debug("Elasticsearch cluster not yet ready: ", e);
    }
    return false;
  }

  @Override
  public void close() {
    if (client != null) {
      client.close();
      log.info("Closed client {}", client.settings().get("node.name"));
    }
  }
}
