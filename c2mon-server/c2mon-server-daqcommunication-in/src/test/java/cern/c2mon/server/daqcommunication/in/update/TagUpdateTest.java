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
package cern.c2mon.server.daqcommunication.in.update;

import cern.c2mon.server.cache.ControlTagCache;
import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.dbaccess.ControlTagMapper;
import cern.c2mon.server.cache.dbaccess.DataTagMapper;
import cern.c2mon.server.cache.loading.ControlTagLoaderDAO;
import cern.c2mon.server.cache.loading.DataTagLoaderDAO;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.daqcommunication.in.junit.CachePopulationRule;
import cern.c2mon.shared.common.datatag.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.Timestamp;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Integration testing of the server-daqcommunication-in module.
 * @author Mark Brightwell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
    "classpath:config/server-cache.xml",
    "classpath:config/server-cachedbaccess.xml",
    "classpath:config/server-cacheloading.xml",
    "classpath:config/server-cachepersistence.xml",
    "classpath:config/server-daqcommunication-in.xml",
    "classpath:config/server-daqcommunication-out.xml",
    "classpath:config/server-supervision.xml",
    "classpath:test-config/server-test-properties.xml"
})
@TestPropertySource(
    locations = "classpath:c2mon-server-default.properties",
    properties = {
        "c2mon.server.cache.bufferedListenerPullFrequency=1"
    }
)
@Ignore("These tests are troublesome... to be rewritten using CountDownLatch")
public class TagUpdateTest implements ApplicationContextAware {

  @Rule
  @Autowired
  public CachePopulationRule cachePopulationRule;

  /**
   * Need context to explicitly start it (cache listeners
   * need explicit starting).
   */
  private ApplicationContext context;
  
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

  
  private DataTag dataTag;
  
  private ControlTag controlTag;
  
  @Before
  public void startContext() {
    ((AbstractApplicationContext) context).start();
  }
  
  @Before
  public void setUp() throws Exception {
    dataTag = dataTagMapper.getItem(200000L);
    controlTag = controlTagMapper.getItem(1221L);
  }
  
  /**
   * Tests that an incoming update is correctly saved in the cache and database.
   */
  @Test
  public void testIncomingDataTag() throws InterruptedException {
//    originalTag = DataTagMapperTest.createTestDataTag();//with id 1'000'000
//    //insert into the database
//    dataTagCacheDAO.insertDataTag(originalTag);
    //load value into the cache
    //dataTagCache.get(dataTag.getId());
    dataTagCache.put(dataTag.getId(), dataTag);
    
    //create a source update for this tag
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    Timestamp daqTimestamp = new Timestamp(System.currentTimeMillis() + 1);
    //check datatag is set to value TRUE
//    assertEquals(Boolean.TRUE, dataTag.getValue());
    
    //set source update with value
    SourceDataTagValue sourceDataTagValue = new SourceDataTagValue(dataTag.getId(), 
                                                                   dataTag.getName(),
                                                                   false, 1,
                                                                   new SourceDataQuality(),
                                                                   timestamp,
                                                                   DataTagConstants.PRIORITY_LOW,
                                                                   false,
                                                                   "test description",
                                                                   DataTagAddress.TTL_FOREVER);
    sourceDataTagValue.setDaqTimestamp(daqTimestamp);
    ArrayList<SourceDataTagValue> tagList = new ArrayList<SourceDataTagValue>();
    tagList.add(sourceDataTagValue);
    DataTagValueUpdate dataTagValueUpdate = new DataTagValueUpdate(90L, tagList);
    sourceUpdateManager.processUpdates(dataTagValueUpdate);
    
    //check update is both in cache and DB
    DataTag cacheObject = (DataTag) dataTagCache.get(dataTag.getId());
    assertEquals(dataTag.getValue(), cacheObject.getValue());
    assertEquals(dataTag.getTimestamp(), cacheObject.getTimestamp());
    assertEquals(dataTag.getDaqTimestamp(), cacheObject.getDaqTimestamp());
    assertEquals(dataTag.getSourceTimestamp(), cacheObject.getSourceTimestamp());

    // include a break for buffer to empty
    Thread.sleep(500);

    DataTag dbObject = (DataTag) dataTagMapper.getItem(cacheObject.getId());
    assertNotNull(dbObject);
    assertEquals(sourceDataTagValue.getValue(), dbObject.getValue());
    assertEquals(sourceDataTagValue.getTimestamp(), dbObject.getTimestamp());
    
  }
  
