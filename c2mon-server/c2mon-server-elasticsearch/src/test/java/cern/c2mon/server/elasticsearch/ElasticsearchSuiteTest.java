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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import cern.c2mon.server.elasticsearch.alarm.AlarmDocumentConverterTests;
import cern.c2mon.server.elasticsearch.alarm.AlarmDocumentIndexerTests;
import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;
import cern.c2mon.server.elasticsearch.supervision.SupervisionEventDocumentIndexerTests;
import cern.c2mon.server.elasticsearch.supervision.SupervisionEventDocumentTests;
import cern.c2mon.server.elasticsearch.tag.TagDocumentConverterTests;
import cern.c2mon.server.elasticsearch.tag.TagDocumentIndexerTests;
import cern.c2mon.server.elasticsearch.tag.config.TagConfigDocumentConverterTests;
import cern.c2mon.server.elasticsearch.tag.config.TagConfigDocumentIndexerTests;
import cern.c2mon.server.elasticsearch.util.EmbeddedElasticsearchManager;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    ElasticsearchModuleIntegrationTests.class,
    IndexManagerTests.class,
    IndexNameManagerTests.class,
    AlarmDocumentConverterTests.class,
    AlarmDocumentIndexerTests.class,
    SupervisionEventDocumentIndexerTests.class,
    SupervisionEventDocumentTests.class,
    TagDocumentConverterTests.class,
    TagDocumentIndexerTests.class,
    TagConfigDocumentConverterTests.class,
    TagConfigDocumentIndexerTests.class
})
public class ElasticsearchSuiteTest {

  private static final ElasticsearchProperties properties = new ElasticsearchProperties();

  public static ElasticsearchProperties getProperties() {
    return properties;
  }

  @BeforeClass
  public static void setUpClass() {
    EmbeddedElasticsearchManager.start(properties);
  }

  @AfterClass
  public static void cleanup() {
    EmbeddedElasticsearchManager.stop();
  }
}
