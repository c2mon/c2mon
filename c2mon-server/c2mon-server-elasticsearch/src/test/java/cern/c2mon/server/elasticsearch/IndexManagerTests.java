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
package cern.c2mon.server.elasticsearch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.core.io.ClassPathResource;

import cern.c2mon.server.elasticsearch.client.ElasticsearchClientRest;
import cern.c2mon.server.elasticsearch.client.ElasticsearchClientTransport;
import cern.c2mon.server.elasticsearch.util.EmbeddedElasticsearchManager;
import cern.c2mon.server.elasticsearch.util.IndexUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for implementations of {@link IndexManager}.
 */
@RunWith(Parameterized.class)
public class IndexManagerTests {
  private static final String MAPPINGS_FILE = "mappings/test.json";

  private static final String NAME = "Test Name";
  private static final String UPDATED_NAME = NAME + " Updated";
  private static final String TEST_JSON = "{\"id\":\"1000\",\"name\":\"" + NAME + "\", \"description\":\"Test description\"}";
  private static final String TEST_JSON_2 = "{\"id\":\"1000\",\"name\":\"" + UPDATED_NAME + "\", \"description\":\"Test description\"}";

  /**
   * Parameters setup. Must include an instance of each of the {@link IndexManager} implementations.
   *
   * @return list of instances of each of the {@link IndexManager} implementations.
   */
  @Parameters
  public static Collection<IndexManager> getIndexManagerClass() {
    return Arrays.asList(
        new IndexManagerRest(new ElasticsearchClientRest(ElasticsearchSuiteTest.getProperties())),
        new IndexManagerTransport(new ElasticsearchClientTransport(ElasticsearchSuiteTest.getProperties())));
  }

  private String indexName;
  private IndexManager indexManager;

  /**
   * Constructor for injecting parameters.
   *
   * @param indexManager instance for current test set execution.
   */
  public IndexManagerTests(IndexManager indexManager) {
    this.indexManager = indexManager;
  }

  @Before
  public void setUp() {
    indexName = "test_index";

    if (indexManager != null) {
      indexManager.purgeIndexCache();
    }
  }

  @After
  public void tearDown() {
    EmbeddedElasticsearchManager.getEmbeddedNode().deleteIndex(indexName);
    EmbeddedElasticsearchManager.getEmbeddedNode().refreshIndices();
  }

  @Test
  public void createTest() throws IOException {
    String mapping = loadMapping(MAPPINGS_FILE);

    indexManager.create(indexName, mapping);

    assertTrue("Index should have been created.",
        IndexUtils.doesIndexExist(indexName, ElasticsearchSuiteTest.getProperties()));
  }

  @Test
  public void indexTestWithoutId() throws IOException {
    String mapping = loadMapping(MAPPINGS_FILE);

    indexManager.create(indexName, mapping);

    EmbeddedElasticsearchManager.getEmbeddedNode().refreshIndices();

    indexManager.index(indexName, TEST_JSON, "1");

    EmbeddedElasticsearchManager.getEmbeddedNode().refreshIndices();

    assertEquals("Index should have one document inserted.", 1,
        EmbeddedElasticsearchManager.getEmbeddedNode().fetchAllDocuments(indexName).size());
  }

  @Test
  public void indexTestWithId() throws IOException {
    String mapping = loadMapping(MAPPINGS_FILE);

    indexManager.create(indexName, mapping);

    EmbeddedElasticsearchManager.getEmbeddedNode().refreshIndices();

    indexManager.index(indexName, TEST_JSON, "1", "1");

    EmbeddedElasticsearchManager.getEmbeddedNode().refreshIndices();

    assertEquals("Index should have one document inserted.", 1,
        EmbeddedElasticsearchManager.getEmbeddedNode().fetchAllDocuments(indexName).size());
  }

  @Test
  public void existsTestWithoutRouting() throws IOException {
    String mapping = loadMapping(MAPPINGS_FILE);

    indexManager.create(indexName, mapping);

    EmbeddedElasticsearchManager.getEmbeddedNode().refreshIndices();

    assertTrue("'exists()' method should report index as exiting.", indexManager.exists(indexName));
  }

  @Test
  public void existsTestWithCachePurging() throws IOException {
    String mapping = loadMapping(MAPPINGS_FILE);

    indexManager.create(indexName, mapping);

    EmbeddedElasticsearchManager.getEmbeddedNode().refreshIndices();

    indexManager.purgeIndexCache();

    assertTrue("'exists()' method should check index existence on the server once cache is purged.",
        indexManager.exists(indexName));
  }

  @Test
  public void existsTestWithRouting() throws IOException {
    String mapping = loadMapping(MAPPINGS_FILE);

    indexManager.create(indexName, mapping);

    EmbeddedElasticsearchManager.getEmbeddedNode().refreshIndices();

    assertTrue("'exists()' method should report index as exiting.",
        indexManager.exists(indexName, "1"));
  }

  @Test
  public void updateExistingIndexNonExistingDocument() throws IOException {
    String mapping = loadMapping(MAPPINGS_FILE);

    indexManager.create(indexName, mapping);

    EmbeddedElasticsearchManager.getEmbeddedNode().refreshIndices();

    indexManager.update(indexName, TEST_JSON_2, "1");

    EmbeddedElasticsearchManager.getEmbeddedNode().refreshIndices();

    List<String> indexData = EmbeddedElasticsearchManager.getEmbeddedNode().fetchAllDocuments(indexName);
    assertEquals("Upsert should create document which does not exist.", 1, indexData.size());

    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode jsonNode = objectMapper.readTree(indexData.get(0));

    assertEquals("Updated document should have updated values.", UPDATED_NAME, jsonNode.get("name").asText());
  }

  @Test
  public void updateExistingIndexExistingDocument() throws IOException {
    String mapping = loadMapping(MAPPINGS_FILE);

    indexManager.create(indexName, mapping);

    EmbeddedElasticsearchManager.getEmbeddedNode().refreshIndices();

    indexManager.index(indexName, TEST_JSON, "1", "1");

    EmbeddedElasticsearchManager.getEmbeddedNode().refreshIndices();

    indexManager.update(indexName, TEST_JSON_2, "1");

    EmbeddedElasticsearchManager.getEmbeddedNode().refreshIndices();

    List<String> indexData = EmbeddedElasticsearchManager.getEmbeddedNode().fetchAllDocuments(indexName);

    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode jsonNode = objectMapper.readTree(indexData.get(0));

    assertEquals("Updated document should have updated values.", UPDATED_NAME, jsonNode.get("name").asText());
  }

  @Test
  public void updateNonExistingIndex() throws UnknownHostException {
    indexManager.update(indexName, TEST_JSON_2, "1");

    EmbeddedElasticsearchManager.getEmbeddedNode().refreshIndices();

    assertEquals("Index should be created if updating non-existing index.", 1,
        EmbeddedElasticsearchManager.getEmbeddedNode().fetchAllDocuments(indexName).size());
  }

  private String loadMapping(String source) throws IOException {
    return new BufferedReader(new InputStreamReader(new ClassPathResource(source).getInputStream()))
        .lines()
        .collect(Collectors.joining(""));
  }
}