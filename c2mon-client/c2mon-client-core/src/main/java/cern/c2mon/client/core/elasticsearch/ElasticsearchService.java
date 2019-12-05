/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
 * <p/>
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * <p/>
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.client.core.elasticsearch;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cern.c2mon.client.core.config.C2monClientProperties;

/**
 * @author Justin Lewis Salmon
 */
@Service
@Slf4j
public class ElasticsearchService {

  private final JestClient client;

  private final String timeSeriesIndex;

  private final String configIndex;

  private final String alarmIndex;

  private final int maxResults;

  @Autowired
  public ElasticsearchService(C2monClientProperties properties, @Value("${c2mon.domain}") String domain) {
    this.timeSeriesIndex = domain + "-tag*";
    this.configIndex = domain + "-tag-config";
    this.alarmIndex = domain + "-alarm*";
    this.maxResults = properties.getElasticsearch().getMaxResults();

    JestClientFactory factory = new JestClientFactory();
    factory.setHttpClientConfig(new HttpClientConfig.Builder(properties.getElasticsearch().getUrl())
        .multiThreaded(true)
        .defaultCredentials(properties.getElasticsearch().getUsername(), properties.getElasticsearch().getPassword())
        .build());
    client = factory.getObject();
  }

  /**
   * Retrieve aggregated history for the given tag for the specified time period.
   * <p>
   * A suitable average aggregation interval is automatically calculated if
   * the given aggregate parameter is set to "auto"
   *
   * @param id        the id of the tag
   * @param min       the beginning of the requested date range (ms)
   * @param max       the end of the requested date range (ms)
   * @param aggregate the aggregation interval (bucket size). Possible values
   *                  are "auto", "1s", "1m", "1h", "none"
   * @return list of [timestamp (ms), value] pairs
   */
  public List<Object[]> getTagHistory(Long id, Long min, Long max, String aggregate) {
    if (aggregate.equals("none")) {
      return getRawHistory(id, min, max);
    } else {
      return getAggregatedHistory(id, min, max, aggregate);
    }
  }

