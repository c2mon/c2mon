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
package cern.c2mon.server.history.mapper;

import java.util.TimeZone;

import cern.c2mon.server.cache.config.inmemory.CacheModule;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModule;
import cern.c2mon.server.command.config.CommandModule;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.daq.config.DaqModule;
import cern.c2mon.server.history.config.HistoryModule;
import cern.c2mon.server.history.structure.TagRecord;
import cern.c2mon.server.supervision.config.SupervisionModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests the iBatis mapper against the Oracle DB.
 *
 * @author Mark Brightwell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    CommonModule.class,
    CacheModule.class,
    CacheDbAccessModule.class,
    CacheLoadingModule.class,
    SupervisionModule.class,
    CommandModule.class,
    DaqModule.class,
    HistoryModule.class
})
public class TagRecordMapperTest {

  /**
   * Test data tag fields.
   */
  private static final Long ID = 10L;
  private static final String DATATYPE = "Integer";
  private static final String TAGVALUE = "25";
  private static final String TAGVALUEDESC = "Tag value description";
  private static final java.sql.Timestamp TAGTIME = new java.sql.Timestamp(System.currentTimeMillis());
  private static final java.sql.Timestamp LOGTIME = new java.sql.Timestamp(System.currentTimeMillis()+1000);

  /**
   * To test.
   */
  @Autowired
  private TagRecordMapper tagRecordMapper;

  /**
   * Removes test values from previous tests in case clean up failed.
   */
  @Before
  public void beforeTest() {
    removeTestData();
  }

  /**
   * Removes test values after test.
   */
  @After
  public void afterTest() {
    removeTestData();
  }

  /**
   * Removes test data.
   */
  private void removeTestData() {
    tagRecordMapper.deleteDataTagLog(ID);
  }

  /**
   * Tests insertion completes successfully when fallback not
   * active (so no logtime set in object).
   */
  @Test
  public void testInsertDataTagLog() {
    TagRecord tag = new TagRecord();
    tag.setTagId(ID);
    tag.setTagDataType(DATATYPE);
    tag.setSourceTimestamp(TAGTIME);
    tag.setDaqTimestamp(TAGTIME);
    tag.setServerTimestamp(TAGTIME);
    tag.setTagValue(TAGVALUE);
    tag.setTagValueDesc(TAGVALUEDESC);
    tagRecordMapper.insertLog(tag);
  }

  /**
   * Tests insertion completes successfully when fallback is
   * active. In this case, the logtime needs including as does
   * a specification of the timezone of the DB.
   */
  @Test
  public void testInsertDataTagLogFromFallback() {
    TagRecord tag = new TagRecord();
    tag.setTagId(ID);
    tag.setTagDataType(DATATYPE);
    tag.setLogDate(LOGTIME);
    tag.setSourceTimestamp(TAGTIME);
    tag.setDaqTimestamp(TAGTIME);
    tag.setServerTimestamp(TAGTIME);
    tag.setTagValue(TAGVALUE);
    tag.setTagValue(TAGVALUEDESC);
    tag.setTimezone(TimeZone.getDefault().getID());
    tagRecordMapper.insertLog(tag);
  }
}
