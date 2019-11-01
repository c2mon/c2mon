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
package cern.c2mon.server.cache.common;

import cern.c2mon.server.cache.AbstractCacheIntegrationTest;
import cern.c2mon.server.cache.TagFacadeGateway;
import cern.c2mon.server.cache.rule.RuleTagCacheTest;
import cern.c2mon.server.common.alarm.TagWithAlarms;
import cern.c2mon.server.common.rule.RuleTag;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

public class TagFacadeGatewayImplTest extends AbstractCacheIntegrationTest {

  @Autowired
  private TagFacadeGateway tagFacadeGateway;

  /**
   * @see RuleTagCacheTest#testSearchWithNameWildcard()
   */
  @Test
  public void testGetTagsWithAlarms() {
    String regex = "DIAMON_clic_CS-CCR-*";
    Collection<TagWithAlarms> tagsWithAlarms = tagFacadeGateway.getTagsWithAlarms(regex);
    assertNotNull(tagsWithAlarms);
    assertEquals(11, tagsWithAlarms.size());
    for (TagWithAlarms ruleTag : tagsWithAlarms) {
      assertTrue(ruleTag.getTag().getName().toLowerCase().startsWith(regex.substring(0, regex.lastIndexOf('*')).toLowerCase()));
      assertTrue(ruleTag.getTag() instanceof RuleTag);
    }
  }

 @Test
 public void testGetKeys() {
    //IDs from c2mon-server-test/src/resources/sql/cache-data-insert.sql
    List<Long> expectedResult = Arrays.asList(1205L, 1260L, 1224L, 1263L, 210007L, 1221L, 1252L, 1262L, 210004L, 210005L, 1200L, 1250L, 1231L, 210006L, 1230L, 1220L, 1222L, 1223L, 1261L, 1240L, 1251L, 1241L, 1232L, 200002L, 200010L, 210002L, 210001L, 200011L, 210003L, 210008L, 200001L, 200000L, 210009L, 210010L, 200005L, 200012L, 210000L, 200004L, 200003L, 60007L, 60005L, 60010L, 59999L, 60011L, 60004L, 60003L, 60001L, 60012L, 60002L, 60000L, 60008L, 60009L, 60006L);
    assertEquals("There should be " + expectedResult.size() + " keys", expectedResult.size(), this.tagFacadeGateway.getKeys().size());
    assertTrue("Some keys are missing", this.tagFacadeGateway.getKeys().containsAll(expectedResult));
 }
}
