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
package cern.c2mon.server.configuration.handler.transacted;

import cern.c2mon.server.cache.*;
import cern.c2mon.server.cache.loading.DataTagLoaderDAO;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.server.test.CacheObjectCreation;
import cern.c2mon.shared.daq.config.DataTagAddressUpdate;
import cern.c2mon.shared.daq.config.DataTagUpdate;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Unit test.
 *
 * @author Mark Brightwell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = GenericApplicationContext.class)
public class DataTagConfigTransactedImplTest {

  IMocksControl control;

  // class to test
  private DataTagConfigHandler dataTagConfigTransacted;

  // mocks
  private EquipmentFacade equipmentFacade;
  private SubEquipmentFacade subEquipmentFacade;
  private RuleTagConfigHandler ruleTagConfigHandler;
  private AlarmConfigHandler alarmConfigHandler;
  private DataTagLoaderDAO dataTagLoaderDAO;
  private DataTagFacade dataTagFacade;
  private DataTagCache dataTagCache;
  private TagLocationService tagLocationService;

  @Autowired
  private GenericApplicationContext context;

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
    dataTagConfigTransacted = new DataTagConfigHandler(dataTagFacade, dataTagLoaderDAO, dataTagCache, equipmentFacade, subEquipmentFacade,
        tagLocationService, context);
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
    EasyMock.expect(dataTagCache.getCopy(dataTag.getId())).andReturn(dataTag);
    dataTagCache.putQuiet(dataTag);
    EasyMock.expectLastCall();
    EasyMock.expect(dataTagFacade.updateConfig(dataTag, new Properties())).andReturn(update);
    dataTagLoaderDAO.updateConfig(dataTag);
    dataTagCache.releaseWriteLockOnKey(dataTag.getId());

    control.replay();

    ProcessChange change = dataTagConfigTransacted.update(dataTag.getId(), new Properties());
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
    EasyMock.expect(dataTagCache.getCopy(dataTag.getId())).andReturn(dataTag);
    dataTagCache.putQuiet(dataTag);
    EasyMock.expectLastCall();
    EasyMock.expect(dataTagFacade.updateConfig(dataTag, new Properties())).andReturn(update);
    dataTagLoaderDAO.updateConfig(dataTag);
    dataTagCache.releaseWriteLockOnKey(dataTag.getId());

    control.replay();

    ProcessChange change = dataTagConfigTransacted.update(dataTag.getId(), new Properties());
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

    EasyMock.expect(dataTagCache.getCopy(dataTag.getId())).andReturn(dataTag);
    dataTagCache.putQuiet(dataTag);
    EasyMock.expectLastCall();
    EasyMock.expect(dataTagFacade.updateConfig(dataTag, properties)).andReturn(update);
    EasyMock.expect(equipmentFacade.getProcessIdForAbstractEquipment(dataTag.getEquipmentId())).andReturn(50L);

    dataTagLoaderDAO.updateConfig(dataTag);
    dataTagCache.releaseWriteLockOnKey(dataTag.getId());

    control.replay();

    ProcessChange change = dataTagConfigTransacted.update(dataTag.getId(), properties);
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

    EasyMock.expect(dataTagCache.getCopy(dataTag.getId())).andReturn(dataTag);
    dataTagCache.putQuiet(dataTag);
    EasyMock.expectLastCall();
    EasyMock.expect(dataTagFacade.updateConfig(dataTag, properties)).andReturn(update);

    dataTagLoaderDAO.updateConfig(dataTag);
    dataTagCache.releaseWriteLockOnKey(dataTag.getId());

    control.replay();

    ProcessChange change = dataTagConfigTransacted.update(dataTag.getId(), properties);
    assertFalse(change.processActionRequired());

    control.verify();
  }
}
