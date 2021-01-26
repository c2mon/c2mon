/*******************************************************************************
 * Copyright (C) 2010-2021 CERN. All rights not expressly granted are reserved.
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
 ******************************************************************************/
package cern.c2mon.server.cache.control;

import java.sql.Timestamp;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.AlarmFacade;
import cern.c2mon.server.cache.ControlTagCache;
import cern.c2mon.server.cache.datatag.DataTagCacheObjectFacade;
import cern.c2mon.server.cache.datatag.DataTagCacheObjectFacadeImpl;
import cern.c2mon.server.cache.datatag.QualityConverter;
import cern.c2mon.server.common.control.ControlTagCacheObject;
import cern.c2mon.shared.common.datatag.util.TagQualityStatus;

public class ControlTagFacadeImplTest {

  private ControlTagFacadeImpl facadeImpl;
  private ControlTagCache controlTagCache;
  
  @Before
  public void before() {
    DataTagCacheObjectFacade dataTagCacheObjectFacade = new DataTagCacheObjectFacadeImpl();
    controlTagCache = EasyMock.createMock(ControlTagCache.class);
    AlarmFacade alarmFacade = EasyMock.createNiceMock(AlarmFacade.class);
    AlarmCache alarmCache = EasyMock.createNiceMock(AlarmCache.class);
    ControlTagCacheObjectFacade controlTagCacheObjectFacade = new ControlTagCacheObjectFacadeImpl();
    QualityConverter qualityConverter = EasyMock.createNiceMock(QualityConverter.class);
    
    facadeImpl = new ControlTagFacadeImpl(dataTagCacheObjectFacade, controlTagCache, alarmFacade, alarmCache, controlTagCacheObjectFacade, qualityConverter);
  }
  
  @Test
  public void testUpdateAndValidateWithValueChange() {
    Long controlTagId = 1L;
    String valueDescription = "test value description";
    ControlTagCacheObject controlTag = createControlTagCacheObject(controlTagId, Boolean.TRUE, valueDescription);
    ControlTagCacheObject result = createControlTagCacheObject(controlTagId, Boolean.FALSE, valueDescription);
    
    // Record mock
    runTestForUpdateAndValidate(controlTag, result);
  }
  
  @Test
  public void testUpdateAndValidateWithDescriptionChange() {
    Long controlTagId = 1L;
    String valueDescription = "test value description";
    ControlTagCacheObject controlTag = createControlTagCacheObject(controlTagId, Boolean.TRUE, valueDescription);
    ControlTagCacheObject result = createControlTagCacheObject(controlTagId, Boolean.TRUE, "result description");
    
    // Record mock
    runTestForUpdateAndValidate(controlTag, result);
  }
  
  @Test
  public void testUpdateAndValidateWithQualityChange() {
    Long controlTagId = 1L;
    String valueDescription = "test value description";
    ControlTagCacheObject controlTag = createControlTagCacheObject(controlTagId, Boolean.TRUE, valueDescription);
    controlTag.getDataTagQuality().addInvalidStatus(TagQualityStatus.EQUIPMENT_DOWN);
    ControlTagCacheObject result = createControlTagCacheObject(controlTagId, controlTag.getValue(), valueDescription);
    
    // Record mock
    runTestForUpdateAndValidate(controlTag, result);
  }
  
  /**
   * This test assures simply that {@link #runTestForUpdateAndValidate(ControlTagCacheObject, ControlTagCacheObject)} 
   * throws an {@link AssertionError} when no change is applied. 
   */
  @Test(expected = AssertionError.class)
  public void testUpdateAndValidateWithNoChange() {
    Long controlTagId = 1L;
    String valueDescription = "test value description";
    ControlTagCacheObject controlTag = createControlTagCacheObject(controlTagId, Boolean.TRUE, valueDescription);
    
    // Record mock
    runTestForUpdateAndValidate(controlTag, controlTag);
  }
  
  @Test
  public void testUpdateAndValidateFilterout() {
    Long controlTagId = 1L;
    String valueDescription = "test value description";
    ControlTagCacheObject controlTag = createControlTagCacheObject(controlTagId, Boolean.TRUE, valueDescription);
    
    // Record mock
    EasyMock.expect(controlTagCache.hasKey(controlTagId)).andReturn(Boolean.TRUE);
    controlTagCache.acquireWriteLockOnKey(controlTagId);
    EasyMock.expect(controlTagCache.get(controlTagId)).andReturn(controlTag);
    controlTagCache.releaseWriteLockOnKey(controlTagId);
    
    // Replay mock
    EasyMock.replay(controlTagCache);
    
    facadeImpl.updateAndValidate(controlTagId, controlTag.getValue(), controlTag.getValueDescription(), new Timestamp(System.currentTimeMillis()));
    EasyMock.verify(controlTagCache);
  }
  
  private void runTestForUpdateAndValidate(ControlTagCacheObject cacheObject, ControlTagCacheObject resultObject) {
    // Record mock
    EasyMock.expect(controlTagCache.hasKey(cacheObject.getId())).andReturn(Boolean.TRUE);
    controlTagCache.acquireWriteLockOnKey(cacheObject.getId());
    EasyMock.expect(controlTagCache.get(cacheObject.getId())).andReturn(cacheObject);
    controlTagCache.put(cacheObject.getId(), resultObject);
    controlTagCache.releaseWriteLockOnKey(cacheObject.getId());
    
    // Replay mock
    EasyMock.replay(controlTagCache);
    
    facadeImpl.updateAndValidate(resultObject.getId(), resultObject.getValue(), resultObject.getValueDescription(), new Timestamp(System.currentTimeMillis()));
    EasyMock.verify(controlTagCache);
  }
  
  private ControlTagCacheObject createControlTagCacheObject(Long id, Object value, String valueDescription) {
    ControlTagCacheObject controlTag = new ControlTagCacheObjectComparator(id);
    controlTag.setValue(value);
    controlTag.setValueDescription(valueDescription);
    controlTag.getDataTagQuality().validate();
    return controlTag;
  }
}
