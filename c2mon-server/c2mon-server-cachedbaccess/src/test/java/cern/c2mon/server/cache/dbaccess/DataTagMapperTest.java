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
package cern.c2mon.server.cache.dbaccess;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.server.cache.dbaccess.structure.DBBatch;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.metadata.Metadata;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.util.TagQualityStatus;

import static org.junit.Assert.*;

public class DataTagMapperTest extends AbstractMapperTest {

  @Autowired
  private DataTagMapper dataTagMapper;

  @Test
  public void mapDataTagWithIntegerArray() {
    DataTagCacheObject cacheObject = createCacheObject(100004L, new Integer[] { 1, 2, 3, 4, 5 });
    dataTagMapper.insertDataTag(cacheObject);

    DataTagCacheObject retrievedObject = (DataTagCacheObject) dataTagMapper.getItem(cacheObject.getId());

    compare(cacheObject, retrievedObject);
  }

  @Test
  public void testDataTagWithArbitraryObject() throws Exception {
    ObjectMapper mapper = new ObjectMapper().enable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY);
      String valueString = mapper.writeValueAsString(
          ArbitraryObject.builder().fields(new Integer[] { 1, 2, 3, 4, 5 }).field1("Test").field2(1.3f).build());
      Object value = mapper.readValue(valueString, Object.class);

      DataTagCacheObject cacheObject = createCacheObject(100005L, value);
      dataTagMapper.insertDataTag(cacheObject);

