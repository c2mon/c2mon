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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.util.TagQualityStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import cern.c2mon.server.common.datatag.DataTagCacheObject;

import java.sql.Timestamp;
import java.util.HashMap;

/**
 * Unit test of TagRecordConverter.
 *
 * @author Mark Brightwell
 *
 */
public class TagRecordConverterTest {

  /**
   * Class to test.
   */
  private TagRecordConverter converter = new TagRecordConverter();

  /**
   * mpper for writting tha value in string format to the LogTag
   */
  private ObjectMapper mapper = new ObjectMapper();

  /**
   * Tests the quality code is correctly generated.
   */
  @Test
  public void testCodeGeneration() {
    DataTagCacheObject tag = new DataTagCacheObject(10L);
    tag.getDataTagQuality().addInvalidStatus(TagQualityStatus.INACCESSIBLE, "desc1");
    tag.getDataTagQuality().addInvalidStatus(TagQualityStatus.UNINITIALISED, "desc1");
    tag.getDataTagQuality().addInvalidStatus(TagQualityStatus.VALUE_OUT_OF_BOUNDS, "desc1");
    assertNotNull(tag.getDataTagQuality());
    Loggable loggable = converter.convertToLogged(tag);
    TagRecord tagRecord = (TagRecord) loggable;
    assertEquals(11, tagRecord.getTagQualityCode());
  }

  /**
   * Tests conversion for some other fields.
   */
  @Test
  public void testConversion() {
    DataTagCacheObject tag = new DataTagCacheObject(10L);
    tag.setValueDescription("value desc");
    TagRecord tagRecord = (TagRecord) converter.convertToLogged(tag);
    assertEquals("value desc", tagRecord.getTagValueDesc());
  }

  @Test
  public void testTagConversionIntegerType(){
    DataTagCacheObject cacheObject = createTestCacheTag(1337, Integer.class);
    Loggable expectedObject = createHistoricTag(1337, Integer.class);

    Loggable testObject = converter.convertToLogged(cacheObject);

    tagLogEquals((TagRecord) expectedObject,(TagRecord)testObject);

  }

  @Test
  public void testTagConversionStringType(){
    DataTagCacheObject cacheObject = createTestCacheTag("hello", String.class);
    Loggable expectedObject = createHistoricTag("hello", String.class);

    Loggable testObject = converter.convertToLogged(cacheObject);

    tagLogEquals((TagRecord) expectedObject,(TagRecord)testObject);

  }

  @Test
  public void testTagConversionBooleanType(){
    DataTagCacheObject cacheObject = createTestCacheTag(Boolean.TRUE, Boolean.class);
    Loggable expectedObject = createHistoricTag(Boolean.TRUE, Boolean.class);

    Loggable testObject = converter.convertToLogged(cacheObject);

    tagLogEquals((TagRecord) expectedObject,(TagRecord)testObject);

  }

  @Test
  public void testTagConversionIntegerTArrayType(){
    DataTagCacheObject cacheObject = createTestCacheTag(new Integer[]{1,2,3}, Integer[].class);
    Loggable expectedObject = createHistoricTag(new Integer[]{1,2,3}, Integer[].class);

    Loggable testObject = converter.convertToLogged(cacheObject);

    tagLogEquals((TagRecord) expectedObject,(TagRecord)testObject);

  }

  @Test
  public void testTagConversionArbitraryType(){

    ComplexTestObject cacheVaule = ComplexTestObject.builder().intField(1).longField(22L).longField(23L).stringField("1234").build();
    ComplexTestObject logVaule = ComplexTestObject.builder().intField(1).longField(22L).longField(23L).stringField("1234").build();

    DataTagCacheObject cacheObject = createTestCacheTag(cacheVaule, ComplexTestObject.class);
    Loggable expectedObject = createHistoricTag(logVaule, ComplexTestObject.class);

    Loggable testObject = converter.convertToLogged(cacheObject);

    tagLogEquals((TagRecord) expectedObject,(TagRecord)testObject);
  }

  private void tagLogEquals(TagRecord arg1, TagRecord arg2){
    assertEquals(arg1.getDaqTimestamp(), arg2.getDaqTimestamp());
    assertEquals(arg1.getLogDate(), arg2.getLogDate());
    assertEquals(arg1.getDaqTimestamp(), arg2.getDaqTimestamp());
    assertEquals(arg1.getServerTimestamp(), arg2.getServerTimestamp());
    assertEquals(arg1.getSourceTimestamp(), arg2.getSourceTimestamp());
    assertEquals(arg1.getTagDataType(), arg2.getTagDataType());
    assertEquals(arg1.getTagDir(), arg2.getTagDir());
    assertEquals(arg1.getDaqTimestamp(), arg2.getDaqTimestamp());
    assertEquals(arg1.getId(), arg2.getId());
    assertEquals(arg1.getTagMode(), arg2.getTagMode());
    assertEquals(arg1.getTagName(), arg2.getTagName());
    assertEquals(arg1.getTagQualityCode(), arg2.getTagQualityCode());
    assertEquals(arg1.getTagValue(), arg2.getTagValue());
    assertEquals(arg1.getTagValueDesc(), arg2.getTagValueDesc());
  }

  private TagRecord createHistoricTag(Object value, Class<?> dataType){
    try {

      TagRecord tag = new TagRecord();
      tag.setTagId(10L);
      tag.setTagName("testName");
      tag.setTagValueDesc("valueDescription");
      tag.setTagDataType(dataType.getName());
      tag.setSourceTimestamp(new Timestamp(1234567890));
      tag.setDaqTimestamp(new Timestamp(1234567890));
      tag.setServerTimestamp(new Timestamp(1234567890));
      tag.setTagQualityCode(11);

      HashMap<TagQualityStatus, String> qualityTags = new HashMap<>();
      qualityTags.put(TagQualityStatus.INACCESSIBLE, "desc1");
      qualityTags.put(TagQualityStatus.UNINITIALISED, "desc1");
      qualityTags.put(TagQualityStatus.VALUE_OUT_OF_BOUNDS, "desc1");

      tag.setTagQualityDesc(mapper.writeValueAsString(qualityTags));

      tag.setTagMode((short) 1);

      tag.setTagValue(mapper.writeValueAsString(value));
      tag.setTagDir("I");

      return tag;
    } catch (JsonProcessingException e) {
      assertTrue(false);
    }
    return null;
  }

  private DataTagCacheObject createTestCacheTag(Object value, Class<?> dataType){
    DataTagCacheObject tag = new DataTagCacheObject(10L);
    tag.setName("testName");
    tag.setDataType(dataType.getName());
    tag.setAddress(new DataTagAddress());
    tag.setEquipmentId(11L);
    tag.setProcessId(1L);
    tag.setDescription("Description");
    tag.setValueDescription("valueDescription");
    tag.setMode((short) 1);
    tag.setJapcAddress("japcAdress");
    tag.setDipAddress("dipAdress");
    tag.setDaqTimestamp(new Timestamp(1234567890));
    tag.setSourceTimestamp(new Timestamp(1234567890));
    tag.setCacheTimestamp(new Timestamp(1234567890));

    tag.getDataTagQuality().addInvalidStatus(TagQualityStatus.INACCESSIBLE, "desc1");
    tag.getDataTagQuality().addInvalidStatus(TagQualityStatus.UNINITIALISED, "desc1");
    tag.getDataTagQuality().addInvalidStatus(TagQualityStatus.VALUE_OUT_OF_BOUNDS, "desc1");

    tag.setValue(value);

    return tag;
  }

}
