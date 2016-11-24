package cern.c2mon.server.elasticsearch.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Justin Lewis Salmon
 */
@Data
@ConfigurationProperties(prefix = "c2mon.server.elasticsearch")
public class ElasticsearchProperties {

  /**
   * Host name or IP address pointing to the Elasticsearch cluster
   */
  private String host = "localhost";

  /**
   * Port number on which to communicate
   */
  private int port = 9300;

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
   * Enable/disable startup of embedded Elasticsearch node
   */
  private boolean embedded = true;

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
   *
   * indexPrefix + "-" entity + "_" + bucket
   *
   * e.g.: c2mon-tag_2017-01
   */
  private String indexPrefix = "c2mon";

  /**
   * Timeseries index bucketing strategy. Possible values:
   *
   * - M (or m): monthly indices (YYYY-MM)
   * - D (or d): daily indices (YYYY-MM-DD)
   # - W (or w): weekly indices (YYYY-ww)
   */
  private String indexType = "M";

  /**
   * Number of shards per index
   */
  private int shardsPerIndex = 10;

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
  private int bulkFlushInterval = 10;

  /**
   * Maximum number of concurrent requests allowed to be executed at the
   * same time
   */
  private int concurrentRequests = 1;

  /**
   * Absolute path the the file to which tag updates will be written in the
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
}
