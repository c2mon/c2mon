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
package cern.c2mon.server.daq.update;

import cern.c2mon.server.cache.ControlTagCache;
import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.config.CacheModule;
import cern.c2mon.server.cache.dbaccess.ControlTagMapper;
import cern.c2mon.server.cache.dbaccess.DataTagMapper;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModule;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.daq.config.DaqModule;
import cern.c2mon.server.daq.junit.DaqCachePopulationRule;
import cern.c2mon.server.supervision.config.SupervisionModule;
import cern.c2mon.shared.common.datatag.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.Timestamp;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Integration testing of the server-daq module.
 *
 * @author Mark Brightwell
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    CommonModule.class,
    CacheModule.class,
    CacheDbAccessModule.class,
    CacheLoadingModule.class,
    SupervisionModule.class,
    DaqModule.class,
})
public class TagUpdateTest {

  @Rule
  @Autowired
  public DaqCachePopulationRule daqCachePopulationRule;

  /**
   * The interface to the module that needs testing.
   */
  @Autowired
  private SourceUpdateManager sourceUpdateManager;

  /**
   * Used to load the test datatag into the cache.
   */
  @Autowired
  private DataTagCache dataTagCache;
  @Autowired
  private ControlTagCache controlTagCache;

  /**
   * Used to access DB values.
   */
  @Autowired
  private DataTagMapper dataTagMapper;
  @Autowired
  private ControlTagMapper controlTagMapper;

  @Autowired
  private JmsContainerManagerImpl jmsContainerManager;

  private DataTag dataTag;

  private ControlTag controlTag;

  @Before
  public void setUp() throws Exception {
    dataTag = dataTagMapper.getItem(200000L);
    controlTag = controlTagMapper.getItem(1221L);
  }

  @After
  public void tearDown() {
    jmsContainerManager.stop();
  }

  /**
   * Tests that an incoming update is correctly saved in the cache and database.
   */
  @Test
  public void testIncomingDataTag() throws InterruptedException {
    dataTagCache.put(dataTag.getId(), dataTag);

    //create a source update for this tag
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    Timestamp daqTimestamp = new Timestamp(System.currentTimeMillis() + 1);
    //check datatag is set to value TRUE
//    assertEquals(Boolean.TRUE, dataTag.getValue());

    //set source update with value
    SourceDataTagValue sourceDataTagValue = SourceDataTagValue.builder()
        .id(dataTag.getId())
        .name(dataTag.getName())
        .controlTag(false)
        .value(1)
        .quality(new SourceDataTagQuality())
        .timestamp(timestamp)
        .daqTimestamp(new Timestamp(System.currentTimeMillis()))
        .priority(DataTagConstants.PRIORITY_LOW)
        .valueDescription("test description")
        .timeToLive(DataTagConstants.TTL_FOREVER)
        .build();
    
    sourceDataTagValue.setDaqTimestamp(daqTimestamp);
    ArrayList<SourceDataTagValue> tagList = new ArrayList<SourceDataTagValue>();
    tagList.add(sourceDataTagValue);
    DataTagValueUpdate dataTagValueUpdate = new DataTagValueUpdate(90L, tagList);
    sourceUpdateManager.processUpdates(dataTagValueUpdate);

    //check update is both in cache and DB
    DataTag cacheObject = (DataTag) dataTagCache.get(dataTag.getId());
    assertEquals(sourceDataTagValue.getValue(), cacheObject.getValue());
    assertEquals(sourceDataTagValue.getTimestamp(), cacheObject.getTimestamp());
    assertEquals(sourceDataTagValue.getDaqTimestamp(), cacheObject.getDaqTimestamp());
    assertEquals(sourceDataTagValue.getTimestamp(), cacheObject.getSourceTimestamp());
  }

