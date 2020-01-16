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
package cern.c2mon.server.elasticsearch.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Justin Lewis Salmon
 * @author Serhiy Boychenko
 */
@Data
@ConfigurationProperties(prefix = "c2mon.server.elasticsearch")
public class ElasticsearchProperties {

  /**
   * Type is being removed in Elasticsearch 6.x (check
   * <a href="https://www.elastic.co/guide/en/elasticsearch/reference/master/removal-of-types.html">Elasticsearch
   * documentation</a> for more details).
   */
  public static final String TYPE = "doc";

  /**
   * Enable/Disable writing to Elasticsearch
   */
  private boolean enabled = true;

  /**
   * Host name or IP address pointing to the Elasticsearch cluster
   */
  private String host = "localhost";

  /**
   * Port number on which to communicate
   */
  private int port = 9300;

  private int httpPort = 9200;

  /**
   * Name of the Elasticsearch cluster to connect to. Must be the same for all
   * nodes meant to lie inside the same cluster
   */
  private String clusterName = "c2mon";

  /**
   * Name of this node
   */
  private String nodeName = "c2mon";

  /**
   * Absolute path where Elasticsearch will store its data (only relevant
   * when running an embedded node)
   */
  private String embeddedStoragePath = "/tmp/elasticsearch-node/";

  /**
   * Enable/disable HTTP transport (only relevant when running an embedded
   * node)
   */
  private boolean httpEnabled = false;

  /**
   * Prefix used for all C2MON indices. The final index format becomes:
   * <p>
   * indexPrefix + "-" entity + "_" + bucket
   * <p>
   * e.g.: c2mon-tag_2017-01
   */
  private String indexPrefix = "c2mon";

  /**
   * Name of the tag configuration index
   */
  public String getTagConfigIndex() {
    return this.indexPrefix + "-tag-config";
  }

  /**
   * Timeseries index bucketing strategy. Possible values:
   * <p>
   * - M (or m): monthly indices (YYYY-MM)
   * - D (or d): daily indices (YYYY-MM-DD)
   * - W (or w): weekly indices (YYYY-ww)
   */
  private String indexType = "M";

  /**
   * Number of shards per index
   */
  private int shardsPerIndex = 5;

  /**
   * Number of replicas for each primary shard
   */
  private int replicasPerShard = 1;

  /**
   * Maximum number of actions to accumulate before sending a batch of tags
   */
  private int bulkActions = 5600;

  /**
   * Maximum size for a batch of tags before sending it
   */
  private int bulkSize = 1;

  /**
   * Flush interval in seconds for a batch of tags
   */
  private int bulkFlushInterval = 5;

  /**
   * Maximum number of concurrent requests allowed to be executed at the
   * same time
   */
  private int concurrentRequests = 1;

  /**
   * Absolute path the file to which tag updates will be written in the
   * event of Elasticsearch communication failure
   */
  private String tagFallbackFile = "/tmp/es-tag-fallback.txt";

  /**
   * Absolute path the the file to which alarms will be written in the
   * event of Elasticsearch communication failure
   */
  private String alarmFallbackFile = "/tmp/es-alarm-fallback.txt";

  /**
   * Absolute path the the file to which supervision updates will be written
   * in the event of Elasticsearch communication failure
   */
  private String supervisionFallbackFile = "/tmp/es-supervision-fallback.txt";

  /**
   * Defines the client to be used to communicate with Elasticsearch (possible values: [rest, transport])
   */
  private String client = "rest";

  /**
   * Defines whether mapping templates are managed by C2MON
   */
  private boolean autoTemplateMapping = true;
}
