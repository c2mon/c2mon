/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * <p/>
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * <p/>
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.eslog.logger;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

/**
 * Insure that the persistence to a fallback file is done effectively.
 * @author Alban Marguet
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class ESPersistenceManagerTest {
  private String backupName = "backup.back";
  private String id = "123456789";
  @InjectMocks
  private ESPersistenceManager esPersistenceManager;

  @Rule
  public TemporaryFolder testFolder = new TemporaryFolder();


  @Before
  public void setup() throws IOException {
    testFolder.newFile(backupName);
    esPersistenceManager.setupBackup(testFolder.getRoot().getPath() + backupName);
  }

  @After
  public void cleanup() {
    testFolder.delete();
  }

  @Test
  public void testFallBackIsWorking() {
    BulkRequest bulk = new BulkRequest();
    log.debug(bulk.toString() + "");
    List<IndexRequest> requestList = new ArrayList<>();
    requestList.add(new IndexRequest("c2mon_2016-01", "tag_string").source(""));
    requestList.add(new IndexRequest("c2mon_2016-02", "tag_boolean").source(""));
    requestList.add(new IndexRequest("c2mon_1973-09", "tag_short", id).source("{\"id\" : \"" + id + "\"}"));
    for (IndexRequest req : requestList) {
      bulk.add(req);
    }

    esPersistenceManager.launchFallBackMechanism(bulk);
    List<IndexRequest> resultList = esPersistenceManager.retrieveBackupData();
    assertEquals(requestList.size(), resultList.size());

    for (int i = 0; i < requestList.size(); i++) {
      IndexRequest expected = requestList.get(i);
      IndexRequest result = resultList.get(i);
      log.debug("testFallBackIsWorking() - Expect: " + expected.toString());
      log.debug("testFallBackIsWorking() - Get: " + result.toString());

      assertEquals(expected.id(), result.id());
      assertEquals(expected.type(), result.type());
      assertEquals(expected.toString(), result.toString());
      assertEquals(expected.index(), result.index());
      assertEquals(expected.source(), result.source());
    }
  }
}