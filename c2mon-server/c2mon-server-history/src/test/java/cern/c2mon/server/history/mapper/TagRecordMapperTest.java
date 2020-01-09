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

import cern.c2mon.server.history.MapperTest;
import cern.c2mon.server.history.structure.TagRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.TimeZone;

/**
 * Tests the iBatis mapper against the Oracle DB.
 *
 * @author Mark Brightwell
 *
 */
public class TagRecordMapperTest extends MapperTest {

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
