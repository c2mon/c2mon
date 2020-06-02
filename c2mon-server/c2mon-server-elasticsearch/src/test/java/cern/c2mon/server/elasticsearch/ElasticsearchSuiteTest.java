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

import org.junit.AfterClass;
import org.junit.ClassRule;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import cern.c2mon.server.elasticsearch.alarm.AlarmDocumentIndexerTestSuite;
import cern.c2mon.server.elasticsearch.supervision.SupervisionEventDocumentIndexerTestSuite;
import cern.c2mon.server.elasticsearch.tag.TagDocumentIndexerTestSuite;
import cern.c2mon.server.elasticsearch.tag.config.TagConfigDocumentIndexerTestSuite;
import cern.c2mon.server.elasticsearch.util.EmbeddedElasticsearchManager;

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
  AlarmDocumentIndexerTestSuite.class,
  SupervisionEventDocumentIndexerTestSuite.class,
  TagDocumentIndexerTestSuite.class,
  TagConfigDocumentIndexerTestSuite.class
})
public class ElasticsearchSuiteTest {

  @ClassRule
  public static Timeout classTimeout = new Timeout(2, TimeUnit.MINUTES);

  @AfterClass
  public static void cleanup() {
    if (ElasticsearchTestDefinition.esContainer != null) {
      ElasticsearchTestDefinition.esContainer.stop();
    } else {
      EmbeddedElasticsearchManager.stop();
    }
  }
}
