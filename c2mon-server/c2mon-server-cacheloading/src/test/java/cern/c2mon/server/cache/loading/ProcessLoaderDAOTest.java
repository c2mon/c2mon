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
package cern.c2mon.server.cache.loading;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.common.process.Process;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration({"classpath:cern/c2mon/server/cache/loading/config/server-cacheloading-process-test.xml"})
public class ProcessLoaderDAOTest {

  @Autowired
  private ProcessDAO processDAO;

  @Value("${c2mon.server.daqcommunication.jms.queue.trunk}")
  private String jmsDaqQueueTrunk;

  @Test
  public void testGetItem() {
    assertNotNull(processDAO.getItem(50L));
  }

  /**
   * Check the default property is picked up (should override that set in the
   * cache object itself).
   */
  @Test
  public void testGetItemDoPostDbLoading() {
    Process process = processDAO.getItem(51L);
    assertNotNull(process);
    assertTopicSetCorrectly(process);
  }

  @Test
  public void testGetAll() {
    assertNotNull(processDAO.getAllAsMap());
    assertEquals(2, processDAO.getAllAsMap().size());
  }

  @Test
  public void testGetAllDoPostDbLoading() {
    for (Map.Entry<Long, Process> entry : processDAO.getAllAsMap().entrySet()) {
      assertTopicSetCorrectly(entry.getValue());
    }
  }

  @Test
  public void testGetNumTags() {
    assertTrue(processDAO.getNumTags(50L).equals(6));
  }

  @Test
  public void testGetNumInvalidTags() {
    // All the test tags are UNINITIALISED and hence invalid
    assertTrue(processDAO.getNumInvalidTags(50L).equals(6));
  }

  private void assertTopicSetCorrectly(Process process) {
    assertEquals(this.jmsDaqQueueTrunk + ".command." + process.getCurrentHost() + "." + process.getName() + "." + process.getProcessPIK(), process.getJmsDaqCommandQueue());
  }

}
