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

import java.sql.Timestamp;
import java.util.List;

import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.control.ControlTagCacheObject;
import cern.c2mon.server.test.CacheObjectCreation;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

public class ControlTagMapperTest extends AbstractMapperTest {

  /**
   * The class to test.
   */
  @Autowired
  private ControlTagMapper controlTagMapper;

  @After
  public void deleteTestTag() {
    controlTagMapper.deleteControlTag(new Long(1001));
  }

  @Test
  public void testGetAllControlTags() {
    assertNotNull(controlTagMapper);
    List<ControlTag> allList = controlTagMapper.getAll();
    assertNotSame(0, allList.size());
  }

  @Test
  public void testInsertControlTagAndGetItem() {
    ControlTagCacheObject cacheObject = (ControlTagCacheObject) CacheObjectCreation.createTestControlTag();
    controlTagMapper.insertControlTag(cacheObject); //insert into DB
    ControlTagCacheObject retrievedObject = (ControlTagCacheObject) controlTagMapper.getItem(cacheObject.getId()); //retrieve from DB

    assertNotNull(retrievedObject);
    cacheObject.setValue(Float.valueOf(1000f));

    //check the persistence was correct
    assertEquals(cacheObject.getId(), retrievedObject.getId());
    assertEquals(cacheObject.getName(), retrievedObject.getName());
    assertEquals(cacheObject.getDescription(), retrievedObject.getDescription());
    assertEquals(cacheObject.getMode(), retrievedObject.getMode());
    assertEquals(cacheObject.getDataType(), retrievedObject.getDataType());
    assertEquals(cacheObject.isLogged(), retrievedObject.isLogged());
    assertEquals(cacheObject.getUnit(), retrievedObject.getUnit());
    assertEquals(cacheObject.getDipAddress(), retrievedObject.getDipAddress());
    assertEquals(cacheObject.getJapcAddress(), retrievedObject.getJapcAddress());
    assertEquals(cacheObject.getValue(), retrievedObject.getValue());
    assertEquals(cacheObject.getValueDescription(), retrievedObject.getValueDescription());
    assertEquals(cacheObject.isSimulated(), retrievedObject.isSimulated());
    assertEquals(cacheObject.getEquipmentId(), retrievedObject.getEquipmentId());
    assertEquals(cacheObject.getMinValue(), retrievedObject.getMinValue());
    assertEquals(cacheObject.getMaxValue(), retrievedObject.getMaxValue());
    assertEquals(cacheObject.getAddress(), retrievedObject.getAddress());
    assertEquals(cacheObject.getDataTagQuality(), retrievedObject.getDataTagQuality());//quality compare code and string
    assertEquals(cacheObject.getTimestamp(), retrievedObject.getTimestamp());
    assertEquals(cacheObject.getSourceTimestamp(), retrievedObject.getSourceTimestamp());
    assertEquals(cacheObject.getRuleIdsString(), retrievedObject.getRuleIdsString());

  }

  @Test
  public void testUpdateControlTag() {
    ControlTagCacheObject cacheObject = CacheObjectCreation.createTestControlTag();
    controlTagMapper.insertControlTag(cacheObject);

    assertTrue(cacheObject.getDataType().equals("Float"));
    cacheObject.setValue(Float.valueOf(1999f));
    cacheObject.setCacheTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setSourceTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setValueDescription("new control value");
    cacheObject.setDataTagQuality(new DataTagQualityImpl(TagQualityStatus.PROCESS_DOWN, "Process down."));
    cacheObject.setSimulated(false);

    controlTagMapper.updateCacheable(cacheObject);

    ControlTagCacheObject retrievedObject = (ControlTagCacheObject) controlTagMapper.getItem(cacheObject.getId());

    assertEquals(cacheObject.getValue(), retrievedObject.getValue());
    assertEquals(Float.class, retrievedObject.getValue().getClass());
    assertEquals(cacheObject.getValueDescription(), retrievedObject.getValueDescription());
    assertEquals(cacheObject.getDataTagQuality(), retrievedObject.getDataTagQuality());//quality compare code and string
    assertEquals(cacheObject.getTimestamp(), retrievedObject.getTimestamp());
    assertEquals(cacheObject.getSourceTimestamp(), retrievedObject.getSourceTimestamp());
    assertEquals(cacheObject.isSimulated(), retrievedObject.isSimulated());

  }

  @Test
  public void testIsInDB() {
    assertTrue(controlTagMapper.isInDb(1230L));
  }

  @Test
  public void testNotInDB() {
    assertFalse(controlTagMapper.isInDb(200004L));
  }

}
