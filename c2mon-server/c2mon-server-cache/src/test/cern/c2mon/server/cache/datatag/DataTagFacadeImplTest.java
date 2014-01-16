package cern.c2mon.server.cache.datatag;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.Properties;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.easymock.internal.LastControl;
import org.junit.Before;
import org.junit.Test;
import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.AlarmFacade;
import cern.c2mon.server.cache.ControlTagCache;
import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.DataTagFacade;
import cern.c2mon.server.cache.EquipmentFacade;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.test.CacheObjectCreation;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import cern.c2mon.shared.daq.datatag.SourceDataQuality;
import cern.c2mon.shared.daq.datatag.SourceDataTagValue;

/**
 * JUnit class for unit testing the DataTagFacade implementation. Also instantiates
 * the DataTagCacheObject facade and quality converter. The rest is mocked.
 * 
 * @author Mark Brightwell
 *
 */
public class DataTagFacadeImplTest {

  /**
   * Tested.
   */
  private DataTagFacade dataTagFacade;
  private DataTagCacheObjectFacade dataTagCacheObjectFacade;
  private QualityConverter qualityConverter;
  
  /**
   * Mocks.
   */
  private DataTagCache dataTagCache;
  private AlarmFacade alarmFacade;
  private AlarmCache alarmCache;
  private EquipmentFacade equipmentFacade;
  
  private IMocksControl control = EasyMock.createControl();
  
  /**
   * Initialize mocks and DataTagFacade.
   */
  @Before
  public void setUp() {   
   
   dataTagCache = control.createMock(DataTagCache.class);   
   alarmFacade = control.createMock(AlarmFacade.class);
   alarmCache = control.createMock(AlarmCache.class);
   equipmentFacade = control.createMock(EquipmentFacade.class);
   dataTagCacheObjectFacade = new DataTagCacheObjectFacadeImpl();
   qualityConverter = new QualityConverterImpl();
   dataTagFacade = new DataTagFacadeImpl(dataTagCacheObjectFacade, dataTagCache, qualityConverter,
                                           alarmFacade, alarmCache, equipmentFacade);    
  }
  
  /**
   * Null valid updates should result in invalidation of Tag with UNKNOWN_REASON flag.
   * Source & DAQ timestamps are NOT updated (correspond to value still). Cache time
   * is updated.
   */
  @Test
  public void testSourceNullValidUpdate() {    
    SourceDataTagValue sourceTag = new SourceDataTagValue(2L, "test tag", false); //has null value
    Timestamp newTime = new Timestamp(System.currentTimeMillis() + 1000);
    sourceTag.setTimestamp(newTime);
    
    DataTagCacheObject dataTag = new DataTagCacheObject(2L, "test name", "Float", DataTagConstants.MODE_OPERATIONAL);
    Timestamp oldTime = new Timestamp(System.currentTimeMillis() - 1000);
    dataTag.setSourceTimestamp(oldTime);
    dataTag.setDaqTimestamp(oldTime);
    dataTag.setValue("value not changed");
    dataTag.getDataTagQuality().validate();
    dataTag.setCacheTimestamp(oldTime);    
    
    assertTrue(sourceTag.getValue() == null);
    assertTrue(sourceTag.isValid());    
    
    recordUpdateFromSourceMock(dataTag);
    
    control.replay();
    
    boolean updated = dataTagFacade.updateFromSource(dataTag.getId(), sourceTag).getReturnValue();
    
    assertTrue(updated);
    
    //is invalid
    assertTrue(!dataTag.isValid());
    assertTrue(dataTag.getDataTagQuality().isInvalidStatusSet(TagQualityStatus.UNKNOWN_REASON));
    
    //only cache timestamp has changed
    assertEquals(oldTime, dataTag.getSourceTimestamp());
    assertEquals(oldTime, dataTag.getDaqTimestamp());
    assertTrue(!dataTag.getCacheTimestamp().equals(oldTime)); //cache time has been updated
    
    //value is not updated
    assertEquals("value not changed", dataTag.getValue());
    
    control.verify();
  }
  
  /**
   * Private helper method to record all mock calls that are needed to the DataTagCache
   * when calling {@link DataTagFacade#updateFromSource(Long, SourceDataTagValue)}
   * @param dataTag
   */
  private final void recordUpdateFromSourceMock(final DataTag dataTag) {
    recordUpdateFromSourceMock(dataTag, true);
  }
  
