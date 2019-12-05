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
package cern.c2mon.server.elasticsearch.util;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic;
import pl.allegro.tech.embeddedelasticsearch.PopularProperties;

import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;

/**
 * Allows to start embedded Elasticsearch server. Should be used for <b>TESTING</b> purposes only.
 *
 * @author Serhiy Boychenko
 */
@Slf4j
public final class EmbeddedElasticsearchManager {

  private static final String ELASTICSEARCH_VERSION = "6.4.0";

  private static EmbeddedElastic embeddedNode;

  private EmbeddedElasticsearchManager() {
    /* Only static methods below. */
  }

  /**
   * Starts embedded Elasticsearch server (if it is not already running, does nothing otherwise).
   *
   * @param properties to setup the instance.
   */
  public static void start(ElasticsearchProperties properties) {
    synchronized (EmbeddedElasticsearchManager.class) {
      if (embeddedNode == null) {
        log.info("********** TESTING PURPOSE ONLY *********");
        log.info("Starting embedded Elasticsearch instance!");

        embeddedNode = EmbeddedElastic.builder()
            .withElasticVersion(ELASTICSEARCH_VERSION)
            .withSetting(PopularProperties.TRANSPORT_TCP_PORT, properties.getPort())
            .withSetting(PopularProperties.HTTP_PORT, properties.getHttpPort())
            .withSetting(PopularProperties.CLUSTER_NAME, properties.getClusterName())
            .withStartTimeout(2, TimeUnit.MINUTES)
            .build();

        try {
          embeddedNode.start();
        } catch (IOException | InterruptedException e) {
          log.error("An error occurred starting embedded Elasticsearch instance!", e);
        }
      }
    }
  }

  /**
   * Stops embedded Elasticsearch server.
   */
  public static void stop() {
    synchronized (EmbeddedElasticsearchManager.class) {
      if (embeddedNode != null) {
        embeddedNode.stop();
      }
    }
  }

  /**
   * @return an instance of running embedded Elasticsearch server.
   */
  public static EmbeddedElastic getEmbeddedNode() {
    if (embeddedNode == null) {
      throw new IllegalStateException("Embedded Elasticsearh instance must be started first!");
    }
    return embeddedNode;
  }
}
