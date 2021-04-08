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

import java.util.concurrent.TimeUnit;

import cern.c2mon.server.elasticsearch.client.ElasticsearchClient;
import cern.c2mon.server.elasticsearch.client.ElasticsearchClientRest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import cern.c2mon.server.elasticsearch.alarm.AlarmDocumentConverterTestSuite;
import cern.c2mon.server.elasticsearch.alarm.AlarmDocumentIndexerTestSuite;
import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;
import cern.c2mon.server.elasticsearch.supervision.SupervisionEventDocumentIndexerTestSuite;
import cern.c2mon.server.elasticsearch.supervision.SupervisionEventDocumentTestSuite;
import cern.c2mon.server.elasticsearch.tag.TagDocumentConverterTestSuite;
import cern.c2mon.server.elasticsearch.tag.TagDocumentIndexerTestSuite;
import cern.c2mon.server.elasticsearch.tag.config.TagConfigDocumentConverterTestSuite;
import cern.c2mon.server.elasticsearch.tag.config.TagConfigDocumentIndexerTestSuite;
import cern.c2mon.server.elasticsearch.util.EmbeddedElasticsearchManager;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Perform the necessary setup to run ES tests
 *
 * @author Serhiy Boychenko
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    ElasticsearchModuleIntegrationTestSuite.class,
    IndexManagerTestSuite.class,
    IndexNameManagerTestSuite.class,
    AlarmDocumentConverterTestSuite.class,
    AlarmDocumentIndexerTestSuite.class,
    SupervisionEventDocumentIndexerTestSuite.class,
    SupervisionEventDocumentTestSuite.class,
    TagDocumentConverterTestSuite.class,
    TagDocumentIndexerTestSuite.class,
    TagConfigDocumentConverterTestSuite.class,
    TagConfigDocumentIndexerTestSuite.class
})
@Testcontainers
public class ElasticsearchSuiteTest {

  @Container
  private static ElasticsearchContainer elasticsearchContainer;

  @ClassRule
  public static Timeout classTimeout = new Timeout(2, TimeUnit.MINUTES);

  private static final ElasticsearchProperties properties = new ElasticsearchProperties();

  public static ElasticsearchProperties getProperties() {
    return properties;
  }

  private static ElasticsearchClientRest client;

  public static ElasticsearchClientRest getElasticsearchClient(){ return client; }

  @BeforeClass
  public static void setUpClass() {
    elasticsearchContainer.start();
    client = new ElasticsearchClientRest(properties);
  }

  @AfterClass
  public static void cleanup() {
    elasticsearchContainer.stop();
  }
}
