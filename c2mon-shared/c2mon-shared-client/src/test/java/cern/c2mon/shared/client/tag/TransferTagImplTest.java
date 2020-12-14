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
package cern.c2mon.shared.client.tag;

import static cern.c2mon.shared.client.serializer.TransferTagSerializer.*;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.Arrays;

import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.client.tag.TransferTagImpl;
import cern.c2mon.shared.client.tag.TransferTagValueImpl;
import junit.framework.Assert;
import org.junit.Test;

import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.util.TagQualityStatus;

/**
 * @author Matthias Braeger
 */
public class TransferTagImplTest {

  /**
   * Helper method to create a <code>TransferTagImpl</code> test object
   * @param tagValue The tag value
   * @return A new <code>TransferTagImpl</code> test object
   */
  private static TransferTagImpl createTagForValue(final Object tagValue) {
    DataTagQualityImpl tagQuality = new DataTagQualityImpl(TagQualityStatus.PROCESS_DOWN, "Process Down");
    Timestamp sourceTimestamp = new Timestamp(System.currentTimeMillis());
    Timestamp daqTimestamp = new Timestamp(System.currentTimeMillis());
    Timestamp serverTimestamp = new Timestamp(System.currentTimeMillis());
    String descr = "Test transfer tag";
    String valDesc = "Test val desc \n \t { \"test\" : \"1212\"}\n}";
    String tagName = "tag:name";
    String topicName = "topic:name";

    TransferTagImpl transferTag = new TransferTagImpl(
        1234L, tagValue, valDesc, tagQuality, TagMode.TEST,
        sourceTimestamp, daqTimestamp, serverTimestamp, descr, tagName, topicName);

    transferTag.addEquipmentIds(Arrays.asList(234L, 4234L, 234L));
    transferTag.addSubEquipmentIds(Arrays.asList(1234L, 14234L, 1234L));
    transferTag.addProcessIds(Arrays.asList(123L, 3214L, 123L));
    transferTag.setValueClassName(tagValue.getClass().getName());

    return transferTag;
  }

  @Test
  public void testFloatJsonMsg() {
    TransferTagImpl tag = createTagForValue(Float.valueOf(3.34535f));
    String jacksonString = toJson(tag);
    TagUpdate receivedTag = fromJson(jacksonString, TransferTagImpl.class);
    assertTrue(receivedTag.getValue() instanceof Float);
    assertEquals(tag.getDescription(), receivedTag.getDescription());
  }

  /**
   * Can a new Json lib be introduced on the clients and still decode current
   * server messages?
   */
  @Test
  public void testFloatBackwardsCompatible() {
    String encoded = "{\"processIds\":[123,3214]," +
    		             "\"equipmentIds\":[4234,234]," +
    		             "\"subEquipmentIds\":[14234,1234]," +
    		             "\"topicName\":\"topic:name\"," +
    		             "\"tagName\":\"tag:name\"," +
    		             "\"tagId\":1234," +
    		             "\"valueClassName\":\"java.lang.Float\"," +
    		             "\"value\":3.34535," +
    		             "\"mode\":\"TEST\"," +
    		             "\"simulated\":false," +
    		             "\"alarmValues\":[]," +
    		             "\"tagQuality\":{\"invalidQualityStates\":{\"PROCESS_DOWN\":\"Process Down\"},\"isValid\":false}," +
    		             "\"description\":\"Test transfer tag\"," +
    		             "\"valueDescription\":\"Test val desc\"," +
    		             "\"sourceTimestamp\":1343812635352," +
    		             "\"serverTimestamp\":1343812635352," +
    		             "\"reportType\":\"RESULT\"," +
    		             "\"totalOperations\":0," +
    		             "\"currentOperation\":0," +
    		             "\"totalParts\":0," +
    		             "\"currentPart\":0}";

    TagUpdate receivedTag = fromJson(encoded, TransferTagImpl.class);
    assertEquals(receivedTag.getId(), Long.valueOf(1234));
    assertEquals(receivedTag.getProcessIds().size(), 2);
    assertTrue(receivedTag.getProcessIds().contains(123L));
    assertTrue(receivedTag.getProcessIds().contains(3214L));
    assertEquals(receivedTag.getEquipmentIds().size(), 2);
    assertTrue(receivedTag.getEquipmentIds().contains(4234L));
    assertTrue(receivedTag.getEquipmentIds().contains(234L));
    assertEquals(receivedTag.getSubEquipmentIds().size(), 2);
    assertTrue(receivedTag.getSubEquipmentIds().contains(14234L));
    assertTrue(receivedTag.getSubEquipmentIds().contains(1234L));
    assertEquals(receivedTag.getTopicName(), "topic:name");
    assertEquals(receivedTag.getName(), "tag:name");
    Assert.assertEquals(((TransferTagValueImpl)receivedTag).getValueClassName(), "java.lang.Float");
    assertEquals(receivedTag.getValue(), Float.valueOf("3.34535"));
    assertEquals(receivedTag.getMode(), TagMode.TEST);
    assertEquals(receivedTag.getAlarms().size(), 0);
    assertEquals(receivedTag.isSimulated(), false);
    assertEquals(receivedTag.getDescription(), "Test transfer tag");
    assertEquals(((TransferTagValueImpl)receivedTag).isResult(), true);
    assertEquals(((TransferTagValueImpl)receivedTag).isErrorReport(), false);
    assertEquals(((TransferTagValueImpl)receivedTag).isProgressReport(), false);
    assertTrue(!receivedTag.getDataTagQuality().isValid());
    assertEquals(receivedTag.getDataTagQuality().isInvalidStatusSet(TagQualityStatus.PROCESS_DOWN), true);
  }

