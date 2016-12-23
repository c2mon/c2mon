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
package cern.c2mon.server.elasticsearch.structure.converter;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cern.c2mon.server.elasticsearch.tag.EsTag;
import cern.c2mon.server.elasticsearch.tag.ElasticsearchTagConverter;
import com.google.gson.GsonBuilder;
import lombok.Data;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.test.CacheObjectCreation;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import cern.c2mon.shared.common.metadata.Metadata;

import static org.junit.Assert.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * Checks on the fields of data appened/set to AbstractEsTag.
 *
 * @author Alban Marguet, Matthias Braeger
 */
@RunWith(MockitoJUnitRunner.class)
public class ElasticsearchTagConverterTest {

  @InjectMocks
  private ElasticsearchTagConverter tagConverter;

  @Mock
  private ProcessCache processCache;

  @Mock
  private EquipmentCache equipmentCache;

  @Mock
  private SubEquipmentCache subEquipmentCache;

  @Mock
  private DataTagQuality dataTagQuality;

  @Mock
  private DataTagCacheObject tag;

  @Mock
  private Tag tagC2MON;

  @Before
  public void setUp() throws Exception {
    reset(processCache,
        equipmentCache,
        subEquipmentCache,
        dataTagQuality,
        tag,
        tagC2MON);
  }

  @Test
  public void testGetProcessName() {
    ProcessCacheObject process = CacheObjectCreation.createTestProcess1();
    String expected = "process1";
    process.setName(expected);

    when(processCache.get(1L)).thenReturn(process);
    assertEquals(expected, processCache.get(1L).getName());
  }

  @Test
  public void testGetEquipmentName() {
    EquipmentCacheObject equipment = CacheObjectCreation.createTestEquipment();
    String expected = "equipment1";
    equipment.setName(expected);

    when(equipmentCache.get(1L)).thenReturn(equipment);
    assertEquals(expected, equipmentCache.get(1L).getName());
  }

  @Test
  public void testGetSubEquipmentName() {
    SubEquipmentCacheObject subEquipment = CacheObjectCreation.createTestSubEquipment();
    String expected = "subEquipment1";
    subEquipment.setName(expected);

    when(subEquipmentCache.get(1L)).thenReturn(subEquipment);
    assertEquals(expected, subEquipmentCache.get(1L).getName());
  }

  @Test
  public void generalTestGetTagMetadataProcess() {
    ProcessCacheObject process = CacheObjectCreation.createTestProcess1();
    process.setName("process");
    EquipmentCacheObject equipment = CacheObjectCreation.createTestEquipment();
    equipment.setName("equipment");
    equipment.setProcessId(process.getId());
    SubEquipmentCacheObject subEquipment = CacheObjectCreation.createTestSubEquipment();
    subEquipment.setName("subEquipment");
    subEquipment.setParentId(equipment.getId());
    HashMap<String, String> expected = new HashMap<>();
    expected.put("Process", "process");
    expected.put("Equipment", "equipment");
    expected.put("SubEquipment", "subEquipment");

    when(processCache.get(1L)).thenReturn(process);
    when(equipmentCache.get(1L)).thenReturn(equipment);
    when(subEquipmentCache.get(1L)).thenReturn(subEquipment);

    assertEquals(processCache.get(1L), process);
    assertEquals(equipmentCache.get(1L), equipment);
    assertEquals(subEquipmentCache.get(1L), subEquipment);

    when(tag.getSubEquipmentIds()).thenReturn(Collections.singleton(subEquipment.getId()));
    assertEquals(subEquipmentCache.get(1L).getParentId(), equipment.getId());
    assertEquals(equipmentCache.get(1L).getProcessId(), process.getId());
  }

  @Test
  public void testGetTagMetadataProcessWithSubEquipment() {
    ProcessCacheObject process = CacheObjectCreation.createTestProcess1();
    process.setName("process");
    EquipmentCacheObject equipment = CacheObjectCreation.createTestEquipment();
    equipment.setName("equipment");
    equipment.setProcessId(process.getId());
    SubEquipmentCacheObject subEquipment = CacheObjectCreation.createTestSubEquipment();
    subEquipment.setName("subEquipment");
    subEquipment.setParentId(equipment.getId());
    HashMap<String, String> expected = new HashMap<>();
    expected.put("process", "process");
    expected.put("equipment", "equipment");
    expected.put("subEquipment", "subEquipment");

    tag.setProcessId(process.getId());
    when(processCache.get(process.getId())).thenReturn(process);
    tag.setEquipmentId(equipment.getId());
    when(equipmentCache.get(equipment.getId())).thenReturn(equipment);
    tag.setSubEquipmentId(subEquipment.getId());
    when(subEquipmentCache.get(subEquipment.getId())).thenReturn(subEquipment);
    when(tag.getSubEquipmentIds()).thenReturn(Collections.singleton(subEquipment.getId()));

    Map<String, String> result = tagConverter.retrieveTagProcessMetadata(tag);

    assertEquals(expected, result);
  }

