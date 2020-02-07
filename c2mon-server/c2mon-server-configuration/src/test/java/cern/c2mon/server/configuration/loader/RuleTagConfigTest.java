package cern.c2mon.server.configuration.loader;

import cern.c2mon.cache.actions.process.ProcessService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.DataTagMapper;
import cern.c2mon.server.cache.dbaccess.RuleTagMapper;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.server.configuration.ConfigurationLoader;
import cern.c2mon.server.configuration.helper.ObjectEqualityComparison;
import cern.c2mon.server.configuration.parser.util.ConfigurationRuleTagUtil;
import cern.c2mon.server.configuration.util.TestConfigurationProvider;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.common.NoSimpleValueParseException;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.daq.config.ConfigurationChangeEventReport;
import org.junit.Test;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static cern.c2mon.server.common.util.Java9Collections.setOf;
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
  
  @Inject
  private ProcessService processService;

  /**
   * No communication should take place with the DAQs during rule configuration.
   */
  @Test
  public void create()  {
    // the mocked ProcessCommmunicationManager will be called once when creating
    // the datatag to base the rule on
//    expect(mockManager.sendConfiguration(eq(50L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());
//    replay(mockManager);

    // insert datatag to base rule on
    configurationLoader.applyConfiguration(1);
    ConfigurationReport report = configurationLoader.applyConfiguration(10);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(expectedObject(), ruleTagCache.get(50100L));
  }

  @Test
  public void createFromDb() {
    // SETUP:
    setUp(configurationLoader, processService);

    // TEST:Build configuration to add the test RuleTag
    cern.c2mon.shared.client.configuration.api.tag.RuleTag ruleTag = ConfigurationRuleTagUtil.buildCreateAllFieldsRuleTag(1500L, null);
    Configuration configuration = new Configuration();
    configuration.addEntity(ruleTag);

    ///apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertEquals(1, report.getElementReports().size());

    // get cacheObject from the cache and compare it with the expected cacheObject
    RuleTagCacheObject cacheObjectRule = (RuleTagCacheObject) ruleTagCache.get(1500L);
    cacheObjectRule.setDataTagQuality(new DataTagQualityImpl());
    RuleTagCacheObject expectedCacheObjectRule = cacheObjectFactory.buildRuleTagCacheObject(1500L, ruleTag);

    ObjectEqualityComparison.assertRuleTagConfigEquals(expectedCacheObjectRule, cacheObjectRule);
    // Check if all caches are updated
    assertNotNull(ruleTagMapper.getItem(1500L));
  }

  @Test
  public void update() throws InterruptedException {
    RuleTagCacheObject expectedObject = expectedObject();
    expectedObject.setJapcAddress("newTestConfigJAPCaddress");
    expectedObject.setRuleText("(2 > 1)[1],true[0]");
    expectedObject.setProcessIds(Collections.emptySet());
    expectedObject.setEquipmentIds(Collections.emptySet());
    expectedObject.getDataTagQuality().validate();

    ConfigurationReport report = configurationLoader.applyConfiguration(11);

    // sleep is to allow for rule evaluation on separate thread
    Thread.sleep(1000);

    RuleTagCacheObject updatedCacheObject = (RuleTagCacheObject) ruleTagCache.get(50100L);
    ObjectEqualityComparison.assertRuleTagConfigEquals(expectedObject, updatedCacheObject);
  }

  @Test
  public void remove() {
    // remove ruletag
    ConfigurationReport report = configurationLoader.applyConfiguration(12);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertFalse(ruleTagCache.containsKey(60007L));
    assertNull(ruleTagMapper.getItem(60007L));

    // dependent rules removed, e.g.
    assertFalse(ruleTagCache.containsKey(60009L));
    assertNull(ruleTagMapper.getItem(60009L));
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

  @Test
  public void updateRuleTag() throws InterruptedException, IllegalAccessException, TransformerException, InstantiationException, NoSimpleValueParseException, ParserConfigurationException, NoSuchFieldException {
    replay(mockManager);
    // SETUP:
    setUp(configurationLoader, processService);
    Configuration createRuleTag = TestConfigurationProvider.createRuleTag();
    configurationLoader.applyConfiguration(createRuleTag);

    final CountDownLatch latch = new CountDownLatch(1);
    ruleTagCache.getCacheListenerManager().registerListener(cacheable -> latch.countDown(), CacheEvent.UPDATE_ACCEPTED);

    // TEST:
    // Build configuration to add the test RuleTagUpdate
    cern.c2mon.shared.client.configuration.api.tag.RuleTag ruleTagUpdate = cern.c2mon.shared.client.configuration.api.tag.RuleTag.update(1500L).ruleText("(2 > 1)[1],true[0]").description("new description").build();
    Configuration configuration = new Configuration();
    configuration.addEntity(ruleTagUpdate);

    ///apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertEquals(1, report.getElementReports().size());

    // get cacheObject from the cache and compare to the an expected cacheObject
    RuleTagCacheObject cacheObjectData = (RuleTagCacheObject) ruleTagCache.get(1500L);
    RuleTagCacheObject expectedCacheObjectRule = cacheObjectFactory.buildRuleTagUpdateCacheObject(cacheObjectData, ruleTagUpdate);
    expectedCacheObjectRule.setProcessIds(Collections.emptySet());
    expectedCacheObjectRule.setEquipmentIds(Collections.emptySet());
    expectedCacheObjectRule.getDataTagQuality().validate();
    latch.await();

    ObjectEqualityComparison.assertRuleTagConfigEquals(expectedCacheObjectRule, (RuleTagCacheObject) ruleTagCache.get(1500L));

    verify(mockManager);
  }

  @Test
  public void deleteRuleWithDeleteDataTag() throws IllegalAccessException, TransformerException, InstantiationException, NoSimpleValueParseException, ParserConfigurationException, NoSuchFieldException {
    // called once when updating the equipment;
    // mock returns a list with the correct number of SUCCESS ChangeReports
    expect(mockManager.sendConfiguration(eq(5L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());
    replay(mockManager);
    setUp(configurationLoader, processService);
    Configuration createRuleTag = TestConfigurationProvider.createRuleTag();
    configurationLoader.applyConfiguration(createRuleTag);

    // TEST:
    // check if the DataTag and rules are in the cache
    assertTrue(ruleTagCache.containsKey(1500L));
    assertNotNull(ruleTagMapper.getItem(1500L));
    assertTrue(dataTagCache.containsKey(1000L));
    assertNotNull(dataTagMapper.getItem(1000L));

    // Build configuration to remove the DataTag
    Configuration removeTag = TestConfigurationProvider.deleteDataTag();
    ConfigurationReport report = configurationLoader.applyConfiguration(removeTag);

    //check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertSame(report.getStatus(), ConfigConstants.Status.OK);
    assertEquals(1, report.getElementReports().size());

    // Check if all caches are updated
    assertFalse(ruleTagCache.containsKey(1500L));
    assertNull(ruleTagMapper.getItem(1500L));
    assertFalse(dataTagCache.containsKey(1000L));
    assertNull(dataTagMapper.getItem(1000L));

    verify(mockManager);
  }

  private static RuleTagCacheObject expectedObject() {
    RuleTagCacheObject expectedObject = new RuleTagCacheObject(50100L);
    expectedObject.setName("test ruletag");
    expectedObject.setDescription("test ruletag description");
    expectedObject.setMode(DataTagConstants.MODE_MAINTENANCE);
    expectedObject.setDataType("Float");
    expectedObject.setLogged(true);
    expectedObject.setUnit("config unit m/sec");
    expectedObject.setDipAddress("testConfigDIPaddress");
    expectedObject.setJapcAddress("testConfigJAPCaddress");
    expectedObject.setRuleText("(#5000000 < 0)|(#5000000 > 200)[1],true[0]");
    expectedObject.setEquipmentIds(setOf(150L));
    expectedObject.setProcessIds(setOf(50L));
    return expectedObject;
  }

  private static void setUp(ConfigurationLoader configurationLoader, ProcessService processService) {
    Configuration createProcess = TestConfigurationProvider.createProcess();
    configurationLoader.applyConfiguration(createProcess);
    Configuration createEquipment = TestConfigurationProvider.createEquipment();
    configurationLoader.applyConfiguration(createEquipment);
    Configuration createSubEquipment = TestConfigurationProvider.createSubEquipment();
    configurationLoader.applyConfiguration(createSubEquipment);
    Configuration createDataTag = TestConfigurationProvider.createEquipmentDataTag(15L);
    configurationLoader.applyConfiguration(createDataTag);
    processService.start(5L, "hostname", new Timestamp(System.currentTimeMillis()));
  }
}
