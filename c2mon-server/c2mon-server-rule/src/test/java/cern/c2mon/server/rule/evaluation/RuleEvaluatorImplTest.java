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

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.impl.SimpleCache;
import cern.c2mon.cache.config.collections.TagCacheCollection;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.server.rule.config.RuleProperties;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.util.TagQualityStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RuleEvaluatorImplTest {
    private RuleEvaluatorImpl ruleEvaluator;
    private C2monCache<DataTag> dataTagCache;
    private C2monCache<RuleTag> ruleTagCache;
    private TagCacheCollection unifiedTagCacheFacade;
    private RuleUpdateBuffer ruleUpdateBuffer;

    @Before
    public void before() {
        ruleTagCache = new SimpleCache<>("rules");
        dataTagCache = new SimpleCache<>("data");
        unifiedTagCacheFacade = new TagCacheCollection(
                new SimpleCache<>("ruleTagCache"), dataTagCache, null, null, null);
        ruleUpdateBuffer = new RuleUpdateBuffer(ruleTagCache);
        RuleProperties properties = new RuleProperties();
        ruleEvaluator = new RuleEvaluatorImpl(ruleTagCache, ruleUpdateBuffer, unifiedTagCacheFacade, properties);
    }

    /**
     * This test checks, if the
     */
    @Test
    public void testEvaluteRuleWithResultNull() {
        String ruleText = "((#1 != 0) | ((#2 - #3) > 20) | ((#4 - #5) > 20))";
        RuleBufferObject result = evaluateRule(ruleText);
        Assert.assertNull(result.getValue());
    }

    @Test
    public void testEvaluteRuleWithResultFalse() {
        String ruleText = "((#1 != 0) | ((#2 - #3) > 20) | ((#4 - #5) > 20))[true], true[false]";
        RuleBufferObject result = evaluateRule(ruleText);
        Assert.assertFalse((Boolean) result.getValue());
    }

    private RuleBufferObject evaluateRule(String ruleText) {
        Long ruleId = 10L;
        RuleTag rule = new RuleTagCacheObject(ruleId, "Test_rule", "java.lang.Boolean", Short.valueOf("0"), ruleText);
        ruleTagCache.put(ruleId, rule);

        dataTagCache.put(1L, createDataTagCacheObject(1L, 0.0f));
        dataTagCache.put(2L, createDataTagCacheObject(2L, null));
        dataTagCache.put(3L, createDataTagCacheObject(3L, 70.6456f));
        dataTagCache.put(4L, createDataTagCacheObject(4L, 100.234f));
        dataTagCache.put(5L, createDataTagCacheObject(5L, 90.0f));

        ruleEvaluator.evaluateRule(ruleId);

        RuleBufferObject result = RuleUpdateBuffer.RULE_OBJECT_BUF.get(ruleId);
        Assert.assertNotNull(result);
        Assert.assertEquals("null value", result.getQualityDescriptions().values().iterator().next());

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