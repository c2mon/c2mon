/*******************************************************************************
 * Copyright (C) 2010-2020 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.server.rule.evaluation;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.server.cache.CacheRegistrationService;
import cern.c2mon.server.cache.RuleTagCache;
import cern.c2mon.server.cache.RuleTagFacade;
import cern.c2mon.server.cache.TagLocationService;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.server.rule.config.RuleProperties;
import cern.c2mon.server.rule.evaluation.RuleUpdateBuffer.RuleBufferObject;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.util.TagQualityStatus;

public class RuleEvaluatorImplTest {
  private RuleEvaluatorImpl ruleEvaluator;
  private RuleTagCache ruleTagCache;
  private TagLocationService tagLocationService;
  private RuleUpdateBuffer ruleUpdateBuffer;
  
  @Before
  public void before() {
    ruleTagCache = EasyMock.createNiceMock(RuleTagCache.class);
    RuleTagFacade ruleTagFacade = EasyMock.createNiceMock(RuleTagFacade.class);
    ruleUpdateBuffer = new RuleUpdateBuffer(ruleTagFacade); 
    tagLocationService = EasyMock.createNiceMock(TagLocationService.class);
    CacheRegistrationService cacheRegistrationService = EasyMock.createNiceMock(CacheRegistrationService.class);
    RuleProperties properties = new RuleProperties();
    ruleEvaluator = new RuleEvaluatorImpl(ruleTagCache, ruleUpdateBuffer, tagLocationService, cacheRegistrationService, properties);
  }
  
  /**
   * This test checks, if the 
   */
  @Test
  public void testEvaluteRuleWithResultNull() {
    String ruleText = "((#1 != 0) | ((#2 - #3) > 20) | ((#4 - #5) > 20))";
    RuleBufferObject result = evaluteRule(ruleText);
    Assert.assertNull(result.getValue());
  }
  
  @Test
  public void testEvaluteRuleWithResultFalse() {
    String ruleText = "((#1 != 0) | ((#2 - #3) > 20) | ((#4 - #5) > 20))[true], true[false]";
    RuleBufferObject result = evaluteRule(ruleText);
    Assert.assertFalse((Boolean) result.getValue());
  }
  
  private RuleBufferObject evaluteRule(String ruleText) {
    Long ruleId = 10L;
    RuleTag rule = new RuleTagCacheObject(ruleId, "Test_rule", "java.lang.Boolean", Short.valueOf("0"), ruleText);
    
    //record
    EasyMock.expect(ruleTagCache.isWriteLockedByCurrentThread(ruleId)).andReturn(false);
    EasyMock.expect(ruleTagCache.get(ruleId)).andReturn(rule);
    EasyMock.expect(tagLocationService.get(1L)).andReturn(createDataTagCacheObject(1L, 0.0f));
    EasyMock.expect(tagLocationService.get(2L)).andReturn(createDataTagCacheObject(2L, null));
    EasyMock.expect(tagLocationService.get(3L)).andReturn(createDataTagCacheObject(3L, 70.6456f));
    EasyMock.expect(tagLocationService.get(4L)).andReturn(createDataTagCacheObject(4L, 100.234f));
    EasyMock.expect(tagLocationService.get(5L)).andReturn(createDataTagCacheObject(5L, 90.0f));
    EasyMock.replay(ruleTagCache, tagLocationService);
    
    ruleEvaluator.evaluateRule(ruleId);
    
    RuleBufferObject result = RuleUpdateBuffer.RULE_OBJECT_BUF.get(ruleId);
    Assert.assertNotNull(result);
    Assert.assertEquals("null value", result.getQualityDescriptions().values().iterator().next());
    EasyMock.verify(ruleTagCache, tagLocationService);
    
    return result;
  }
  
  private DataTagCacheObject createDataTagCacheObject(Long id, Object value) {
    String clazz = "java.lang.Float";
    if (value != null) {
      clazz = value.getClass().getName();
    }
    
    DataTagCacheObject tag = new DataTagCacheObject(id, "Tag_" + id, clazz, Short.valueOf("0"));
    tag.setValue(value);
    
    if (value == null) {
      tag.setDataTagQuality(new DataTagQualityImpl(TagQualityStatus.UNINITIALISED, "null value"));
    } else {
      tag.getDataTagQuality().validate();
    }
    
    return tag;
  }
}
