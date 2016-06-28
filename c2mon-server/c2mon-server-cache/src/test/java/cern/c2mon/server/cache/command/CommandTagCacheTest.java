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
package cern.c2mon.server.cache.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.List;

import cern.c2mon.server.cache.AbstractCacheIntegrationTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.cache.dbaccess.CommandTagMapper;
import cern.c2mon.server.common.command.CommandTagCacheObject;
import cern.c2mon.server.test.CacheObjectComparison;
import cern.c2mon.shared.common.command.CommandTag;

/**
 * Integration test of the CommandTagCache with the cache loading
 * and cache DB access modules.
 * 
 * @author Mark Brightwell
 *
 */
public class CommandTagCacheTest extends AbstractCacheIntegrationTest {

  @Autowired  
  private CommandTagMapper commandTagMapper;
  
  @Autowired
  private CommandTagCacheImpl commandTagCache;
  
  @Test
  public void testCacheLoading() {
    assertNotNull(commandTagCache);
    
    List<CommandTag> commandList = commandTagMapper.getAll(); 
    
    //test the cache is the same size as in DB
    assertEquals(commandList.size(), commandTagCache.getCache().getKeys().size());
    //compare all the objects from the cache and buffer
    Iterator<CommandTag> it = commandList.iterator();
    while (it.hasNext()) {
      CommandTag currentCommandTag = it.next();
      CacheObjectComparison.equals((CommandTagCacheObject) currentCommandTag, 
                                      (CommandTagCacheObject) commandTagCache.get(currentCommandTag.getId()));
    }
  }
  
}
