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
package cern.c2mon.server.eslog.structure.converter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cern.c2mon.server.eslog.structure.converter.DataTagESLogConverter;
import cern.c2mon.server.eslog.structure.types.TagNumeric;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
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
import cern.c2mon.server.eslog.structure.mappings.Mapping.ValueType;
import cern.c2mon.server.eslog.structure.types.TagBoolean;
import cern.c2mon.server.eslog.structure.types.TagES;
import cern.c2mon.server.test.CacheObjectCreation;
import cern.c2mon.shared.common.datatag.DataTagQuality;

/**
 * Checks on the fields of data appened/set to TagES.
 * 
 * @author Alban Marguet.
 */
@RunWith(MockitoJUnitRunner.class)
public class DataTagESLogConverterTest {

  /**
   * The class under test.
   */
  @InjectMocks
  DataTagESLogConverter esLogConverter;

  @Mock
  ProcessCache processCache;

  @Mock
  EquipmentCache equipmentCache;

  @Mock
  SubEquipmentCache subEquipmentCache;

  @Mock
  DataTagQuality dataTagQuality;

  @Mock
  DataTagCacheObject tag;

  @Mock
  Tag tagC2MON;

  @Mock
  TagES tagES;

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

    Map<String, String> result = esLogConverter.getTagMetadataProcess(tag);

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

    Map<String, String> result = esLogConverter.getTagMetadataProcess(tag);

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

    Map<String, String> result = esLogConverter.getTagMetadataProcess(tag);
    assertEquals(expected, result);
  }

  @Test
  public void testGetMetadataProcessWithNull() {
    HashMap<String, String> expected = new HashMap<>();

    Map<String, String> result = esLogConverter.getTagMetadataProcess(tag);
    assertEquals(expected, result);
  }

  @Test
  public void testGetTagMetadataWithNothing() {
    HashMap<String, String> expected = new HashMap<>();
    DataTagCacheObject tag = CacheObjectCreation.createTestDataTag();
    tag.setEquipmentId(null);
    tag.setSubEquipmentId(null);
    tag.setProcessId(null);
    assertEquals(expected, esLogConverter.getTagMetadataProcess(tag));
  }

  @Test
  public void testConvertToTagES() throws IOException {
    long id = 1L;
    String name = "tag";
    ValueType type = ValueType.boolType;
    long timeStamp = 123456L;
    boolean value = true;
    String valueDesc = "ok";
    when(tagC2MON.getId()).thenReturn(id);
    when(tagC2MON.getName()).thenReturn(name);
    when(tagC2MON.getDataType()).thenReturn(type.toString());
    when(tagC2MON.getCacheTimestamp()).thenReturn(new Timestamp(timeStamp));
    when(tagC2MON.getDataTagQuality()).thenReturn(null);
    when(tagC2MON.getValue()).thenReturn(value);
    when(tagC2MON.getValueDescription()).thenReturn(valueDesc);

    TagES tagES = esLogConverter.convertToTagES(tagC2MON);
    assertEquals(id, tagES.getId());
    assertEquals(name, tagES.getName());
    assertEquals(type.toString(), tagES.getDataType());
    assertEquals(timeStamp, tagES.getServerTimestamp());
    assertEquals(0, tagES.getStatus());
    assertEquals(value, tagES.getValue());
    assertEquals(valueDesc, tagES.getValueDescription());
    assertNull(tagES.getQuality());
    assertTrue(tagES.getValid());

    when(tagC2MON.getDataType()).thenReturn(ValueType.stringType.toString());
    when(tagC2MON.getValue()).thenReturn(name);
    tagES = esLogConverter.convertToTagES(tagC2MON);
    assertEquals(name, tagES.getValue());

    when(tagC2MON.getDataType()).thenReturn(ValueType.longType.toString());
    when(tagC2MON.getValue()).thenReturn(timeStamp);
    tagES = esLogConverter.convertToTagES(tagC2MON);
    assertEquals(timeStamp, tagES.getValue());

    when(tagC2MON.getDataTagQuality()).thenReturn(new DataTagQualityImpl(TagQualityStatus.EQUIPMENT_DOWN));
    tagES = esLogConverter.convertToTagES(tagC2MON);
    assertNotNull(tagES.getQuality());
    assertFalse(tagES.getValid());
  }

  @Test
  public void testBadValues() {
    DataTagCacheObject tag = CacheObjectCreation.createTestDataTag();
    tag.setDaqTimestamp(null);
    tag.setSourceTimestamp(null);
    TagES tagES = esLogConverter.convertToTagES(tag);
    assertTrue(tagES instanceof TagBoolean);
    assertEquals(0, tagES.getDaqTimestamp());
    assertEquals(0, tagES.getSourceTimestamp());
  }

  @Test
  public void ModuleConvertsTagsAsExpected() {
    Tag tag = createTagExample();
    TagES tagES = esLogConverter.convertToTagES(tag);

    assertTrue(tagES instanceof TagBoolean);
    assertTrue(tagES.getValueBoolean());
    assertEquals(1, tagES.getValueNumeric());
    assertNull(tagES.getValueString());
    assertTrue((Boolean) tagES.getValue());
    assertEquals("boolean", tagES.getDataType());

    DataTagCacheObject tagNumeric = new DataTagCacheObject();
    tagNumeric.setId(1L);
    tagNumeric.setDataType("Integer");
    tagNumeric.setValue(126387213);

    tagES = esLogConverter.convertToTagES(tagNumeric);

    assertTrue(tagES instanceof TagNumeric);
    assertNull(tagES.getValueBoolean());
    assertEquals(126387213, tagES.getValueNumeric());
    assertEquals(126387213, tagES.getValue());
    assertNull(tagES.getValueString());
    assertEquals("integer", tagES.getDataType());
  }

  /**
   * @return Tag of type boolean with value true.
   */
  private DataTagCacheObject createTagExample() {
    DataTagCacheObject tag = CacheObjectCreation.createTestDataTag();
    tag.setDataType("boolean");
    tag.setLogged(true);

    return tag;
  }
}