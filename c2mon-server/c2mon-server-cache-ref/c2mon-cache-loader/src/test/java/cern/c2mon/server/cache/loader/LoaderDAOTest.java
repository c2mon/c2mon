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
package cern.c2mon.server.cache.loader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.server.cache.loader.impl.CommandTagDAOImpl;
import cern.c2mon.server.cache.dbaccess.CommandTagMapper;
import cern.c2mon.server.common.command.CommandTagCacheObject;
import cern.c2mon.shared.common.command.CommandTag;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the common loader interface for all caches
 * (common implementation currently).
 *
 * @author Mark Brightwell
 */
public class LoaderDAOTest {

  /**
   * Object to test (take DataTag loader but tests all loader functionalities so far)
   */
  private CommandTagDAO commandTagDAO;

  /**
   * The mock DataTagMapper
   */
  private CommandTagMapper mockMapper;

  @Before
  public void setUp() {
    mockMapper = createMock(CommandTagMapper.class);
    commandTagDAO = new CommandTagDAOImpl(mockMapper);
  }

  @Test
  public void testGetAllDataTagsAsMap() {
    List<CommandTag> returnList = new ArrayList<CommandTag>();
    returnList.add(new CommandTagCacheObject(23L));
    returnList.add(new CommandTagCacheObject(24L));
    expect(mockMapper.getAll()).andReturn(returnList);
    replay(mockMapper);
    Map<Long, CommandTag> returnMap = commandTagDAO.getAllAsMap();
    assertEquals(returnList.size(), returnMap.size()); //2 objects put in setUp
    assertTrue(returnMap.keySet().contains(new Long(23)));
    assertTrue(returnMap.keySet().contains(new Long(24)));
    verify(mockMapper);
  }

}
