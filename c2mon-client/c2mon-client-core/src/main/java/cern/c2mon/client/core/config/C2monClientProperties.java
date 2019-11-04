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
package cern.c2mon.client.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import cern.c2mon.shared.client.config.ClientJmsProperties;

/**
 * Contains default values for all configurable properties.
 *
 * @author Justin Lewis Salmon
 */
@Data
@ConfigurationProperties(prefix = "c2mon.client")
public class C2monClientProperties {

  /**
   * Maximum number of tags per request to a server
   */
  private int maxTagsPerRequest = 500;

  /**
   * Maximum number of threads used to request tags from server
   */
  private int maxRequestThreads = 5;

  /**
   * JMS properties
   */
  private final ClientJmsProperties jms = new ClientJmsProperties();

  /**
   * Elasticsearch properties
   */
  private Elasticsearch elasticsearch = new Elasticsearch();

  @Data
  public static class Elasticsearch {

    /**
     * URL of the Elasticsearch instance
     */
    private String url = "http://localhost:9200";

    /** The username for connecting to Elasticsearch via REST */
    private String username = "";

    /** The password of the given username for connecting to Elasticsearch via REST */
    private String password = "";

    /** The maximum amount of results that shall be returned from Elasticsearch query */
    private int maxResults = 10_000;
  }
}
