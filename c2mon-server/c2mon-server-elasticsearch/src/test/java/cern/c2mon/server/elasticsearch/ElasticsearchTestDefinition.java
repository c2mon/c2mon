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
import cern.c2mon.cache.impl.configuration.C2monIgniteConfiguration;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModuleRef;
import cern.c2mon.server.cache.test.CachePopulationRule;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.elasticsearch.config.ElasticsearchModule;
import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;
import cern.c2mon.server.elasticsearch.util.ElasticsearchTestClient;
import cern.c2mon.server.supervision.config.SupervisionModule;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * When running in GitLab CI, Docker-in-Docker is set up so that Testcontainers pull
 * and run the necessary Elasticsearch image, as configured in {@code .gitlab-ci.yml}
 * and {@code c2mon-server-gitlab-ci.properties}. For local testing, an embedded
 * Elasticsearch service is used by default, while a setup similar to that of the
 * pipelines can be achieved by running Docker locally and changing the property
 * {@code c2mon.server.elasticsearch.serviceType} to {@code containerized}.
 */
// @TestPropertySource(properties = {"c2mon.server.elasticsearch.serviceType=containerized"})
@ContextConfiguration(classes = {
  CommonModule.class,
  CacheActionsModuleRef.class,
  CacheConfigModuleRef.class,
  CacheDbAccessModule.class,
  CacheLoadingModuleRef.class,
  SupervisionModule.class,
  ElasticsearchModule.class,
  C2monIgniteConfiguration.class,
  CachePopulationRule.class
})
@RunWith(SpringRunner.class)
public abstract class ElasticsearchTestDefinition {

  @Autowired
  protected ElasticsearchProperties properties;

  @Autowired
  protected ElasticsearchTestClient esTestClient;

  @After
  public void tearDown() {
    esTestClient.deleteIndex("_all");
    esTestClient.refreshIndices();
  }
}
