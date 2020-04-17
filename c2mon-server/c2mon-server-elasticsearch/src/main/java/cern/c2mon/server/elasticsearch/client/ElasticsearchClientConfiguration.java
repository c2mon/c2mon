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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;
import cern.c2mon.server.elasticsearch.util.EmbeddedElasticsearchManager;

/**
 * Configures the {@link ElasticsearchClient} bean instance based on provided configuration
 *
 * @author Serhiy Boychenko
 */
@Configuration
public class ElasticsearchClientConfiguration {

  public static final long CLIENT_SETUP_TIMEOUT = 120000;

  private final ElasticsearchProperties properties;

  /**
   * Constructor with properties parameter
   *
   * @param properties to be used to set up the client
   */
  @Autowired
  public ElasticsearchClientConfiguration(ElasticsearchProperties properties) {
    this.properties = properties;
  }

  /**
   * Ini
   *
   * @return the instance the instantiated {@link ElasticsearchClient} bean
   */
  @Bean
  public ElasticsearchClient getClient() {
    if (!properties.isEnabled()) {
      return new ElasticsearchClientStub();
    }

    if (properties.isEmbedded()) {
      EmbeddedElasticsearchManager.start(properties);
      return new ElasticsearchClientRest(properties);
    }

    if (ElasticsearchClientType.TRANSPORT.name().equalsIgnoreCase(properties.getClient())) {
      return new ElasticsearchClientTransport(properties);
    } else {
      return new ElasticsearchClientRest(properties);
    }
  }
}
