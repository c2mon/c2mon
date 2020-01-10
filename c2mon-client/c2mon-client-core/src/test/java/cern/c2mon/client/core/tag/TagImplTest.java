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
package cern.c2mon.client.core.tag;

import cern.c2mon.client.common.listener.BaseTagListener;
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.common.tag.TypeNumeric;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.alarm.AlarmValueImpl;
import cern.c2mon.shared.client.metadata.Metadata;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.client.tag.TransferTagImpl;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import cern.c2mon.shared.rule.RuleFormatException;
import org.easymock.EasyMock;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.junit.Assert.*;

public class TagImplTest {

  private TagUpdate createValidTransferTag(final Long tagId) {
    return createValidTransferTag(tagId, Float.valueOf(1.234f));
  }

  private TagUpdate createValidTransferTag(final Long tagId, Object value) {
    DataTagQuality tagQuality = new DataTagQualityImpl(
        TagQualityStatus.EQUIPMENT_DOWN, "its down!");
    Metadata metadata = new Metadata();
    metadata.addMetadata("testString", "hello");
    metadata.addMetadata("tesInt", 1);
    metadata.addMetadata("booleanFoo", true);
    metadata.addMetadata("tesLong", 1L);
    metadata.addMetadata("tesFloat", 1.0f);
    metadata.addMetadata("tesDouble", 1.0);

    tagQuality.addInvalidStatus(TagQualityStatus.EQUIPMENT_DOWN, "its down!");
    tagQuality.validate();

    TransferTagImpl tagUpdate =
        new TransferTagImpl(
            tagId,
            value,
            "test value desc",
            (DataTagQualityImpl) tagQuality,
            TagMode.TEST,
            new Timestamp(System.currentTimeMillis() - 10000L),
            new Timestamp(System.currentTimeMillis() - 5000L),
            new Timestamp(System.currentTimeMillis()),
            "Test description",
            "My.data.tag.name",
            "My.jms.topic");
    tagUpdate.setValueClassName(value !=null ? value.getClass().getName() : null);
    tagUpdate.setMetadata(metadata.getMetadata());
    tagUpdate.addAlarmValue(createAlarmValue(tagId));
    tagUpdate.setUnit("kw");
    return tagUpdate;
  }

  /**
   * Private helper method for creating an <code>AlarmValueImpl</code>.
   *
   */
  private static AlarmValueImpl createAlarmValue(Long tagId) {
    Metadata metadata = new Metadata();
    metadata.addMetadata("testString", "hello");
    metadata.addMetadata("tesInt", 1);
    metadata.addMetadata("booleanFoo", true);
    metadata.addMetadata("tesLong", 1L);
    metadata.addMetadata("tesFloat", 1.0f);
    metadata.addMetadata("tesDouble", 1.0);

    AlarmValueImpl alarmValue =
        new AlarmValueImpl(
            4321L,
            1007,
            "getFaultMember",
            "getFaultFamily",
            "getInfo",
            tagId,
            new Timestamp(System.currentTimeMillis()),
            new Timestamp(System.currentTimeMillis() - 10),
            true);
      alarmValue.setMetadata(metadata.getMetadata());
    return alarmValue;
  }

  private void checkTagValueCopy(final Tag original, final Tag copy) {
    assertNotSame("The two objects should not point to the same reference in memory!", original, copy);
    assertTrue(original.equals(copy));
    assertEquals(original.getServerTimestamp(), copy.getServerTimestamp());
    assertEquals(original.getTimestamp(), copy.getTimestamp());
    assertEquals(original.getDescription(), copy.getDescription());
    assertEquals(original.getDataTagQuality(), copy.getDataTagQuality());
    assertEquals(original.getId(), copy.getId());
    assertEquals(original.getName(), copy.getName());
    assertEquals(original.getRuleExpression(), copy.getRuleExpression());
    assertEquals(original.getType(), copy.getType());
    assertEquals(original.getTypeNumeric(), copy.getTypeNumeric());
    assertEquals(original.getUnit(), copy.getUnit());
    assertEquals(original.getValue(), copy.getValue());
    assertEquals(original.getValueDescription(), copy.getValueDescription());
    checkMetadataCopy(original.getMetadata(), copy.getMetadata());
    checkAlarmValueCopy(new ArrayList<>(original.getAlarms()),new ArrayList<>(copy.getAlarms()));
  }

  private void checkAlarmValueCopy(final List<AlarmValue> original, final List<AlarmValue> copy) {
    assertEquals(original.size(),copy.size());
    for(int i =0; i <copy.size(); i++){
      assertNotSame("The two objects should not point to the same reference in memory!", original.get(i), copy.get(i));
      assertTrue(original.get(i).equals(copy.get(i)));
      assertEquals(original.get(i).getFaultMember(), copy.get(i).getFaultMember());
      assertEquals(original.get(i).getFaultFamily(), copy.get(i).getFaultFamily());
      assertEquals(original.get(i).getFaultCode(), copy.get(i).getFaultCode());
      assertEquals(original.get(i).getInfo(), copy.get(i).getInfo());
      assertEquals(original.get(i).getTagId(), copy.get(i).getTagId());
      assertEquals(original.get(i).getTimestamp(), copy.get(i).getTimestamp());
      checkMetadataCopy(original.get(i).getMetadata(), copy.get(i).getMetadata());
    }
  }