  @Test
  public void testIncomingDataInvalidTagWithNullValue() throws InterruptedException {
    dataTagCache.put(dataTag.getId(), dataTag);

    //create a source update for this tag
    Timestamp timestamp = new Timestamp(System.currentTimeMillis() - 20);
    Timestamp daqTimestamp = new Timestamp(System.currentTimeMillis() - 10);
    //check datatag is set to value TRUE
//    assertEquals(Boolean.TRUE, dataTag.getValue());

    //set source update with value
    SourceDataTagValue sourceDataTagValue = SourceDataTagValue.builder()
        .id(dataTag.getId())
        .name(dataTag.getName())
        .controlTag(false)
        .value(1)
        .quality(new SourceDataTagQuality(SourceDataTagQualityCode.DATA_UNAVAILABLE))
        .timestamp(timestamp)
        .daqTimestamp(new Timestamp(System.currentTimeMillis()))
        .priority(DataTagConstants.PRIORITY_LOW)
        .valueDescription("test description")
        .timeToLive(DataTagConstants.TTL_FOREVER)
        .build();
    sourceDataTagValue.setDaqTimestamp(daqTimestamp);
    ArrayList<SourceDataTagValue> tagList = new ArrayList<SourceDataTagValue>();
    tagList.add(sourceDataTagValue);
    DataTagValueUpdate dataTagValueUpdate = new DataTagValueUpdate(90L, tagList);
    sourceUpdateManager.processUpdates(dataTagValueUpdate);

    //check update is both in cache and DB
    DataTag cacheObject = (DataTag) dataTagCache.get(dataTag.getId());
    assertEquals(sourceDataTagValue.getValue(), cacheObject.getValue());
    assertEquals(sourceDataTagValue.getTimestamp(), cacheObject.getTimestamp());
    assertEquals(sourceDataTagValue.getDaqTimestamp(), cacheObject.getDaqTimestamp());
    assertEquals(sourceDataTagValue.getTimestamp(), cacheObject.getSourceTimestamp());
    assertTrue(cacheObject.getDataTagQuality().getInvalidQualityStates().containsKey(TagQualityStatus.INACCESSIBLE));
  }

  /**
   * Tests that an incoming control tag is correctly saved in the cache and database.
   */
  @Test
  public void testIncomingControlTag() throws InterruptedException {
    controlTagCache.put(controlTag.getId(), controlTag);

    //check controltag value is not set to 2000
//    assertFalse(controlTag.getValue().equals(2000L));

    //create a source update for this tag
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    Timestamp daqTimestamp = new Timestamp(System.currentTimeMillis() + 1);
    SourceDataTagValue sourceDataTagValue = SourceDataTagValue.builder()
        .id(controlTag.getId())
        .name(controlTag.getName())
        .controlTag(true)
        .value(2000)
        .quality(new SourceDataTagQuality())
        .timestamp(timestamp)
        .daqTimestamp(new Timestamp(System.currentTimeMillis()))
        .priority(DataTagConstants.PRIORITY_LOW)
        .valueDescription("test description")
        .timeToLive(DataTagConstants.TTL_FOREVER)
        .build();
    sourceDataTagValue.setDaqTimestamp(daqTimestamp);
    ArrayList<SourceDataTagValue> tagList = new ArrayList<SourceDataTagValue>();
    tagList.add(sourceDataTagValue);
    DataTagValueUpdate dataTagValueUpdate = new DataTagValueUpdate(new Long(90L), tagList); //1000 is invented processId
    sourceUpdateManager.processUpdates(dataTagValueUpdate);

    //check update is both in cache and DB
    ControlTag cacheObject = (ControlTag) controlTagCache.get(controlTag.getId());
    assertEquals(sourceDataTagValue.getValue(), cacheObject.getValue());
    assertEquals(sourceDataTagValue.getTimestamp(), cacheObject.getTimestamp());
    assertEquals(sourceDataTagValue.getDaqTimestamp(), cacheObject.getDaqTimestamp());
    assertEquals(sourceDataTagValue.getTimestamp(), cacheObject.getSourceTimestamp());
  }
}
