package cern.c2mon.client.core.tag;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.sql.Timestamp;

import org.easymock.EasyMock;
import org.junit.Test;

import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.ClientDataTag;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.common.tag.TypeNumeric;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.client.tag.TransferTagImpl;
import cern.tim.shared.common.datatag.DataTagQuality;
import cern.tim.shared.common.datatag.DataTagQualityImpl;
import cern.tim.shared.common.datatag.TagQualityStatus;

public class ClientDataTagImplTest {

  private TagUpdate createValidTransferTag(final Long tagId) {
    return createValidTransferTag(tagId, Float.valueOf(1.234f));
  }
  
  private TagUpdate createValidTransferTag(final Long tagId, Object value) {
    DataTagQuality tagQuality = new DataTagQualityImpl();
    tagQuality.validate();
    TagUpdate tagUpdate = 
      new TransferTagImpl(
          tagId,
          value,
          tagQuality,
          TagMode.TEST,
          new Timestamp(System.currentTimeMillis() - 10000L),
          new Timestamp(System.currentTimeMillis()),
          "Test description",
          "My.data.tag.name",
          "My.jms.topic");
    
    return tagUpdate;
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
  }
  
  private void checkTagCopy(final ClientDataTagImpl original, final ClientDataTagImpl copy) {
    checkTagValueCopy(original, copy);
    if (original.getUpdateListeners().isEmpty()) {
      assertEquals(original.getUpdateListeners().size(), copy.getUpdateListeners().size());
    }
    else {
      assertNotSame(original.getUpdateListeners().size(), copy.getUpdateListeners().size());
    }
    assertEquals(0, copy.getUpdateListeners().size());
  }
  
  @Test
  public void testClean() throws CloneNotSupportedException {
    final ClientDataTagImpl cdt = new ClientDataTagImpl(1234L);
    cdt.onUpdate(createValidTransferTag(1234L));
    
    ClientDataTag copy = cdt.clone();
    copy.clean();
    
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
    final ClientDataTag cdt = new ClientDataTagImpl(1234L);
    
    cdt.onUpdate(createValidTransferTag(1234L, Float.valueOf(1.234f)));
    assertEquals(TypeNumeric.TYPE_FLOAT, cdt.getTypeNumeric());
    
    cdt.clean();
    cdt.onUpdate(createValidTransferTag(1234L, Long.valueOf(234324L)));
    assertEquals(TypeNumeric.TYPE_LONG, cdt.getTypeNumeric());
    
    cdt.clean();
    cdt.onUpdate(createValidTransferTag(1234L, Boolean.FALSE));
    assertEquals(TypeNumeric.TYPE_BOOLEAN, cdt.getTypeNumeric());
    
    cdt.clean();
    cdt.onUpdate(createValidTransferTag(1234L, Byte.valueOf((byte) 0x000A)));
    assertEquals(TypeNumeric.TYPE_BYTE, cdt.getTypeNumeric());
    
    cdt.clean();
    cdt.onUpdate(createValidTransferTag(1234L, Double.valueOf(1231231324123d)));
    assertEquals(TypeNumeric.TYPE_DOUBLE, cdt.getTypeNumeric());
    
    cdt.clean();
    cdt.onUpdate(createValidTransferTag(1234L, Integer.valueOf(123324123)));
    assertEquals(TypeNumeric.TYPE_INTEGER, cdt.getTypeNumeric());
    
    cdt.clean();
    cdt.onUpdate(createValidTransferTag(1234L, Short.valueOf((short) -123)));
    assertEquals(TypeNumeric.TYPE_SHORT, cdt.getTypeNumeric());
    
    cdt.clean();
    cdt.onUpdate(createValidTransferTag(1234L, "Test string"));
    assertEquals(TypeNumeric.TYPE_STRING, cdt.getTypeNumeric());
    
    cdt.clean();
    cdt.onUpdate(createValidTransferTag(1234L, new Object()));
    assertEquals(TypeNumeric.TYPE_UNKNOWN, cdt.getTypeNumeric());
    
    cdt.clean();
    cdt.onUpdate(createValidTransferTag(1234L, null));
    assertEquals(TypeNumeric.TYPE_UNKNOWN, cdt.getTypeNumeric());
  }
  
  @Test
  public void testUpdateListenerIntialUpdate() throws CloneNotSupportedException {
    //test setup
    ClientDataTagImpl cdt = new ClientDataTagImpl(1234L);
    cdt.onUpdate(createValidTransferTag(1234L));
    DataTagUpdateListener mockUpdateListener = EasyMock.createMock(DataTagUpdateListener.class);
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
      }
    });

    cdt.onUpdate(createValidTransferTag(1234L));
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
    ClientDataTag clone = cdt.clone();
    clone.clean();
    
    assertEquals(cdt, clone);
    
    ClientDataTagImpl cdt2 = new ClientDataTagImpl(4321L);
    assertFalse(cdt.equals(cdt2));
  }
}
