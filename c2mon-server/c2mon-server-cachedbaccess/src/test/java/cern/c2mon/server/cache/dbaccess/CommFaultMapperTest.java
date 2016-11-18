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

import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.test.DatabasePopulationRule;
import cern.c2mon.shared.common.Cacheable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
    "classpath:config/server-cachedbaccess.xml"
})
@TestPropertySource("classpath:c2mon-server-default.properties")
public class CommFaultMapperTest {

  @Rule
  @Autowired
  public DatabasePopulationRule databasePopulationRule;

  @Autowired
  private CommFaultTagMapper commFaultTagMapper;

  @Test
  public void testGetAll() {
    List<CommFaultTag> tagList = commFaultTagMapper.getAll();
    assertTrue(tagList.size() > 2);
  }

  @Test
  public void testGetOne() {
    Cacheable item = commFaultTagMapper.getItem(1232L); //needs to correspond to one in DB
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
