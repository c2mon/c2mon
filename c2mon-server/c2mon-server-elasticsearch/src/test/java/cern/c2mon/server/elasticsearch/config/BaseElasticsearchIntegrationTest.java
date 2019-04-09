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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.node.NodeValidationException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.FileSystemUtils;

import cern.c2mon.server.cache.config.CacheModule;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModule;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.elasticsearch.Indices;
import cern.c2mon.server.elasticsearch.MappingFactory;
import cern.c2mon.server.elasticsearch.client.ElasticsearchClient;
import cern.c2mon.server.elasticsearch.junit.CachePopulationRule;
import cern.c2mon.server.supervision.config.SupervisionModule;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    CommonModule.class,
    CacheModule.class,
    CacheDbAccessModule.class,
    CacheLoadingModule.class,
    SupervisionModule.class,
    ElasticsearchModule.class,
    CachePopulationRule.class
})
@Slf4j
public abstract class BaseElasticsearchIntegrationTest {

  private static final ElasticsearchProperties elasticsearchProperties = new ElasticsearchProperties();

  //the embedded ES node will start
  //when the client is instantiatied (magically by Spring)
  //we don't shutdown the embedded server at the end of each
  //test because it may be used by other tests.
  @Autowired
  protected ElasticsearchClient client;

  @BeforeClass
  public static void cleanup() {
    log.debug("Clean ES properties");
    FileSystemUtils.deleteRecursively(new java.io.File(elasticsearchProperties.getEmbeddedStoragePath()));
  }

  @Before
  public void setupElasticsearch() throws InterruptedException, NodeValidationException {
    try {
      log.debug("Setup Elasticsearch cluster for testing...");
      CompletableFuture<Void> nodeReady = CompletableFuture.runAsync(() -> {
        client.waitForYellowStatus();
        client.getClient().admin().indices().delete(new DeleteIndexRequest(elasticsearchProperties.getTagConfigIndex()));
        Awaitility.await().until(() -> Indices.create(elasticsearchProperties.getTagConfigIndex(), "tag_config", MappingFactory.createTagConfigMapping()));
      });
      nodeReady.get(120, TimeUnit.SECONDS);
      log.debug("Elasticseach cluster ready for testing!");
    } catch (ExecutionException | TimeoutException e) {
      Assert.fail("Timeout when waiting for embedded elasticsearch node to start!");
      throw new RuntimeException("Timeout when waiting for embedded elasticsearch node to start!", e);
    }
  }

  @After
  public void tearDown() {
    log.debug("Closing ES client...");
    client.close();
  }
}
