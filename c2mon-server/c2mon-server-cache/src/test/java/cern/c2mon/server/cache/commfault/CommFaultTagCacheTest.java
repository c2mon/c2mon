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
package cern.c2mon.server.cache.commfault;

import cern.c2mon.server.cache.AbstractCacheIntegrationTest;
import cern.c2mon.server.cache.dbaccess.CommFaultTagMapper;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.commfault.CommFaultTag;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Integration test of the CommFaultTagCache with the
 * cache loading and cache DB access modules.
 * 
 * @author Mark Brightwell
 *
 */
public class CommFaultTagCacheTest extends AbstractCacheIntegrationTest {
  
  @Autowired
  private CommFaultTagCacheImpl commFaultTagCache;
  
  @Autowired
  private CommFaultTagMapper commFaultTagMapper;
  
  @Test
  public void testCacheLoading() {
    assertNotNull(commFaultTagCache);
    
    List<CommFaultTag> commFaultList = commFaultTagMapper.getAll();

    //test the cache is the same size as in DB
    assertEquals(commFaultList.size(), commFaultTagCache.getCache().getKeys().size());
    //compare all the objects from the cache and buffer
    Iterator<CommFaultTag> it = commFaultList.iterator();
    while (it.hasNext()) {
      CommFaultTag currentTag = (CommFaultTag) it.next();
      //equality of DataTagCacheObjects => currently only compares names
      assertEquals(currentTag.getEquipmentId(), (((CommFaultTag) commFaultTagCache.getCopy(currentTag.getId())).getEquipmentId()));
    }
  }
}