  @Test
  public void testGetTagMetadataProcessWithEquipment() {
    ProcessCacheObject process = CacheObjectCreation.createTestProcess1();
    process.setName("process");
    EquipmentCacheObject equipment = CacheObjectCreation.createTestEquipment();
    equipment.setName("equipment");
    equipment.setProcessId(process.getId());

    HashMap<String, String> expected = new HashMap<>();
    expected.put("process", "process");
    expected.put("equipment", "equipment");

    tag.setProcessId(process.getId());
    when(processCache.get(process.getId())).thenReturn(process);
    tag.setEquipmentId(equipment.getId());
    when(equipmentCache.get(equipment.getId())).thenReturn(equipment);
    when(tag.getEquipmentIds()).thenReturn(Collections.singleton(equipment.getId()));

    Map<String, String> result = tagConverter.retrieveTagProcessMetadata(tag);

    assertEquals(expected, result);
  }

  @Test
  public void testGetMetadataProcessWithProcess() {
    ProcessCacheObject process = CacheObjectCreation.createTestProcess1();
    process.setName("process");

    HashMap<String, String> expected = new HashMap<>();
    expected.put("process", "process");

    tag.setProcessId(process.getId());
    when(processCache.get(process.getId())).thenReturn(process);
    when(tag.getProcessIds()).thenReturn(Collections.singleton(process.getId()));

    Map<String, String> result = tagConverter.retrieveTagProcessMetadata(tag);
    assertEquals(expected, result);
  }

  @Test
  public void testGetMetadataProcessWithNull() {
    HashMap<String, String> expected = new HashMap<>();

    Map<String, String> result = tagConverter.retrieveTagProcessMetadata(tag);
    assertEquals(expected, result);
  }

  @Test
  public void testGetTagMetadataWithNothing() {
    HashMap<String, String> expected = new HashMap<>();
    DataTagCacheObject tag = CacheObjectCreation.createTestDataTag();
    tag.setEquipmentId(null);
    tag.setSubEquipmentId(null);
    tag.setProcessId(null);
    assertEquals(expected, tagConverter.retrieveTagProcessMetadata(tag));
  }

  @Test
  public void testConvertToTagES() throws IOException {
    convertToTagES(Boolean.FALSE, EsTag.TYPE_BOOLEAN);

    convertToTagES(Integer.MAX_VALUE, EsTag.TYPE_NUMBER);
    convertToTagES(0, EsTag.TYPE_NUMBER);
    convertToTagES(null, "Integer", EsTag.TYPE_NUMBER);

    convertToTagES(Long.MIN_VALUE, EsTag.TYPE_NUMBER);
    convertToTagES(Double.MIN_VALUE, EsTag.TYPE_NUMBER);
    convertToTagES(Float.MIN_VALUE, EsTag.TYPE_NUMBER);
    convertToTagES(Short.MAX_VALUE, EsTag.TYPE_NUMBER);

    convertToTagES("John Lennon", EsTag.TYPE_STRING);

    ComplexType test = new ComplexType();
    test.setBool(true);
    test.setInteger(5);
    test.setString("Paul McCartney");
    convertToTagES(new GsonBuilder().create().toJson(test), test.getClass().getName(), EsTag.TYPE_OBJECT);

  }

  private void convertToTagES(final Object value, final String type) throws IOException {
    convertToTagES(value, value.getClass().getName(), type);
    convertToTagES(value, value.getClass().getName().substring("java.lang.".length()), type);
  }

