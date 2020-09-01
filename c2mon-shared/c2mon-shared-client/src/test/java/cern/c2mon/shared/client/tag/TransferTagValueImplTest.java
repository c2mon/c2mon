/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.shared.client.tag;

import java.sql.Timestamp;

import org.junit.Test;

import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.alarm.AlarmValueImpl;
import cern.c2mon.shared.client.serializer.TransferTagSerializer;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.util.TagQualityStatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TransferTagValueImplTest {

  /**
   * Helper method to create a <code>TransferTagValueImpl</code> test object
   * @param tagValue The tag value
   * @return A new <code>TransferTagValueImpl</code> test object
   */
  public static TransferTagValueImpl createTagForValue(final Object tagValue) {
    TransferTagValueImpl result;

    DataTagQualityImpl tagQuality = new DataTagQualityImpl(TagQualityStatus.PROCESS_DOWN, "Process Down");
    Timestamp sourceTimestamp = new Timestamp(System.currentTimeMillis());
    Timestamp daqTimestamp = new Timestamp(System.currentTimeMillis());
    Timestamp serverTimestamp = new Timestamp(System.currentTimeMillis());
    String descr = "Test transfer tag";
    String valDesc = "Test val desc {{ \"aaa:\" \"342343\" } \n , ] }";

    result = new TransferTagValueImpl(
        1234L, tagValue, valDesc, tagQuality, TagMode.TEST,
        sourceTimestamp, daqTimestamp, serverTimestamp, descr);

    result.setValueClassName(tagValue.getClass().getName());
    return result;
  }

  @Test
  public void testFloatJsonMsg() {
    Float value = Float.valueOf(3.34535f);
    String jacksonString = TransferTagSerializer.toJson(createTagForValue(value));
    TagValueUpdate receivedTag = TransferTagSerializer.fromJson(jacksonString, TransferTagValueImpl.class);
    assertTrue(receivedTag.getValue() instanceof Float);
    assertEquals(value, receivedTag.getValue());
  }


  @Test
  public void testIntegerJsonMsg() {
    Integer value = Integer.valueOf(3);
    String jacksonString = TransferTagSerializer.toJson(createTagForValue(value));
    TagValueUpdate receivedTag = TransferTagSerializer.fromJson(jacksonString, TransferTagValueImpl.class);
    assertTrue(receivedTag.getValue() instanceof Integer);
    assertEquals(value, receivedTag.getValue());
  }

  @Test
  public void testLongJsonMsg() {
    Long value = Long.valueOf(23453534634563246L);
    String jacksonString = TransferTagSerializer.toJson(createTagForValue(value));
    TagValueUpdate receivedTag = TransferTagSerializer.fromJson(jacksonString, TransferTagValueImpl.class);
    assertTrue(receivedTag.getValue() instanceof Long);
    assertEquals(value, receivedTag.getValue());
  }

  @Test
  public void testDoubleJsonMsg() {
    Double value = Double.valueOf(2345356324.3245325D);
    String jacksonString = TransferTagSerializer.toJson(createTagForValue(value));
    TagValueUpdate receivedTag = TransferTagSerializer.fromJson(jacksonString, TransferTagValueImpl.class);
    assertTrue(receivedTag.getValue() instanceof Double);
    assertEquals(value, receivedTag.getValue());
  }

  @Test
  public void testShortJsonMsg() {
    Short value = Short.valueOf("077");
    String jacksonString = TransferTagSerializer.toJson(createTagForValue(value));
    TagValueUpdate receivedTag = TransferTagSerializer.fromJson(jacksonString, TransferTagValueImpl.class);
    assertTrue(receivedTag.getValue() instanceof Short);
    assertEquals(value, receivedTag.getValue());
  }

  @Test
  public void testByteJsonMsg() {
    Byte value = Byte.valueOf((byte) 0x000A);
    String jacksonString = TransferTagSerializer.toJson(createTagForValue(value));
    TagValueUpdate receivedTag = TransferTagSerializer.fromJson(jacksonString, TransferTagValueImpl.class);
    assertTrue(receivedTag.getValue() instanceof Byte);
    assertEquals(value, receivedTag.getValue());
  }

  @Test
  public void testBooleanJsonMsg() {
    Boolean value = Boolean.TRUE;
    String jacksonString = TransferTagSerializer.toJson(createTagForValue(value));
    TagValueUpdate receivedTag = TransferTagSerializer.fromJson(jacksonString, TransferTagValueImpl.class);
    assertTrue(receivedTag.getValue() instanceof Boolean);
    assertEquals(value, receivedTag.getValue());
  }

  @Test
  public void testStringJsonMsg() {
    String jacksonString = TransferTagSerializer.toJson(createTagForValue("This is a test String value message!"));
    TagValueUpdate receivedTag = TransferTagSerializer.fromJson(jacksonString, TransferTagValueImpl.class);
    assertEquals("This is a test String value message!", receivedTag.getValue());

    String floatStr = "45234326.324";
    jacksonString = TransferTagSerializer.toJson(createTagForValue(floatStr));
    receivedTag = TransferTagSerializer.fromJson(jacksonString, TransferTagValueImpl.class);
    assertTrue(receivedTag.getValue() instanceof String);
    assertEquals(floatStr, receivedTag.getValue());
  }

  @Test
  public void testAlarmValueAdding() {
    TransferTagValueImpl tagValue = createTagForValue(Integer.valueOf(234234));
    AlarmValueImpl alarmValid =
        new AlarmValueImpl(12342L, 1, "FaultMember1", "FaultFamily1", "Info1", 1234L,
            new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() - 10), true);

    assertTrue(tagValue.addAlarmValue(alarmValid));


    AlarmValueImpl alarmInvalid =
        new AlarmValueImpl(12342L, 1, "FaultMember1", "FaultFamily1", "Info1", 4532453L,
            new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() - 10), true);
    assertFalse(tagValue.addAlarmValue(alarmInvalid));

    assertEquals(1, tagValue.getAlarms().size());
    TagValueUpdate receivedTag = TransferTagSerializer.fromJson(TransferTagSerializer.toJson(tagValue), TransferTagValueImpl.class);
    assertEquals(1, receivedTag.getAlarms().size());
    assertEquals(alarmValid, receivedTag.getAlarms().toArray()[0]);

    AlarmValue receivedAlarm = receivedTag.getAlarms().iterator().next();
    assertEquals(alarmValid.getFaultCode(), receivedAlarm.getFaultCode());
    assertEquals(alarmValid.getFaultFamily(), receivedAlarm.getFaultFamily());
    assertEquals(alarmValid.getFaultMember(), receivedAlarm.getFaultMember());
    assertEquals(alarmValid.getId(), receivedAlarm.getId());
    assertEquals(alarmValid.getInfo(), receivedAlarm.getInfo());
    assertEquals(alarmValid.getTagId(), receivedAlarm.getTagId());
    assertEquals(alarmValid.getTimestamp(), receivedAlarm.getTimestamp());
    assertEquals(alarmValid.isActive(), receivedAlarm.isActive());

  }

  @Test
  public void testTagQuality() {
    TransferTagValueImpl tagValue = createTagForValue(Integer.valueOf(234234));
    tagValue.getDataTagQuality().addInvalidStatus(TagQualityStatus.VALUE_EXPIRED, "Value has expired");
    TagValueUpdate receivedTag = TransferTagSerializer.fromJson(TransferTagSerializer.toJson(tagValue), TransferTagValueImpl.class);
    assertEquals(tagValue.getDataTagQuality().getInvalidQualityStates().size(), receivedTag.getDataTagQuality().getInvalidQualityStates().size());
    assertTrue(receivedTag.getDataTagQuality().isInvalidStatusSet(TagQualityStatus.VALUE_EXPIRED));
    assertEquals(tagValue.getDataTagQuality().getInvalidQualityStates().get(TagQualityStatus.VALUE_EXPIRED),
              receivedTag.getDataTagQuality().getInvalidQualityStates().get(TagQualityStatus.VALUE_EXPIRED));
  }

  //arbitrary complex type encoding is no longer supported (and was never used)
