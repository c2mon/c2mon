package cern.c2mon.server.configuration.loader;

import cern.c2mon.cache.actions.process.ProcessService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.DataTagMapper;
import cern.c2mon.server.cache.dbaccess.RuleTagMapper;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.server.configuration.ConfigurationLoader;
import cern.c2mon.server.configuration.parser.util.ConfigurationRuleTagUtil;
import cern.c2mon.server.configuration.util.TestConfigurationProvider;
import cern.c2mon.server.rule.RuleTagService;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import org.junit.Ignore;
import org.junit.Test;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static cern.c2mon.server.common.util.Java9Collections.setOf;
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

  @Inject
  private RuleTagService ruleTagService;

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

    assertEquals(cacheObjectFactory.buildRuleTagCacheObject(1500L, ruleTag), ruleTagCache.get(1500L));
    // Check if all caches are updated
    assertNotNull(ruleTagMapper.getItem(1500L));
  }

  @Test
  public void update() throws InterruptedException {
    configurationLoader.applyConfiguration(1);
    dataTagCache.computeQuiet(5000000L, dataTag -> {
      ((DataTagCacheObject) dataTag).setValue(15.0f);
      dataTag.getDataTagQuality().validate();
    });
    // Reregister evaluation listeners, because the ConfigRuleChain nuked them
    ruleTagService.init();
    RuleTagCacheObject expectedObject = afterUpdateObject();

    // Expecting 2 updates and 2 evaluations
    final CountDownLatch latch = new CountDownLatch(1);

    ruleTagCache.getCacheListenerManager().registerListener(ruleTag -> {
      System.out.println("Received: " + ruleTag);
      if (ruleTag.getDataTagQuality().isValid())
        latch.countDown();
    }, CacheEvent.UPDATE_ACCEPTED);
    configurationLoader.applyConfiguration(10);
    configurationLoader.applyConfiguration(11);

    assertTrue(latch.await(1, TimeUnit.SECONDS));

    assertEquals("Object should be evaluated", expectedObject, ruleTagCache.get(50100L));
  }

  @Test
  public void remove() {
    // remove ruletag
    ConfigurationReport report = configurationLoader.applyConfiguration(12);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertFalse(ruleTagCache.containsKey(60007L));
    assertNull(ruleTagMapper.getItem(60007L));
  }

  @Test
  @Ignore("This behaviour was changed with the cache refactoring of 2020")
  public void removingRuleDeletesDependentRules() {
    configurationLoader.applyConfiguration(12);
    // dependent rules removed, e.g.
    assertFalse(ruleTagCache.containsKey(60009L));
    assertNull(ruleTagMapper.getItem(60009L));
  }

  @Test
  @Ignore("This behaviour was changed with the cache refactoring of 2020 to no longer delete dependent rules")
  public void removingTagDeletesDependentRules() {
    Long tagId = 200001L;
    Long ruleId1 = 60000L; // two of the rules that should be removed
    Long ruleId2 = 59999L;
    assertTrue(ruleTagCache.containsKey(ruleId1));
    assertNotNull(ruleTagMapper.getItem(ruleId1));
    assertTrue(ruleTagCache.containsKey(ruleId2));
    assertNotNull(ruleTagMapper.getItem(ruleId2));
    assertTrue(dataTagCache.containsKey(tagId));
    assertNotNull(dataTagMapper.getItem(tagId));

    // test removal of tag 20004L removes the rule also
    configurationLoader.applyConfiguration(7);

    assertFalse(ruleTagCache.containsKey(ruleId1));
    assertNull(ruleTagMapper.getItem(ruleId1));
    assertFalse(ruleTagCache.containsKey(ruleId2));
    assertNull(ruleTagMapper.getItem(ruleId2));
    assertFalse(dataTagCache.containsKey(tagId));
    assertNull(dataTagMapper.getItem(tagId));
  }

  @Test
  public void updateRuleTag() throws InterruptedException {
    // SETUP:
    setUp(configurationLoader, processService);
    configurationLoader.applyConfiguration(TestConfigurationProvider.createRuleTag());
    ruleTagService.init();

    final CountDownLatch latch = new CountDownLatch(1);
    ruleTagCache.getCacheListenerManager().registerListener(ruleTag -> {
      if (ruleTag.getDataTagQuality().isValid())
        latch.countDown();
    }, CacheEvent.UPDATE_ACCEPTED);

    // TEST:
    // Build configuration to add the test RuleTagUpdate
    cern.c2mon.shared.client.configuration.api.tag.RuleTag ruleTagUpdate =
      cern.c2mon.shared.client.configuration.api.tag.RuleTag
        .update(1500L).ruleText("(2 > 1)[1],true[0]").description("new description").build();
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
    expectedCacheObjectRule.setValue(1);
    expectedCacheObjectRule.setValueDescription("Rule result");

    latch.await(1, TimeUnit.SECONDS);

    assertEquals(expectedCacheObjectRule, ruleTagCache.get(1500L));
  }

  @Test
  public void deleteDataTagDoesNotDeleteRule() {
    setUp(configurationLoader, processService);
    configurationLoader.applyConfiguration(TestConfigurationProvider.createRuleTag());

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
    assertEquals(1, report.getElementReports().size());

    // Check if all caches are updated
    assertTrue(ruleTagCache.containsKey(1500L));
    assertNotNull(ruleTagMapper.getItem(1500L));
    assertFalse(dataTagCache.containsKey(1000L));
    assertNull(dataTagMapper.getItem(1000L));
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

  private RuleTagCacheObject afterUpdateObject() {
    RuleTagCacheObject expectedObject = expectedObject();
    expectedObject.setJapcAddress("newTestConfigJAPCaddress");
    expectedObject.setRuleText("(2 > 1)[1],true[0]");
    expectedObject.setValueDescription("Rule result");
    expectedObject.setProcessIds(Collections.emptySet());
    expectedObject.setEquipmentIds(Collections.emptySet());
    expectedObject.getDataTagQuality().validate();
    expectedObject.setValue(1.0F);
    return expectedObject;
  }
}
