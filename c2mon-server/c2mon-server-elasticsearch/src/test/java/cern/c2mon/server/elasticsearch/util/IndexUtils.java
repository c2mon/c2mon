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

import cern.c2mon.server.elasticsearch.ElasticsearchSuiteTest;
import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Serhiy Boychenko
 */
public class IndexUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexUtils.class);

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private IndexUtils() {
    // only static methods below
  }

  public static boolean doesIndexExist(String indexName) throws IOException {
    HttpHead httpRequest = new HttpHead("http://" + ElasticsearchSuiteTest.getProperties().getHost() +
            ":" + ElasticsearchSuiteTest.getProperties().getPort() + "/" + indexName);
    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponse httpResponse = httpClient.execute(httpRequest);
    return httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
  }

  public static long countDocuments(String indexName) throws IOException, JSONException {
    HttpGet httpRequest = new HttpGet(("http://" + ElasticsearchSuiteTest.getProperties().getHost() + ":" + ElasticsearchSuiteTest.getProperties().getPort() + "/" + indexName + "/_count"));
    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponse httpResponse = httpClient.execute(httpRequest);
    return new JSONObject(IOUtils.toString(httpResponse.getEntity().getContent(), Charset.defaultCharset())).getLong("count");
  }

  public static List<String> fetchAllDocuments(String... indices) {
    return indices.length == 0 ? (List) searchForDocuments(Optional.empty()).collect(Collectors.toList()) : (List) Stream.of(indices).flatMap((index) -> {
      return searchForDocuments(Optional.of(index));
    }).collect(Collectors.toList());
  }

  private static Stream<String> searchForDocuments(Optional<String> indexMaybe) {
    String searchCommand = prepareQuery(indexMaybe);
    String body = fetchDocuments(searchCommand);
    return parseDocuments(body);
  }

  private static String prepareQuery(Optional<String> indexMaybe) {
    return (String)indexMaybe.map((index) -> {
      return "/" + index + "/_search";
    }).orElse("/_search");
  }

  private static String fetchDocuments(String searchCommand) {
    HttpGet request = new HttpGet(url(ElasticsearchSuiteTest.getProperties(), searchCommand));
    try {
      return (String) HttpClientBuilder.create().build().execute(request, (response) -> {
        assertOk(response, "Error during search (" + searchCommand + ")");
        return readBodySafely(response);
      });
    } catch (IOException e) {
        LOGGER.error("An error occurred while fetching the documents", e);
    }
    return "";
  }

  private static Stream<String> parseDocuments(String body) {
    try {
      JsonNode jsonNode = OBJECT_MAPPER.readTree(body);
      return StreamSupport.stream(jsonNode.get("hits").get("hits").spliterator(), false).map((hitNode) -> {
        return hitNode.get("_source");
      }).map(JsonNode::toString);
    } catch (IOException var3) {
      throw new RuntimeException(var3);
    }
  }

  private static String url(ElasticsearchProperties properties, String path) {
    return "http://" + properties.getHost() + ":" + properties.getPort() + path;
  }

  private static void assertOk(HttpResponse response, String message) {
    if (response.getStatusLine().getStatusCode() != 200) {
      throw new IllegalStateException(message + "\nResponse body:\n" + readBodySafely(response));
    }
  }

  private static String readBodySafely(HttpResponse response) {
    try {
      return IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
    } catch (IOException var3) {
      //log.error("Error during reading response body", var3);
      return "";
    }
  }
}