  private void convertToTagES(final Object value, final String dataType, final String type) throws IOException {
    long id = 1L;
    String name = "tag";
    long timeStamp = 123456L;
    String valueDesc = "ok";

    when(tagC2MON.getId()).thenReturn(id);
    when(tagC2MON.getName()).thenReturn(name);
    when(tagC2MON.getDataType()).thenReturn(dataType);
    when(tagC2MON.getCacheTimestamp()).thenReturn(new Timestamp(timeStamp));
    when(tagC2MON.getDataTagQuality()).thenReturn(null);
    when(tagC2MON.getValue()).thenReturn(value);
    when(tagC2MON.getTimestamp()).thenReturn(new Timestamp(1234567));
    when(tagC2MON.getDataTagQuality()).thenReturn(new DataTagQualityImpl());
    when(tagC2MON.getValueDescription()).thenReturn(valueDesc);
    when(tagC2MON.getUnit()).thenReturn("km");

    EsTag esTag = tagConverter.convert(tagC2MON);
    assertEquals(id, esTag.getIdAsLong());
    assertEquals(name, esTag.getName());
    assertEquals(type, esTag.getType());
    assertNotNull(esTag.getC2mon());
    assertEquals(dataType, esTag.getC2mon().getDataType());
    assertEquals(timeStamp, esTag.getC2mon().getServerTimestamp());
    assertEquals(1, esTag.getQuality().getStatus());
    assertEquals(valueDesc, esTag.getValueDescription());
    assertNotNull(esTag.getQuality());
    assertFalse(esTag.getQuality().isValid());
    assertEquals("km", esTag.getUnit());

    when(tagC2MON.getDataTagQuality()).thenReturn(new DataTagQualityImpl(TagQualityStatus.EQUIPMENT_DOWN));
    esTag = tagConverter.convert(tagC2MON);
    assertNotNull(esTag.getQuality());
    assertFalse(esTag.getQuality().isValid());
  }

  @Test
  public void testBadValues() {
    DataTagCacheObject tag = CacheObjectCreation.createTestDataTag();
    tag.setDaqTimestamp(null);
    tag.setSourceTimestamp(null);

    EsTag esTag = tagConverter.convert(tag);
    assertEquals(esTag.getType(), EsTag.TYPE_BOOLEAN);
    assertNotNull(esTag.getTimestamp());
    assertNotNull(esTag.getC2mon());
    assertNotNull(esTag.getC2mon().getDaqTimestamp());
  }

  @Test
  public void ModuleConvertsTagsAsExpected() {
    DataTagCacheObject tag = CacheObjectCreation.createTestDataTag();
    tag.setDataType("Boolean");
    tag.setLogged(true);

    EsTag esTag = tagConverter.convert(tag);

    assertEquals(EsTag.TYPE_BOOLEAN, esTag.getType());
    assertTrue(esTag.getValueBoolean());
    assertEquals(1, esTag.getValue());
    assertNull(esTag.getValueString());
    assertNotNull(esTag.getC2mon());
    assertEquals("Boolean", esTag.getC2mon().getDataType());

    assertEquals(EsTag.TYPE_BOOLEAN, esTag.getType());

    DataTagCacheObject tagNumeric = new DataTagCacheObject();
    tagNumeric.setId(1L);
    tagNumeric.setDataType("Integer");
    tagNumeric.setValue(126387213);

    esTag = tagConverter.convert(tagNumeric);

    assertEquals(esTag.getType(), EsTag.TYPE_NUMBER);
    assertNull(esTag.getValueBoolean());
    assertEquals(126387213, esTag.getValue());
    assertNull(esTag.getValueString());
    assertNotNull(esTag.getC2mon());
    assertEquals("Integer", esTag.getC2mon().getDataType());
    assertEquals(EsTag.TYPE_NUMBER, esTag.getType());
  }

  @Test
  public void testMetadata() {
    Map<String, Object> map = new HashMap<>();
    map.put("building", "1");
    map.put("responsible", "coucou");
    map.put("intShouldGoToStringMetadata", 2);
    Metadata metadata = new Metadata(map);

    DataTagCacheObject tag = CacheObjectCreation.createTestDataTag();
    tag.setDataType("Boolean");
    tag.setLogged(true);

    tag.setMetadata(metadata);

    EsTag esTag = tagConverter.convert(tag);
    assertNotNull(esTag.getMetadata());
    assertEquals("1", esTag.getMetadata().get("building"));
    assertEquals("2", esTag.getMetadata().get("intShouldGoToStringMetadata"));
    assertEquals("coucou", esTag.getMetadata().get("responsible"));
  }

  @Test
  public void testConvert_WhenValueTypeIsBoolean_ConvertsToEsTagAndAssignsType() {
    DataTagCacheObject tag = CacheObjectCreation.createTestDataTag();

    EsTag esTag = tagConverter.convert(tag);

    assertNotNull(esTag);
    assertEquals(EsTag.TYPE_BOOLEAN, esTag.getType());
  }


  @Data
  private class ComplexType {
    String string;
    Integer integer;
    Boolean bool;
  }

}
