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
package cern.c2mon.server.cache.dbaccess;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

import java.sql.Timestamp;

import javax.annotation.Resource;
import javax.validation.constraints.AssertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.server.cache.dbaccess.structure.DBBatch;
import cern.c2mon.server.cache.dbaccess.test.TestDataHelper;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.test.CacheObjectComparison;
import cern.c2mon.server.common.alarm.AlarmCondition;


/**
 * Tests the iBatis AlarmMapper.
 * @author Mark Brightwell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(transactionManager="cacheTransactionManager", defaultRollback=true)
@Transactional
@ContextConfiguration({"classpath:cern/c2mon/server/cache/dbaccess/config/server-cachedbaccess-test.xml"})
public class AlarmMapperTest {
  
  @Resource
  private AlarmMapper alarmMapper;
  
  @Resource
  private TestDataHelper testDataHelper;  
  
  
  /**
   * Makes sure DB is cleaned if failed to
   * run in a previous test.
   */
  @BeforeTransaction
  public void cleanAtStartUp() {
    testDataHelper.removeTestData();
  }
  
  /**
   * Calls the global create and insert on the
   * helper, so tests all DB inserts at once.
   */
  @Test
  public void testDataHelper() {
    testDataHelper.createTestData();
    testDataHelper.insertTestDataIntoDB();
  }
  
  /**
   * Tests the alarm is inserted and retrieved correctly.
   */
  @Test
  public void testInsertAndRetrieve() {
    testDataHelper.createTestData();
    testDataHelper.insertTestDataIntoDB();
    AlarmCacheObject alarmOriginal = testDataHelper.getAlarm3();    
    AlarmCacheObject alarmRetrieved = (AlarmCacheObject) alarmMapper.getItem(alarmOriginal.getId());
    CacheObjectComparison.equals(alarmOriginal, alarmRetrieved);
    
  }
  
  /**
   * Compares size of table with number of records.
   */
  @Test
  public void testGetAll() {
    int tableSize = alarmMapper.getNumberItems();
    int alarmsRetrieved = alarmMapper.getAll().size();
    assertEquals(tableSize, alarmsRetrieved);
  }
  
  /**
   * Checks runs.
   */
  @Test
  public void testGetMinId() {
    alarmMapper.getMinId();
  }
  
  /**
   * Checks runs.
   */
  @Test
  public void testGetMaxId() {
    alarmMapper.getMaxId();
  }  
  
  /**
   * Retrieves a batch of 10 and checks the number
   * retrieved is correct (checks > 10 in table).
   */
  @Test
  public void testGetRowBatch() {
    int tableSize = alarmMapper.getNumberItems();
    int alarmsRetrieved = alarmMapper.getRowBatch(new DBBatch(1L, 400000L)).size();
    if (tableSize >= 10) {
      assertEquals(10, alarmsRetrieved);
    } else {
      assertEquals(tableSize, alarmsRetrieved);
    }
  }
  
  @Test
  public void testUpdateAlarm() {
    testDataHelper.createTestData();
    testDataHelper.insertTestDataIntoDB();
    AlarmCacheObject alarmOriginal = testDataHelper.getAlarm1();
    //check is terminated
    assertEquals(alarmOriginal.getState(), AlarmCondition.TERMINATE);
    //update fields
    alarmOriginal.setState(AlarmCondition.ACTIVE);
    alarmOriginal.setTimestamp(new Timestamp(System.currentTimeMillis()));
    alarmOriginal.setInfo("updated info");
    alarmOriginal.hasBeenPublished(new Timestamp(System.currentTimeMillis() - 100));
    assertTrue(alarmOriginal.isPublishedToLaser());
    assertTrue(alarmOriginal.getLastPublication() != null);
    //update in DB
    alarmMapper.updateCacheable(alarmOriginal);
    //retrieve from DB
    AlarmCacheObject alarmRetrieved = (AlarmCacheObject) alarmMapper.getItem(alarmOriginal.getId());
    //compare
    CacheObjectComparison.equals(alarmOriginal, alarmRetrieved);
  }
  
  @Test
  public void testIsInDB() {
    assertTrue(alarmMapper.isInDb(350000L));
  }
  
  @Test
  public void testNotInDB() {
    assertFalse(alarmMapper.isInDb(450000L));
  }
  
  @After
  public void cleanDb() {
    testDataHelper.removeTestData();
  }
  
}

