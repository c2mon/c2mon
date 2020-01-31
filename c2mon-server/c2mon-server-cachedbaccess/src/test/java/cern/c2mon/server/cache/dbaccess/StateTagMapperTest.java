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

import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.common.supervision.SupervisionEntity;
import org.junit.Test;

import javax.inject.Inject;
import java.util.List;

import static org.junit.Assert.*;

public class StateTagMapperTest extends AbstractMapperTest {

  @Inject
  private SupervisionStateTagMapper stateTagMapper;

  @Test
  public void testGetAll() {
    List<SupervisionStateTag> stateTags = stateTagMapper.getAll();
    assertEquals(6, stateTags.size());
  }

  @Test
  public void testGetOne() {
    Cacheable item = stateTagMapper.getItem(1220L); //needs to correspond to one in DB
    assertNotNull(item);
  }

  @Test
  public void loadProcess() {
    assertEquals(SupervisionEntity.PROCESS, stateTagMapper.getItem(1220L).getSupervisedEntity());
  }

  @Test
  public void loadEquipment() {
    assertEquals(SupervisionEntity.EQUIPMENT, stateTagMapper.getItem(1222L).getSupervisedEntity());
  }

  @Test
  public void loadSubEquipment() {
    assertEquals(SupervisionEntity.SUBEQUIPMENT, stateTagMapper.getItem(1230L).getSupervisedEntity());
  }

  @Test
  public void testIsInDB() {
    assertTrue(stateTagMapper.isInDb(1220L));
  }

  @Test
  public void testNotInDB() {
    assertFalse(stateTagMapper.isInDb(12345L));
  }
}