  /**
   * Private helper method to record all mock calls that are needed to the DataTagCache
   * when calling {@link DataTagFacade#updateFromSource(Long, SourceDataTagValue)}
   * @param dataTag
   */
  private final void recordUpdateFromSourceMock(final DataTag dataTag, boolean expectNotifyListeners) {
    dataTagCache.acquireWriteLockOnKey(dataTag.getId());
    EasyMock.expectLastCall().times(2);
    EasyMock.expect(dataTagCache.get(dataTag.getId())).andReturn(dataTag);
    if (expectNotifyListeners) {
      dataTagCache.notifyListenersOfUpdate(EasyMock.isA(DataTag.class));
    }
    dataTagCache.releaseWriteLockOnKey(dataTag.getId());
    EasyMock.expectLastCall().times(2);
  }
  
  /**
   * Null invalid updates should result in invalidation of Tag with converted quality flag, but
   * the source & daq timestamp should remain unchanged (so as to remain coherent with the 
   * value, even if the quality is now adjusted). Cache time is updated.
   */
  @Test
  public void testSourceNullInvalidUpdate() {    
    SourceDataTagValue sourceTag = new SourceDataTagValue(2L, "test tag", false); //has null value
    sourceTag.setQuality(new SourceDataQuality(SourceDataQuality.OUT_OF_BOUNDS));
    Timestamp newTime = new Timestamp(System.currentTimeMillis() + 1000);
    sourceTag.setTimestamp(newTime);
    
    DataTagCacheObject dataTag = new DataTagCacheObject(2L, "test name", "Float", DataTagConstants.MODE_OPERATIONAL);
    dataTag.setValue("value not changed");
    dataTag.getDataTagQuality().validate();
    Timestamp oldTime = new Timestamp(System.currentTimeMillis() - 1000);
    dataTag.setSourceTimestamp(oldTime);
    dataTag.setDaqTimestamp(oldTime);
    dataTag.setCacheTimestamp(oldTime);    
    
    assertTrue(sourceTag.getValue() == null);
    assertTrue(!sourceTag.isValid());
    
    recordUpdateFromSourceMock(dataTag);
    
    control.replay();
    
    boolean updated = dataTagFacade.updateFromSource(dataTag.getId(), sourceTag).getReturnValue();
    
    assertTrue(updated);
    
    //is invalid
    assertTrue(!dataTag.isValid());
    assertTrue(dataTag.getDataTagQuality().isInvalidStatusSet(TagQualityStatus.VALUE_OUT_OF_BOUNDS));
    assertTrue(!dataTag.getDataTagQuality().isInvalidStatusSet(TagQualityStatus.UNKNOWN_REASON));
        
    //only cache timestamp has changed
    assertEquals(oldTime, dataTag.getSourceTimestamp());
    assertEquals(oldTime, dataTag.getDaqTimestamp());
    assertTrue(!dataTag.getCacheTimestamp().equals(oldTime)); //cache time has been updated
    
    //value is not updated
    assertEquals("value not changed", dataTag.getValue());
    
    control.verify();
  }
  
  /**
   * DataTag should never be null here, but just in case...
   */
  @Test
  public void testNoFailureOnNullTag() {
    SourceDataTagValue sourceTag = new SourceDataTagValue(Long.valueOf(2), "test tag", false);
    
    control.replay();
    
    //when fed with null, log error but do not crash
    boolean updated = dataTagFacade.updateFromSource(null, sourceTag).getReturnValue();
    assertTrue(!updated);
    
    control.verify(); //no listener notification
  }
  
  /**
   * SourceDataTagValue should never be null here, but just in case...
   */
  @Test
  public void testNoFailureOnNullSource() {
    DataTagCacheObject dataTag = new DataTagCacheObject(Long.valueOf(2), "test name", "Float", DataTagConstants.MODE_OPERATIONAL);
    
    recordUpdateFromSourceMock(dataTag, false);
    control.replay();
    
    boolean updated = dataTagFacade.updateFromSource(2L, null).getReturnValue();
    assertTrue(!updated);
    
    control.verify(); //no listener notification
  }
  
