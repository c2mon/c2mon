package cern.c2mon.server.configuration.handler.transacted;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.DataTagFacade;
import cern.c2mon.server.cache.EquipmentFacade;
import cern.c2mon.server.cache.SubEquipmentFacade;
import cern.c2mon.server.cache.TagLocationService;
import cern.c2mon.server.cache.loading.DataTagLoaderDAO;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.configuration.handler.AlarmConfigHandler;
import cern.c2mon.server.configuration.handler.RuleTagConfigHandler;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.server.test.CacheObjectCreation;
import cern.c2mon.shared.daq.config.DataTagAddressUpdate;
import cern.c2mon.shared.daq.config.DataTagUpdate;

/**
 * Unit test.
 *
 * @author Mark Brightwell
 *
 */
public class DataTagConfigTransactedImplTest {

  IMocksControl control;

  // class to test
  private DataTagConfigTransactedImpl dataTagConfigTransacted;

  // mocks
  private EquipmentFacade equipmentFacade;
  private SubEquipmentFacade subEquipmentFacade;
  private RuleTagConfigHandler ruleTagConfigHandler;
  private AlarmConfigHandler alarmConfigHandler;
  private DataTagLoaderDAO dataTagLoaderDAO;
  private DataTagFacade dataTagFacade;
  private DataTagCache dataTagCache;
  private TagLocationService tagLocationService;

  @Before
  public void setUp() {
    control = EasyMock.createControl();
    equipmentFacade = control.createMock(EquipmentFacade.class);
    subEquipmentFacade = control.createMock(SubEquipmentFacade.class);
    ruleTagConfigHandler = control.createMock(RuleTagConfigHandler.class);
    alarmConfigHandler = control.createMock(AlarmConfigHandler.class);
    dataTagLoaderDAO = control.createMock(DataTagLoaderDAO.class);
    dataTagFacade = control.createMock(DataTagFacade.class);
    dataTagCache = control.createMock(DataTagCache.class);
    tagLocationService = control.createMock(TagLocationService.class);
    dataTagConfigTransacted = new DataTagConfigTransactedImpl(dataTagFacade, dataTagLoaderDAO, dataTagCache, equipmentFacade, subEquipmentFacade,
        tagLocationService);
  }

  @Test
  public void testEmptyUpdateDataTag() throws IllegalAccessException {
    control.reset();

    DataTagCacheObject dataTag = CacheObjectCreation.createTestDataTag();
    // mimic the actions of the datatag facade
    DataTagUpdate update = new DataTagUpdate();
    update.setDataTagId(dataTag.getId());
    update.setEquipmentId(dataTag.getEquipmentId());
    dataTagCache.acquireWriteLockOnKey(dataTag.getId());
    EasyMock.expect(dataTagCache.get(dataTag.getId())).andReturn(dataTag);
    EasyMock.expect(dataTagFacade.updateConfig(dataTag, new Properties())).andReturn(update);
    dataTagLoaderDAO.updateConfig(dataTag);
    dataTagCache.releaseWriteLockOnKey(dataTag.getId());

    control.replay();

    ProcessChange change = dataTagConfigTransacted.doUpdateDataTag(dataTag.getId(), new Properties());
    assertTrue(!change.processActionRequired());

    control.verify();
  }

  /**
   * Tests a non-empty update gets through to DAQ.
   *
   * @throws IllegalAccessException
   */
  @Test
  public void testNotEmptyUpdateDataTag() throws IllegalAccessException {
    control.reset();

    DataTagCacheObject dataTag = CacheObjectCreation.createTestDataTag();
    // mimic the actions of the datatag facade
    DataTagUpdate update = new DataTagUpdate();
    update.setDataTagId(dataTag.getId());
    update.setEquipmentId(dataTag.getEquipmentId());
    update.setName("new name");
    dataTagCache.acquireWriteLockOnKey(dataTag.getId());
    EasyMock.expect(dataTagCache.get(dataTag.getId())).andReturn(dataTag);
    EasyMock.expect(dataTagFacade.updateConfig(dataTag, new Properties())).andReturn(update);
    dataTagLoaderDAO.updateConfig(dataTag);
    dataTagCache.releaseWriteLockOnKey(dataTag.getId());

    control.replay();

    ProcessChange change = dataTagConfigTransacted.doUpdateDataTag(dataTag.getId(), new Properties());
    assertFalse(change.processActionRequired());
    assertEquals(null, change.getProcessId());

    control.verify();
  }

  @Test
  public void testUpdateDAQRelatedPropertiesOfDataTag() throws IllegalAccessException {
    control.reset();

    DataTagCacheObject dataTag = CacheObjectCreation.createTestDataTag();

    DataTagUpdate update = new DataTagUpdate();
    update.setDataTagId(dataTag.getId());
    update.setEquipmentId(dataTag.getEquipmentId());
    update.setDataTagAddressUpdate(new DataTagAddressUpdate());

    Properties properties = new Properties();
    properties.put("address", "new address");
    properties.put("dataType", "new data type");
    properties.put("minValue", "new min val");
    properties.put("maxValue", "new max val");

    dataTagCache.acquireWriteLockOnKey(dataTag.getId());

    EasyMock.expect(dataTagCache.get(dataTag.getId())).andReturn(dataTag);
    EasyMock.expect(dataTagFacade.updateConfig(dataTag, properties)).andReturn(update);
    EasyMock.expect(equipmentFacade.getProcessIdForAbstractEquipment(dataTag.getEquipmentId())).andReturn(50L);

    dataTagLoaderDAO.updateConfig(dataTag);
    dataTagCache.releaseWriteLockOnKey(dataTag.getId());

    control.replay();

    ProcessChange change = dataTagConfigTransacted.doUpdateDataTag(dataTag.getId(), properties);
    assertTrue(change.processActionRequired());
    assertEquals(Long.valueOf(50), change.getProcessId());

    control.verify();
  }

  @Test
  public void testUpdateNonDAQRelatedPropertiesOfDataTag() throws IllegalAccessException {
    control.reset();
    DataTagCacheObject dataTag = CacheObjectCreation.createTestDataTag();

    DataTagUpdate update = new DataTagUpdate();
    update.setDataTagId(dataTag.getId());
    update.setEquipmentId(dataTag.getEquipmentId());
    update.setName("new name");

    // Update all properties that do not require DAQ reconfiguration
    Properties properties = new Properties();
    properties.put("id", dataTag.getId());
    properties.put("name", "new name");
    properties.put("description", "new description");
    properties.put("mode", "new mode");
    properties.put("isLogged", "new logged");
    properties.put("unit", "new unit");
    properties.put("equipmentId", dataTag.getEquipmentId());
    properties.put("valueDictionary", "new dict");
    properties.put("japcAddress", "new japc address");
    properties.put("dipAddress", "new dip address");

    dataTagCache.acquireWriteLockOnKey(dataTag.getId());

    EasyMock.expect(dataTagCache.get(dataTag.getId())).andReturn(dataTag);
    EasyMock.expect(dataTagFacade.updateConfig(dataTag, properties)).andReturn(update);

    dataTagLoaderDAO.updateConfig(dataTag);
    dataTagCache.releaseWriteLockOnKey(dataTag.getId());

    control.replay();

    ProcessChange change = dataTagConfigTransacted.doUpdateDataTag(dataTag.getId(), properties);
    assertFalse(change.processActionRequired());

    control.verify();
  }
}