  private void checkMetadataCopy(Map<String,Object> original, Map<String,Object> copy) {
    assertEquals(original.size(),copy.size());
    for(String key : original.keySet()){
      assertTrue(copy.containsKey(key));
      assertNotSame("The two objects should not point to the same reference in memory!", original.get(key), copy.get(key));
      assertEquals(original.get(key), copy.get(key));
    }
  }

  private void checkTagCopy(final TagController original, final TagController copy) {
    checkTagValueCopy(original.getTagImpl(), copy.getTagImpl());
    if (original.getUpdateListeners().isEmpty()) {
      assertEquals(original.getUpdateListeners().size(), copy.getUpdateListeners().size());
    } else {
      assertNotSame(original.getUpdateListeners().size(), copy.getUpdateListeners().size());
    }
    assertEquals(0, copy.getUpdateListeners().size());
  }

  @Test
  public void testClean() throws CloneNotSupportedException {
    final TagController tagController = new TagController(1234L);
    tagController.onUpdate(createValidTransferTag(1234L));

    TagController copy = new TagController(tagController.getTagImpl().clone());

    copy.clean();

    assertNotSame("The two objects should not point to the same reference in memory!", tagController, copy);
    assertTrue(tagController.getTagImpl().equals(copy.getTagImpl()));
    assertNotNull(copy.getTagImpl().getServerTimestamp());
    assertTrue(tagController.getTagImpl().getServerTimestamp().after(copy.getTagImpl().getServerTimestamp()));
    assertNull(copy.getTagImpl().getValue());
    assertEquals(0, copy.getTagImpl().getAlarmIds().size());
    assertEquals(new Timestamp(0L), copy.getTagImpl().getTimestamp());
    assertEquals("Tag not initialised.", copy.getTagImpl().getDescription());
    assertFalse(copy.getTagImpl().getDataTagQuality().isInitialised());
    assertEquals(tagController.getTagImpl().getName(), copy.getTagImpl().getName());
    assertEquals(tagController.getTagImpl().getRuleExpression(), copy.getTagImpl().getRuleExpression());
    assertNull(copy.getTagImpl().getType());
    assertEquals(TypeNumeric.TYPE_UNKNOWN, copy.getTagImpl().getTypeNumeric());
    assertEquals(tagController.getTagImpl().getUnit(), copy.getTagImpl().getUnit());
    assertNull(copy.getTagImpl().getValue());
  }

  @Test
  public void testTypeNumeric() {
    final TagController tagController = new TagController(1234L);

    tagController.onUpdate(createValidTransferTag(1234L, Float.valueOf(1.234f)));
    assertEquals(TypeNumeric.TYPE_FLOAT, tagController.getTagImpl().getTypeNumeric());

    tagController.clean();
    tagController.onUpdate(createValidTransferTag(1234L, Long.valueOf(234324L)));
    assertEquals(TypeNumeric.TYPE_LONG, tagController.getTagImpl().getTypeNumeric());

    tagController.clean();
    tagController.onUpdate(createValidTransferTag(1234L, Boolean.FALSE));
    assertEquals(TypeNumeric.TYPE_BOOLEAN, tagController.getTagImpl().getTypeNumeric());

    tagController.clean();
    tagController.onUpdate(createValidTransferTag(1234L, Byte.valueOf((byte) 0x000A)));
    assertEquals(TypeNumeric.TYPE_BYTE, tagController.getTagImpl().getTypeNumeric());

    tagController.clean();
    tagController.onUpdate(createValidTransferTag(1234L, Double.valueOf(1231231324123d)));
    assertEquals(TypeNumeric.TYPE_DOUBLE, tagController.getTagImpl().getTypeNumeric());

    tagController.clean();
    tagController.onUpdate(createValidTransferTag(1234L, Integer.valueOf(123324123)));
    assertEquals(TypeNumeric.TYPE_INTEGER, tagController.getTagImpl().getTypeNumeric());

    tagController.clean();
    tagController.onUpdate(createValidTransferTag(1234L, Short.valueOf((short) -123)));
    assertEquals(TypeNumeric.TYPE_SHORT, tagController.getTagImpl().getTypeNumeric());

    tagController.clean();
    tagController.onUpdate(createValidTransferTag(1234L, "Test string"));
    assertEquals(TypeNumeric.TYPE_STRING, tagController.getTagImpl().getTypeNumeric());

    tagController.clean();
    tagController.onUpdate(createValidTransferTag(1234L, null));
    assertEquals(TypeNumeric.TYPE_UNKNOWN, tagController.getTagImpl().getTypeNumeric());
  }