  @Test
  public void testValidUpdateMinimalFields() {    
    SourceDataTagValue sourceTag = new SourceDataTagValue(Long.valueOf(2), "test tag", false); 
    sourceTag.setValue(Float.valueOf(1));
    Timestamp sourceDaqTime = new Timestamp(System.currentTimeMillis() - 1000); //reset to compare
    sourceTag.setDaqTimestamp(sourceDaqTime);
    
    //src, DAQ timestamps are null, cache t.s. is not null
    DataTagCacheObject dataTag = new DataTagCacheObject(Long.valueOf(2), "test name", "Float", DataTagConstants.MODE_OPERATIONAL); 
    Timestamp cacheTime = new Timestamp(System.currentTimeMillis() - 1000); //reset to compare
    
    dataTagCache.acquireWriteLockOnKey(dataTag.getId());
    EasyMock.expectLastCall().times(2);
    EasyMock.expect(dataTagCache.get(dataTag.getId())).andReturn(dataTag);
    dataTagCache.notifyListenersOfUpdate(EasyMock.isA(DataTag.class));
    dataTagCache.releaseWriteLockOnKey(dataTag.getId());
    EasyMock.expectLastCall().times(2);
    
    control.replay();
    
    boolean updated = dataTagFacade.updateFromSource(dataTag.getId(), sourceTag).getReturnValue();
    
    assertTrue(updated);
    
    assertTrue(dataTag.isValid());
    assertEquals(1f, dataTag.getValue());
    assertTrue(dataTag.getSourceTimestamp() == null); //no source time was set (currently DAQ sets this anyway to DAQ time)
    assertEquals(sourceDaqTime, dataTag.getDaqTimestamp());
    assertTrue(!dataTag.getCacheTimestamp().equals(cacheTime)); //cache time has been updated
    
    control.verify();
  }
  
  /**
   * As the previous test, but all SourceDataTagValue fields are now set.
   */
  @Test
  public void testValidUpdateAllFieldsSet() {    
    Timestamp sourceTime = new Timestamp(System.currentTimeMillis() - 1000);
    SourceDataTagValue sourceTag = new SourceDataTagValue(2L, "tag name", false, "new value", new SourceDataQuality(), sourceTime, 
                    DataTagConstants.PRIORITY_HIGH, false, "value desc", DataTagConstants.TTL_FOREVER); 
    
    //src, DAQ timestamps are null, cache t.s. is not null
    DataTagCacheObject dataTag = new DataTagCacheObject(2L, "test name", "String", DataTagConstants.MODE_OPERATIONAL); 
    Timestamp cacheTime = new Timestamp(System.currentTimeMillis() - 1000); //reset to compare
    
    recordUpdateFromSourceMock(dataTag);
    
    control.replay();
    
    boolean updated = dataTagFacade.updateFromSource(dataTag.getId(), sourceTag).getReturnValue();
    
    assertTrue(updated);
    
    assertTrue(dataTag.isValid());
    assertEquals("new value", dataTag.getValue());
    assertEquals(sourceTime, dataTag.getSourceTimestamp());
    assertNotNull(dataTag.getDaqTimestamp()); //set in constructor
    assertTrue(!dataTag.getCacheTimestamp().equals(cacheTime)); //cache time has been updated
    assertEquals("value desc", dataTag.getValueDescription());
    
    control.verify();
  }
  
  
  @Test
  public void testUpdateFromSource() {
   SourceDataTagValue sourceTag = new SourceDataTagValue(Long.valueOf(2), "test tag", false);
   DataTagCacheObject dataTag = new DataTagCacheObject(Long.valueOf(2), "test name", "Float", DataTagConstants.MODE_OPERATIONAL);    
 
   recordUpdateFromSourceMock(dataTag);
   control.replay();
   
   //3 value of sourceTag is null, updated is true since the tag is invalidated
   boolean updated  = dataTagFacade.updateFromSource(dataTag.getId(), sourceTag).getReturnValue();
   assertTrue(updated);
   control.verify();
   control.reset();
   
   //set source value:
   sourceTag.setValue(Float.valueOf(1));
   
   //4 filter out if older incoming DAQ timstamp
   long currentMillis = System.currentTimeMillis();
   Timestamp newTimestamp = new Timestamp(currentMillis);
   Timestamp oldTimestamp = new Timestamp(currentMillis - 100);
   dataTag.setSourceTimestamp(oldTimestamp);
   dataTag.setDaqTimestamp(newTimestamp);
   sourceTag.setTimestamp(newTimestamp);
   sourceTag.setDaqTimestamp(oldTimestamp);
   
   recordUpdateFromSourceMock(dataTag, false);
   control.replay();
   
   updated  = dataTagFacade.updateFromSource(dataTag.getId(), sourceTag).getReturnValue();
   assertTrue(!updated);
   control.verify();
   control.reset();
   
   //4.1 if incoming source timestamp is older, should not be filtered out    
   dataTag.setSourceTimestamp(newTimestamp);
   dataTag.setDaqTimestamp(oldTimestamp);
   sourceTag.setTimestamp(oldTimestamp);
   sourceTag.setDaqTimestamp(newTimestamp);
   
   recordUpdateFromSourceMock(dataTag);
   control.replay();
   
   updated  = dataTagFacade.updateFromSource(dataTag.getId(), sourceTag).getReturnValue();
   assertTrue(updated);
   
   control.verify();
   control.reset();
   
   //4 if INACCESSIBLE the update should still take place
//   dataTag.setDataTagQuality(new DataTagQualityImpl(TagQualityStatus.PROCESS_DOWN));
//   updated = dataTagFacade.updateFromSource(dataTag, sourceTag);
//   assertTrue(updated);
   
   //5 if the timestamps are the same and the value is the same (and both are valid), no update should take place
   //make sure value is set correctly
   dataTag = new DataTagCacheObject(Long.valueOf(2), "test name", "Float", DataTagConstants.MODE_OPERATIONAL);
   dataTag.setValue(Float.valueOf(2));
   DataTagQuality dataTagQuality = new DataTagQualityImpl();
   dataTagQuality.validate();
   dataTag.setDataTagQuality(dataTagQuality);
   sourceTag = new SourceDataTagValue(Long.valueOf(2), "test tag", false);
   sourceTag.setValue(Float.valueOf(2));
   sourceTag.setQuality(new SourceDataQuality(SourceDataQuality.OK));
   Timestamp identicalTimestamp = new Timestamp(currentMillis);   
   dataTag.setSourceTimestamp(identicalTimestamp);
   sourceTag.setTimestamp(identicalTimestamp);
   
   recordUpdateFromSourceMock(dataTag, false);
   control.replay();
   
   updated = dataTagFacade.updateFromSource(dataTag.getId(), sourceTag).getReturnValue();
   assertTrue(!updated);
   control.verify();
   control.reset();
   
   //6 if the values are different the update should take place (same timestamps)
   sourceTag.setValue(Long.valueOf(1));
   recordUpdateFromSourceMock(dataTag);
   control.replay();
   updated = dataTagFacade.updateFromSource(dataTag.getId(), sourceTag).getReturnValue();
   assertTrue(updated);
   control.verify();
  }
  
