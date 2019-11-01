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
package cern.c2mon.server.cache.common;

import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.AlarmFacade;
import cern.c2mon.server.cache.C2monCacheWithListeners;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.alarm.TagWithAlarms;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import cern.c2mon.shared.daq.config.Change;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Unit test of AbstractTagFacade class. All used beans are mocked.
 *
 * @author Mark Brightwell
 *
 */
public class AbstractTagFacadeTest {

  /**
   * Class to test.
   */
  private AbstractTagFacade<DataTag> facade;

  /**
   * Mocks
   */
  private C2monCacheWithListeners<Long, DataTag> c2monCache;
  private AlarmFacade alarmFacade;
  private AlarmCache alarmCache;

  @SuppressWarnings("unchecked")
  @Before
  public void init() {

    c2monCache = EasyMock.createMock(C2monCacheWithListeners.class);
    alarmFacade = EasyMock.createMock(AlarmFacade.class);
    alarmCache = EasyMock.createMock(AlarmCache.class);

    facade = new AbstractTagFacade<DataTag>(c2monCache, alarmFacade, alarmCache) {

      @Override
      public DataTag createCacheObject(Long id, Properties properties) throws IllegalAccessException {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      protected void validateConfig(DataTag cacheObject) {
        // TODO Auto-generated method stub

      }

      @Override
      protected Change configureCacheObject(DataTag cacheObject, Properties properties) throws IllegalAccessException {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      protected void invalidateQuietly(DataTag tag, TagQualityStatus statusToAdd, String description,
          Timestamp timestamp) {
        // TODO Auto-generated method stub

      }
    };
  }


  /**
   * Tests the getTagWithAlarms() method.
   */
  @Test
  public void testGetTagWithAlarms() {
    DataTagCacheObject tag = new DataTagCacheObject(1L);
    AlarmCacheObject alarm = new AlarmCacheObject(10L);
    List<Long> alarms = new ArrayList<Long>();
    alarms.add(10L);
    tag.setAlarmIds(alarms);

    c2monCache.acquireReadLockOnKey(tag.getId());
    c2monCache.releaseReadLockOnKey(tag.getId());
    EasyMock.expect(c2monCache.getCopy(1L)).andReturn(tag);
    EasyMock.expect(alarmCache.getCopy(10L)).andReturn(alarm);

    EasyMock.replay(c2monCache);
    EasyMock.replay(alarmCache);

    TagWithAlarms tagWithAlarms = facade.getTagWithAlarms(1L);

    EasyMock.verify(c2monCache);
    EasyMock.verify(alarmCache);

    Assert.assertEquals(tag, tagWithAlarms.getTag());
    Assert.assertEquals(alarm, tagWithAlarms.getAlarms().iterator().next());
  }


}
