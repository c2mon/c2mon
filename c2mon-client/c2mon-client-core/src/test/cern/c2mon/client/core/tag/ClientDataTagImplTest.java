package cern.c2mon.client.core.tag;

import static junit.framework.Assert.*;

import java.sql.Timestamp;

import org.junit.Test;

import cern.c2mon.client.core.listener.DataTagUpdateListener;
import cern.c2mon.client.core.tag.ClientDataTag;
import cern.c2mon.client.core.tag.ClientDataTagImpl;
import cern.c2mon.client.core.tag.ClientDataTagValue;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.client.tag.TransferTagImpl;
import cern.tim.shared.common.datatag.DataTagQuality;
import cern.tim.shared.common.datatag.DataTagQualityImpl;

public class ClientDataTagImplTest {

  private TagUpdate createValidTransferTag(final Long tagId) {
    DataTagQuality tagQuality = new DataTagQualityImpl();
    tagQuality.validate();
    TagUpdate tagUpdate = 
      new TransferTagImpl(
          tagId,
          Float.valueOf(1.23f),
          tagQuality,
          new Timestamp(System.currentTimeMillis() - 10000L),
          new Timestamp(System.currentTimeMillis()),
          "Test description",
          "My.data.tag.name",
          "My.jms.topic");
    
    return tagUpdate;
  }
  
  @Test
  public void testClone() throws Exception {
    ClientDataTag cdt = new ClientDataTagImpl(1234L);
    ClientDataTag clone = cdt.clone();
    checkCopy(cdt, clone);
    
    cdt.update(createValidTransferTag(1234L));
    clone = cdt.clone();
    checkCopy(cdt, clone);
    
    cdt.addUpdateListener(new DataTagUpdateListener() {
      @Override
      public void onUpdate(ClientDataTagValue tagUpdate) {
        // Do nothing
      }
    });
  
    clone = cdt.clone();
    checkCopy(cdt, clone);
    
    cdt.invalidate("Down");
    clone = cdt.clone();
    checkCopy(cdt, clone);
    
    cdt.getDataTagQuality().validate();
    clone = cdt.clone();
    checkCopy(cdt, clone);
  }
  
  private void checkCopy(final ClientDataTag original, final ClientDataTag copy) {
    assertTrue(original.equals(copy));
    assertEquals(original.getServerTimestamp(), copy.getServerTimestamp());
    assertEquals(original.getSourceTimestamp(), copy.getSourceTimestamp());
    assertEquals(original.getDescription(), copy.getDescription());
    assertEquals(original.getDataTagQuality(), copy.getDataTagQuality());
    assertEquals(original.getId(), copy.getId());
    assertEquals(original.getName(), copy.getName());
    assertEquals(original.getRuleExpression(), copy.getRuleExpression());
    assertEquals(original.getType(), copy.getType());
    assertEquals(original.getTypeNumeric(), copy.getTypeNumeric());
    assertEquals(original.getUnit(), copy.getUnit());
    assertEquals(original.getValue(), copy.getValue());
    if (original.getUpdateListeners().isEmpty()) {
      assertEquals(original.getUpdateListeners().size(), copy.getUpdateListeners().size());
    }
    else {
      assertNotSame(original.getUpdateListeners().size(), copy.getUpdateListeners().size());
    }
    assertEquals(0, copy.getUpdateListeners().size());
  }
}
