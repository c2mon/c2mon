/**
 * Copyright (C) 2010-2020 CERN. All rights not expressly granted are reserved.
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
 */
package cern.c2mon.shared.daq.datatag;

import cern.c2mon.shared.common.datatag.DataTagValueUpdate;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by fritter on 02/03/16.
 */
public class DataTagValueUpdateConverterTest {

  static private ObjectMapper mapper;

  static SourceDataTagValue valueObject;
  static DataTagValueUpdate value;
  static DataTagValueUpdate deserializeValue;


  @BeforeClass
  /**
   * Use the same setup than in {@link DataTagValueUpdateConverter}.
   *
   * Note: If the setup is changed there it should also be chaged here!
   */
  public static void ini() {

    mapper = new ObjectMapper();
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    mapper.enable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY);

  }

  @Before
  public void beforeTest() {
    valueObject = SourceDataTagValue.builder()
        .id(11L)
        .name("name")
        .controlTag(true)
        .value(null)
        .timestamp(new Timestamp(System.currentTimeMillis()))
        .daqTimestamp(new Timestamp(System.currentTimeMillis()))
        .priority(1)
        .valueDescription("description")
        .guaranteedDelivery(true)
        .timeToLive(666)
        .build();
    value = new DataTagValueUpdate(11L);
    value.addValue(valueObject);
  }

  @Test
  public void serializeAndDeserializeArrays() {

    //data setup:
    Integer[] intArray = new Integer[]{1, 2, 3, 4, 5};
    String[] stringArray = new String[]{"1", "2", "3", "4", "5"};
    Double[] doubleArray = new Double[]{1.1, 2.2, 3.3, 4.4, 5.5};
    Boolean[] booleanArray = new Boolean[]{true, false, true, false, true};
    Float[] floatArray = new Float[]{1f, 2f, 3f, 4f, 5f};
    Long[] longArray = new Long[]{100L, 200L, 300L, 400L, 500L};
    Short[] shortArray = new Short[]{10, 11, 12, 13, 14};

    String serializedValue;
    Object[] deserializedArray;

    try {

      //==============================================================================
      // test Integer-Array:
      //==============================================================================
      valueObject.setValue(intArray);
      serializedValue = mapper.writeValueAsString(value);
      deserializeValue = mapper.readValue(serializedValue, DataTagValueUpdate.class);

      deserializedArray = getToObjectArray(deserializeValue);
      assertTrue(Arrays.equals(deserializedArray, intArray));

      //==============================================================================
      // test String-Array:
      //==============================================================================
      valueObject.setValue(stringArray);
      serializedValue = mapper.writeValueAsString(value);
      deserializeValue = mapper.readValue(serializedValue, DataTagValueUpdate.class);

      deserializedArray = getToObjectArray(deserializeValue);
      assertTrue(Arrays.equals(deserializedArray, stringArray));

      //==============================================================================
      // test Double-Array:
      //==============================================================================
      valueObject.setValue(doubleArray);
      serializedValue = mapper.writeValueAsString(value);
      deserializeValue = mapper.readValue(serializedValue, DataTagValueUpdate.class);

      deserializedArray = getToObjectArray(deserializeValue);
      assertTrue(Arrays.equals(deserializedArray, doubleArray));

      //==============================================================================
      // test Boolean-Array:
      //==============================================================================
      valueObject.setValue(booleanArray);
      serializedValue = mapper.writeValueAsString(value);
      deserializeValue = mapper.readValue(serializedValue, DataTagValueUpdate.class);

      deserializedArray = getToObjectArray(deserializeValue);
      assertTrue(Arrays.equals(deserializedArray, booleanArray));

      //==============================================================================
      // test Float-Array:
      //==============================================================================
      valueObject.setValue(floatArray);
      serializedValue = mapper.writeValueAsString(value);
      deserializeValue = mapper.readValue(serializedValue, DataTagValueUpdate.class);

      deserializedArray = getToObjectArray(deserializeValue);
      assertTrue(Arrays.equals(deserializedArray, floatArray));

      //==============================================================================
      // test Long-Array:
      //==============================================================================
      valueObject.setValue(longArray);
      serializedValue = mapper.writeValueAsString(value);
      deserializeValue = mapper.readValue(serializedValue, DataTagValueUpdate.class);

      deserializedArray = getToObjectArray(deserializeValue);
      assertTrue(Arrays.equals(deserializedArray, longArray));

      //==============================================================================
      // test Short-Array:
      //==============================================================================
      valueObject.setValue(shortArray);
      serializedValue = mapper.writeValueAsString(value);
      deserializeValue = mapper.readValue(serializedValue, DataTagValueUpdate.class);

      deserializedArray = getToObjectArray(deserializeValue);
      assertTrue(Arrays.equals(deserializedArray, shortArray));

    } catch (JsonProcessingException e) {
      e.printStackTrace();
      assertTrue(false);
    } catch (IOException e) {
      e.printStackTrace();
      assertTrue(false);
    }

  }


  @Test
  public void serializeAndDeserializePrimitiveObjects() {

    //data setup:
    Integer intObject = new Integer(1);
    String stringObject = "testText";
    Double doubleObject = new Double(1.1);
    Boolean booleanObject = Boolean.FALSE;
    Float floatObject = new Float(1.111f);
    Long longObject = new Long(100L);
    Short shortObject = 10;

    String serializedValue;
    Object deserializedObject;

    try {

      //==============================================================================
      // test Integer:
      //==============================================================================
      valueObject.setValue(intObject);
      serializedValue = mapper.writeValueAsString(value);
      deserializeValue = mapper.readValue(serializedValue, DataTagValueUpdate.class);

      deserializedObject = getToObject(deserializeValue);
      assertEquals(deserializedObject, intObject);

      //==============================================================================
      // test String:
      //==============================================================================
      valueObject.setValue(stringObject);
      serializedValue = mapper.writeValueAsString(value);
      deserializeValue = mapper.readValue(serializedValue, DataTagValueUpdate.class);

      deserializedObject = getToObject(deserializeValue);
      assertEquals(deserializedObject, stringObject);

      //==============================================================================
      // test Double:
      //==============================================================================
      valueObject.setValue(doubleObject);
      serializedValue = mapper.writeValueAsString(value);
      deserializeValue = mapper.readValue(serializedValue, DataTagValueUpdate.class);

      deserializedObject = getToObject(deserializeValue);
      assertEquals(deserializedObject, doubleObject);

      //==============================================================================
      // test Boolean:
      //==============================================================================
      valueObject.setValue(booleanObject);
      serializedValue = mapper.writeValueAsString(value);
      deserializeValue = mapper.readValue(serializedValue, DataTagValueUpdate.class);

      deserializedObject = getToObject(deserializeValue);
      assertEquals(deserializedObject, booleanObject);

      //==============================================================================
      // test Float:
      //==============================================================================
      valueObject.setValue(floatObject);
      serializedValue = mapper.writeValueAsString(value);
      deserializeValue = mapper.readValue(serializedValue, DataTagValueUpdate.class);

      deserializedObject = getToObject(deserializeValue);
      assertEquals(deserializedObject, floatObject);

      //==============================================================================
      // test Long-Array:
      //==============================================================================
      valueObject.setValue(longObject);
      serializedValue = mapper.writeValueAsString(value);
      deserializeValue = mapper.readValue(serializedValue, DataTagValueUpdate.class);

      deserializedObject = getToObject(deserializeValue);
      assertEquals(deserializedObject, longObject);

      //==============================================================================
      // test Short-Array:
      //==============================================================================
      valueObject.setValue(shortObject);
      serializedValue = mapper.writeValueAsString(value);
      deserializeValue = mapper.readValue(serializedValue, DataTagValueUpdate.class);

      deserializedObject = getToObject(deserializeValue);
      assertEquals(deserializedObject, shortObject);

    } catch (JsonProcessingException e) {
      e.printStackTrace();
      assertTrue(false);
    } catch (IOException e) {
      e.printStackTrace();
      assertTrue(false);
    }

  }

  @Test
  public void serializeAndDeserializeArbitraryObjects() {

    ArbitraryObject testObject1 = ArbitraryObject.builder().field1("TestFiled").field2(1.4f).build();
    ArbitraryObject testObject2 = ArbitraryObject.builder().field1("TestFiled").field2(1.4f).fields(new Integer[]{1, 2, 3}).build();
    ArbitraryObject testObject3 = ArbitraryObject.builder().field1("TestFiled").field2(1.4f).objectField(testObject1).build();
    ArbitraryObject testObject4 = ArbitraryObject.builder().field1("TestFiled").field2(1.4f).addEntry("testValue1", 1.0).addEntry("testValue2", 2.0).build();

    String serializedValue;
    Object deserializedObject;


    try {
      //==============================================================================
      // test simple object:
      //==============================================================================
      valueObject.setValue(testObject1);
      serializedValue = mapper.writeValueAsString(value);
      deserializeValue = mapper.readValue(serializedValue, DataTagValueUpdate.class);

      deserializedObject = getToObject(deserializeValue);
      assertEquals(deserializedObject, testObject1);

      //==============================================================================
      // test simple object:
      //==============================================================================
      valueObject.setValue(testObject1);
      serializedValue = mapper.writeValueAsString(value);
      deserializeValue = mapper.readValue(serializedValue, DataTagValueUpdate.class);

      deserializedObject = getToObject(deserializeValue);
      assertEquals(deserializedObject, testObject1);

      //==============================================================================
      // test object with array:
      //==============================================================================
      valueObject.setValue(testObject2);
      serializedValue = mapper.writeValueAsString(value);
      deserializeValue = mapper.readValue(serializedValue, DataTagValueUpdate.class);

      deserializedObject = getToObject(deserializeValue);
      assertEquals(deserializedObject, testObject2);

      //==============================================================================
      // test object with object inside:
      //==============================================================================
      valueObject.setValue(testObject3);
      serializedValue = mapper.writeValueAsString(value);
      deserializeValue = mapper.readValue(serializedValue, DataTagValueUpdate.class);

      deserializedObject = getToObject(deserializeValue);
      assertEquals(deserializedObject, testObject3);

      //==============================================================================
      // test object with object inside:
      //==============================================================================
      valueObject.setValue(testObject4);
      serializedValue = mapper.writeValueAsString(value);
      deserializeValue = mapper.readValue(serializedValue, DataTagValueUpdate.class);

      deserializedObject = getToObject(deserializeValue);
      assertEquals(deserializedObject, testObject4);

    } catch (JsonProcessingException e) {
      e.printStackTrace();
      assertTrue(false);
    } catch (IOException e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }

  @Test
  public void serializeAndDeserializeArbitraryObjectWithCircle() {

    ArbitraryObject testObject1 = ArbitraryObject.builder().field1("TestFiled").field2(1.4f).build();
    testObject1.setObjectField(testObject1);

    String serializedValue;
    Object deserializedObject;


    try {
      //==============================================================================
      // test simple object:
      //==============================================================================
      valueObject.setValue(testObject1);
      serializedValue = mapper.writeValueAsString(value);
      deserializeValue = mapper.readValue(serializedValue, DataTagValueUpdate.class);

      deserializedObject = getToObject(deserializeValue);
      assertEquals(deserializedObject, testObject1);


    } catch (JsonProcessingException e) {
      if (e.getMessage().contains("Direct self-reference leading to cycle")) {
        assertTrue(true);
      } else {
        assertTrue(false);
      }

    } catch (IOException e) {
      e.printStackTrace();
      assertTrue(false);
    }

  }

  private Object[] getToObjectArray(DataTagValueUpdate update) {
    return (Object[]) ((SourceDataTagValue) value.getValues().toArray()[0]).getValue();
  }

  private Object getToObject(DataTagValueUpdate update) {
    return ((SourceDataTagValue) value.getValues().toArray()[0]).getValue();
  }

  @Data
  @Builder
  static class ArbitraryObject {
    private Integer[] fields;

    @Singular("addEntry")
    private Map<String, Double> map;

    private String field1;

    private Float field2;

    private Object objectField;

  }
}