  @Test
  public void testCreateCacheObject() throws IllegalAccessException {
    Properties properties = new Properties();
    properties.put("minValue", 1);
    properties.put("maxValue", 20);
    properties.put("name", "tag_name");
    properties.put("dataType", "String");
    properties.put("mode", 0);
    properties.put("equipmentId", "20");
    properties.put("address", "<DataTagAddress><HardwareAddress class=\"cern.c2mon.shared.common.datatag.address.impl.JAPCHardwareAddressImpl\"><protocol>yami</protocol><service>yami</service><device-name>TEST.CLIC.DIAMON.1</device-name><property-name>Acquisition</property-name><data-field-name>sys.mem.inactpct</data-field-name><column-index>-1</column-index><row-index>-1</row-index></HardwareAddress><time-to-live>3600000</time-to-live><priority>2</priority><guaranteed-delivery>false</guaranteed-delivery></DataTagAddress>");
    
    dataTagCache.acquireReadLockOnKey(10L);
    dataTagCache.acquireWriteLockOnKey(10L);
    EasyMock.expect(equipmentFacade.getProcessIdForAbstractEquipment(20L)).andReturn(2L);
    dataTagCache.releaseReadLockOnKey(10L);
    dataTagCache.releaseWriteLockOnKey(10L);
    
    EasyMock.replay(equipmentFacade);
    
    DataTag tag = dataTagFacade.createCacheObject(10L, properties);
    //is default as not in Spring context
    assertEquals("c2mon.client.tag.default" + "." + tag.getProcessId(), tag.getTopic());    
    EasyMock.verify(equipmentFacade);
  }
  
  /**
   * Tests setting certain fields to null is possible using the configureCacheObject(Properties)
   * method.
   * @throws IllegalAccessException 
   */
  @Test
  public void testConfigureCacheObjectNullFields() throws IllegalAccessException {
    DataTag dataTag = CacheObjectCreation.createTestDataTag();
    Properties properties = new Properties();
    //these fields should be set to null
    properties.put("minValue", "null");
    properties.put("maxValue", "null");
    properties.put("japcAddress", "null");
    properties.put("dipAddress", "null");
    
    //these fields do not implement this, so "null" as String is used
    properties.put("name", "null");
    
    dataTagFacade.configureCacheObject(dataTag, properties);
    
    assertTrue(dataTag.getMinValue() == null);
    assertTrue(dataTag.getMaxValue() == null);
    assertTrue(dataTag.getJapcAddress() == null);
    assertTrue(dataTag.getDipAddress() == null);
    
    assertEquals("null", dataTag.getName());
  }
  
  @Test(expected=ConfigurationException.class)
  public void testFailCreateCacheObject() throws IllegalAccessException {
    Properties properties = new Properties();
    dataTagFacade.createCacheObject(10L, properties);
  }
  
  
}
