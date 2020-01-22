package cern.c2mon.server.configuration.loader;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.DataTagMapper;
import cern.c2mon.server.cache.dbaccess.RuleTagMapper;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.server.configuration.helper.ObjectEqualityComparison;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.common.NoSimpleValueParseException;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.daq.config.ConfigurationChangeEventReport;
import org.junit.Test;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class RuleTagConfigTest extends ConfigurationCacheLoaderTest<RuleTag> {

  @Inject
  private C2monCache<RuleTag> ruleTagCache;

  @Inject
  private RuleTagMapper ruleTagMapper;

  @Inject
  private C2monCache<DataTag> dataTagCache;

  @Inject
  private DataTagMapper dataTagMapper;

  /**
   * No communication should take place with the DAQs during rule configuration.
   *
   * @throws InterruptedException
   * @throws NoSimpleValueParseException
   * @throws NoSuchFieldException
   * @throws TransformerException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws ParserConfigurationException
   */
  @Test
  public void testCreateUpdateRemoveRuleTag() throws InterruptedException, ParserConfigurationException, IllegalAccessException, InstantiationException,
    TransformerException, NoSuchFieldException, NoSimpleValueParseException {
    // the mocked ProcessCommmunicationManager will be called once when creating
    // the datatag to base the rule on
    expect(mockManager.sendConfiguration(eq(50L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());
    replay(mockManager);

    // insert datatag to base rule on
    configurationLoader.applyConfiguration(1);
    ConfigurationReport report = configurationLoader.applyConfiguration(10);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    RuleTagCacheObject cacheObject = (RuleTagCacheObject) ruleTagCache.get(50100L);

    RuleTagCacheObject expectedObject = new RuleTagCacheObject(50100L);
    expectedObject.setName("test ruletag"); // non null
    expectedObject.setDescription("test ruletag description");
    expectedObject.setMode(DataTagConstants.MODE_MAINTENANCE); // non null
    expectedObject.setDataType("Float"); // non null
    expectedObject.setLogged(true); // null allowed
    expectedObject.setUnit("config unit m/sec");
    expectedObject.setDipAddress("testConfigDIPaddress");
    expectedObject.setJapcAddress("testConfigJAPCaddress");
    expectedObject.setRuleText("(#5000000 < 0)|(#5000000 > 200)[1],true[0]");
    Set<Long> eqIds = new HashSet<Long>();
    eqIds.add(150L);
    expectedObject.setEquipmentIds(eqIds);
    Set<Long> procIds = new HashSet<Long>();
    procIds.add(50L);
    expectedObject.setProcessIds(procIds);

    ObjectEqualityComparison.assertRuleTagConfigEquals(expectedObject, cacheObject);

    // update ruletag
    expectedObject.setJapcAddress("newTestConfigJAPCaddress");
    expectedObject.setRuleText("(2 > 1)[1],true[0]");
    expectedObject.setProcessIds(Collections.EMPTY_SET);
    expectedObject.setEquipmentIds(Collections.EMPTY_SET);
    expectedObject.getDataTagQuality().validate();
    report = configurationLoader.applyConfiguration(11);
    Thread.sleep(1000); // sleep 1s to allow for rule evaluation on separate
    // thread

    RuleTagCacheObject updatedCacheObject = (RuleTagCacheObject) ruleTagCache.get(50100L);
    ObjectEqualityComparison.assertRuleTagConfigEquals(expectedObject, updatedCacheObject);

    verify(mockManager);
  }

  @Test
  public void testRemoveRuleTag() {

    replay(mockManager);

    // remove ruletag
    ConfigurationReport report = configurationLoader.applyConfiguration(12);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertFalse(ruleTagCache.containsKey(60007L));
    assertNull(ruleTagMapper.getItem(60007L));

    // dependent rules removed, e.g.
    assertFalse(ruleTagCache.containsKey(60009L));
    assertNull(ruleTagMapper.getItem(60009L));

    verify(mockManager);
  }

  /**
   * Tests a dependent rule is removed when a tag is.
   */

  @Test
  public void testRuleRemovedOnTagRemoval() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerException,
    NoSuchFieldException, NoSimpleValueParseException {
    Long tagId = 200001L;
    Long ruleId1 = 60000L; // two of the rules that should be removed
    Long ruleId2 = 59999L;
    assertTrue(ruleTagCache.containsKey(ruleId1));
    assertNotNull(ruleTagMapper.getItem(ruleId1));
    assertTrue(ruleTagCache.containsKey(ruleId2));
    assertNotNull(ruleTagMapper.getItem(ruleId2));
    assertTrue(dataTagCache.containsKey(tagId));
    assertNotNull(dataTagMapper.getItem(tagId));

    // for tag removal
    expect(mockManager.sendConfiguration(eq(50L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());

    replay(mockManager);

    // test removal of tag 20004L removes the rule also
    configurationLoader.applyConfiguration(7);

    assertFalse(ruleTagCache.containsKey(ruleId1));
    assertNull(ruleTagMapper.getItem(ruleId1));
    assertFalse(ruleTagCache.containsKey(ruleId2));
    assertNull(ruleTagMapper.getItem(ruleId2));
    assertFalse(dataTagCache.containsKey(tagId));
    assertNull(dataTagMapper.getItem(tagId));

    verify(mockManager);
  }
}
