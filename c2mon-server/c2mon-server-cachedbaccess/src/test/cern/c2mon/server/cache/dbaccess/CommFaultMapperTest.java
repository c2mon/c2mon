/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2010 CERN.
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

package cern.c2mon.server.cache.dbaccess;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.shared.common.Cacheable;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/server/cache/dbaccess/config/server-cachedbaccess-test.xml"})
@TransactionConfiguration(transactionManager="cacheTransactionManager", defaultRollback=true)
@Transactional
public class CommFaultMapperTest {

  @Autowired
  private CommFaultTagMapper commFaultTagMapper;
  
  @Test
  public void testGetAll() {
    List<CommFaultTag> tagList = commFaultTagMapper.getAll();
    assertTrue(tagList.size() > 2);
  }
  
  @Test
  public void testGetOne() {
    Cacheable item = commFaultTagMapper.getItem(1232); //needs to correspond to one in DB 
    assertNotNull(item);
  }
  
  @Test
  public void testIsInDB() {
    assertTrue(commFaultTagMapper.isInDb(1263L));
  }
  
  @Test
  public void testNotInDB() {
    assertFalse(commFaultTagMapper.isInDb(1240L));
  }
}
