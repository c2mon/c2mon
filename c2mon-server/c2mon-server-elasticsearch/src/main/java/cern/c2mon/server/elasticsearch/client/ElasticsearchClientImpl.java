/******************************************************************************
 * Copyright (C) 2010-2017 CERN. All rights not expressly granted are reserved.
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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.node.InternalSettingsPreparer;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeValidationException;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.transport.Netty4Plugin;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

  //static because we should only ever start 1 embedded node
  private static Node embeddedNode = null;

  @Autowired
  public ElasticsearchClientImpl(ElasticsearchProperties properties) throws NodeValidationException {
    this.properties = properties;
    this.client = createClient();

    if (properties.isEmbedded()) {
      startEmbeddedNode();
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

    TransportClient client = new PreBuiltTransportClient(settingsBuilder.build());
    try {
      client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(properties.getHost()), properties.getPort()));
    } catch (UnknownHostException e) {
      log.error("Error connecting to the Elasticsearch cluster at {}:{}", properties.getHost(), properties.getPort(), e);
      return null;
    }

    return client;
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
        //client.admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();
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
              Thread.sleep(100L);
            } catch (InterruptedException ignored) {
            }
          }
          log.info("Elasticsearch cluster is yellow");
        }
      );
      nodeReady.get(120, TimeUnit.SECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
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

  public boolean isClusterYellow() {
    ClusterHealthStatus status = getClusterHealth().getStatus();
    return status.equals(ClusterHealthStatus.YELLOW) || status.equals(ClusterHealthStatus.GREEN);
  }

  //@TODO "using Node directly within an application is not officially supported"
  //https://www.elastic.co/guide/en/elasticsearch/reference/5.5/breaking_50_java_api_changes.html
  //@TODO Embedded ES is no longer supported
  @Override
  public void startEmbeddedNode() throws NodeValidationException {
    if (this.embeddedNode != null) {
      log.info("Embedded Elasticsearch cluster already running");
      return;
    }
    log.info("Launching an embedded Elasticsearch cluster: {}", properties.getClusterName());

    Collection<Class<? extends Plugin>> plugins = Arrays.asList(Netty4Plugin.class);
    embeddedNode = new PluginConfigurableNode(Settings.builder()
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
     .put("http.cors.allow-origin", "/.*/")
     .build(), plugins);

      embeddedNode.start();
  }

  //solution from here: https://github.com/elastic/elasticsearch-hadoop/blob/fefcf8b191d287aca93a04144c67b803c6c81db5/mr/src/itest/java/org/elasticsearch/hadoop/EsEmbeddedServer.java
  private static class PluginConfigurableNode extends Node {
    public PluginConfigurableNode(Settings settings, Collection<Class<? extends Plugin>> classpathPlugins) {
      super(InternalSettingsPreparer.prepareEnvironment(settings, null), classpathPlugins);
    }
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
    if(embeddedNode != null) {
      embeddedNode.close();
      this.close();
    }
  }
}
