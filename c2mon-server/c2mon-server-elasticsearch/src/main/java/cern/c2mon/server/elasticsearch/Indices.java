package cern.c2mon.server.elasticsearch;

import cern.c2mon.server.elasticsearch.alarm.AlarmDocument;
import cern.c2mon.server.elasticsearch.client.ElasticsearchClient;
import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;
import cern.c2mon.server.elasticsearch.supervision.SupervisionEventDocument;
import cern.c2mon.server.elasticsearch.tag.TagDocument;
import cern.c2mon.server.elasticsearch.tag.config.TagConfigDocument;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ResourceAlreadyExistsException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Static utility singleton for working with Elasticsearch indices.
 *
 * @author Justin Lewis Salmon
 */
@Slf4j
@Component
public class Indices {

  @Getter
  private ElasticsearchClient client;

  @Getter
  private ElasticsearchProperties properties;

  private final List<String> indexCache = new CopyOnWriteArrayList<>();

  private static Indices self;

  @Autowired
  public Indices(ElasticsearchClient client, ElasticsearchProperties properties) {
    this.client = client;
    this.properties = properties;
    self = this;
  }

  /**
   * Create a new index with an empty mapping.
   *
   * @param indexName the name of the index to create
   *
   * @return true if the index was successfully created, false otherwise
   */
  public static boolean create(String indexName) {
    return create(indexName, null, null);
  }

  /**
   * Create a new index with an initial mapping.
   *
   * @param indexName the name of the index to create
   * @param type      the mapping type
   * @param mapping   the mapping source
   *
   * @return true if the index was successfully created, false otherwise
   */
  public static boolean create(String indexName, String type, String mapping) {
    synchronized (Indices.class) {
      if (exists(indexName)) {
        return true;
      }

      CreateIndexRequestBuilder builder = self.client.getClient().admin().indices().prepareCreate(indexName);
      builder.setSettings(Settings.builder()
          .put("number_of_shards", self.properties.getShardsPerIndex())
          .put("number_of_replicas", self.properties.getReplicasPerShard())
          .build());

      if (mapping != null) {
        builder.addMapping(type, mapping, XContentType.JSON);
      }

      log.debug("Creating new index with name {}", indexName);
      boolean created;

      try {
        CreateIndexResponse response = builder.get();
        created = response.isAcknowledged();
      } catch (ResourceAlreadyExistsException ex) {
        created = true;
      }

      self.client.waitForYellowStatus();

      if (created) {
        self.indexCache.add(indexName);
      }

      return created;
    }
  }

  /**
   * Check if a given index exists.
   * <p>
   * The node-local index cache will be searched first before querying
   * Elasticsearch directly.
   *
   * @param indexName the name of the index
   *
   * @return true if the index exists, false otherwise
   */
  public static boolean exists(String indexName) {
    synchronized (Indices.class) {
      boolean exists = self.indexCache.contains(indexName);
      if (!exists) {
        self.client.waitForYellowStatus();
        IndexMetaData indexMetaData = self.client.getClient().admin().cluster()
            .state(Requests.clusterStateRequest())
            .actionGet()
            .getState()
            .getMetaData()
            .index(indexName);

        if (indexMetaData != null) {
          self.indexCache.add(indexName);
          exists = true;
        }
      }
      return exists;
    }
  }

  /**
   * Delete an index in Elasticsearch.
   *
   * @param indexName
   *
   * @return true if the request was acknowledged.
   */
  public static boolean delete(String indexName) {
    synchronized (Indices.class) {
      try {
        DeleteIndexResponse response = self.client.getClient().admin().indices().delete(new DeleteIndexRequest(indexName)).get();
        if (response.isAcknowledged()) {
          self.indexCache.remove(indexName);
          return true;
        } else {
          return false;
        }
      } catch (InterruptedException|ExecutionException e) {
        log.error("Error while deleting index", e);
        return false;
      }
    }
  }
  /**
   * Generate an index for the given {@link TagDocument} based on its
   * timestamp.
   *
   * @param tag the tag to generate an index for
   *
   * @return the generated index name
   */
  public static String indexFor(TagDocument tag) {
    String prefix = self.properties.getIndexPrefix() + "-tag_";
    return getIndexName(prefix, (Long) tag.get("timestamp"));
  }

  /**
   * Generate an index for the given {@link TagConfigDocument}.
   *
   * @param tag the tag to generate an index for
   *
   * @return the generated index name
   */
  public static String indexFor(TagConfigDocument tag) {
    return self.properties.getTagConfigIndex();
  }

  /**
   * Generate an index for the given {@link AlarmDocument} based on its
   * timestamp.
   *
   * @param alarm the alarm to generate an index for
   *
   * @return the generated index name
   */
  public static String indexFor(AlarmDocument alarm) {
    String prefix = self.properties.getIndexPrefix() + "-alarm_";
    return getIndexName(prefix, (Long) alarm.get("timestamp"));
  }

  /**
   * Generate an index for the given {@link SupervisionEventDocument}
   * based on its timestamp.
   *
   * @param supervisionEvent the supervision event to generate an index for
   *
   * @return the generated index name
   */
  public static String indexFor(SupervisionEventDocument supervisionEvent) {
    String prefix = self.properties.getIndexPrefix() + "-supervision_";
    return getIndexName(prefix, (Long) supervisionEvent.get("timestamp"));
  }

  /**
   * Generate an index for the given prefix and timestamp, based on the current
   * time series indexing strategy.
   *
   * @param prefix    the index prefix
   * @param timestamp the timestamp which will be used to generate the index
   *
   * @return the generated index name
   */
  private static String getIndexName(String prefix, long timestamp) {
    String indexType = self.properties.getIndexType();
    String dateFormat;

    switch (indexType.toLowerCase()) {
      case "d":
        dateFormat = "yyyy-MM-dd";
        break;
      case "w":
        dateFormat = "yyyy-'W'ww";
        break;
      case "m":
      default:
        dateFormat = "yyyy-MM";
        break;
    }

    return prefix + new SimpleDateFormat(dateFormat).format(new Date(timestamp));
  }

  static ElasticsearchProperties getProperties() {
    return self.properties;
  }
}