//  @Test
//  public void testComplexObjectJsonMsg() {
//    AlarmValue value = new AlarmValueImpl(12342L, 1, "FaultMember1", "FaultFamily1", "Info1",
//        4532453L, new Timestamp(System.currentTimeMillis()), true);
//    TransferTagValueImpl tagValue = createTagForValue(value);
//    TagValueUpdate receivedTag = TransferTagValueImpl.fromJson(tagValue.toJson());
//
//    Object receivedValue = receivedTag.getValue();
//    assertTrue(receivedValue instanceof AlarmValueImpl);
//    assertEquals(value, receivedValue);
//  }

  /**
   * Can a new Json lib be introduced on the clients and still decode current
   * server messages?
   */
  @Test
  public void testStringBackwardsCompatibility() {

    //current publication format of Tag + associated alarm values
    String jsonString = "{\"tagId\":100003," +
    		"\"valueClassName\":\"java.lang.String\"," +
    		"\"tagValue\":\"DOWN\"," +
    		"\"mode\":\"TEST\"," +
    		"\"simulated\":false," +
    		"\"alarmValues\":[{\"id\":1," +
    				                "\"faultCode\":0," +
    				                "\"faultFamily\":\"fault family\"," +
    				                "\"faultMember\":\"fault member\"," +
    				                "\"info\":\"alarm info\"," +
    				                "\"tagId\":100003," +
    				                "\"timestamp\":1343809447020," +
    				                "\"active\":false," +
    				                "\"reportType\":\"RESULT\"," +
    				                "\"totalOperations\":0," +
    				                "\"currentOperation\":0," +
    				                "\"totalParts\":0," +
    				                "\"currentPart\":0}," +
    				                "{\"id\":3," +
    				                "\"faultCode\":0," +
    				                "\"faultFamily\":\"fault family\"," +
    				                "\"faultMember\":\"fault member\"," +
    				                "\"info\":\"alarm info\"," +
    				                "\"tagId\":100003," +
    				                "\"timestamp\":1343809447020," +
    				                "\"active\":false," +
    				                "\"reportType\":\"RESULT\"," +
    				                "\"totalOperations\":0," +
    				                "\"currentOperation\":0," +
    				                "\"totalParts\":0," +
    				                "\"currentPart\":0}]," +
    			"\"tagQuality\":{\"invalidQualityStates\":{},\"isValid\":false}," +
    			"\"description\":\"test description\"," +
    			"\"valueDescription\":\"test value description\"," +
    			"\"sourceTimestamp\":1343809448989," +
    			"\"serverTimestamp\":1343809448989," +
    			"\"reportType\":\"RESULT\"," +
    			"\"totalOperations\":0," +
    			"\"currentOperation\":0," +
    			"\"totalParts\":0," +
    			"\"currentPart\":0}";

    TagValueUpdate receivedTag = TransferTagSerializer.fromJson(jsonString, TransferTagValueImpl.class);

    assertEquals(((TransferTagValueImpl)receivedTag).getValueClassName(), "java.lang.String");
    assertEquals(receivedTag.getId(), Long.valueOf(100003));
    assertEquals(receivedTag.getValue(), "DOWN");
    assertEquals(receivedTag.getMode(), TagMode.TEST);
    assertEquals(receivedTag.isSimulated(), false);
    assertEquals(receivedTag.getDataTagQuality().isValid(), true);
    assertEquals(receivedTag.getDescription(), "test description");
    assertEquals(receivedTag.getValueDescription(), "test value description");
    assertEquals(receivedTag.getSourceTimestamp(), new Timestamp(1343809448989L));
    assertEquals(receivedTag.getServerTimestamp(), new Timestamp(1343809448989L));
    assertEquals(((TransferTagValueImpl)receivedTag).isResult(), true);
    assertEquals(receivedTag.getAlarms().size(), 2);
    for (AlarmValue alarmValue : receivedTag.getAlarms()) {
      assertTrue(alarmValue.getId().equals(1L) || alarmValue.getId().equals(3L));
    }
  }

  /**
   * Same test but for Long encoding.
   */
  @Test
  public void testLongBackwardsCompatibility() {

    //current publication format of Tag + associated alarm values
    String jsonString = "{\"tagId\":100003," +
        "\"valueClassName\":\"java.lang.Long\"," +
        "\"tagValue\":1843809447020," +
        "\"mode\":\"TEST\"," +
        "\"simulated\":false," +
        "\"alarmValues\":[{\"id\":1," +
                            "\"faultCode\":0," +
                            "\"faultFamily\":\"fault family\"," +
                            "\"faultMember\":\"fault member\"," +
                            "\"info\":\"alarm info\"," +
                            "\"tagId\":100003," +
                            "\"timestamp\":1343809447020," +
                            "\"active\":false," +
                            "\"reportType\":\"RESULT\"," +
                            "\"totalOperations\":0," +
                            "\"currentOperation\":0," +
                            "\"totalParts\":0," +
                            "\"currentPart\":0}," +
                            "{\"id\":3," +
                            "\"faultCode\":0," +
                            "\"faultFamily\":\"fault family\"," +
                            "\"faultMember\":\"fault member\"," +
                            "\"info\":\"alarm info\"," +
                            "\"tagId\":100003," +
                            "\"timestamp\":1343809447020," +
                            "\"active\":false," +
                            "\"reportType\":\"RESULT\"," +
                            "\"totalOperations\":0," +
                            "\"currentOperation\":0," +
                            "\"totalParts\":0," +
                            "\"currentPart\":0}]," +
          "\"tagQuality\":{\"invalidQualityStates\":{},\"isValid\":false}," +
          "\"description\":\"test description\"," +
          "\"valueDescription\":\"test value description\"," +
          "\"sourceTimestamp\":1343809448989," +
          "\"serverTimestamp\":1343809448989," +
          "\"reportType\":\"RESULT\"," +
          "\"totalOperations\":0," +
          "\"currentOperation\":0," +
          "\"totalParts\":0," +
          "\"currentPart\":0}";

    TagValueUpdate receivedTag = TransferTagSerializer.fromJson(jsonString, TransferTagValueImpl.class);

    assertEquals("java.lang.Long", ((TransferTagValueImpl)receivedTag).getValueClassName());
    assertEquals(Long.valueOf("1843809447020"), receivedTag.getValue());
  }

}
