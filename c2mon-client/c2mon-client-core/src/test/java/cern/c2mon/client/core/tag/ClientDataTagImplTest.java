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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.alarm.AlarmValueImpl;
import cern.c2mon.shared.common.metadata.Metadata;
import org.easymock.EasyMock;
import org.junit.Test;

import cern.c2mon.client.common.listener.BaseListener;
import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.common.tag.TypeNumeric;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.client.tag.TransferTagImpl;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.TagQualityStatus;

public class ClientDataTagImplTest {

  private TagUpdate createValidTransferTag(final Long tagId) {
    return createValidTransferTag(tagId, Float.valueOf(1.234f));
  }

  private TagUpdate createValidTransferTag(final Long tagId, Object value) {
    DataTagQuality tagQuality = new DataTagQualityImpl(
        TagQualityStatus.EQUIPMENT_DOWN, "its down!");
    Metadata metadata = Metadata.builder().addMetadata("testString", "hello").addMetadata("tesInt", 1).addMetadata("booleanFoo", true).addMetadata("tesLong", 1L).addMetadata("tesFloat", 1.0f).addMetadata("tesDouble", 1.0).build();

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
    return tagUpdate;
  }

  /**
   * Private helper method for creating an <code>AlarmValueImpl</code>.
   *
   */
  private static AlarmValueImpl createAlarmValue(Long tagId) {
    Metadata metadata = Metadata.builder().addMetadata("testString", "hello").addMetadata("tesInt", 1).addMetadata("booleanFoo", true).addMetadata("tesLong", 1L).addMetadata("tesFloat", 1.0f).addMetadata("tesDouble", 1.0).build();

    AlarmValueImpl alarmValue =
        new AlarmValueImpl(
            4321L,
            1007,
            "getFaultMember",
            "getFaultFamily",
            "getInfo",
            tagId,
            new Timestamp(System.currentTimeMillis()),
            true);
      alarmValue.setMetadata(metadata.getMetadata());
    return alarmValue;
  }