  /**
   * Same test but for String encoding.
   */
  @Test
  public void testStringBackwardsCompatible() {
    String encoded = "{\"processIds\":[123,3214]," +
                     "\"equipmentIds\":[4234,234]," +
                     "\"subEquipmentIds\":[14234,1234]," +
                     "\"topicName\":\"topic:name\"," +
                     "\"name\":\"tag:name\"," +
                     "\"id\":1234," +
                     "\"valueClassName\":\"java.lang.String\"," +
                     "\"value\":\"This is a test String value message!\"," +
                     "\"mode\":\"TEST\"," +
                     "\"simulated\":false," +
                     "\"alarmValues\":[]," +
                     "\"dataTagQuality\":{\"invalidQualityStates\":{\"PROCESS_DOWN\":\"Process Down\"},\"isValid\":false}," +
                     "\"description\":\"Test transfer tag\"," +
                     "\"valueDescription\":\"Test val desc\"," +
                     "\"sourceTimestamp\":1343812635352," +
                     "\"serverTimestamp\":1343812635352," +
                     "\"reportType\":\"RESULT\"," +
                     "\"totalOperations\":0," +
                     "\"currentOperation\":0," +
                     "\"totalParts\":0," +
                     "\"currentPart\":0}";

    TagUpdate receivedTag = fromJson(encoded, TransferTagImpl.class);
    assertEquals(((TransferTagValueImpl)receivedTag).getValueClassName(), "java.lang.String");
    assertEquals(receivedTag.getValue(), "This is a test String value message!");
  }

  @Test
  public void testIntegerJsonMsg() {
    String jacksonString = toJson(createTagForValue(Integer.valueOf(3)));
    TagUpdate receivedTag = fromJson(jacksonString, TransferTagImpl.class);
    assertTrue(receivedTag.getValue() instanceof Integer);
  }

  @Test
  public void testLongJsonMsg() {
    String jacksonString = toJson(createTagForValue(Long.valueOf(23453534634563246L)));
    TagUpdate receivedTag = fromJson(jacksonString, TransferTagImpl.class);
    assertTrue(receivedTag.getValue() instanceof Long);
  }

  @Test
  public void testDoubleJsonMsg() {
    Double value = Double.valueOf(2345356324.3245325D);
    String jacksonString = toJson(createTagForValue(value));
    TagUpdate receivedTag = fromJson(jacksonString, TransferTagImpl.class);
    assertTrue(receivedTag.getValue() instanceof Double);
    assertEquals(value, receivedTag.getValue());
  }

  @Test
  public void testShortJsonMsg() {
    String jacksonString = toJson(createTagForValue(Short.valueOf("077")));
    TagUpdate receivedTag = fromJson(jacksonString, TransferTagImpl.class);
    assertTrue(receivedTag.getValue() instanceof Short);
  }

  @Test
  public void testByteJsonMsg() {
    String jacksonString = toJson(createTagForValue(Byte.valueOf((byte) 0x000A)));
    TagUpdate receivedTag = fromJson(jacksonString, TransferTagImpl.class);
    assertTrue(receivedTag.getValue() instanceof Byte);
  }

  @Test
  public void testBooleanJsonMsg() {
    String jacksonString = toJson(createTagForValue(Boolean.TRUE));
    TagUpdate receivedTag = fromJson(jacksonString, TransferTagImpl.class);
    assertTrue(receivedTag.getValue() instanceof Boolean);
  }

  @Test
  public void testStringJsonMsg() {
    String jacksonString = toJson(createTagForValue("This is a test String value message!"));
    TagUpdate receivedTag = fromJson(jacksonString, TransferTagImpl.class);
    assertTrue(receivedTag.getValue() instanceof String);
  }

  @Test
  public void testRuleIdsTransfer() {
    TransferTagImpl originalTransferTag = createTagForValue(Long.valueOf(234533246L));
    String jacksonString = toJson(originalTransferTag);
    TagUpdate receivedTag = fromJson(jacksonString, TransferTagImpl.class);
    assertTrue(receivedTag.getValue() instanceof Long);
  }

  @Test
  public void testPublicationsTransfer() {
    TransferTagImpl originalTransferTag = createTagForValue(Long.valueOf(444L));
    String jacksonString = toJson(originalTransferTag);
    TagUpdate receivedTag = fromJson(jacksonString, TransferTagImpl.class);
    assertTrue(receivedTag.getValue() instanceof Long);
  }

}
