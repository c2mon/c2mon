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

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;

/**
 * Wrapper around {@link RestHighLevelClient}. Connects asynchronously, but also provides
 * methods to block until a healthy connection is established.
 *
 * @author Serhiy Boychenko
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "c2mon.server.elasticsearch.rest", havingValue = "true")
public final class ElasticsearchClientRest implements ElasticsearchClient<RestHighLevelClient> {
  @Getter
  private final ElasticsearchProperties properties;
  @Getter
  private final RestHighLevelClient client;

  /**
   * @param properties to initialize REST client.
   */
  @Autowired
  public ElasticsearchClientRest(ElasticsearchProperties properties) {
    this.properties = properties;

    RestClientBuilder restClientBuilder =
        RestClient.builder(new HttpHost(properties.getHost(), properties.getHttpPort(), "http"));

    client = new RestHighLevelClient(restClientBuilder);

    try {
      if (!client.ping(RequestOptions.DEFAULT)) {
        log.error("Error pinging to the Elasticsearch cluster at {}:{}", properties.getHost(), properties.getHttpPort());
      }
    } catch (IOException e) {
      log.error("IOError connecting to the Elasticsearch cluster at {}:{}", properties.getHost(), properties.getHttpPort(), e);
    }
  }

  @Override
  public void waitForYellowStatus() {
    ClusterHealthRequest request = new ClusterHealthRequest();
    request.timeout("60s");
    request.waitForYellowStatus();

    client.cluster().healthAsync(request, RequestOptions.DEFAULT, new ActionListener<ClusterHealthResponse>() {
      @Override
      public void onResponse(ClusterHealthResponse response) {
        log.info("Waiting for Elasticsearch yellow status completed successfully. ");
      }

      @Override
      public void onFailure(Exception e) {
        log.error("Exception when waiting for yellow status", e);
        throw new IllegalStateException("Timeout when waiting for Elasticsearch yellow status!");
      }
    });
  }

  private ClusterHealthStatus getClusterHealth() {
    ClusterHealthRequest request = new ClusterHealthRequest();
    request.timeout("60s");
    request.waitForYellowStatus();

    try {
      return client.cluster().health(request, RequestOptions.DEFAULT).getStatus();
    } catch (IOException e) {
      log.error("There was a problem executing Elasticsearch cluster health check request.", e);
    }

    return ClusterHealthStatus.RED;
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

  @Override
  public boolean isClusterYellow() {
    byte status = getClusterHealth().value();
    return status == ClusterHealthStatus.YELLOW.value() || status == ClusterHealthStatus.GREEN.value();
  }
}
