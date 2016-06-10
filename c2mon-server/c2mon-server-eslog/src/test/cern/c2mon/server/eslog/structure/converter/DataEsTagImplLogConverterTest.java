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

import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.eslog.structure.mappings.EsMapping.ValueType;
import cern.c2mon.server.eslog.structure.types.tag.AbstractEsTag;
import cern.c2mon.server.eslog.structure.types.tag.EsTagBoolean;
import cern.c2mon.server.eslog.structure.types.tag.EsTagNumeric;
import cern.c2mon.server.eslog.structure.types.tag.EsValueType;
import cern.c2mon.server.test.CacheObjectCreation;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import cern.c2mon.shared.common.metadata.Metadata;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Checks on the fields of data appened/set to AbstractEsTag.
 *
 * @author Alban Marguet.
 */
@RunWith(MockitoJUnitRunner.class)
public class DataEsTagImplLogConverterTest {
  @InjectMocks
  private EsTagLogConverter esLogConverter;
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
  @Mock
  private AbstractEsTag esTagImpl;

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

    Map<String, String> result = esLogConverter.retrieveTagProcessMetadata(tag);

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

    Map<String, String> result = esLogConverter.retrieveTagProcessMetadata(tag);

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

    Map<String, String> result = esLogConverter.retrieveTagProcessMetadata(tag);
    assertEquals(expected, result);
  }

  @Test
  public void testGetMetadataProcessWithNull() {
    HashMap<String, String> expected = new HashMap<>();

    Map<String, String> result = esLogConverter.retrieveTagProcessMetadata(tag);
    assertEquals(expected, result);
  }

  @Test
  public void testGetTagMetadataWithNothing() {
    HashMap<String, String> expected = new HashMap<>();
    DataTagCacheObject tag = CacheObjectCreation.createTestDataTag();
    tag.setEquipmentId(null);
    tag.setSubEquipmentId(null);
    tag.setProcessId(null);
    assertEquals(expected, esLogConverter.retrieveTagProcessMetadata(tag));
  }

  @Test
  public void testConvertToTagES() throws IOException {
    long id = 1L;
    String name = "tag";
    ValueType type = ValueType.BOOLEAN;
    long timeStamp = 123456L;
    boolean value = true;
    String valueDesc = "ok";
    when(tagC2MON.getId()).thenReturn(id);
    when(tagC2MON.getName()).thenReturn(name);
    when(tagC2MON.getDataType()).thenReturn(type.toString());
    when(tagC2MON.getCacheTimestamp()).thenReturn(new Timestamp(timeStamp));
    when(tagC2MON.getDataTagQuality()).thenReturn(null);
    when(tagC2MON.getValue()).thenReturn(value);
    when(tagC2MON.getTimestamp()).thenReturn(new Timestamp(1234567));
    when(tagC2MON.getDataTagQuality()).thenReturn(new DataTagQualityImpl());
    when(tagC2MON.getValueDescription()).thenReturn(valueDesc);
    when(tagC2MON.getUnit()).thenReturn("km");

    AbstractEsTag esTag = esLogConverter.convert(tagC2MON);
    assertEquals(id, esTag.getIdAsLong());
    assertEquals(name, esTag.getName());
    assertNotNull(esTag.getC2mon());
    assertEquals(type.toString(), esTag.getC2mon().getDataType());
    assertEquals(timeStamp, esTag.getC2mon().getServerTimestamp());
    assertEquals(1, esTag.getQuality().getStatus());
    assertEquals(value, esTag.getRawValue());
    assertEquals(valueDesc, esTag.getValueDescription());
    assertNotNull(esTag.getQuality());
    assertFalse(esTag.getQuality().isValid());

    when(tagC2MON.getDataType()).thenReturn(ValueType.STRING.toString());
    when(tagC2MON.getValue()).thenReturn(name);
    esTag = esLogConverter.convert(tagC2MON);
    assertEquals(name, esTag.getRawValue());

    when(tagC2MON.getDataType()).thenReturn(ValueType.LONG.toString());
    when(tagC2MON.getValue()).thenReturn(timeStamp);
    esTag = esLogConverter.convert(tagC2MON);
    assertEquals(timeStamp, esTag.getRawValue());

    when(tagC2MON.getDataTagQuality()).thenReturn(new DataTagQualityImpl(TagQualityStatus.EQUIPMENT_DOWN));
    esTag = esLogConverter.convert(tagC2MON);
    assertNotNull(esTag.getQuality());
    assertFalse(esTag.getQuality().isValid());
    assertEquals("km", esTag.getUnit());
  }

  @Test
  public void testBadValues() {
    DataTagCacheObject tag = CacheObjectCreation.createTestDataTag();
    tag.setDaqTimestamp(null);
    tag.setSourceTimestamp(null);

    AbstractEsTag esTag = esLogConverter.convert(tag);
    assertTrue(esTag instanceof EsTagBoolean);
    assertNotNull(esTag.getTimestamp());
    assertNotNull(esTag.getC2mon());
    assertNotNull(esTag.getC2mon().getDaqTimestamp());
  }

  @Test
  public void ModuleConvertsTagsAsExpected() {
    Tag tag = createTagExample();

    AbstractEsTag esTag = esLogConverter.convert(tag);

    assertTrue(esTag instanceof EsTagBoolean);
    assertTrue(esTag.getValueBoolean());
    assertEquals(1, esTag.getValue());
    assertNull(esTag.getValueString());
    assertTrue((Boolean) esTag.getRawValue());
    assertNotNull(esTag.getC2mon());
    assertEquals("boolean", esTag.getC2mon().getDataType());
    assertEquals(EsValueType.OBJECT.getFriendlyName(), esTag.getType());

    DataTagCacheObject tagNumeric = new DataTagCacheObject();
    tagNumeric.setId(1L);
    tagNumeric.setDataType("integer");
    tagNumeric.setValue(126387213);

    esTag = esLogConverter.convert(tagNumeric);

    assertTrue(esTag instanceof EsTagNumeric);
    assertNull(esTag.getValueBoolean());
    assertEquals(126387213, esTag.getValue());
    assertEquals(126387213, esTag.getRawValue());
    assertNull(esTag.getValueString());
    assertNotNull(esTag.getC2mon());
    assertEquals("integer", esTag.getC2mon().getDataType());
    assertEquals(EsValueType.OBJECT.getFriendlyName(), esTag.getType());
  }

  @Test
  public void testMetadata() {
    Map<String, Object> map = new HashMap<>();
    map.put("building", "1");
    map.put("responsible", "coucou");
    map.put("intShouldGoToStringMetadata", 2);
    Metadata metadata = new Metadata(map);
    DataTagCacheObject tag = createTagExample();

    tag.setMetadata(metadata);

    esTagImpl = esLogConverter.convert(tag);
    assertNotNull(esTagImpl.getMetadata());
    assertEquals("1", esTagImpl.getMetadata().get("building"));
    assertEquals("2", esTagImpl.getMetadata().get("intShouldGoToStringMetadata"));
    assertEquals("coucou", esTagImpl.getMetadata().get("responsible"));
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