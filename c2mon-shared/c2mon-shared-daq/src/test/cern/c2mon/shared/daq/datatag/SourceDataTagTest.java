package cern.c2mon.shared.daq.datatag;

import java.sql.Timestamp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.DataTagConstants;

/**
 * Unit test of SourceDataTag class.
 * 
 * @author Mark Brightwell
 *
 */
public class SourceDataTagTest {

  /**
   * Class to test.
   */
  private SourceDataTag tag;
  
  @Before
  public void init() {
    tag = new SourceDataTag(1L, "test tag", false, DataTagConstants.MODE_OPERATIONAL, "Object", new DataTagAddress());
  }
  
  /**
   * Test the timestamp and DAQ timestamp are correctly set in update method.
   */
  @Test
  public void testUpdateTimestamps() {
    Object value = new Object();
    
    Timestamp timestamp = new Timestamp(System.currentTimeMillis() - 1);    
    tag.update(value, "test value description", timestamp);
    
    Assert.assertEquals(value, tag.getCurrentValue().getValue());
    Assert.assertEquals(timestamp, tag.getCurrentValue().getTimestamp());
    //DAQ timestamp should be later
    Assert.assertNotNull(tag.getCurrentValue().getDaqTimestamp());
    Assert.assertTrue(tag.getCurrentValue().getDaqTimestamp().after(timestamp));
    
    
    //recheck with current value set
    Object newValue = new Object();
    
    Timestamp newTimestamp = new Timestamp(System.currentTimeMillis() - 1);    
    tag.update(newValue, "test value description", newTimestamp);
    
    Assert.assertEquals(newValue, tag.getCurrentValue().getValue());
    Assert.assertEquals(newTimestamp, tag.getCurrentValue().getTimestamp());
    //DAQ timestamp should be later
    Assert.assertNotNull(tag.getCurrentValue().getDaqTimestamp());
    Assert.assertTrue(tag.getCurrentValue().getDaqTimestamp().after(newTimestamp));
    Assert.assertTrue(tag.getCurrentValue().getDaqTimestamp().after(newTimestamp));
  }
  
  /**
   * Test the timestamp and DAQ timestamp are correctly set in invalidate method.
   */
  @Test
  public void testInvalidateTimestamps() {
       
    Timestamp timestamp = new Timestamp(System.currentTimeMillis() - 1);
    SourceDataQuality quality = new SourceDataQuality(SourceDataQuality.DATA_UNAVAILABLE);
    tag.invalidate(quality, timestamp);
        
    Assert.assertEquals(timestamp, tag.getCurrentValue().getTimestamp());
    //DAQ timestamp should be later
    Assert.assertNotNull(tag.getCurrentValue().getDaqTimestamp());
    Assert.assertTrue(tag.getCurrentValue().getDaqTimestamp().after(timestamp));
    
    
    //for null timestamp, src and DAQ timestamp should be the same
    tag.invalidate(quality, null);
    
    Assert.assertEquals(tag.getCurrentValue().getDaqTimestamp(), tag.getCurrentValue().getTimestamp());
  }
  
}
