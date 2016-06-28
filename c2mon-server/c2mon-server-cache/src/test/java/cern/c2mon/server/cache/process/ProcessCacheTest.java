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
package cern.c2mon.server.cache.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.List;

import cern.c2mon.server.cache.AbstractCacheIntegrationTest;
import cern.c2mon.server.cache.junit.CachePopulationRule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.server.cache.dbaccess.ProcessMapper;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.shared.common.Cacheable;

/**
 * Integration test of ProcessCache with loading
 * and DB access cache modules.
 * 
 * @author Mark Brightwell
 *
 */
public class ProcessCacheTest extends AbstractCacheIntegrationTest {
  
  @Autowired  
  private ProcessMapper processMapper;
  
  @Autowired
  private ProcessCacheImpl processCache;
  
  @Test
  public void testCacheLoading() {
    assertNotNull(processCache);
    
    List<Cacheable> processList = processMapper.getAll(); //IN FACT: GIVES TIME FOR CACHE TO FINISH LOADING ASYNCH BEFORE COMPARISON BELOW...
    
    //test the cache is the same size as in DB
    assertEquals(processList.size(), processCache.getCache().getKeys().size());
    //compare all the objects from the cache and buffer
    Iterator<Cacheable> it = processList.iterator();
    while (it.hasNext()) {
      Process currentProcess = (Process) it.next();
      //equality of DataTagCacheObjects => currently only compares names
      assertEquals(currentProcess.getName(), (((Process) processCache.getCopy(currentProcess.getId())).getName()));
    }
  }
  
}
