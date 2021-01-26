/******************************************************************************
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
 *****************************************************************************/
package cern.c2mon.server.cache.datatag;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.server.cache.rule.RuleTagFacadeImpl;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.shared.common.datatag.util.TagQualityStatus;

/**
 * JUnit test of AbstractTagFacade class.
 * @author Mark Brightwell
 *
 */
public class AbstractTagFacadeTest {

  /**
   * Testing AbstractTagFacade.
   */
  private RuleTagFacadeImpl ruleTagFacade;

  @Before
  public void init() {
    ruleTagFacade = new RuleTagFacadeImpl(null, null, null, null);
  }

  /**
   * Testing filterout() method.
   */
  @Test
  public void testInvalidUpdateFilteredOut() {
    RuleTagCacheObject rule = new RuleTagCacheObject(10L);
    rule.setValue(20L);
    rule.setValueDescription("value description");
    rule.getDataTagQuality().addInvalidStatus(TagQualityStatus.VALUE_EXPIRED, "quality description");

    //should be filterout out
    assertTrue(ruleTagFacade.filterout(rule, 20L, "value description", TagQualityStatus.VALUE_EXPIRED, "quality description"));
  }

  @Test
  public void testValidUpdateNotFilteredOut() {
    RuleTagCacheObject rule = new RuleTagCacheObject(10L);
    rule.setValue(21L);
    rule.setValueDescription("value description");

    //should be filterout out
    assertFalse(ruleTagFacade.filteroutValid(rule, 20L, "value description"));
  }

  @Test
  public void testNewValueDescriptionNotFilteredOut() {
    RuleTagCacheObject rule = new RuleTagCacheObject(10L);
    rule.setValue(20L);
    rule.setValueDescription("value description");
    rule.getDataTagQuality().addInvalidStatus(TagQualityStatus.VALUE_EXPIRED, "quality description");

    //should be filterout out
    assertFalse(ruleTagFacade.filterout(rule, 20L, "new value description", TagQualityStatus.VALUE_EXPIRED, "quality description"));
  }

  @Test(expected=IllegalArgumentException.class)
  public void testFailWithOnlyQualityDescription() {
    RuleTagCacheObject rule = new RuleTagCacheObject(10L);
    rule.setValue(21L);
    rule.setValueDescription("value description");

    //should be filterout out
    assertTrue(ruleTagFacade.filterout(rule, 20L, "new value description", null, "quality description"));
  }

  /**
   * As above but with null description and quality description.
   */
  @Test
  public void testInvalidUpdateFilteredOut2() {
    RuleTagCacheObject rule = new RuleTagCacheObject(10L);
    rule.setValue(20L);
    rule.getDataTagQuality().addInvalidStatus(TagQualityStatus.VALUE_EXPIRED, null);

    //should be filterout out
    assertTrue(ruleTagFacade.filterout(rule, 20L, null, TagQualityStatus.VALUE_EXPIRED, null));
  }

  @Test
  public void testFilterOutRepeatedNull() {
    RuleTagCacheObject rule = new RuleTagCacheObject(10L);
    rule.setValue(null);
    rule.getDataTagQuality().addInvalidStatus(TagQualityStatus.VALUE_EXPIRED, null);

    //should be filterout out
    assertTrue(ruleTagFacade.filterout(rule, null, null, TagQualityStatus.VALUE_EXPIRED, null));
  }

  @Test(expected=NullPointerException.class)
  public void testFilteroutWithNullTag() {
    ruleTagFacade.filteroutValid(null, null, null);
  }

  @Test
  public void testFilterOutInvalidation() {
    RuleTagCacheObject rule = new RuleTagCacheObject(10L);
    rule.setValue(20L);
    rule.setValueDescription("value description");
    rule.getDataTagQuality().addInvalidStatus(TagQualityStatus.VALUE_EXPIRED, "quality description");

    //should be filterout out
    assertTrue(ruleTagFacade.filteroutInvalidation(rule, TagQualityStatus.VALUE_EXPIRED, "quality description"));
  }

}
