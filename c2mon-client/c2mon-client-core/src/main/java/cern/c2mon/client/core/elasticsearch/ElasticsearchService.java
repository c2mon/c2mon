package cern.c2mon.client.core.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.AvgAggregation;
import io.searchbox.core.search.aggregation.DateHistogramAggregation;
import io.searchbox.core.search.aggregation.DateHistogramAggregation.DateHistogram;
import io.searchbox.indices.mapping.GetMapping;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.core.config.C2monClientProperties;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * @author Justin Lewis Salmon
 */
@Service
@Slf4j
public class ElasticsearchService {

  private JestClient client;

  private final String timeSeriesIndex;

  private final String configIndex;

  @Autowired
  public ElasticsearchService(C2monClientProperties properties) {
    this.timeSeriesIndex = properties.getElasticsearch().getIndexPrefix() + "-tag*";
    this.configIndex = properties.getElasticsearch().getTagConfigIndex();

    JestClientFactory factory = new JestClientFactory();
    factory.setHttpClientConfig(new HttpClientConfig.Builder(properties.getElasticsearch().getUrl())
        .multiThreaded(true)
        .build());
    client = factory.getObject();
  }

  /**
   * Retrieve aggregated history for the given tag for the specified time period.
   * <p>
   * A suitable average aggregation interval is automatically calculated.
   *
   * @param id  the id of the tag
   * @param min the beginning of the requested date range (ms)
   * @param max the end of the requested date range (ms)
   *
   * @return list of [timestamp (ms), value] pairs
   */
  public List<Object[]> getHistory(Long id, Long min, Long max) {
    List<Object[]> results = new ArrayList<>();

    // Figure out the right interval
    String interval = getInterval(min, max);
    log.info("Using interval: " + interval);

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(termQuery("id", id))
        .size(0)
        .aggregation(AggregationBuilders.filter("time-range")
            .filter(rangeQuery("timestamp").from(min).to(max)).subAggregation(
                AggregationBuilders.dateHistogram("events-per-interval")
                    .field("timestamp")
                    .interval(new DateHistogramInterval(interval))
                    .subAggregation(
                        AggregationBuilders.avg("avg-value").field("value")
                    )));

    SearchResult result;
    Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex(timeSeriesIndex).build();
    long start = System.currentTimeMillis();

    try {
      result = client.execute(search);
    } catch (IOException e) {
      throw new RuntimeException("Error querying history for tag #" + id, e);
    }

    DateHistogramAggregation aggregation = result.getAggregations().getFilterAggregation("time-range").getDateHistogramAggregation("events-per-interval");
    for (DateHistogram bucket : aggregation.getBuckets()) {
      AvgAggregation avg = bucket.getAvgAggregation("avg-value");
      results.add(new Object[]{Long.parseLong(bucket.getTimeAsString()), avg.getAvg()});
    }

    log.info("Loaded {} values in {}ms", results.size(), System.currentTimeMillis() - start);
    return results;
  }

  private String getInterval(Long min, Long max) {
    String interval;
    Long range = max - min;

    // One minute range loads second data
    if (range <= 60 * 1000L) {
      interval = "1s";
    }
    // 2 hour range loads minute data
    else if (range <= 2 * 3600 * 1000L) {
      interval = "1m";
    }
    // 2 day range loads 10 minute data
    else if (range <= 2 * 24 * 3600 * 1000L) {
      interval = "10m";
    }
    // 2 month range loads hourly data
    else if (range <= 2 * 31 * 24 * 3600 * 1000L) {
      interval = "1h";
    }
    // One year range loads daily data
    else if (range <= 15 * 31 * 24 * 3600 * 1000L) {
      interval = "1d";
    }
    // Greater range loads weekly data
    else {
      interval = "1w";
    }

    return interval;
  }

  /**
   * Get the top {@literal size} most active tags.
   *
   * @param size the number of top tags to retrieve
   *
   * @return a list of tag ids
   */
  public List<Long> getTopTags(Integer size) {
    List<Long> tagIds = new ArrayList<>();

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.aggregation(AggregationBuilders.terms("group-by-id")
        .field("id")
        .size(size));

    SearchResult result;
    Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex(timeSeriesIndex).build();

    try {
      result = client.execute(search);
    } catch (IOException e) {
      throw new RuntimeException("Error querying top most active tags", e);
    }

    tagIds.addAll(result.getAggregations().getTermsAggregation("group-by-id").getBuckets()
        .stream()
        .map(bucket -> Long.valueOf(bucket.getKey())).collect(Collectors.toList()));

    return tagIds;
  }

  /**
   * Find all tags by name with a given prefix.
   *
   * @param query the tag name prefix
   *
   * @return a list of tags whose names match the given prefix
   */
  public Collection<Long> findByName(String query) {
    List<Long> tagIds = new ArrayList<>();

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder
        .query(prefixQuery("name", query));

    SearchResult result;
    Search search = new Search.Builder(searchSourceBuilder.toString())
            .addIndex(configIndex).build();

    try {
      result = client.execute(search);
    } catch (IOException e) {
      throw new RuntimeException("Error querying tags", e);
    }

    for(SearchResult.Hit<Map, Void> hit : result.getHits(Map.class)) {
      double id = (double) hit.source.get("id");
      tagIds.add((long) id);
    }

    return tagIds;
  }

  /**
   * Find all tags containing the exact metadata key/value pair.
   *
   * @param key   the metadata key
   * @param value the metadata value
   *
   * @return a list of tags containing the exact metadata requested
   */
  public Collection<Long> findByMetadata(String key, String value) {
    List<Long> tagIds = new ArrayList<>();

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder
        .query(prefixQuery("metadata." + key, value))
        .size(0)
        .aggregation(AggregationBuilders.terms("group-by-id")
            .field("id")
        );

    SearchResult result;
    Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex(configIndex).build();

    try {
      result = client.execute(search);
    } catch (IOException e) {
      throw new RuntimeException("Error querying tags", e);
    }

    for(SearchResult.Hit<Map, Void> hit : result.getHits(Map.class)) {
      double id = (double) hit.source.get("id");
      tagIds.add((long) id);
    }

    return tagIds;
  }

  /**
   * Return a set of all metadata keys that have been configured on all tag
   * indices.
   * <p>
   * This is done by inspecting the mappings themselves, since Elasticsearch
   * updates the mappings when new metadata keys are added.
   *
   * @return a set of all distinct metadata keys in use across all mappings
   */
  public Set<String> getDistinctMetadataKeys() {
    Set<String> keys = new HashSet<>();

    JestResult result;
    GetMapping get = new GetMapping.Builder().addIndex(timeSeriesIndex).build();

    try {
      result = client.execute(get);
    } catch (IOException e) {
      throw new RuntimeException("Error getting index mapping", e);
    }

    for (Map.Entry<String, JsonElement> index : result.getJsonObject().entrySet()) {
      for (Map.Entry<String, JsonElement> mapping : index.getValue().getAsJsonObject().entrySet()) {
        for (Map.Entry<String, JsonElement> type : mapping.getValue().getAsJsonObject().entrySet()) {
          JsonObject metadata = type.getValue().getAsJsonObject()
              .getAsJsonObject("properties")
              .getAsJsonObject("metadata")
              .getAsJsonObject("properties");

          if (metadata == null) continue;
          keys.addAll(metadata.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList()));
        }
      }
    }

    return keys;
  }
}
