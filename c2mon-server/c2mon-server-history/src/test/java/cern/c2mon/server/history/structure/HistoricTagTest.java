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
package cern.c2mon.server.history.structure;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

import java.sql.Timestamp;

import org.junit.Test;

import cern.c2mon.pmanager.fallback.exception.DataFallbackException;

/**
 * Unit test of fallback conversion to test file and back.
 *
 * @author Mark Brightwell
 *
 */
public class HistoricTagTest {

  private static HistoricTag getTag() {
    HistoricTag tag = new HistoricTag();
    Timestamp ts = new Timestamp(System.currentTimeMillis()-20);
    Timestamp ts2 = new Timestamp(System.currentTimeMillis()-10);
    Timestamp ts3 = new Timestamp(System.currentTimeMillis()-5);
    tag.setTagId(10L);
    tag.setTagName("name");
    tag.setTagValue("1223");
    tag.setTagValueDesc("value desc");
    tag.setTagDataType("Integer");
    tag.setSourceTimestamp(ts);
    tag.setDaqTimestamp(ts2);
    tag.setServerTimestamp(ts3);
    tag.setTagQualityCode(3);
    tag.setTagQualityDesc("{\"UNKNOWN_REASON\":\"Quality description was too long: unable to store in history table\"}");
    tag.setTagDir("D");
    tag.setTagMode(Short.valueOf("1"));
    return tag;
  }

  @Test
  public void testStringEncoding() throws DataFallbackException {
    HistoricTag tag = getTag();
    String encoded = tag.toString();
    HistoricTag retrievedLog = (HistoricTag) tag.getObject(encoded);
    assertSameLog(tag, retrievedLog);
    //log date is set when logging to file
    assertNotNull(retrievedLog.getLogDate());
  }

  @Test
  public void testEncodingWithNullDaqTimestamp() throws DataFallbackException {
    HistoricTag tag = getTag();
    tag.setDaqTimestamp(null);
    String encoded = tag.toString();
    HistoricTag retrievedLog = (HistoricTag) tag.getObject(encoded);
    assertSameLog(tag, retrievedLog);
    //log date is set when logging to file
    assertNotNull(retrievedLog.getLogDate());
  }

  @Test
  public void testEncodingWithNullSourceTimestamp() throws DataFallbackException {
    HistoricTag tag = getTag();
    tag.setSourceTimestamp(null);
    String encoded = tag.toString();
    HistoricTag retrievedLog = (HistoricTag) tag.getObject(encoded);
    assertSameLog(tag, retrievedLog);
    //log date is set when logging to file
    assertNotNull(retrievedLog.getLogDate());
  }

  @Test
  public void testEncodingWithNullValue() throws DataFallbackException {
    HistoricTag tag = getTag();
    tag.setTagValue(null);
    String encoded = tag.toString();
    HistoricTag retrievedLog = (HistoricTag) tag.getObject(encoded);
    assertSameLog(tag, retrievedLog);
    //log date is set when logging to file
    assertNotNull(retrievedLog.getLogDate());
  }

  @Test
  public void testEncodingWithNullValueDesc() throws DataFallbackException {
    HistoricTag tag = getTag();
    tag.setTagValueDesc(null);
    String encoded = tag.toString();
    HistoricTag retrievedLog = (HistoricTag) tag.getObject(encoded);
    assertSameLog(tag, retrievedLog);
    //log date is set when logging to file
    assertNotNull(retrievedLog.getLogDate());
  }

  private void assertSameLog(HistoricTag log, HistoricTag retrievedLog) {
    assertEquals(log.getTagId(), retrievedLog.getTagId());
    assertEquals(log.getTagName(), retrievedLog.getTagName());
    assertEquals(log.getTagDataType(), retrievedLog.getTagDataType());
    assertEquals(log.getTagValue(), retrievedLog.getValue());
    assertEquals(log.getTagValueDesc(), retrievedLog.getTagValueDesc());
    assertEquals(log.getSourceTimestamp(), retrievedLog.getSourceTimestamp());
    assertEquals(log.getDaqTimestamp(), retrievedLog.getDaqTimestamp());
    assertEquals(log.getServerTimestamp(), retrievedLog.getServerTimestamp());
    assertEquals(log.getTagQualityCode(), retrievedLog.getTagQualityCode());
    assertEquals(log.getTagQualityDesc(), retrievedLog.getTagQualityDesc());
    assertEquals(log.getTagDir(), retrievedLog.getTagDir());
    assertEquals(log.getTagMode(), retrievedLog.getTagMode());
  }

}
