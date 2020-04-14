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
package cern.c2mon.server.elasticsearch.monitor;

import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import cern.c2mon.server.elasticsearch.client.ElasticsearchClient;
import cern.c2mon.server.elasticsearch.client.ElasticsearchClientConfiguration;

/**
 * Performs period checks of configured ES client health
 *
 * @author sboychen
 */
@Slf4j
@Component
@EnableScheduling
public class ElasticsearchClientHealthMonitor {

  private final ElasticsearchProperties properties;

  private final ElasticsearchClient client;

  /**
   * Constructs ES client health monitor
   *
   * @param properties of Elasticsearch instance
   * @param client to be monitored
   */
  @Autowired
  public ElasticsearchClientHealthMonitor(ElasticsearchProperties properties, ElasticsearchClient client) {
    this.properties = properties;
    this.client = client;
  }

  @Scheduled(fixedRate = 10000, initialDelay = ElasticsearchClientConfiguration.CLIENT_SETUP_TIMEOUT)
  private void check() {
    if (!properties.isEnabled()) {
      return ;
    }

    if (!client.isClientHealthy()) {
      log.warn("ES client is not healthy! Setting up a new client . . .");
      client.setup();
      log.info("New ES client set up completed.");
    }
  }
}
