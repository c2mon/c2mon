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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Test;

import cern.c2mon.client.common.listener.BaseListener;
import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.common.tag.TypeNumeric;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.alarm.AlarmValueImpl;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.client.tag.TransferTagImpl;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import cern.c2mon.shared.common.metadata.Metadata;

import static junit.framework.Assert.*;

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
    final TagController cdt = new TagController(1234L);
    cdt.onUpdate(createValidTransferTag(1234L));

    TagController copy = new TagController(cdt.getTagImpl().clone());

    copy.clean();

    assertNotSame("The two objects should not point to the same reference in memory!", cdt, copy);
    assertTrue(cdt.getTagImpl().equals(copy.getTagImpl()));
    assertNotNull(copy.getTagImpl().getServerTimestamp());
    assertTrue(cdt.getTagImpl().getServerTimestamp().after(copy.getTagImpl().getServerTimestamp()));
    assertNull(copy.getTagImpl().getValue());
    assertEquals(0, copy.getTagImpl().getAlarmIds().size());
    assertEquals(new Timestamp(0L), copy.getTagImpl().getTimestamp());
    assertEquals("Tag not initialised.", copy.getTagImpl().getDescription());
    assertFalse(copy.getTagImpl().getDataTagQuality().isInitialised());
    assertEquals(cdt.getTagImpl().getName(), copy.getTagImpl().getName());
    assertEquals(cdt.getTagImpl().getRuleExpression(), copy.getTagImpl().getRuleExpression());
    assertNull(copy.getTagImpl().getType());
    assertEquals(TypeNumeric.TYPE_UNKNOWN, copy.getTagImpl().getTypeNumeric());
    assertEquals(cdt.getTagImpl().getUnit(), copy.getTagImpl().getUnit());
    assertNull(copy.getTagImpl().getValue());
  }

  @Test
  public void testTypeNumeric() {
    final TagController cdt = new TagController(1234L);

    cdt.onUpdate(createValidTransferTag(1234L, Float.valueOf(1.234f)));
    assertEquals(TypeNumeric.TYPE_FLOAT, cdt.getTagImpl().getTypeNumeric());

    cdt.clean();
    cdt.onUpdate(createValidTransferTag(1234L, Long.valueOf(234324L)));
    assertEquals(TypeNumeric.TYPE_LONG, cdt.getTagImpl().getTypeNumeric());

    cdt.clean();
    cdt.onUpdate(createValidTransferTag(1234L, Boolean.FALSE));
    assertEquals(TypeNumeric.TYPE_BOOLEAN, cdt.getTagImpl().getTypeNumeric());

    cdt.clean();
    cdt.onUpdate(createValidTransferTag(1234L, Byte.valueOf((byte) 0x000A)));
    assertEquals(TypeNumeric.TYPE_BYTE, cdt.getTagImpl().getTypeNumeric());

    cdt.clean();
    cdt.onUpdate(createValidTransferTag(1234L, Double.valueOf(1231231324123d)));
    assertEquals(TypeNumeric.TYPE_DOUBLE, cdt.getTagImpl().getTypeNumeric());

    cdt.clean();
    cdt.onUpdate(createValidTransferTag(1234L, Integer.valueOf(123324123)));
    assertEquals(TypeNumeric.TYPE_INTEGER, cdt.getTagImpl().getTypeNumeric());

    cdt.clean();
    cdt.onUpdate(createValidTransferTag(1234L, Short.valueOf((short) -123)));
    assertEquals(TypeNumeric.TYPE_SHORT, cdt.getTagImpl().getTypeNumeric());

    cdt.clean();
    cdt.onUpdate(createValidTransferTag(1234L, "Test string"));
    assertEquals(TypeNumeric.TYPE_STRING, cdt.getTagImpl().getTypeNumeric());

    cdt.clean();
    cdt.onUpdate(createValidTransferTag(1234L, null));
    assertEquals(TypeNumeric.TYPE_UNKNOWN, cdt.getTagImpl().getTypeNumeric());
  }

  @Test
  public void testUpdateListenerIntialUpdate() throws CloneNotSupportedException {
    //test setup
    TagController cdt = new TagController(1234L);
    cdt.onUpdate(createValidTransferTag(1234L));
    BaseListener<Tag> mockUpdateListener = EasyMock.createMock(BaseListener.class);
    mockUpdateListener.onUpdate(EasyMock.and(EasyMock.not(EasyMock.same(cdt.getTagImpl())), EasyMock.eq(cdt.getTagImpl())));

    //run test
    EasyMock.replay(mockUpdateListener);
    cdt.addUpdateListener(mockUpdateListener);

    //check test success
    EasyMock.verify(mockUpdateListener);
  }

  @Test
  public void testUpdateListener() {
    final TagController cdt = new TagController(1234L);
    cdt.addUpdateListener(new DataTagUpdateListener() {
      @Override
      public void onUpdate(final Tag tagUpdate) {
        assertNotNull(tagUpdate);
        assertEquals(cdt.getTagImpl(), tagUpdate);
        checkTagValueCopy(cdt.getTagImpl(), tagUpdate);
        assertFalse(cdt == tagUpdate);
      }
    });

    ((TagController) cdt).onUpdate(createValidTransferTag(1234L));
  }

  @Test
  public void testClone() throws Exception {
    TagController cdt = new TagController(1234L);
    TagController clone = new TagController(cdt.getTagImpl().clone());
    checkTagCopy(cdt, clone);

    cdt.update(createValidTransferTag(1234L));
    clone = new TagController(cdt.getTagImpl().clone());
    checkTagCopy(cdt, clone);

    cdt.addUpdateListener((DataTagUpdateListener) tagUpdate -> {
      // Do nothing
    });

    clone = new TagController(cdt.getTagImpl().clone());
    checkTagCopy(cdt, clone);

    cdt.invalidate(TagQualityStatus.INACCESSIBLE, "Down");
    clone = new TagController(cdt.getTagImpl().clone());
    checkTagCopy(cdt, clone);

    cdt.getTagImpl().getDataTagQuality().validate();
    clone.setTagImpl(cdt.getTagImpl().clone());
    checkTagCopy(cdt, clone);
  }

  @Test
  public void testEquals() throws CloneNotSupportedException {
    TagController cdt = new TagController(1234L);
    TagController clone = new TagController(cdt.getTagImpl().clone());
    clone.clean();

    assertEquals(cdt.getTagImpl(), clone.getTagImpl());

    TagController cdt2 = new TagController(4321L);
    assertFalse(cdt.equals(cdt2));
  }


  @Test
  public void testXMLSerialization() throws Exception {

    TagController cdt = new TagController(1234L);
    cdt.onUpdate(createValidTransferTag(1234L));

    assertTrue(cdt.getTagImpl().getXml().contains("<isValid>true</isValid>"));
    TagQualityStatus statusToAdd1 = TagQualityStatus.VALUE_OUT_OF_BOUNDS;
    TagQualityStatus statusToAdd2 = TagQualityStatus.INACCESSIBLE;
    cdt.getTagImpl().getDataTagQuality().addInvalidStatus(statusToAdd1, "Value is over 9000!");
    cdt.getTagImpl().getDataTagQuality().addInvalidStatus(statusToAdd2, "It's down!");
    assertTrue(cdt.getTagImpl().getXml().contains("<isValid>false</isValid>"));
    cdt.getTagImpl().getDataTagQuality().removeInvalidStatus(statusToAdd1);
    cdt.getTagImpl().getDataTagQuality().removeInvalidStatus(statusToAdd2);
    assertTrue(cdt.getTagImpl().getXml().contains("<isValid>true</isValid>"));
    cdt.getTagImpl().getDataTagQuality().addInvalidStatus(statusToAdd1, "Value is over 9000!");
    cdt.getTagImpl().getDataTagQuality().addInvalidStatus(statusToAdd2, "It's down!");
    assertTrue(cdt.getTagImpl().getXml().contains("<isValid>false</isValid>"));

    TagImpl cdt2 = TagImpl.fromXml(cdt.getTagImpl().toString());
    assertEquals(cdt.getTagImpl().getId(), cdt2.getId());
  }

}
