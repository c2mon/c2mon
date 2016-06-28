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


  private void checkTagValueCopy(final ClientDataTagValue original, final ClientDataTagValue copy) {
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

  private void checkTagCopy(final ClientDataTagImpl original, final ClientDataTagImpl copy) {
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
    final ClientDataTagImpl cdt = new ClientDataTagImpl(1234L);
    cdt.onUpdate(createValidTransferTag(1234L));

    Tag copy = cdt.clone();
    ((ClientDataTagImpl) copy).clean();

    assertNotSame("The two objects should not point to the same reference in memory!", cdt, copy);
    assertTrue(cdt.equals(copy));
    assertNotNull(copy.getServerTimestamp());
    assertTrue(cdt.getServerTimestamp().after(copy.getServerTimestamp()));
    assertNull(copy.getValue());
    assertEquals(0, copy.getAlarmIds().size());
    assertEquals(new Timestamp(0L), copy.getTimestamp());
    assertEquals("Tag not initialised.", copy.getDescription());
    assertFalse(copy.getDataTagQuality().isInitialised());
    assertEquals(cdt.getName(), copy.getName());
    assertEquals(cdt.getRuleExpression(), copy.getRuleExpression());
    assertNull(copy.getType());
    assertEquals(TypeNumeric.TYPE_UNKNOWN, copy.getTypeNumeric());
    assertEquals(cdt.getUnit(), copy.getUnit());
    assertNull(copy.getValue());
  }

  @Test
  public void testTypeNumeric() {
    final Tag cdt = new ClientDataTagImpl(1234L);

    ((ClientDataTagImpl) cdt).onUpdate(createValidTransferTag(1234L, Float.valueOf(1.234f)));
    assertEquals(TypeNumeric.TYPE_FLOAT, cdt.getTypeNumeric());

    ((ClientDataTagImpl) cdt).clean();
    ((ClientDataTagImpl) cdt).onUpdate(createValidTransferTag(1234L, Long.valueOf(234324L)));
    assertEquals(TypeNumeric.TYPE_LONG, cdt.getTypeNumeric());

    ((ClientDataTagImpl) cdt).clean();
    ((ClientDataTagImpl) cdt).onUpdate(createValidTransferTag(1234L, Boolean.FALSE));
    assertEquals(TypeNumeric.TYPE_BOOLEAN, cdt.getTypeNumeric());

    ((ClientDataTagImpl) cdt).clean();
    ((ClientDataTagImpl) cdt).onUpdate(createValidTransferTag(1234L, Byte.valueOf((byte) 0x000A)));
    assertEquals(TypeNumeric.TYPE_BYTE, cdt.getTypeNumeric());

    ((ClientDataTagImpl) cdt).clean();
    ((ClientDataTagImpl) cdt).onUpdate(createValidTransferTag(1234L, Double.valueOf(1231231324123d)));
    assertEquals(TypeNumeric.TYPE_DOUBLE, cdt.getTypeNumeric());

    ((ClientDataTagImpl) cdt).clean();
    ((ClientDataTagImpl) cdt).onUpdate(createValidTransferTag(1234L, Integer.valueOf(123324123)));
    assertEquals(TypeNumeric.TYPE_INTEGER, cdt.getTypeNumeric());

    ((ClientDataTagImpl) cdt).clean();
    ((ClientDataTagImpl) cdt).onUpdate(createValidTransferTag(1234L, Short.valueOf((short) -123)));
    assertEquals(TypeNumeric.TYPE_SHORT, cdt.getTypeNumeric());

    ((ClientDataTagImpl) cdt).clean();
    ((ClientDataTagImpl) cdt).onUpdate(createValidTransferTag(1234L, "Test string"));
    assertEquals(TypeNumeric.TYPE_STRING, cdt.getTypeNumeric());

    ((ClientDataTagImpl) cdt).clean();
    ((ClientDataTagImpl) cdt).onUpdate(createValidTransferTag(1234L, null));
    assertEquals(TypeNumeric.TYPE_UNKNOWN, cdt.getTypeNumeric());
  }

  @Test
  public void testUpdateListenerIntialUpdate() throws CloneNotSupportedException {
    //test setup
    ClientDataTagImpl cdt = new ClientDataTagImpl(1234L);
    ((ClientDataTagImpl) cdt).onUpdate(createValidTransferTag(1234L));
    BaseListener<Tag> mockUpdateListener = EasyMock.createMock(BaseListener.class);
    mockUpdateListener.onUpdate(EasyMock.and(EasyMock.not(EasyMock.same(cdt)), EasyMock.eq(cdt)));

    //run test
    EasyMock.replay(mockUpdateListener);
    cdt.addUpdateListener(mockUpdateListener);

    //check test success
    EasyMock.verify(mockUpdateListener);
  }

  @Test
  public void testUpdateListener() {
    final ClientDataTagImpl cdt = new ClientDataTagImpl(1234L);
    cdt.addUpdateListener(new DataTagUpdateListener() {
      @Override
      public void onUpdate(final ClientDataTagValue tagUpdate) {
        assertNotNull(tagUpdate);
        assertEquals(cdt, tagUpdate);
        checkTagValueCopy(cdt, tagUpdate);
        assertFalse(cdt == tagUpdate);
      }
    });

    ((ClientDataTagImpl) cdt).onUpdate(createValidTransferTag(1234L));
  }

  @Test
  public void testClone() throws Exception {
    ClientDataTagImpl cdt = new ClientDataTagImpl(1234L);
    ClientDataTagImpl clone = cdt.clone();
    checkTagCopy(cdt, clone);

    cdt.update(createValidTransferTag(1234L));
    clone = cdt.clone();
    checkTagCopy(cdt, clone);

    cdt.addUpdateListener(new DataTagUpdateListener() {
      @Override
      public void onUpdate(ClientDataTagValue tagUpdate) {
        // Do nothing
      }
    });

    clone = cdt.clone();
    checkTagCopy(cdt, clone);

    cdt.invalidate(TagQualityStatus.INACCESSIBLE, "Down");
    clone = cdt.clone();
    checkTagCopy(cdt, clone);

    cdt.getDataTagQuality().validate();
    clone = cdt.clone();
    checkTagCopy(cdt, clone);
  }

  @Test
  public void testEquals() throws CloneNotSupportedException {
    ClientDataTagImpl cdt = new ClientDataTagImpl(1234L);
    Tag clone = cdt.clone();
    ((ClientDataTagImpl) clone).clean();

    assertEquals(cdt, clone);

    ClientDataTagImpl cdt2 = new ClientDataTagImpl(4321L);
    assertFalse(cdt.equals(cdt2));
  }


  @Test
  public void testXMLSerialization() throws Exception {

    ClientDataTagImpl cdt = new ClientDataTagImpl(1234L);
    ((ClientDataTagImpl) cdt).onUpdate(createValidTransferTag(1234L));

    assertTrue(cdt.getXml().contains("<isValid>true</isValid>"));
    TagQualityStatus statusToAdd1 = TagQualityStatus.VALUE_OUT_OF_BOUNDS;
    TagQualityStatus statusToAdd2 = TagQualityStatus.INACCESSIBLE;
    cdt.getDataTagQuality().addInvalidStatus(statusToAdd1, "Value is over 9000!");
    cdt.getDataTagQuality().addInvalidStatus(statusToAdd2, "It's down!");
    assertTrue(cdt.getXml().contains("<isValid>false</isValid>"));
    cdt.getDataTagQuality().removeInvalidStatus(statusToAdd1);
    cdt.getDataTagQuality().removeInvalidStatus(statusToAdd2);
    assertTrue(cdt.getXml().contains("<isValid>true</isValid>"));
    cdt.getDataTagQuality().addInvalidStatus(statusToAdd1, "Value is over 9000!");
    cdt.getDataTagQuality().addInvalidStatus(statusToAdd2, "It's down!");
    assertTrue(cdt.getXml().contains("<isValid>false</isValid>"));

    ClientDataTagImpl cdt2 = ClientDataTagImpl.fromXml(cdt.toString());
    assertEquals(cdt.getId(), cdt2.getId());
  }

}
