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
package cern.c2mon.server.elasticsearch.alarm;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.elasticsearch.ElasticsearchSuiteTest;
import cern.c2mon.server.elasticsearch.ElasticsearchTestDefinition;
import cern.c2mon.server.elasticsearch.IndexNameManager;
import cern.c2mon.server.elasticsearch.util.EmbeddedElasticsearchManager;
import cern.c2mon.server.elasticsearch.util.EntityUtils;
import cern.c2mon.server.elasticsearch.util.IndexUtils;

import static junit.framework.TestCase.assertTrue;

/**
 * @author Alban Marguet
 * @author Justin Lewis Salmon
 */
public class AlarmDocumentIndexerTests extends ElasticsearchTestDefinition {

  @Autowired
  private IndexNameManager indexNameManager;

  @Autowired
  private AlarmDocumentIndexer indexer;

  private AlarmDocument document;

  @Before
  public void setUp() {
    AlarmCacheObject alarm = (AlarmCacheObject) EntityUtils.createAlarm();
    alarm.setTimestamp(new Timestamp(0));
    document = new AlarmValueDocumentConverter().convert(alarm);
    indexName = indexNameManager.indexFor(document);
  }

  @Test
  public void indexSingleAlarmTest() throws IDBPersistenceException, IOException {
    indexer.storeData(document);

    EmbeddedElasticsearchManager.getEmbeddedNode().refreshIndices();

    assertTrue("Index should have been created.", IndexUtils.doesIndexExist(indexName, ElasticsearchSuiteTest.getProperties()));

    List<String> indexData = EmbeddedElasticsearchManager.getEmbeddedNode().fetchAllDocuments(indexName);
    Assert.assertEquals("Index should have one document inserted.", 1, indexData.size());
  }

  @Test
  public void indexMultipleAlarmTest() throws IDBPersistenceException, IOException {
    indexer.storeData(document);
    indexer.storeData(document);

    EmbeddedElasticsearchManager.getEmbeddedNode().refreshIndices();

    assertTrue("Index should have been created.", IndexUtils.doesIndexExist(indexName, ElasticsearchSuiteTest.getProperties()));

    List<String> indexData = EmbeddedElasticsearchManager.getEmbeddedNode().fetchAllDocuments(indexName);
    Assert.assertEquals("Index should have two documents inserted.", 2, indexData.size());
  }
}
