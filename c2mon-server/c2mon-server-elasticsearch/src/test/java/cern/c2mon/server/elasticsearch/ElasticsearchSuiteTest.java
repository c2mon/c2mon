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

import cern.c2mon.server.elasticsearch.client.ElasticsearchClientRest;
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
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Perform the necessary setup to run ES tests
 *
 * @author Serhiy Boychenko
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    ElasticsearchModuleIntegrationTestSuite.class,
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

public class ElasticsearchSuiteTest {

  private static final DockerImageName ELASTICSEARCH_IMAGE = DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch");

  private static final String ELASTICSEARCH_TAG = "7.12.0";

  @ClassRule
  public static ElasticsearchContainer elasticsearchContainer =
          new ElasticsearchContainer(ELASTICSEARCH_IMAGE.withTag(ELASTICSEARCH_TAG)).withExposedPorts(9200,9300);

  @ClassRule
  public static Timeout classTimeout = new Timeout(2, TimeUnit.MINUTES);

  public static IndexManager indexManager;

  private static ElasticsearchClientRest client;

  public static ElasticsearchClientRest getElasticsearchClient(){ return client; }

  private static final ElasticsearchProperties properties = new ElasticsearchProperties();

  @BeforeClass
  public static void setUpClass() {

    properties.setHost(elasticsearchContainer.getHost());
    properties.setPort(elasticsearchContainer.getMappedPort(9200));

    client = new ElasticsearchClientRest(properties);
    indexManager = new IndexManager(client);
  }

  public static ElasticsearchProperties getProperties() {
    return properties;
  }
}