      DataTagCacheObject retrievedObject = (DataTagCacheObject) dataTagMapper.getItem(cacheObject.getId());
      compare(cacheObject, retrievedObject);
  }

  @Test
  public void testAlarmCollectionCorrect() {
    DataTagCacheObject dataTagFromDB = (DataTagCacheObject) dataTagMapper.getItem(200000L);
    assertEquals(2, dataTagFromDB.getAlarmIds().size());
    assertTrue(dataTagFromDB.getAlarmIds().contains(350002L));
    assertTrue(dataTagFromDB.getAlarmIds().contains(350003L));
  }

  @Test
  public void testGetNumberItems() {
    assertTrue(dataTagMapper.getNumberItems() > 5);
  }

  @Test
  public void testGetRowBatch() {
    DBBatch dbBatch = new DBBatch(1L, 3L);
    List<DataTag> datatags = dataTagMapper.getRowBatch(dbBatch);
    assertNotNull(datatags);
    assertTrue(datatags.size() == 3);

    DBBatch dbBatch2 = new DBBatch(16L, 20L);
    List<DataTag> datatags2 = dataTagMapper.getRowBatch(dbBatch2);
    assertNotNull(datatags2);
    assertTrue(datatags2.size() == 1); // since there are only 16 entries in the db
  }

  @Test
  public void loadEmptyRowBatch() {
    DBBatch dbBatch = new DBBatch(100L, 200L);
    List<DataTag> dataTags = dataTagMapper.getRowBatch(dbBatch);
    assertNotNull(dataTags);
    assertTrue(dataTags.size() == 0);
  }

  @Test
  public void testGetAllDataTags() {
    dataTagMapper.getAll();
  }

  @Test
  public void testGetBooleanDataTag() {
    DataTagCacheObject cacheObject = createCacheObject(150000L, Boolean.TRUE);
    Metadata metadata = new Metadata();
    metadata.addMetadata("String", 11);
    cacheObject.setMetadata(metadata);

    // put in database
    dataTagMapper.insertDataTag(cacheObject);
    assertTrue(dataTagMapper.isInDb(cacheObject.getId()));

    // retrieve from database
    DataTagCacheObject retrievedObject = (DataTagCacheObject) dataTagMapper.getItem(150000L);

    compare(cacheObject, retrievedObject);
  }

  @Test
  public void testGetStringDataTag() {
    DataTagCacheObject cacheObject = createCacheObject(150007L, "RUNNING");
    cacheObject.setMinValue(null);
    cacheObject.setMaxValue(null);

    // put in database
    dataTagMapper.insertDataTag(cacheObject);
    assertTrue(dataTagMapper.isInDb(cacheObject.getId()));

    // retrieve from database
    DataTagCacheObject retrievedObject = (DataTagCacheObject) dataTagMapper.getItem(150007L);

    compare(cacheObject, retrievedObject);
  }

  @Test
  public void testGetDataTagAttachedToSubEquipment() {
    // construct fake DataTagCacheObject, setting all fields
    DataTagCacheObject cacheObject = createCacheObject(150001L, Boolean.TRUE);
    cacheObject.setSubEquipmentId(250L); // need test equipment inserted - use EquipmentMapperTest
    cacheObject.setEquipmentId(null);
    cacheObject.setProcessId(null);

    // put in database
    dataTagMapper.insertDataTag(cacheObject);
    assertTrue(dataTagMapper.isInDb(cacheObject.getId()));

    // retrieve from database
    DataTagCacheObject retrievedObject = (DataTagCacheObject) dataTagMapper.getItem(150001L);

    compare(cacheObject, retrievedObject);
  }

  @Test
  public void testUpdateDataTag() {
    // construct fake DataTagCacheObject
    DataTagCacheObject cacheObject = new DataTagCacheObject();
    cacheObject.setId(150000L); // must be non null in DB
    cacheObject.setName("Junit_test_tag"); // non null
    cacheObject.setMode(DataTagConstants.MODE_TEST); // non null
    cacheObject.setDataType("Boolean"); // non null
    cacheObject.setEquipmentId(150L); // need test equipment inserted
    Metadata metadata = new Metadata();
    metadata.addMetadata("metadata", 11);
    cacheObject.setMetadata(metadata);

    dataTagMapper.insertDataTag(cacheObject);

    cacheObject.setValue(Boolean.TRUE);
    cacheObject.setValueDescription("test value description");
    cacheObject.setSimulated(false); // null allowed
    cacheObject.setDataTagQuality(new DataTagQualityImpl(TagQualityStatus.UNDEFINED_VALUE, "undefined value"));
    cacheObject.setCacheTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setSourceTimestamp(new Timestamp(System.currentTimeMillis()));
    metadata = new Metadata();
    metadata.addMetadata("metadata_boolean", true);
    cacheObject.setMetadata(metadata);

    dataTagMapper.updateCacheable(cacheObject);

    DataTagCacheObject retrievedObject = (DataTagCacheObject) dataTagMapper.getItem(new Long(150000));

    // updated values are changed
    assertEquals(cacheObject.getValue(), retrievedObject.getValue());
    assertEquals(cacheObject.getValueDescription(), retrievedObject.getValueDescription());
    assertEquals(cacheObject.isSimulated(), retrievedObject.isSimulated());
    assertEquals(cacheObject.getDataTagQuality(), retrievedObject.getDataTagQuality());
    assertEquals(cacheObject.getTimestamp(), retrievedObject.getTimestamp());
    assertEquals(cacheObject.getSourceTimestamp(), retrievedObject.getSourceTimestamp());
    metadata = new Metadata();
    metadata.addMetadata("metadata", 11);
    assertEquals(metadata, retrievedObject.getMetadata());

    // other values should be the same or ...
    assertEquals(cacheObject.getId(), retrievedObject.getId());
    assertEquals(cacheObject.getName(), retrievedObject.getName());
    assertEquals(cacheObject.getMode(), retrievedObject.getMode());
    assertEquals(cacheObject.getDataType(), retrievedObject.getDataType());

    // ... null/default
    assertNull(retrievedObject.getDescription());
    assertEquals(false, retrievedObject.isLogged()); // default boolean
    assertNull(retrievedObject.getUnit());
    assertNull(retrievedObject.getDipAddress());
    assertNull(retrievedObject.getJapcAddress());
    assertNull(retrievedObject.getMinValue());
    assertNull(retrievedObject.getMaxValue());
    assertNull(retrievedObject.getAddress());

    dataTagMapper.deleteDataTag(cacheObject.getId());
  }

  @Test
  public void testIsInDB() {
    assertTrue(dataTagMapper.isInDb(200000L));
  }

  @Test
  public void testNotInDB() {
    assertFalse(dataTagMapper.isInDb(60010L));
  }

  private void compareLinkedHashMap(LinkedHashMap<String, Object> map1, LinkedHashMap<String, Object> map2) {
    assertEquals(map1.size(), map2.size());

    for (Map.Entry<String, Object> entry : map1.entrySet()) {
      String key1 = entry.getKey();
      assertTrue(map2.containsKey(key1));

      Object value1 = entry.getValue();
      Object value2 = map2.get(key1);

      if (value1 instanceof LinkedHashMap && value2 instanceof LinkedHashMap) {
        compareLinkedHashMap((LinkedHashMap) value1, (LinkedHashMap) value2);
      } else if (value1 instanceof Object[] && value2 instanceof Object[]) {
        assertTrue(Arrays.equals((Object[]) value1, (Object[]) value2));
      } else {
        assertEquals(value1, value2);
      }
    }
  }

  @Data
  @Builder
  static class ArbitraryObject {
    private Integer[] fields;
    private String field1;
    private Float field2;
  }

  @SuppressWarnings("unchecked")
  private void compare(DataTagCacheObject  cacheObject, DataTagCacheObject retrievedObject) {
    assertEquals(cacheObject.getId(), retrievedObject.getId());
    assertEquals(cacheObject.getName(), retrievedObject.getName());
    assertEquals(cacheObject.getDescription(), retrievedObject.getDescription());
    assertEquals(cacheObject.getMode(), retrievedObject.getMode());
    assertEquals(cacheObject.getDataType(), retrievedObject.getDataType());
    assertEquals(cacheObject.isLogged(), retrievedObject.isLogged());
    assertEquals(cacheObject.getUnit(), retrievedObject.getUnit());
    assertEquals(cacheObject.getDipAddress(), retrievedObject.getDipAddress());
    assertEquals(cacheObject.getJapcAddress(), retrievedObject.getJapcAddress());
    if (cacheObject.getValue() instanceof LinkedHashMap && retrievedObject.getValue() instanceof LinkedHashMap) {
      compareLinkedHashMap((LinkedHashMap<String, Object>) cacheObject.getValue(), (LinkedHashMap<String, Object>) retrievedObject.getValue());
    }
    else if (cacheObject.getDataType().startsWith("[Ljava.")) {
      assertTrue(Arrays.equals((Object[]) cacheObject.getValue(), (Object[]) retrievedObject.getValue()));
    }
    else {
      assertEquals(cacheObject.getValue(), retrievedObject.getValue());
    }
    assertEquals(cacheObject.getValueDescription(), retrievedObject.getValueDescription());
    assertEquals(cacheObject.isSimulated(), retrievedObject.isSimulated());
    assertEquals(cacheObject.getEquipmentId(), retrievedObject.getEquipmentId());
    assertEquals(cacheObject.getProcessId(), retrievedObject.getProcessId());
    assertEquals(cacheObject.getSubEquipmentId(), retrievedObject.getSubEquipmentId());
    if (cacheObject.getValue() instanceof Number) {
      assertEquals(cacheObject.getMinValue(), retrievedObject.getMinValue());
      assertEquals(cacheObject.getMaxValue(), retrievedObject.getMaxValue());
    }
    else {
      assertEquals(null, retrievedObject.getMinValue()); // min value is not persisted for non numeric data types
      assertEquals(null, retrievedObject.getMaxValue()); // max value is not persisted for non numeric data types
    }
    assertEquals(cacheObject.getAddress(), retrievedObject.getAddress());
    assertEquals(cacheObject.getDataTagQuality(), retrievedObject.getDataTagQuality());
    assertEquals(cacheObject.getTimestamp(), retrievedObject.getTimestamp());
    assertEquals(cacheObject.getSourceTimestamp(), retrievedObject.getSourceTimestamp());
    assertEquals(cacheObject.getRuleIdsString(), retrievedObject.getRuleIdsString());
    assertEquals(cacheObject.getMetadata(), retrievedObject.getMetadata());
  }

  private DataTagCacheObject createCacheObject(long id, Object value) {
    DataTagCacheObject cacheObject = new DataTagCacheObject();
    cacheObject.setId(Long.valueOf(id)); // must be non null in DB
    cacheObject.setName("Junit_test_datatag5"); // non null
    cacheObject.setDescription("test description");
    cacheObject.setMode(DataTagConstants.MODE_TEST); // non null
    cacheObject.setDataType(value.getClass().getName()); // non null
    cacheObject.setLogged(false); // null allowed
    cacheObject.setUnit("test unit");
    cacheObject.setDipAddress("testDIPaddress");
    cacheObject.setJapcAddress("testJAPCaddress");
    cacheObject.setValue(value);
    cacheObject.setValueDescription("test value description");
    cacheObject.setMinValue(Float.valueOf(23.3f));
    cacheObject.setMaxValue(Float.valueOf(12.2f));
    cacheObject.setRuleIdsString("1234");
    cacheObject.setSimulated(false); // null allowed
    cacheObject.setEquipmentId(new Long(150)); // need test equipment inserted
    cacheObject.setAddress(new DataTagAddress());
    cacheObject.setDataTagQuality(new DataTagQualityImpl(TagQualityStatus.EQUIPMENT_DOWN));
    cacheObject.setCacheTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setDaqTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setSourceTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setProcessId(50L);

    return cacheObject;
  }
}
