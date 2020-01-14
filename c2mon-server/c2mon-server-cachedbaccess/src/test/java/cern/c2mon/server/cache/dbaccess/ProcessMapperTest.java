/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.server.cache.dbaccess;

import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.common.process.ProcessCacheObject.LocalConfig;
import cern.c2mon.shared.common.supervision.SupervisionStatus;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.Assert.*;

public class ProcessMapperTest extends AbstractMapperTest {

  /**
   * Class to test
   */
  @Autowired
  private ProcessMapper processMapper;

  private ProcessCacheObject originalProcess;

  @Before
  public void insertTestProcess() {
    originalProcess = (ProcessCacheObject) processMapper.getItem(50L);
  }

  /**
   * Test does not insert associated equipment yet and test equipment retrieval
   *  - add later TODO
   */
  @Test
  public void testInsertAndRetrieve() {
//    ProcessCacheObject originalProcess = createTestProcess1();
//    processMapper.insertProcess(originalProcess);

    ProcessCacheObject retrievedProcess = (ProcessCacheObject) processMapper.getItem(originalProcess.getId());

    assertNotNull(retrievedProcess);

    assertEquals(originalProcess.getId(), retrievedProcess.getId());
    assertEquals(originalProcess.getName(), retrievedProcess.getName());
    assertEquals(originalProcess.getDescription(), retrievedProcess.getDescription());
    assertEquals(originalProcess.getMaxMessageDelay(), retrievedProcess.getMaxMessageDelay());
    assertEquals(originalProcess.getMaxMessageSize(), retrievedProcess.getMaxMessageSize());
    assertEquals(originalProcess.getStateTagId(), retrievedProcess.getStateTagId());
    assertEquals(originalProcess.getAliveInterval(), retrievedProcess.getAliveInterval());
    assertEquals(originalProcess.getAliveTagId(), retrievedProcess.getAliveTagId());
//    assertEquals(originalProcess.getSupervisionStatus(), retrievedProcess.getSupervisionStatus()); no longer persisted to DB; set to UNCERTAIN when server starts
    assertEquals(originalProcess.getStartupTime(), retrievedProcess.getStartupTime());
    assertEquals(originalProcess.getCurrentHost(), retrievedProcess.getCurrentHost());
    assertEquals(originalProcess.getEquipmentIds(), retrievedProcess.getEquipmentIds());
    assertEquals(originalProcess.getRequiresReboot(), retrievedProcess.getRequiresReboot());
    assertEquals(originalProcess.getStatusTime(), retrievedProcess.getStatusTime());
    assertEquals(originalProcess.getStatusDescription(), retrievedProcess.getStatusDescription());
    assertEquals(originalProcess.getProcessPIK(), retrievedProcess.getProcessPIK());
    assertEquals(originalProcess.getLocalConfig(), retrievedProcess.getLocalConfig());

  }

  @Test
  public void getByName() {
    Long retrievedId = processMapper.getIdByName("P_TESTHANDLER03");
    assertEquals((long)retrievedId, 50L);
  }

  @Test
  public void getByNameFailure() {
    Long retrievedId = processMapper.getIdByName("Test Process not there");

    assertNull(retrievedId);

  }

  /**
   * Tests the result set is none empty.
   */
  @Test
  public void testGetAll() {
    List<Process> returnList = processMapper.getAll();
    assertTrue(returnList.size() > 0);
  }

  /**
   * Tests the cache persistence method.
   */
  @Test
  public void testUpdate() {
    assertFalse(originalProcess.getSupervisionStatus().equals(SupervisionStatus.RUNNING));
    originalProcess.setSupervisionStatus(SupervisionStatus.RUNNING);
    Timestamp ts = new Timestamp(System.currentTimeMillis() + 1000);
    originalProcess.setStartupTime(ts);
    originalProcess.setRequiresReboot(true);
    originalProcess.setStatusDescription("New status description.");
    originalProcess.setStatusTime(ts);
    originalProcess.setProcessPIK(67890L);
    originalProcess.setLocalConfig(LocalConfig.N);

    processMapper.updateCacheable(originalProcess);

    ProcessCacheObject retrievedProcess = (ProcessCacheObject) processMapper.getItem(originalProcess.getId());
    assertEquals(SupervisionStatus.RUNNING, retrievedProcess.getSupervisionStatus());
    assertEquals(ts, retrievedProcess.getStartupTime());
    assertEquals(originalProcess.getRequiresReboot(), retrievedProcess.getRequiresReboot());
    assertEquals(originalProcess.getStatusDescription(), retrievedProcess.getStatusDescription());
    assertEquals(originalProcess.getStatusTime(), retrievedProcess.getStatusTime());
    assertEquals(originalProcess.getProcessPIK(), retrievedProcess.getProcessPIK());
    assertEquals(originalProcess.getLocalConfig(), retrievedProcess.getLocalConfig());
  }

  @Test
  public void testIsInDB() {
    assertTrue(processMapper.isInDb(50L));
  }

  @Test
  public void testNotInDB() {
    assertFalse(processMapper.isInDb(150L));
  }

  @Test
  public void testGetNumTags() {
    assertTrue(processMapper.getNumTags(50L).equals(6));
  }

  @Test
  public void testGetNumInvalidTags() {
    assertTrue(processMapper.getNumInvalidTags(90L).equals(0));
  }
}
