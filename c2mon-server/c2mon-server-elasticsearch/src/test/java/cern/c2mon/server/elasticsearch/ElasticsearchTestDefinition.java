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

import cern.c2mon.cache.actions.CacheActionsModuleRef;
import cern.c2mon.cache.config.CacheConfigModuleRef;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModuleRef;
import cern.c2mon.server.cache.test.CachePopulationRule;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.elasticsearch.config.ElasticsearchModule;
import cern.c2mon.server.elasticsearch.util.EmbeddedElasticsearchManager;
import cern.c2mon.server.supervision.config.SupervisionModule;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(classes = {
    CommonModule.class,
    CacheActionsModuleRef.class,
    CacheConfigModuleRef.class,
    CacheDbAccessModule.class,
    CacheLoadingModuleRef.class,
    SupervisionModule.class,
    ElasticsearchModule.class,
    CachePopulationRule.class
})
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class ElasticsearchTestDefinition {

  protected String indexName;

  @After
  public void tearDown() {
    EmbeddedElasticsearchManager.getEmbeddedNode().deleteIndex(indexName);
    EmbeddedElasticsearchManager.getEmbeddedNode().refreshIndices();
  }
}
