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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.codelibs.elasticsearch.runner.ElasticsearchClusterRunner;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.Settings.Builder;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.node.NodeValidationException;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;

import static org.codelibs.elasticsearch.runner.ElasticsearchClusterRunner.newConfigs;

/**
 * Wrapper around {@link Client}. Connects asynchronously, but also provides
 * methods to block until a healthy connection is established.
 *
 * @author Alban Marguet
 * @author Justin Lewis Salmon
 * @author James Hamilton
 */
@Slf4j
public class ElasticsearchClientImpl implements ElasticsearchClient {

  @Getter
  private ElasticsearchProperties properties;

  @Getter
  private Client client;

  @Getter
  private static ElasticsearchClusterRunner runner = null;

  public ElasticsearchClientImpl(ElasticsearchProperties properties) throws NodeValidationException {
    this.properties = properties;

    if (properties.isEmbedded()) {
      startEmbeddedNode();
      this.client = runner.client();
    } else {
      this.client = createClient();
    }

    connectAsynchronously();
  }

  /**
   * Creates a {@link Client} to communicate with the Elasticsearch cluster.
   *
   * @return the {@link Client} instance
   */
  private Client createClient() {
    final Settings.Builder settingsBuilder = Settings.builder();

    settingsBuilder.put("node.name", properties.getNodeName())
        .put("cluster.name", properties.getClusterName())
        .put("http.enabled", properties.isHttpEnabled());

    TransportClient transportClient = new PreBuiltTransportClient(settingsBuilder.build());
    try {
      transportClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(properties.getHost()), properties.getPort()));
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

  /**
   * Block and wait for the cluster to become yellow.
   */
  @Override
  public void waitForYellowStatus() {
    try {
      CompletableFuture<Void> nodeReady = CompletableFuture.runAsync(() -> {
          while (true) {
            log.info("Waiting for yellow status of Elasticsearch cluster...");

            try {
              if (isClusterYellow()) {
                break;
              }
            } catch (Exception e) {
              log.info("Elasticsearch cluster not yet ready: {}", e.getMessage());
            }

            try {
              log.info("Waiting 3 sec before retrying to connect to Elasticsearch...");
              Thread.sleep(3000L);
            } catch (InterruptedException ignored) {
            }
          }
          log.info("Elasticsearch cluster is yellow");
        }
      );
      nodeReady.get(120, TimeUnit.SECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      log.error("Exception when waiting for yellow status", e);
      throw new RuntimeException("Timeout when waiting for Elasticsearch yellow status!");
    }
  }

  @Override
  public ClusterHealthResponse getClusterHealth() {
    return client.admin().cluster().prepareHealth()
        .setWaitForYellowStatus()
        .setTimeout(TimeValue.timeValueMillis(100))
        .get();
  }

  @Override
  public boolean isClusterYellow() {
    ClusterHealthStatus status = getClusterHealth().getStatus();
    return status.equals(ClusterHealthStatus.YELLOW) || status.equals(ClusterHealthStatus.GREEN);
  }

  @Override
  public void startEmbeddedNode() throws NodeValidationException {
    if (runner != null) {
      log.info("Embedded Elasticsearch cluster already running");
      return;
    }
    log.info("Launching an embedded Elasticsearch cluster: {}", properties.getClusterName());

    // create runner instance
    runner = new ElasticsearchClusterRunner();
    // create ES nodes
    runner.onBuild(new ElasticsearchClusterRunner.Builder() {
      @Override
      public void build(int number, Builder settingsBuilder) {
        // put elasticsearch settings
        settingsBuilder
     .put("path.home", properties.getEmbeddedStoragePath())
     .put("cluster.name", properties.getClusterName())
     .put("node.name", properties.getNodeName())
     .put("transport.type", "netty4")
     .put("node.data", true)
     .put("node.master", true)
     .put("network.host", "0.0.0.0")
     .put("http.type", "netty4")
     .put("http.enabled", true)
     .put("http.cors.enabled", true)
     .put("http.cors.allow-origin", "/.*/");
      }
    }).build(newConfigs().clusterName(properties.getClusterName()).numOfNode(2));

    // wait for yellow status
    runner.ensureYellow();
  }

  @Override
  public void close() {
    if (client != null) {
      client.close();
      log.info("Closed client {}", client.settings().get("node.name"));
      client = null;
    }
  }

  @Override
  public void closeEmbeddedNode() throws IOException {
    if(runner != null) {
      runner.close();
      runner.clean();
      this.close();
    }
  }
}
