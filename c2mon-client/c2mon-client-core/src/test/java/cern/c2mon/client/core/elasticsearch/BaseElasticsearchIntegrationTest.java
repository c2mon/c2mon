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
package cern.c2mon.client.core.elasticsearch;

import org.awaitility.Awaitility;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.cache.config.CacheModule;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.elasticsearch.Indices;
import cern.c2mon.server.elasticsearch.MappingFactory;
import cern.c2mon.server.elasticsearch.client.ElasticsearchClient;
import cern.c2mon.server.elasticsearch.config.ElasticsearchModule;
import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;
import cern.c2mon.server.supervision.config.SupervisionModule;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    ElasticsearchModule.class,
    CacheModule.class,
    SupervisionModule.class,
    CommonModule.class
})
public abstract class BaseElasticsearchIntegrationTest {

  private static final ElasticsearchProperties elasticsearchProperties = new ElasticsearchProperties();

  /**
   * the embedded ES node will start
   * when the client is instantiated (magically by Spring)
   * we don't shutdown the embedded server at the end of each
   * test because it may be used by other tests.
   */
  @Autowired
  protected ElasticsearchClient client;

  @Before
  public void setUp() throws Exception {
    client.getClient().admin().indices().delete(new DeleteIndexRequest(elasticsearchProperties.getIndexPrefix() + "*"));
    Awaitility.await().until(() -> Indices.create(elasticsearchProperties.getTagConfigIndex(), "tag_config", MappingFactory.createTagConfigMapping()));
    client.waitForYellowStatus();
  }
}
