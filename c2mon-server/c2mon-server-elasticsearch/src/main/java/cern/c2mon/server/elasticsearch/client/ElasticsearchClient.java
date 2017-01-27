/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
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

import javax.annotation.PostConstruct;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**
 * @author Alban Marguet
 * @author Justin Lewis Salmon
 */
@Slf4j
@Service
public class ElasticsearchClient {

  @Autowired
  private ElasticsearchProperties properties;

  @Getter
  private Client client;

  @Getter
  private boolean isClusterYellow;

  @PostConstruct
  public void init() {
    client = createClient();

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
    final Settings.Builder settingsBuilder = Settings.settingsBuilder();

    settingsBuilder.put("node.name", properties.getNodeName())
        .put("cluster.name", properties.getClusterName())
        .put("http.enabled", properties.isHttpEnabled());

    log.debug("Creating client {} at {}:{} in cluster {}",
        properties.getNodeName(), properties.getHost(), properties.getPort(), properties.getClusterName());
    TransportClient client = TransportClient.builder().settings(settingsBuilder.build()).build();

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
  public void waitForYellowStatus() {
    while (!isClusterYellow) {
      log.debug("Waiting for yellow status of Elasticsearch cluster...");

      try {
        ClusterHealthStatus status = getClusterHealth().getStatus();
        if (status.equals(ClusterHealthStatus.YELLOW) || status.equals(ClusterHealthStatus.GREEN)) {
          isClusterYellow = true;
          break;
        }
      } catch (Exception e) {
        log.trace("Elasticsearch cluster not yet ready: {}", e.getMessage());
      }

      try {
        Thread.sleep(100L);
      } catch (InterruptedException ignored) {}
    }

    log.debug("Elasticsearch cluster is yellow");
  }

  private ClusterHealthResponse getClusterHealth() {
    return client.admin().cluster().prepareHealth()
        .setWaitForYellowStatus()
        .setTimeout(TimeValue.timeValueMillis(100))
        .get();
  }

  private void startEmbeddedNode() {
    log.info("Launching an embedded Elasticsearch cluster: {}", properties.getClusterName());

    nodeBuilder().settings(Settings.settingsBuilder()
        .put("path.home", properties.getEmbeddedStoragePath())
        .put("cluster.name", properties.getClusterName())
        .put("node.name", properties.getNodeName())
        .put("node.local", false)
        .put("node.data", true)
        .put("node.master", true)
        .put("network.host", "0.0.0.0")
        .put("http.enabled", true)
        .put("http.cors.enabled", true)
        .put("http.cors.allow-origin", "/.*/")
        .build()).node();
  }

  public void close(Client client) {
    if (client != null) {
      client.close();
      log.info("Closed client {}", client.settings().get("node.name"));
    }
  }
}