  private void checkTagValueCopy(final CloneableTagBean original, final CloneableTagBean copy) {
    assertNotSame("The two objects should not point to the same reference in memory!", original, copy);
    assertTrue(original.getTagBean().equals(copy.getTagBean()));
    assertEquals(original.getTagBean().getServerTimestamp(), copy.getTagBean().getServerTimestamp());
    assertEquals(original.getTagBean().getTimestamp(), copy.getTagBean().getTimestamp());
    assertEquals(original.getTagBean().getDescription(), copy.getTagBean().getDescription());
    assertEquals(original.getTagBean().getDataTagQuality(), copy.getTagBean().getDataTagQuality());
    assertEquals(original.getTagBean().getId(), copy.getTagBean().getId());
    assertEquals(original.getTagBean().getName(), copy.getTagBean().getName());
    assertEquals(original.getTagBean().getRuleExpression(), copy.getTagBean().getRuleExpression());
    assertEquals(original.getTagBean().getType(), copy.getTagBean().getType());
    assertEquals(original.getTagBean().getTypeNumeric(), copy.getTagBean().getTypeNumeric());
    assertEquals(original.getTagBean().getUnit(), copy.getTagBean().getUnit());
    assertEquals(original.getTagBean().getValue(), copy.getTagBean().getValue());
    assertEquals(original.getTagBean().getValueDescription(), copy.getTagBean().getValueDescription());
    checkMetadataCopy(original.getMetadata(), copy.getMetadata());
    checkAlarmValueCopy(new ArrayList<>(original.getTagBean().getAlarms()),new ArrayList<>(copy.getTagBean().getAlarms()));
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

  private void checkTagCopy(final CloneableTagBean original, final CloneableTagBean copy) {
    checkTagValueCopy(original, copy);
    if (original.getUpdateListeners().isEmpty()) {
      assertEquals(original.getUpdateListeners().size(), copy.getUpdateListeners().size());
    } else {
      assertNotSame(original.getUpdateListeners().size(), copy.getUpdateListeners().size());
    }
    assertEquals(0, copy.getUpdateListeners().size());
  }

  @Test
  public void testClean() throws CloneNotSupportedException {
    final CloneableTagBean cdt = new CloneableTagBean(1234L);
    cdt.onUpdate(createValidTransferTag(1234L));

    CloneableTagBean copy = new CloneableTagBean(cdt.getTagBean().clone());

    copy.clean();

    assertNotSame("The two objects should not point to the same reference in memory!", cdt, copy);
    assertTrue(cdt.getTagBean().equals(copy.getTagBean()));
    assertNotNull(copy.getTagBean().getServerTimestamp());
    assertTrue(cdt.getTagBean().getServerTimestamp().after(copy.getTagBean().getServerTimestamp()));
    assertNull(copy.getTagBean().getValue());
    assertEquals(0, copy.getTagBean().getAlarmIds().size());
    assertEquals(new Timestamp(0L), copy.getTagBean().getTimestamp());
    assertEquals("Tag not initialised.", copy.getTagBean().getDescription());
    assertFalse(copy.getTagBean().getDataTagQuality().isInitialised());
    assertEquals(cdt.getTagBean().getName(), copy.getTagBean().getName());
    assertEquals(cdt.getTagBean().getRuleExpression(), copy.getTagBean().getRuleExpression());
    assertNull(copy.getTagBean().getType());
    assertEquals(TypeNumeric.TYPE_UNKNOWN, copy.getTagBean().getTypeNumeric());
    assertEquals(cdt.getTagBean().getUnit(), copy.getTagBean().getUnit());
    assertNull(copy.getTagBean().getValue());
  }

  @Test
  public void testTypeNumeric() {
    final CloneableTagBean cdt = new CloneableTagBean(1234L);

    cdt.onUpdate(createValidTransferTag(1234L, Float.valueOf(1.234f)));
    assertEquals(TypeNumeric.TYPE_FLOAT, cdt.getTagBean().getTypeNumeric());

    cdt.clean();
    cdt.onUpdate(createValidTransferTag(1234L, Long.valueOf(234324L)));
    assertEquals(TypeNumeric.TYPE_LONG, cdt.getTagBean().getTypeNumeric());

    cdt.clean();
    cdt.onUpdate(createValidTransferTag(1234L, Boolean.FALSE));
    assertEquals(TypeNumeric.TYPE_BOOLEAN, cdt.getTagBean().getTypeNumeric());

    cdt.clean();
    cdt.onUpdate(createValidTransferTag(1234L, Byte.valueOf((byte) 0x000A)));
    assertEquals(TypeNumeric.TYPE_BYTE, cdt.getTagBean().getTypeNumeric());

    cdt.clean();
    cdt.onUpdate(createValidTransferTag(1234L, Double.valueOf(1231231324123d)));
    assertEquals(TypeNumeric.TYPE_DOUBLE, cdt.getTagBean().getTypeNumeric());

    cdt.clean();
    cdt.onUpdate(createValidTransferTag(1234L, Integer.valueOf(123324123)));
    assertEquals(TypeNumeric.TYPE_INTEGER, cdt.getTagBean().getTypeNumeric());

    cdt.clean();
    cdt.onUpdate(createValidTransferTag(1234L, Short.valueOf((short) -123)));
    assertEquals(TypeNumeric.TYPE_SHORT, cdt.getTagBean().getTypeNumeric());

    cdt.clean();
    cdt.onUpdate(createValidTransferTag(1234L, "Test string"));
    assertEquals(TypeNumeric.TYPE_STRING, cdt.getTagBean().getTypeNumeric());

    cdt.clean();
    cdt.onUpdate(createValidTransferTag(1234L, null));
    assertEquals(TypeNumeric.TYPE_UNKNOWN, cdt.getTagBean().getTypeNumeric());
  }

//  @Test
//  public void testUpdateListenerIntialUpdate() throws CloneNotSupportedException {
//    //test setup
//    CloneableTagBean cdt = new CloneableTagBean(1234L);
//    cdt.onUpdate(createValidTransferTag(1234L));
//    BaseListener<Tag> mockUpdateListener = EasyMock.createMock(BaseListener.class);
//    mockUpdateListener.onUpdate(EasyMock.and(EasyMock.not(EasyMock.same(cdt)), EasyMock.eq(cdt)));
//
//    //run test
//    EasyMock.replay(mockUpdateListener);
//    cdt.addUpdateListener(mockUpdateListener);
//
//    //check test success
//    EasyMock.verify(mockUpdateListener);
//  }

//  @Test
//  public void testUpdateListener() {
//    final CloneableTagBean cdt = new CloneableTagBean(1234L);
//    cdt.addUpdateListener((DataTagUpdateListener) tagUpdate -> {
//      assertNotNull(tagUpdate);
//      assertEquals(cdt, tagUpdate);
//      checkTagValueCopy(cdt, tagUpdate);
//      assertFalse(cdt == tagUpdate);
//    });
//
//    cdt.onUpdate(createValidTransferTag(1234L));
//  }

  @Test
  public void testClone() throws Exception {
    CloneableTagBean cdt = new CloneableTagBean(1234L);
    CloneableTagBean clone = new CloneableTagBean(cdt.getTagBean().clone());
    checkTagCopy(cdt, clone);

    cdt.update(createValidTransferTag(1234L));
    clone = new CloneableTagBean(cdt.getTagBean().clone());
    checkTagCopy(cdt, clone);

    cdt.addUpdateListener((DataTagUpdateListener) tagUpdate -> {
      // Do nothing
    });

    clone = new CloneableTagBean(cdt.getTagBean().clone());
    checkTagCopy(cdt, clone);

    cdt.invalidate(TagQualityStatus.INACCESSIBLE, "Down");
    clone = new CloneableTagBean(cdt.getTagBean().clone());
    checkTagCopy(cdt, clone);

    cdt.getTagBean().getDataTagQuality().validate();
    clone.setTagBean(cdt.getTagBean().clone());
    checkTagCopy(cdt, clone);
  }

  @Test
  public void testEquals() throws CloneNotSupportedException {
    CloneableTagBean cdt = new CloneableTagBean(1234L);
    CloneableTagBean clone = new CloneableTagBean(cdt.getTagBean().clone());
    clone.clean();

    assertEquals(cdt.getTagBean(), clone.getTagBean());

    CloneableTagBean cdt2 = new CloneableTagBean(4321L);
    assertFalse(cdt.equals(cdt2));
  }


  @Test
  public void testXMLSerialization() throws Exception {

    CloneableTagBean cdt = new CloneableTagBean(1234L);
    cdt.onUpdate(createValidTransferTag(1234L));

    assertTrue(cdt.getTagBean().getXml().contains("<isValid>true</isValid>"));
    TagQualityStatus statusToAdd1 = TagQualityStatus.VALUE_OUT_OF_BOUNDS;
    TagQualityStatus statusToAdd2 = TagQualityStatus.INACCESSIBLE;
    cdt.getTagBean().getDataTagQuality().addInvalidStatus(statusToAdd1, "Value is over 9000!");
    cdt.getTagBean().getDataTagQuality().addInvalidStatus(statusToAdd2, "It's down!");
    assertTrue(cdt.getTagBean().getXml().contains("<isValid>false</isValid>"));
    cdt.getTagBean().getDataTagQuality().removeInvalidStatus(statusToAdd1);
    cdt.getTagBean().getDataTagQuality().removeInvalidStatus(statusToAdd2);
    assertTrue(cdt.getTagBean().getXml().contains("<isValid>true</isValid>"));
    cdt.getTagBean().getDataTagQuality().addInvalidStatus(statusToAdd1, "Value is over 9000!");
    cdt.getTagBean().getDataTagQuality().addInvalidStatus(statusToAdd2, "It's down!");
    assertTrue(cdt.getTagBean().getXml().contains("<isValid>false</isValid>"));

    TagBean cdt2 = TagBean.fromXml(cdt.getTagBean().toString());
    assertEquals(cdt.getTagBean().getId(), cdt2.getId());
  }

}
