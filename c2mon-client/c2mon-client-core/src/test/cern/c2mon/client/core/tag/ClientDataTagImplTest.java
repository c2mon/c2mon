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
  
    cdt.update(createValidTransferTag(1234L));
    cdt.addUpdateListener(new DataTagUpdateListener() {
      @Override
      public void onUpdate(ClientDataTagValue tagUpdate) {
        // Do nothing
      }
    });
  
    ClientDataTag clone = cdt.clone();
    assertTrue(cdt.equals(clone));
    assertEquals(cdt.getServerTimestamp(), clone.getServerTimestamp());
    assertEquals(cdt.getSourceTimestamp(), clone.getSourceTimestamp());
    assertEquals(cdt.getDescription(), clone.getDescription());
    assertEquals(cdt.getDataTagQuality(), clone.getDataTagQuality());
    assertEquals(cdt.getId(), clone.getId());
    assertEquals(cdt.getName(), clone.getName());
    assertEquals(cdt.getRuleExpression(), clone.getRuleExpression());
    assertEquals(cdt.getType(), clone.getType());
    assertEquals(cdt.getTypeNumeric(), clone.getTypeNumeric());
    assertEquals(cdt.getUnit(), clone.getUnit());
    assertEquals(cdt.getValue(), clone.getValue());
    assertNotSame(cdt.getUpdateListeners().size(), clone.getUpdateListeners().size());
    assertEquals(0, clone.getUpdateListeners().size());
  }
}
