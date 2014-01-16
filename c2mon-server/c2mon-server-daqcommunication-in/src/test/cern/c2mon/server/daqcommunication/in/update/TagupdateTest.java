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
package cern.c2mon.server.daqcommunication.in.update;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.sql.Timestamp;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.cache.ControlTagCache;
import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.dbaccess.ControlTagMapper;
import cern.c2mon.server.cache.dbaccess.DataTagMapper;
import cern.c2mon.server.cache.dbaccess.test.TestDataHelper;
import cern.c2mon.server.cache.loading.ControlTagLoaderDAO;
import cern.c2mon.server.cache.loading.DataTagLoaderDAO;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.test.broker.TestBrokerService;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import cern.c2mon.shared.daq.datatag.DataTagValueUpdate;
import cern.c2mon.shared.daq.datatag.SourceDataQuality;
import cern.c2mon.shared.daq.datatag.SourceDataTagValue;

/**
 * Integration testing of the server-daqcommunication-in module.
 * @author Mark Brightwell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration({"classpath:cern/c2mon/server/daqcommunication/in/config/server-daq-in-tagupdate-test.xml" })
public class TagupdateTest implements ApplicationContextAware {
  
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
   * Used to create a test datatag in the database.
   */
  @Autowired
  private DataTagLoaderDAO dataTagCacheDAO;
  @Autowired
  private ControlTagLoaderDAO controlTagCacheDAO;
  
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
  private TestDataHelper testDataHelper;
  
  private DataTag dataTag;
  
  private ControlTag controlTag;
  
  private TestBrokerService testBrokerService;
  
  @Before
  public void startContext() {
    ((AbstractApplicationContext) context).start();
  }
  
  @Before
  public void setUp() throws Exception {
    testBrokerService = new TestBrokerService();
    testBrokerService.createAndStartBroker();
    testDataHelper.removeTestData();
    testDataHelper.createTestData();
    testDataHelper.insertTestDataIntoDB();
    dataTag = testDataHelper.getDataTag();
    controlTag = testDataHelper.getProcessAliveTag();    
  }
  
  @After
  public void cleanUp() throws Exception {    
    testDataHelper.removeTestData();
    testBrokerService.stopBroker();
  }
  
  /**
   * Tests that an incoming update is correctly saved in the cache and database.
   */
  @Test
  @DirtiesContext
  public void testIncomingDataTag() {
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
    assertEquals(Boolean.TRUE, dataTag.getValue());
    
    //set source update with value FALSE
    SourceDataTagValue sourceDataTagValue = new SourceDataTagValue(dataTag.getId(), 
                                                                   dataTag.getName(),
                                                                   false, Boolean.FALSE,
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
    
    
    //include a break for buffer to empty
    try {
      Thread.sleep(15000);
    } catch (InterruptedException e) {      
      e.printStackTrace();
    }
    DataTag dbObject = (DataTag) dataTagMapper.getItem(cacheObject.getId());
    assertNotNull(dbObject);
    assertEquals(sourceDataTagValue.getValue(), dbObject.getValue());
    assertEquals(sourceDataTagValue.getTimestamp(), dbObject.getTimestamp());
    
  }
  
  @Test
  @DirtiesContext
  public void testIncomingDataInvalidTagWithNullValue() {
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
    assertEquals(Boolean.TRUE, dataTag.getValue());
    
    //set source update with value FALSE
    SourceDataTagValue sourceDataTagValue = new SourceDataTagValue(dataTag.getId(), 
                                                                   dataTag.getName(),
                                                                   false, null,
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
    
    
    //include a break for buffer to empty
    try {
      Thread.sleep(15000);
    } catch (InterruptedException e) {      
      e.printStackTrace();
    }
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
  @DirtiesContext
  public void testIncomingControlTag() {     
    controlTagCache.put(controlTag.getId(), controlTag);
    
    //check controltag value is not set to 2000
    assertFalse(controlTag.getValue().equals(2000L));
    
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
    
    //include a break for buffer to empty, before DB check
    try {
      Thread.sleep(15000);
    } catch (InterruptedException e) {      
      e.printStackTrace();
    }
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