  @Test
  public void testIncomingDataInvalidTagWithNullValue() throws InterruptedException {
//    originalTag = DataTagMapperTest.createTestDataTag();//with id 1'000'000
//    //insert into the database
//    dataTagCacheDAO.insertDataTag(originalTag);
    //load value into the cache
    //dataTagCache.get(dataTag.getId());
    dataTagCache.put(dataTag.getId(), dataTag);
    
    //create a source update for this tag
    Timestamp timestamp = new Timestamp(System.currentTimeMillis() - 20);
    Timestamp daqTimestamp = new Timestamp(System.currentTimeMillis() - 10);
    //check datatag is set to value TRUE
//    assertEquals(Boolean.TRUE, dataTag.getValue());
    
    //set source update with value
    SourceDataTagValue sourceDataTagValue = new SourceDataTagValue(dataTag.getId(), 
                                                                   dataTag.getName(),
                                                                   false, 1,
                                                                   new SourceDataQuality(Short.valueOf("4")), //invalid
                                                                   timestamp,
                                                                   DataTagConstants.PRIORITY_LOW,
                                                                   false,
                                                                   "test description",
                                                                   DataTagAddress.TTL_FOREVER);
    sourceDataTagValue.setDaqTimestamp(daqTimestamp);
    ArrayList<SourceDataTagValue> tagList = new ArrayList<SourceDataTagValue>();
    tagList.add(sourceDataTagValue);
    DataTagValueUpdate dataTagValueUpdate = new DataTagValueUpdate(90L, tagList);
    sourceUpdateManager.processUpdates(dataTagValueUpdate);
    
    //check update is both in cache and DB
    DataTag cacheObject = (DataTag) dataTagCache.get(dataTag.getId());
    assertEquals(dataTag.getValue(), cacheObject.getValue());
    assertEquals(dataTag.getTimestamp(), cacheObject.getTimestamp());
    assertEquals(dataTag.getDaqTimestamp(), cacheObject.getDaqTimestamp());
    assertEquals(dataTag.getSourceTimestamp(), cacheObject.getSourceTimestamp());

    // include a break for buffer to empty
    Thread.sleep(500);

    DataTag dbObject = (DataTag) dataTagMapper.getItem(cacheObject.getId());
    assertNotNull(dbObject);
    //original value
    assertEquals(dataTag.getValue(), dbObject.getValue());
    //new timestamp
    //assertEquals(sourceDataTagValue.getTimestamp(), dbObject.getTimestamp());
    assertTrue(!sourceDataTagValue.getDaqTimestamp().equals(dbObject.getDaqTimestamp()));
    //invalid quality inaccessible
    assertTrue(dbObject.getDataTagQuality().getInvalidQualityStates().containsKey(TagQualityStatus.INACCESSIBLE));
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
    SourceDataTagValue sourceDataTagValue = new SourceDataTagValue(controlTag.getId(),
                                                                   controlTag.getName(),
                                                                   true, new Long(2000),   //CONTROL TAG WITH VALUE 2000
                                                                   new SourceDataQuality(),
                                                                   timestamp,
                                                                   DataTagConstants.PRIORITY_LOW,
                                                                   false,
                                                                   "test description",
                                                                   DataTagAddress.TTL_FOREVER);
    sourceDataTagValue.setDaqTimestamp(daqTimestamp);
    ArrayList<SourceDataTagValue> tagList = new ArrayList<SourceDataTagValue>();
    tagList.add(sourceDataTagValue);    
    DataTagValueUpdate dataTagValueUpdate = new DataTagValueUpdate(new Long(90L), tagList); //1000 is invented processId
    sourceUpdateManager.processUpdates(dataTagValueUpdate);
    
    //check update is both in cache and DB
    ControlTag cacheObject = (ControlTag) controlTagCache.get(controlTag.getId());
    assertEquals(controlTag.getValue(), cacheObject.getValue());
    assertEquals(controlTag.getTimestamp(), cacheObject.getTimestamp());
    assertEquals(controlTag.getDaqTimestamp(), cacheObject.getDaqTimestamp());
    assertEquals(controlTag.getSourceTimestamp(), cacheObject.getSourceTimestamp());

    // include a break for buffer to empty
    Thread.sleep(500);

    ControlTag dbObject = (ControlTag) controlTagMapper.getItem(cacheObject.getId());
    assertEquals(sourceDataTagValue.getValue(), dbObject.getValue());
    assertEquals(sourceDataTagValue.getTimestamp(), dbObject.getTimestamp());
    
  }
  
  /**
   * Set the application context. Used for explicit start.
   */
  @Override
  public void setApplicationContext(ApplicationContext arg0) throws BeansException {
    context = arg0;
  }
  
  
}
