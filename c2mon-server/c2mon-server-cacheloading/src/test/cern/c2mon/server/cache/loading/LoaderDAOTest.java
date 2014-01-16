/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.cache.loading;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.server.cache.dbaccess.CommandTagMapper;
import cern.c2mon.server.cache.loading.impl.CommandTagDAOImpl;
import cern.c2mon.server.common.command.CommandTagCacheObject;
import cern.c2mon.shared.daq.command.CommandTag;

/**
 * Tests the common loader interface for all caches
 * (common implementation currently).
 * 
 * @author Mark Brightwell
 *
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
