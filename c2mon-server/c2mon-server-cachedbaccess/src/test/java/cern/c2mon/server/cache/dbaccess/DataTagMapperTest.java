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

import cern.c2mon.server.cache.dbaccess.structure.DBBatch;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.shared.common.datatag.*;
import cern.c2mon.shared.common.metadata.Metadata;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class DataTagMapperTest extends AbstractMapperTest {

  @Autowired
  private DataTagMapper dataTagMapper;

  @Test
  public void mapDataTagWithIntegerArray() {
    DataTagCacheObject cacheObject = new DataTagCacheObject();
    cacheObject.setId(new Long(100004));  //must be non null in DB
    cacheObject.setName("Junit_test_datatag4"); //non null
    cacheObject.setDescription("test description");
    cacheObject.setMode(DataTagConstants.MODE_TEST); //non null
    cacheObject.setDataType(Integer[].class.getName()); // non null
    cacheObject.setLogged(false); //null allowed
    cacheObject.setUnit("test unit");
    cacheObject.setDipAddress("testDIPaddress");
    cacheObject.setJapcAddress("testJAPCaddress");
    cacheObject.setValue(new Integer[]{1,2,3,4,5});
    cacheObject.setValueDescription("test value description");
    cacheObject.setSimulated(false); //null allowed
    cacheObject.setEquipmentId(new Long(150)); //need test equipment inserted
    cacheObject.setAddress(new DataTagAddress());
    cacheObject.setDataTagQuality(new DataTagQualityImpl(TagQualityStatus.EQUIPMENT_DOWN));
    cacheObject.setCacheTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setDaqTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setSourceTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setProcessId(50L);
    dataTagMapper.insertDataTag(cacheObject);

    DataTagCacheObject retrievedObject = (DataTagCacheObject) dataTagMapper.getItem(cacheObject.getId());

    assertEquals(cacheObject.getId(), retrievedObject.getId());
    assertEquals(cacheObject.getName(), retrievedObject.getName());
    assertEquals(cacheObject.getDescription(), retrievedObject.getDescription());
    assertEquals(cacheObject.getMode(), retrievedObject.getMode());
    assertEquals(cacheObject.getDataType(), retrievedObject.getDataType());
    assertEquals(cacheObject.isLogged(), retrievedObject.isLogged());
    assertEquals(cacheObject.getUnit(), retrievedObject.getUnit());
    assertEquals(cacheObject.getDipAddress(), retrievedObject.getDipAddress());
    assertEquals(cacheObject.getJapcAddress(), retrievedObject.getJapcAddress());
    assertTrue(Arrays.equals((Object[])cacheObject.getValue(), (Object[])retrievedObject.getValue()));
    assertEquals(cacheObject.getValueDescription(), retrievedObject.getValueDescription());
    assertEquals(cacheObject.isSimulated(), retrievedObject.isSimulated());
    assertEquals(cacheObject.getEquipmentId(), retrievedObject.getEquipmentId());
    assertEquals(cacheObject.getProcessId(), retrievedObject.getProcessId());
    assertEquals(null, retrievedObject.getMinValue()); // min value is not persisted for boolean data types
    assertEquals(null, retrievedObject.getMaxValue()); // max value is not persisted for boolean data types
    assertEquals(cacheObject.getAddress(), retrievedObject.getAddress());
    assertEquals(cacheObject.getDataTagQuality(), retrievedObject.getDataTagQuality());
    assertEquals(cacheObject.getTimestamp(), retrievedObject.getTimestamp());
    assertEquals(cacheObject.getSourceTimestamp(), retrievedObject.getSourceTimestamp());
  }

  @Test
  public void testDataTagWithArbitraryObject() {
    ObjectMapper mapper = new ObjectMapper().enable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY);
    try {
      String valueString = mapper.writeValueAsString(ArbitraryObject.builder().fields(new Integer[]{1,2,3,4,5}).field1("Test").field2(1.3f).build());
      Object value = mapper.readValue(valueString, Object.class);

      DataTagCacheObject cacheObject = new DataTagCacheObject();
      cacheObject.setId(new Long(100005));  //must be non null in DB
      cacheObject.setName("Junit_test_datatag5"); //non null
      cacheObject.setDescription("test description");
      cacheObject.setMode(DataTagConstants.MODE_TEST); //non null
      cacheObject.setDataType(ArbitraryObject[].class.getName()); // non null
      //cacheObject.setTopic("tim.testdatatag.XADDRESS");
      cacheObject.setLogged(false); //null allowed
      cacheObject.setUnit("test unit");
      cacheObject.setDipAddress("testDIPaddress");
      cacheObject.setJapcAddress("testJAPCaddress");
      cacheObject.setValue(value);
      cacheObject.setValueDescription("test value description");
      cacheObject.setSimulated(false); //null allowed
      cacheObject.setEquipmentId(new Long(150)); //need test equipment inserted
      cacheObject.setAddress(new DataTagAddress());
      cacheObject.setDataTagQuality(new DataTagQualityImpl(TagQualityStatus.EQUIPMENT_DOWN));
      cacheObject.setCacheTimestamp(new Timestamp(System.currentTimeMillis()));
      cacheObject.setDaqTimestamp(new Timestamp(System.currentTimeMillis()));
      cacheObject.setSourceTimestamp(new Timestamp(System.currentTimeMillis()));
      cacheObject.setProcessId(50L);
      dataTagMapper.insertDataTag(cacheObject);

      DataTagCacheObject retrievedObject = (DataTagCacheObject) dataTagMapper.getItem(cacheObject.getId());

      assertEquals(cacheObject.getId(), retrievedObject.getId());
      assertEquals(cacheObject.getName(), retrievedObject.getName());
      assertEquals(cacheObject.getDescription(), retrievedObject.getDescription());
      assertEquals(cacheObject.getMode(), retrievedObject.getMode());
      assertEquals(cacheObject.getDataType(), retrievedObject.getDataType());
      assertEquals(cacheObject.isLogged(), retrievedObject.isLogged());
      assertEquals(cacheObject.getUnit(), retrievedObject.getUnit());
      assertEquals(cacheObject.getDipAddress(), retrievedObject.getDipAddress());
      assertEquals(cacheObject.getJapcAddress(), retrievedObject.getJapcAddress());
      assertEquals(cacheObject.getValueDescription(), retrievedObject.getValueDescription());
      assertEquals(cacheObject.isSimulated(), retrievedObject.isSimulated());
      assertEquals(cacheObject.getEquipmentId(), retrievedObject.getEquipmentId());
      assertEquals(cacheObject.getProcessId(), retrievedObject.getProcessId());
      assertEquals(null, retrievedObject.getMinValue()); // min value is not persisted for boolean data types
      assertEquals(null, retrievedObject.getMaxValue()); // max value is not persisted for boolean data types
      assertEquals(cacheObject.getAddress(), retrievedObject.getAddress());
      assertEquals(cacheObject.getDataTagQuality(), retrievedObject.getDataTagQuality());
      assertEquals(cacheObject.getTimestamp(), retrievedObject.getTimestamp());
      assertEquals(cacheObject.getSourceTimestamp(), retrievedObject.getSourceTimestamp());

      if(cacheObject.getValue() instanceof LinkedHashMap && retrievedObject.getValue() instanceof LinkedHashMap){
        compareLinkedHashMap((LinkedHashMap)cacheObject.getValue(), (LinkedHashMap)retrievedObject.getValue());
      }

    } catch (JsonProcessingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
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
    assertTrue(datatags2.size() == 1); //since there are only 16 entries in the db
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
  public void testGetDataTag() {
    //construct fake DataTagCacheObject, setting all fields
    DataTagCacheObject cacheObject = new DataTagCacheObject();
    cacheObject.setId(new Long(150000));  //must be non null in DB
    cacheObject.setName("Junit_test_tag"); //non null
    cacheObject.setDescription("test description");
    cacheObject.setMode(DataTagConstants.MODE_TEST); //non null
    cacheObject.setDataType("Boolean"); // non null
    cacheObject.setLogged(false); //null allowed
    cacheObject.setUnit("test unit m/sec");
    cacheObject.setDipAddress("testDIPaddress");
    cacheObject.setJapcAddress("testJAPCaddress");
    cacheObject.setValue(Boolean.TRUE);
    cacheObject.setValueDescription("test value description");
    cacheObject.setSimulated(false); //null allowed
    cacheObject.setEquipmentId(new Long(150)); //need test equipment inserted - use EquipmentMapperTest
    cacheObject.setMinValue(new Float(23.3));
    cacheObject.setMaxValue(new Float(12.2));
    cacheObject.setAddress(new DataTagAddress());
    cacheObject.setDataTagQuality(new DataTagQualityImpl(TagQualityStatus.EQUIPMENT_DOWN));
    cacheObject.setCacheTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setSourceTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setRuleIdsString("1234");
    cacheObject.setMetadata(Metadata.builder().setNewMetadata("String",11).build());
    cacheObject.setProcessId(50L); //need test process also (P_JAPC01)

    //put in database
    dataTagMapper.insertDataTag(cacheObject);
    assertTrue(dataTagMapper.isInDb(cacheObject.getId()));

    //retrieve from database
    DataTagCacheObject retrievedObject = (DataTagCacheObject) dataTagMapper.getItem(new Long(150000));

    assertEquals(cacheObject.getId(), retrievedObject.getId());
    assertEquals(cacheObject.getName(), retrievedObject.getName());
    assertEquals(cacheObject.getDescription(), retrievedObject.getDescription());
    assertEquals(cacheObject.getMode(), retrievedObject.getMode());
    assertEquals(cacheObject.getDataType(), retrievedObject.getDataType());
    assertEquals(cacheObject.isLogged(), retrievedObject.isLogged());
    assertEquals(cacheObject.getUnit(), retrievedObject.getUnit());
    assertEquals(cacheObject.getDipAddress(), retrievedObject.getDipAddress());
    assertEquals(cacheObject.getJapcAddress(), retrievedObject.getJapcAddress());
    assertEquals(cacheObject.getValue(), retrievedObject.getValue());
    assertEquals(cacheObject.getValueDescription(), retrievedObject.getValueDescription());
    assertEquals(cacheObject.isSimulated(), retrievedObject.isSimulated());
    assertEquals(cacheObject.getEquipmentId(), retrievedObject.getEquipmentId());
    assertEquals(cacheObject.getProcessId(), retrievedObject.getProcessId());
    assertEquals(null, retrievedObject.getMinValue()); // min value is not persisted for boolean data types
    assertEquals(null, retrievedObject.getMaxValue()); // max value is not persisted for boolean data types
    assertEquals(cacheObject.getAddress(), retrievedObject.getAddress());
    assertEquals(cacheObject.getDataTagQuality(), retrievedObject.getDataTagQuality());
    assertEquals(cacheObject.getTimestamp(), retrievedObject.getTimestamp());
    assertEquals(cacheObject.getSourceTimestamp(), retrievedObject.getSourceTimestamp());
    assertEquals(cacheObject.getRuleIdsString(), retrievedObject.getRuleIdsString());
    assertEquals(cacheObject.getMetadata(), retrievedObject.getMetadata());

//    dataTagMapper.deleteDataTag(cacheObject.getId());
//    assertFalse(dataTagMapper.isInDb(cacheObject.getId()));
//
//    // attach to a subequipment
//    cacheObject.setId(150001L);
//    cacheObject.setEquipmentId(null);
//    cacheObject.setSubEquipmentId(250L);
//
//    //put in database
//    dataTagMapper.insertDataTag(cacheObject);
//    assertTrue(dataTagMapper.isInDb(cacheObject.getId()));
//
//    //retrieve from database
//    retrievedObject = (DataTagCacheObject) dataTagMapper.getItem(new Long(150001));
//    assertNotNull(retrievedObject);
//    assertEquals(null, retrievedObject.getEquipmentId());
//    assertEquals(cacheObject.getSubEquipmentId(), retrievedObject.getSubEquipmentId());
  }

  @Test
  public void testGetDataTagAttachedToSubEquipment() {
    //construct fake DataTagCacheObject, setting all fields
    DataTagCacheObject cacheObject = new DataTagCacheObject();
    cacheObject.setId(new Long(150001));  //must be non null in DB
    cacheObject.setName("Junit_test_tag"); //non null
    cacheObject.setDescription("test description");
    cacheObject.setMode(DataTagConstants.MODE_TEST); //non null
    cacheObject.setDataType("Boolean"); // non null
    cacheObject.setLogged(false); //null allowed
    cacheObject.setUnit("test unit m/sec");
    cacheObject.setDipAddress("testDIPaddress");
    cacheObject.setJapcAddress("testJAPCaddress");
    cacheObject.setValue(Boolean.TRUE);
    cacheObject.setValueDescription("test value description");
    cacheObject.setSimulated(false); //null allowed
    cacheObject.setSubEquipmentId(new Long(250)); //need test equipment inserted - use EquipmentMapperTest
    cacheObject.setMinValue(new Float(23.3));
    cacheObject.setMaxValue(new Float(12.2));
    cacheObject.setAddress(new DataTagAddress());
    cacheObject.setDataTagQuality(new DataTagQualityImpl(TagQualityStatus.EQUIPMENT_DOWN));
    cacheObject.setCacheTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setSourceTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setRuleIdsString("1234");
//    cacheObject.setProcessId(50L); //need test process also (P_JAPC01)

    //put in database
    dataTagMapper.insertDataTag(cacheObject);
    assertTrue(dataTagMapper.isInDb(cacheObject.getId()));

    //retrieve from database
    DataTagCacheObject retrievedObject =
        (DataTagCacheObject) dataTagMapper.getItem
        (new Long(150001));

    assertEquals(cacheObject.getId(), retrievedObject.getId());
    assertEquals(cacheObject.getName(), retrievedObject.getName());
    assertEquals(cacheObject.getDescription(), retrievedObject.getDescription());
    assertEquals(cacheObject.getMode(), retrievedObject.getMode());
    assertEquals(cacheObject.getDataType(), retrievedObject.getDataType());
    assertEquals(cacheObject.isLogged(), retrievedObject.isLogged());
    assertEquals(cacheObject.getUnit(), retrievedObject.getUnit());
    assertEquals(cacheObject.getDipAddress(), retrievedObject.getDipAddress());
    assertEquals(cacheObject.getJapcAddress(), retrievedObject.getJapcAddress());
    assertEquals(cacheObject.getValue(), retrievedObject.getValue());
    assertEquals(cacheObject.getValueDescription(), retrievedObject.getValueDescription());
    assertEquals(cacheObject.isSimulated(), retrievedObject.isSimulated());
    assertEquals(cacheObject.getEquipmentId(), retrievedObject.getEquipmentId());
    assertEquals(cacheObject.getSubEquipmentId(), retrievedObject.getSubEquipmentId());
    assertEquals(cacheObject.getProcessId(), retrievedObject.getProcessId());
    assertEquals(null, retrievedObject.getMinValue()); // min value is not persisted for boolean data types
    assertEquals(null, retrievedObject.getMaxValue()); // max value is not persisted for boolean data types
    assertEquals(cacheObject.getAddress(), retrievedObject.getAddress());
    assertEquals(cacheObject.getDataTagQuality(), retrievedObject.getDataTagQuality());
    assertEquals(cacheObject.getTimestamp(), retrievedObject.getTimestamp());
    assertEquals(cacheObject.getSourceTimestamp(), retrievedObject.getSourceTimestamp());
    assertEquals(cacheObject.getRuleIdsString(), retrievedObject.getRuleIdsString());
  }

  @Test
  public void testUpdateDataTag() {
    //construct fake DataTagCacheObject
    DataTagCacheObject cacheObject = new DataTagCacheObject();
    cacheObject.setId(new Long(150000));  //must be non null in DB
    cacheObject.setName("Junit_test_tag"); //non null
    cacheObject.setMode(DataTagConstants.MODE_TEST); //non null
    cacheObject.setDataType("Boolean"); // non null
    cacheObject.setEquipmentId(new Long(150)); //need test equipment inserted
    cacheObject.setMetadata(Metadata.builder().setNewMetadata("metadata",11).build());

    dataTagMapper.insertDataTag(cacheObject);

    cacheObject.setValue(Boolean.TRUE);
    cacheObject.setValueDescription("test value description");
    cacheObject.setSimulated(false); //null allowed
    cacheObject.setDataTagQuality(new DataTagQualityImpl(TagQualityStatus.UNDEFINED_VALUE,"undefined value"));
    cacheObject.setCacheTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setSourceTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setMetadata(Metadata.builder().setNewMetadata("metadata_boolean",true).build());

    dataTagMapper.updateCacheable(cacheObject);

    DataTagCacheObject retrievedObject = (DataTagCacheObject) dataTagMapper.getItem(new Long(150000));

    //updated values are changed
    assertEquals(cacheObject.getValue(), retrievedObject.getValue());
    assertEquals(cacheObject.getValueDescription(), retrievedObject.getValueDescription());
    assertEquals(cacheObject.isSimulated(), retrievedObject.isSimulated());
    assertEquals(cacheObject.getDataTagQuality(), retrievedObject.getDataTagQuality());
    assertEquals(cacheObject.getTimestamp(), retrievedObject.getTimestamp());
    assertEquals(cacheObject.getSourceTimestamp(), retrievedObject.getSourceTimestamp());
    assertEquals(Metadata.builder().setNewMetadata("metadata",11).build(), retrievedObject.getMetadata());

    //other values should be the same or ...
    assertEquals(cacheObject.getId(), retrievedObject.getId());
    assertEquals(cacheObject.getName(), retrievedObject.getName());
    assertEquals(cacheObject.getMode(), retrievedObject.getMode());
    assertEquals(cacheObject.getDataType(), retrievedObject.getDataType());

    //... null/default
    assertNull(retrievedObject.getDescription());
    assertEquals(false, retrievedObject.isLogged()); //default boolean
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


  private void compareLinkedHashMap(LinkedHashMap<String, Object> map1, LinkedHashMap<String, Object> map2){
    assertEquals(map1.size(), map2.size());

    for(Map.Entry<String, Object> entry : map1.entrySet()){
      String key1 = entry.getKey();
      assertTrue(map2.containsKey(key1));

      Object value1 = entry.getValue();
      Object value2 = map2.get(key1);

      if (value1 instanceof LinkedHashMap &&  value2 instanceof LinkedHashMap){
        compareLinkedHashMap((LinkedHashMap) value1, (LinkedHashMap) value2);
      } else if(value1 instanceof Object[] &&  value2 instanceof Object[]){

        assertTrue(Arrays.equals((Object[])value1, (Object[])value2));

      } else {

        assertEquals(value1, value2);
      }
    }
  }

  @Data
  @Builder
  static class ArbitraryObject{
    private Integer[] fields;

    private String field1;

    private Float field2;
  }

}

/**
 * Make sure the test DataTag is deleted after each method.
 */
//  @After
//  public void deleteTestDataTag() {
//    dataTagMapper.deleteDataTag(dataTag.getId());
//  }

