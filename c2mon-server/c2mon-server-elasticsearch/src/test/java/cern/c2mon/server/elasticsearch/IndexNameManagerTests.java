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

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.server.elasticsearch.tag.TagDocument;

import static junit.framework.TestCase.assertEquals;

/**
 * @author Justin Lewis Salmon
 */
public class IndexNameManagerTests {

  private static final String TIMESTAMP_PROPERTY = "timestamp";

  private IndexNameManager indexNameManager = new IndexNameManager(ElasticsearchSuiteTest.getProperties());

  @Before
  public void setUp() {
    indexNameManager.setClock(Clock.fixed(Instant.ofEpochMilli(1448928000000L), ZoneId.systemDefault()));
  }

  @Test
  public void monthlyIndex() {
    indexNameManager.getProperties().setIndexType("M");

    TagDocument document = new TagDocument();
    document.put(TIMESTAMP_PROPERTY, 1448928000000L);

    String index = indexNameManager.indexFor(document);
    assertEquals("Monthly index name should contain month definition", "c2mon-tag_2015-12", index);
  }

  @Test
  public void weeklyIndex() {
    indexNameManager.getProperties().setIndexType("W");

    TagDocument document = new TagDocument();
    document.put(TIMESTAMP_PROPERTY, 1448928000000L);

    String index = indexNameManager.indexFor(document);
    assertEquals("Weekly index name should contain week definition", "c2mon-tag_2015-W49", index);
  }

  @Test
  public void dailyIndex() {
    indexNameManager.getProperties().setIndexType("D");

    TagDocument document = new TagDocument();
    document.put(TIMESTAMP_PROPERTY, 1448928000000L);

    String index = indexNameManager.indexFor(document);
    assertEquals("Daily index name should contain day definition", "c2mon-tag_2015-12-01", index);
  }
}
