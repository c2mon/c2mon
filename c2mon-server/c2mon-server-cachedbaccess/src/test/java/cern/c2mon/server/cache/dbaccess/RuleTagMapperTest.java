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
package cern.c2mon.server.cache.dbaccess;

import cern.c2mon.server.cache.dbaccess.structure.DBBatch;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.util.TagQualityStatus;

import org.junit.Rule;
import org.junit.Test;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.Assert.*;

public class RuleTagMapperTest extends AbstractMapperTest {

  @Inject
  private RuleTagMapper ruleTagMapper;

  @Test
  public void testInsertCompletes() {
    RuleTagCacheObject ruleTag = createTestRuleTag();
    ruleTagMapper.insertRuleTag(ruleTag);
    ruleTagMapper.deleteRuleTag(ruleTag.getId());
  }


  @Test
  public void testGetAllControlTags() {
    assertNotNull(ruleTagMapper);
    List<RuleTag> allList = ruleTagMapper.getAll();
    assertTrue(allList.size() != 0);
  }

  @Test
  public void testGetNumberItems() {
    assertTrue(ruleTagMapper.getNumberItems() > 5);
  }

  @Test
  /**
   * Need 25 rules in the DATATAG table.
   */
  public void testGetRowBatch() {
    DBBatch dbBatch = new DBBatch(1L, 7L);
    List<RuleTag> ruletags = ruleTagMapper.getRowBatch(dbBatch);
    assertNotNull(ruletags);
    assertEquals(7, ruletags.size());

    DBBatch dbBatch2 = new DBBatch(1L, 1L);
    List<RuleTag> ruletags2 = ruleTagMapper.getRowBatch(dbBatch2);
    assertNotNull(ruletags2);
    assertEquals(1, ruletags2.size());
  }

  @Test
  public void testInsertAndRetrieve() {
    RuleTagCacheObject ruleTag = createTestRuleTag();
    ruleTagMapper.insertRuleTag(ruleTag);
    RuleTagCacheObject retrievedObject = (RuleTagCacheObject) ruleTagMapper.getItem(ruleTag.getId());

    assertNotNull(retrievedObject);

    //compare
    assertEquals(ruleTag.getId(), retrievedObject.getId());
    assertEquals(ruleTag.getName(), retrievedObject.getName());
    assertEquals(ruleTag.getDescription(), retrievedObject.getDescription());
    assertEquals(ruleTag.getMode(), retrievedObject.getMode());
    assertEquals(ruleTag.getDataType(), retrievedObject.getDataType());
    assertEquals(ruleTag.isLogged(), retrievedObject.isLogged());
    assertEquals(ruleTag.getUnit(), retrievedObject.getUnit());
    assertEquals(ruleTag.getDipAddress(), retrievedObject.getDipAddress());
    assertEquals(ruleTag.getJapcAddress(), retrievedObject.getJapcAddress());
    assertEquals(ruleTag.getValue(), retrievedObject.getValue());
    assertEquals(ruleTag.getValueDescription(), retrievedObject.getValueDescription());
    assertEquals(ruleTag.isSimulated(), retrievedObject.isSimulated());
    assertEquals(ruleTag.getDataTagQuality(), retrievedObject.getDataTagQuality());
    assertEquals(ruleTag.getTimestamp(), retrievedObject.getTimestamp());
    assertEquals(ruleTag.getRuleIdsString(), retrievedObject.getRuleIdsString());

    //rule specific
    assertEquals(ruleTag.getRuleIdsString(), retrievedObject.getRuleIdsString());
  }

  @Test
  public void testUpdateRuleTag() {
    RuleTagCacheObject ruleTag = createTestRuleTag();
    ruleTagMapper.insertRuleTag(ruleTag);

    ruleTag.setValue(new Integer(2000));
    ruleTag.setCacheTimestamp(new Timestamp(System.currentTimeMillis()));
    ruleTag.setValueDescription("new control value");
    ruleTag.setDataTagQuality(new DataTagQualityImpl(TagQualityStatus.UNKNOWN_REASON, "test quality unknown reason"));
    ruleTag.setSimulated(false);

    ruleTagMapper.updateCacheable(ruleTag);

    RuleTagCacheObject retrievedObject = (RuleTagCacheObject) ruleTagMapper.getItem(ruleTag.getId());

    assertEquals(ruleTag.getValue(), retrievedObject.getValue());
    assertEquals(ruleTag.getValueDescription(), retrievedObject.getValueDescription());
    assertEquals(ruleTag.getDataTagQuality(), retrievedObject.getDataTagQuality());
    assertEquals(ruleTag.getTimestamp(), retrievedObject.getTimestamp());
    assertEquals(ruleTag.isSimulated(), retrievedObject.isSimulated());
  }

  @Test
  public void testIsInDB() {
    assertTrue(ruleTagMapper.isInDb(60001L));
  }

  @Test
  public void testNotInDB() {
    assertFalse(ruleTagMapper.isInDb(200000L));
  }

  private static RuleTagCacheObject createTestRuleTag() {
    RuleTagCacheObject cacheObject =
      new RuleTagCacheObject(130L,
        "Junit_test_tag",
        "Integer",
        DataTagConstants.MODE_TEST,
        "(#100000 = true)&(#100001 = true)[2],true[3]"); //rule text set here - only extra field on top of abstract class
    cacheObject.setName("Junit_test_rule_tag"); //non null
    cacheObject.setDescription("test rule description");
    cacheObject.setMode(DataTagConstants.MODE_TEST); //non null
    cacheObject.setDataType("Integer"); // non null
    cacheObject.setLogged(false); //null allowed
    cacheObject.setUnit("test unit m/sec");
    cacheObject.setDipAddress("testDIPaddress");
    cacheObject.setJapcAddress("testJAPCaddress");
    cacheObject.setValue(1000);
    cacheObject.setValueDescription("test value description");
    cacheObject.setSimulated(false); //null allowed
    cacheObject.setDataTagQuality(createValidQuality());
    cacheObject.setCacheTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setRuleIdsString("");
    return cacheObject;
  }

  private static DataTagQuality createValidQuality() {
    DataTagQuality dataTagQuality = new DataTagQualityImpl();
    dataTagQuality.validate();
    return dataTagQuality;
  }
}