  private List<Object[]> getAggregatedHistory(Long id, Long min, Long max, String aggregate) {
    // Figure out the right interval
    String interval = aggregate.equals("auto") ? getInterval(min, max) : aggregate;
    log.info("Using interval: " + interval);
    String query = String.format("{\n" +
        "  \"size\" : " + maxResults + ",\n" +
        "  \"query\" : {\n" +
        "    \"term\" : {\n" +
        "      \"id\" : %d\n" +
        "    }\n" +
        "  },\n" +
        "  \"aggregations\" : {\n" +
        "    \"time-range\" : {\n" +
        "      \"filter\" : {\n" +
        "        \"range\" : {\n" +
        "          \"timestamp\" : {\n" +
        "            \"from\" : %d,\n" +
        "            \"to\" : %d,\n" +
        "            \"include_lower\" : true,\n" +
        "            \"include_upper\" : true\n" +
        "          }\n" +
        "        }\n" +
        "      },\n" +
        "      \"aggregations\" : {\n" +
        "        \"events-per-interval\" : {\n" +
        "          \"date_histogram\" : {\n" +
        "            \"field\" : \"timestamp\",\n" +
        "            \"interval\" : \"%s\"\n" +
        "          },\n" +
        "          \"aggregations\" : {\n" +
        "            \"avg-value\" : {\n" +
        "              \"avg\" : {\n" +
        "                \"field\" : \"value\"\n" +
        "              }\n" +
        "            }\n" +
        "          }\n" +
        "        }\n" +
        "      }\n" +
        "    }\n" +
        "  }\n" +
        "}", id, min, max, aggregate);
    Search search = new Search.Builder(query).addIndex(timeSeriesIndex).build();
    long start = System.currentTimeMillis();
    try {
      List<Object[]> results = new ArrayList<>();
      SearchResult result = client.execute(search);
      DateHistogramAggregation aggregation = result.getAggregations().getFilterAggregation("time-range").getDateHistogramAggregation("events-per-interval");
      for (DateHistogram bucket : aggregation.getBuckets()) {
        AvgAggregation avg = bucket.getAvgAggregation("avg-value");
        results.add(new Object[]{Long.parseLong(bucket.getTimeAsString()), avg.getAvg()});
      }
      log.info("Loaded {} values in {}ms", results.size(), System.currentTimeMillis() - start);
      return results;
    } catch (IOException e) {
      throw new RuntimeException("Error querying history for tag #" + id, e);
    }
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

  private List<Object[]> getRawHistory(Long id, Long min, Long max) {
    String query = String.format("{\n" +
        "  \"size\" : " + maxResults + ",\n" +
        "  \"query\" : {\n" +
        "    \"bool\" : {\n" +
        "      \"must\" : [ {\n" +
        "        \"term\" : {\n" +
        "          \"id\" : %d\n" +
        "        }\n" +
        "      }, {\n" +
        "        \"range\" : {\n" +
        "          \"timestamp\" : {\n" +
        "            \"from\" : %d,\n" +
        "            \"to\" : %d,\n" +
        "            \"include_lower\" : true,\n" +
        "            \"include_upper\" : true\n" +
        "          }\n" +
        "        }\n" +
        "      } ]\n" +
        "    }\n" +
        "  },\n" +
        "  \"sort\" : [ {\n" +
        "    \"timestamp\" : {\n" +
        "      \"order\" : \"asc\"\n" +
        "    }\n" +
        "  } ]\n" +
        "}", id, min, max);
    Search search = new Search.Builder(query).addIndex(timeSeriesIndex).build();
    try {
      SearchResult result = client.execute(search);
      List<Object[]> results = new ArrayList<>();
      for (SearchResult.Hit<Map, Void> hit : result.getHits(Map.class)) {
        results.add(new Object[]{hit.source.get("timestamp"), hit.source.get("value")});
      }
      return results;
    } catch (IOException e) {
      throw new RuntimeException("Error querying raw tag history", e);
    }

  }

  /**
   * Send query to elasticsearch and convert the results with supplied converter
   *
   * @param query           string with elasticsearch query
   * @param outputConverter function to convert query results into output format
   * @param indexName       name of index to use
   * @param errorMessage    message to log in case of an error
   * @param <T>             type of output format
   * @return converted query results
   */
  public <T> T findTagsByQuery(String query, Function<SearchResult, T> outputConverter, String indexName, String errorMessage) {
    Search search = new Search.Builder(query).addIndex(indexName).build();
    try {
      SearchResult result = client.execute(search);
      return outputConverter.apply(result);
    } catch (IOException e) {
      throw new RuntimeException(errorMessage, e);
    }
  }

  /**
   * Get the top {@literal size} most active tags.
   *
   * @param size the number of top tags to retrieve
   * @return a list of tag ids
   */
  public List<Long> getTopTags(Integer size) {
    String query = String.format("{\n" +
        "  \"sort\" : [ {\n" +
        "    \"timestamp\" : {\n" +
        "      \"order\" : \"desc\"\n" +
        "    }\n" +
        "  } ],\n" +
        "  \"aggregations\" : {\n" +
        "    \"group-by-id\" : {\n" +
        "      \"terms\" : {\n" +
        "        \"field\" : \"id\",\n" +
        "        \"size\" : %d\n" +
        "      }\n" +
        "    }\n" +
        "  }\n" +
        "}", size);
    Function<SearchResult, List<Long>> converter = result ->
        new ArrayList<>(result.getAggregations().getTermsAggregation("group-by-id").getBuckets()
            .stream()
            .map(bucket -> Long.valueOf(bucket.getKey()))
            .collect(Collectors.toList()));
    return findTagsByQuery(query, converter, timeSeriesIndex, "Error querying top most active tags");
  }

  /**
   * Send query to elasticsearch and collect a list of tag ids matching the query
   *
   * @param query        string with elasticsearch query
   * @param errorMessage message to log in case of an error
   * @return list of tag ids returned from elasticsearch
   */
  public Collection<Long> findTagsByQuery(String query, String errorMessage) {
    Function<SearchResult, Collection<Long>> converter =
        result -> {
          if (!result.isSucceeded()) {
            return new ArrayList<>();
          } else {
            return new ArrayList<>(result.getHits(Map.class)
                .stream()
                .map(hit -> (long) (double) hit.source.get("id"))
                .collect(Collectors.toList()));
          }
        };
    return findTagsByQuery(query, converter, configIndex, errorMessage);
  }

  public Collection<Long> findTagsByNameAndMetadata(String tagNameRegex, String key, String value) {
    String query = String.format("{\n" +
        "  \"size\" : " + maxResults + ",\n" +
        "  \"query\" : {\n" +
        "    \"bool\" : {\n" +
        "      \"must\" : [ {\n" +
        "        \"regexp\" : {\n" +
        "          \"name\" : {\n" +
        "            \"value\" : \"%s\",\n" +
        "            \"flags_value\" : 65535\n" +
        "          }\n" +
        "        }\n" +
        "      }, {\n" +
        "        \"match\" : {\n" +
        "          \"metadata.%s\" : {\n" +
        "            \"query\" : \"%s\"\n" +
        "          }\n" +
        "        }\n" +
        "      } ]\n" +
        "    }\n" +
        "  }\n" +
        "}", tagNameRegex, key, value);
    return findTagsByQuery(query, "Error when collecting tags for given name and metadata");
  }

  /**
   * Find all tags by name with a given prefix.
   *
   * @param regexQuery the tag name prefix
   * @return a list of tags whose names match the given prefix
   */
  public Collection<Long> findTagsByName(String regexQuery) {
    String query = String.format("{\n" +
        "  \"size\" : " + maxResults + ",\n" +
        "  \"query\" : {\n" +
        "    \"regexp\" : {\n" +
        "      \"name\" : {\n" +
        "        \"value\" : \"%s\",\n" +
        "        \"flags_value\" : 65535\n" +
        "      }\n" +
        "    }\n" +
        "  }\n" +
        "}", regexQuery);
    return findTagsByQuery(query, "Error when collecting tags for given name and metadata");
  }

  /**
   * Find all tags containing the exact metadata key/value pair.
   *
   * @param key   the metadata key
   * @param value the metadata value
   * @return a list of tags containing the exact metadata requested
   */
  public Collection<Long> findTagsByMetadata(String key, String value) {
    String queryString = String.format("{\n" +
        "  \"size\" : " + maxResults + ",\n" +
        "  \"query\" : {\n" +
        "    \"match\" : {\n" +
        "      \"metadata.%s\" : {\n" +
        "        \"query\" : \"%s\"\n" +
        "      }\n" +
        "    }\n" +
        "  }\n" +
        "}", key, value);
    return findTagsByQuery(queryString, "Error when collecting tags for given metadata");
  }

  /**
   * Find all tags containing the provided metadata key.
   *
   * @param key   the metadata key
   * @return a list of tags containing the metadata requested
   */
  public Collection<Long> findTagsByMetadata(String key) {
    String queryString = String.format("{\n" +
        "  \"size\" : " + maxResults + ",\n" +
        "  \"query\" : {\n" +
        "    \"exists\" : {\n" +
        "      \"field\" : \"metadata.%s\"\n" +
        "    }\n" +
        "  }\n" +
        "}", key);
    return findTagsByQuery(queryString, "Error when collecting tags for given metadata");
  }

  /**
   * Find all tags containing the provided alarm metadata key.
   *
   * @param key The alarm metadata key
   * @return A list of tags containing the metadata requested
   */
  public Collection<Long> findTagsByAlarmMetadata(String key) {
    String queryString = String.format("{\n" +
        "  \"size\" : " + maxResults + ",\n" +
        "  \"query\" : {\n" +
        "    \"exists\" : {\n" +
        "      \"field\" : \"alarms.metadata.%s\"\n" +
        "    }\n" +
        "  }\n" +
        "}", key);
    return findTagsByQuery(queryString, "Error when collecting tags for given alarm metadata");
  }

  /**
   * Find all tags containing the exact metadata key/value pair.
   *
   * @param key   the alarm metadata key
   * @param value the alarm metadata value
   * @return a list of tags containing the exact metadata requested
   */
  public Collection<Long> findTagsByAlarmMetadata(String key, String value) {
    String queryString = String.format("{\n" +
        "  \"size\" : " + maxResults + ",\n" +
        "  \"query\" : {\n" +
        "    \"match\" : {\n" +
        "      \"alarms.metadata.%s\" : {\n" +
        "        \"query\" : \"%s\"\n" +
        "      }\n" +
        "    }\n" +
        "  }\n" +
        "}", key, value);
    return findTagsByQuery(queryString, "Error when collecting tags for given alarm metadata");
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
  public Set<String> getDistinctTagMetadataKeys() {
    GetMapping get = new GetMapping.Builder().addIndex(timeSeriesIndex).build();
    try {
      Set<String> keys = new HashSet<>();
      JestResult result = client.execute(get);
      for (Map.Entry<String, JsonElement> index : result.getJsonObject().entrySet()) {
        for (Map.Entry<String, JsonElement> mapping : index.getValue().getAsJsonObject().entrySet()) {
          for (Map.Entry<String, JsonElement> type : mapping.getValue().getAsJsonObject().entrySet()) {
            JsonObject metadata = type.getValue().getAsJsonObject()
                .getAsJsonObject("properties")
                .getAsJsonObject("metadata")
                .getAsJsonObject("properties");
            if (metadata == null) {
              continue;
            }
            keys.addAll(metadata.entrySet()
                .stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList()));
          }
        }
      }
      return keys;
    } catch (IOException e) {
      throw new RuntimeException("Error getting index mapping", e);
    }
  }

  public List<Long> getTopAlarms(Integer size) {
    String query = String.format("{\n" +
        "  \"aggregations\" : {\n" +
        "    \"group-by-id\" : {\n" +
        "      \"terms\" : {\n" +
        "        \"field\" : \"id\",\n" +
        "        \"size\" : %d\n" +
        "      }\n" +
        "    }\n" +
        "  }\n" +
        "}", size);
    Function<SearchResult, List<Long>> converter = result ->
        new ArrayList<>(result.getAggregations().getTermsAggregation("group-by-id").getBuckets()
            .stream()
            .map(bucket -> Long.valueOf(bucket.getKey()))
            .collect(Collectors.toList()));
    return findTagsByQuery(query, converter, alarmIndex,"Error querying top most active alarms");
  }

  public List<Object[]> getAlarmHistory(Long id, Long min, Long max) {
    String query = String.format("{\n" +
        "  \"size\" : " + maxResults + ",\n" +
        "  \"query\" : {\n" +
        "    \"bool\" : {\n" +
        "      \"must\" : [ {\n" +
        "        \"term\" : {\n" +
        "          \"id\" : %d\n" +
        "        }\n" +
        "      }, {\n" +
        "        \"range\" : {\n" +
        "          \"timestamp\" : {\n" +
        "            \"from\" : %d,\n" +
        "            \"to\" : %d,\n" +
        "            \"include_lower\" : true,\n" +
        "            \"include_upper\" : true\n" +
        "          }\n" +
        "        }\n" +
        "      } ]\n" +
        "    }\n" +
        "  },\n" +
        "  \"sort\" : [ {\n" +
        "    \"timestamp\" : {\n" +
        "      \"order\" : \"desc\"\n" +
        "    }\n" +
        "  } ]\n" +
        "}", id, min, max);
    Function<SearchResult,List<Object[]>> converter = result ->
        new ArrayList<>(result.getHits(Map.class)
        .stream()
        .map(hit -> new Object[]{hit.source.get("timestamp"), hit.source.get("active")})
        .collect(Collectors.toList()));
    return findTagsByQuery(query, converter,  alarmIndex,"Error querying alarm history");
  }

  public Collection<Long> findAlarmsByName(String name) {
    String query = String.format("{\n" +
        "  \"size\" : " + maxResults + ",\n" +
        "  \"query\" : {\n" +
        "    \"bool\" : {\n" +
        "      \"should\" : [ {\n" +
        "        \"regexp\" : {\n" +
        "          \"faultFamily\" : {\n" +
        "            \"value\" : \"%s\",\n" +
        "            \"flags_value\" : 65535\n" +
        "          }\n" +
        "        }\n" +
        "      }, {\n" +
        "        \"regexp\" : {\n" +
        "          \"faultMember\" : {\n" +
        "            \"value\" : \"%s\",\n" +
        "            \"flags_value\" : 65535\n" +
        "          }\n" +
        "        }\n" +
        "      } ]\n" +
        "    }\n" +
        "  },\n" +
        "  \"sort\" : [ {\n" +
        "    \"timestamp\" : {\n" +
        "      \"order\" : \"desc\"\n" +
        "    }\n" +
        "  } ]\n" +
        "}", name, name);
    Function<SearchResult, Collection<Long>> converter = result ->
        new ArrayList<>(result.getHits(Map.class)
        .stream()
        .map(hit -> (long) hit.source.get("id"))
        .collect(Collectors.toList()));
    return findTagsByQuery(query, converter, alarmIndex,"Error querying alarms by name");
  }
}