  @Test
  public void testUpdateListenerInitialUpdate() throws CloneNotSupportedException, RuleFormatException {
    //test setup
    TagController tagController = new TagController(1234L);
    TagUpdate tagUpdate = createValidTransferTag(1234L);

    tagController.update(tagUpdate);
    BaseTagListener mockUpdateListener = EasyMock.createMock(BaseTagListener.class);
    mockUpdateListener.onUpdate(EasyMock.and(EasyMock.not(EasyMock.same(tagController.getTagImpl())), EasyMock.eq(tagController.getTagImpl())));

    //run test
    EasyMock.replay(mockUpdateListener);
    tagController.addUpdateListener(mockUpdateListener);

    //check test success
    EasyMock.verify(mockUpdateListener);

    TagImpl tag = tagController.getTagImpl();

    assertEquals(tagUpdate.getAlarms(), tag.getAlarms());
    assertEquals(tagUpdate.getAlarms().size(), tag.getAlarmIds().size());
    assertEquals(tagUpdate.getDaqTimestamp(), tag.getDaqTimestamp());
    assertEquals(tagUpdate.getServerTimestamp(), tag.getServerTimestamp());
    assertEquals(tagUpdate.getSourceTimestamp(), tag.getSourceTimestamp());
    assertEquals(tagUpdate.getDataTagQuality(), tag.getDataTagQuality());
    assertEquals(tagUpdate.getDescription(), tag.getDescription());
    tagUpdate.getEquipmentIds().stream().forEach(id -> assertTrue(tag.getEquipmentIds().contains(id)));
    assertEquals(tagUpdate.getId(), tag.getId());

    for (Entry<String, Object> entrySet : tag.getMetadata().entrySet()) {
      assertEquals(entrySet.getValue(), tag.getMetadata().get(entrySet.getKey()));
    }

    assertEquals(tagUpdate.getMode(), tag.getMode());
    tagUpdate.getProcessIds().stream().forEach(id -> assertTrue(tag.getProcessIds().contains(id)));
    assertEquals(tagUpdate.getRuleExpression(), tag.getRuleExpression());
    tagUpdate.getSubEquipmentIds().stream().forEach(id -> assertTrue(tag.getSubEquipmentIds().contains(id)));
    assertEquals(tagUpdate.getName(), tag.getName());
    assertEquals(tagUpdate.getTopicName(), tag.getTopicName());
    assertEquals(tagUpdate.getUnit(), tag.getUnit());
    assertEquals(tagUpdate.getValue(), tag.getValue());
    assertEquals(tagUpdate.getValueDescription(), tag.getValueDescription());

  }

  @Test
  public void testUpdateListener() {
    final TagController tagController = new TagController(1234L);
    tagController.addUpdateListener(new BaseTagListener() {
      @Override
      public void onUpdate(final Tag tagUpdate) {
        assertNotNull(tagUpdate);
        assertEquals(tagController.getTagImpl(), tagUpdate);
        checkTagValueCopy(tagController.getTagImpl(), tagUpdate);
        assertFalse(tagController == tagUpdate);
      }
    });

    tagController.onUpdate(createValidTransferTag(1234L));
  }

  @Test
  public void testClone() throws Exception {
    TagController tagController = new TagController(1234L);
    TagController clone = new TagController(tagController.getTagImpl().clone());
    checkTagCopy(tagController, clone);

    tagController.update(createValidTransferTag(1234L));
    clone = new TagController(tagController.getTagImpl().clone());
    checkTagCopy(tagController, clone);

    tagController.addUpdateListener((BaseTagListener) tagUpdate -> {
      // Do nothing
    });

    clone = new TagController(tagController.getTagImpl().clone());
    checkTagCopy(tagController, clone);

    tagController.invalidate(TagQualityStatus.INACCESSIBLE, "Down");
    clone = new TagController(tagController.getTagImpl().clone());
    checkTagCopy(tagController, clone);

    tagController.getTagImpl().getDataTagQuality().validate();
    clone.setTagImpl(tagController.getTagImpl().clone());
    checkTagCopy(tagController, clone);
  }

  @Test
  public void testEquals() throws CloneNotSupportedException {
    TagController tagController = new TagController(1234L);
    TagController clone = new TagController(tagController.getTagImpl().clone());
    clone.clean();

    assertEquals(tagController.getTagImpl(), clone.getTagImpl());

    TagController cdt2 = new TagController(4321L);
    assertFalse(tagController.equals(cdt2));
  }

  @Test
  public void retrieveTopicName() {
    TagController tagController = new TagController(1234L);
    tagController.onUpdate(createValidTransferTag(1234L));
    tagController.getTagImpl().setTopicName("test");

    assertTrue(tagController.getTagImpl().getTopicName().equals("test"));
  }
}
