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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import cern.c2mon.server.elasticsearch.domain.IndexMetadata;
import cern.c2mon.server.elasticsearch.util.IndexUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link IndexManager}, executed by {@link ElasticsearchSuiteTest}.
 *
 * NOTE: The naming convention (&lt;class name&gt;TestSuite) is used specifically to prevent test execution plugins
 * (like Surefire) to execute the tests individually.
 *
 * @author Serhiy Boychenko
 */
public class IndexManagerTestSuite extends ElasticsearchTestDefinition {

  private static final String MAPPINGS_FILE = "mappings/test.json";
  private static final String NAME = "Test Name";
  private static final String UPDATED_NAME = NAME + " Updated";
  private static final String TEST_JSON = "{\"id\":\"1000\",\"name\":\"" + NAME + "\", \"description\":\"Test description\"}";
  private static final String TEST_JSON_2 = "{\"id\":\"1000\",\"name\":\"" + UPDATED_NAME + "\", \"description\":\"Test description\"}";

  @Autowired
  private IndexManager indexManager;

  private String indexName;

  @Before
  public void setUp() {
    indexName = "test_index";
    indexManager.purgeIndexCache();
  }

  @Test
  public void createTest() throws IOException {
    String mapping = loadMapping(MAPPINGS_FILE);

    indexManager.create(IndexMetadata.builder().name(indexName).build(), mapping);

    assertTrue("Index should have been created.",
        IndexUtils.doesIndexExist(indexName, ElasticsearchSuiteTest.getProperties()));
  }

  @Test
  public void indexTestWithoutId() throws IOException {
    String mapping = loadMapping(MAPPINGS_FILE);

    indexManager.create(IndexMetadata.builder().name(indexName).build(), mapping);

    esTestClient.refreshIndices();

    indexManager.index(IndexMetadata.builder().name(indexName).routing("1").build(), TEST_JSON);

    esTestClient.refreshIndices();

    assertEquals("Index should have one document inserted.", 1,
        esTestClient.fetchAllDocuments(indexName).size());
  }

  @Test
  public void indexTestWithId() throws IOException {
    String mapping = loadMapping(MAPPINGS_FILE);

    indexManager.create(IndexMetadata.builder().name(indexName).build(), mapping);

    esTestClient.refreshIndices();

    indexManager.index(IndexMetadata.builder().name(indexName).id("1").routing("1").build(), TEST_JSON);

    esTestClient.refreshIndices();

    assertEquals("Index should have one document inserted.", 1,
        esTestClient.fetchAllDocuments(indexName).size());
  }

  @Test
  public void existsTestWithoutRouting() throws IOException {
    String mapping = loadMapping(MAPPINGS_FILE);

    indexManager.create(IndexMetadata.builder().name(indexName).build(), mapping);

    esTestClient.refreshIndices();

    assertTrue("'exists()' method should report index as exiting.",
        indexManager.exists(IndexMetadata.builder().name(indexName).build()));
  }

  @Test
  public void existsTestWithCachePurging() throws IOException {
    String mapping = loadMapping(MAPPINGS_FILE);

    indexManager.create(IndexMetadata.builder().name(indexName).build(), mapping);

    esTestClient.refreshIndices();

    indexManager.purgeIndexCache();

    assertTrue("'exists()' method should check index existence on the server once cache is purged.",
        indexManager.exists(IndexMetadata.builder().name(indexName).build()));
  }

  @Test
  public void existsTestWithRouting() throws IOException {
    String mapping = loadMapping(MAPPINGS_FILE);

    indexManager.create(IndexMetadata.builder().name(indexName).build(), mapping);

    esTestClient.refreshIndices();

    assertTrue("'exists()' method should report index as exiting.",
        indexManager.exists(IndexMetadata.builder().name(indexName).routing("1").build()));
  }

  @Test
  public void updateExistingIndexNonExistingDocument() throws IOException {
    String mapping = loadMapping(MAPPINGS_FILE);

    indexManager.create(IndexMetadata.builder().name(indexName).build(), mapping);

    esTestClient.refreshIndices();

    indexManager.update(IndexMetadata.builder().name(indexName).id("1").build(), TEST_JSON_2);

    esTestClient.refreshIndices();

    List<Map<String, Object>> indexData = esTestClient.fetchAllDocuments(indexName);
    assertEquals("Upsert should create document which does not exist.", 1, indexData.size());

    assertEquals("Updated document should have updated values.", UPDATED_NAME, indexData.get(0).get("name"));
  }

  @Test
  public void updateExistingIndexExistingDocument() throws IOException {
    String mapping = loadMapping(MAPPINGS_FILE);

    indexManager.create(IndexMetadata.builder().name(indexName).build(), mapping);

    esTestClient.refreshIndices();

    indexManager.index(IndexMetadata.builder().name(indexName).id("1").routing("1").build(), TEST_JSON);

    esTestClient.refreshIndices();

    indexManager.update(IndexMetadata.builder().name(indexName).id("1").build(), TEST_JSON_2);

    esTestClient.refreshIndices();

    List<Map<String, Object>> indexData = esTestClient.fetchAllDocuments(indexName);

    assertEquals("Updated document should have updated values.", UPDATED_NAME, indexData.get(0).get("name"));
  }

  @Test
  public void updateNonExistingIndex() throws UnknownHostException {
    indexManager.update(IndexMetadata.builder().name(indexName).id("1").build(), TEST_JSON_2);

    esTestClient.refreshIndices();

    assertEquals("Index should be created if updating non-existing index.", 1,
        esTestClient.fetchAllDocuments(indexName).size());
  }

  private String loadMapping(String source) throws IOException {
    return new BufferedReader(new InputStreamReader(new ClassPathResource(source).getInputStream()))
        .lines()
        .collect(Collectors.joining(""));
  }
}