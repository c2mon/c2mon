package cern.c2mon.server.configuration.parser.configuration;

import cern.c2mon.server.cache.RuleTagCache;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.server.configuration.parser.impl.ConfigurationParserImpl;
import cern.c2mon.server.configuration.parser.util.Pair;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigConstants.Entity;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.tag.RuleTag;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Properties;

import static cern.c2mon.server.configuration.parser.util.ConfigurationRuleTagUtil.*;
import static cern.c2mon.server.configuration.parser.util.ConfigurationUtil.getConfBuilderRuleTag;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:cern/c2mon/server/configuration/config/server-configuration-parser-test.xml" })
public class ConfigurationParseRuleTagTest {

  @Autowired
  ConfigurationParserImpl configurationParser;

  @Autowired
  RuleTagCache ruleTagCache;


  @Rule
  public ExpectedException processUpdate = ExpectedException.none();

  @Rule
  public ExpectedException processCreate = ExpectedException.none();

  @Rule
  public ExpectedException processDelete = ExpectedException.none();

  @Before
  public void resetMocks(){
    EasyMock.reset(ruleTagCache);
  }

  @Test
  public void processUpdate_withNoFields() {
    // Setup Configuration Instance
    Configuration configuration = getConfBuilderRuleTag(buildRuleTagWtihId(1L)._1);

//    DataTag.builder().address(new DataTagAddress(new PLCHardwareAddressImpl(0, 0, 0, 0, 0, 0.0f, "")));

    // Setup Mock
    // Set expectations
    EasyMock.expect(ruleTagCache.hasKey(1L)).andReturn(true);

    // Switch to replay mode
    EasyMock.replay(ruleTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertFalse(elements.size() > 0);

    // Verify mock methods were called
    EasyMock.verify(ruleTagCache);
  }

  @Test
  public void processUpdate_notExistingInstance() {
    // Setup Exception
    processUpdate.expect(ConfigurationParseException.class);
    processUpdate.expectMessage("Creating RuleTag (id = 1) failed. Not enough arguments.");

    // Setup Configuration Instance
    Pair<RuleTag,Properties> pair = buildUpdateRuleTagWtihSomeFields(1l);
    Configuration configuration = getConfBuilderRuleTag(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(ruleTagCache.hasKey(1l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(ruleTagCache);

    // Run the code to be tested
   configurationParser.parse(configuration);

    // Verify mock methods were called
    EasyMock.verify(ruleTagCache);
  }

  @Test
  public void processUpdate_withSomeFields() {
    // Setup Configuration Instance
    Pair<RuleTag,Properties> pair = buildUpdateRuleTagWtihSomeFields(1l);
    Configuration configuration = getConfBuilderRuleTag(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(ruleTagCache.hasKey(1l)).andReturn(true);

    // Switch to replay mode
    EasyMock.replay(ruleTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().equals(pair._2));
    assertTrue(elements.get(0).getEntity().equals(Entity.RULETAG));
    assertTrue(elements.get(0).getAction().equals(Action.UPDATE));

    // Verify mock methods were called
    EasyMock.verify(ruleTagCache);
  }

  @Test
  public void processUpdate_withAllFields() {
    // Setup Configuration Instance
    Pair<RuleTag,Properties> pair = buildUpdateRuleTagWithAllFields(1l);
    Configuration configuration = getConfBuilderRuleTag(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(ruleTagCache.hasKey(1l)).andReturn(true);

    // Switch to replay mode
    EasyMock.replay(ruleTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().equals(pair._2));
    assertTrue(elements.get(0).getEntity().equals(Entity.RULETAG));
    assertTrue(elements.get(0).getAction().equals(Action.UPDATE));

    // Verify mock methods were called
    EasyMock.verify(ruleTagCache);
  }

  @Test
  public void processUpdate_multipleInstances_withAllFields() {
    // Setup Configuration Instance
    Pair<RuleTag,Properties> pair1 = buildUpdateRuleTagWithAllFields(1l);
    Pair<RuleTag,Properties> pair2 = buildUpdateRuleTagWithAllFields(2l);
    Pair<RuleTag,Properties> pair3 = buildUpdateRuleTagWithAllFields(3l);
    Pair<RuleTag,Properties> pair4 = buildUpdateRuleTagWithAllFields(4l);
    Pair<RuleTag,Properties> pair5 = buildUpdateRuleTagWithAllFields(5l);

    Configuration configuration = getConfBuilderRuleTag(pair1._1,pair2._1, pair3._1, pair4._1, pair5._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(ruleTagCache.hasKey(1l)).andReturn(true);
    EasyMock.expect(ruleTagCache.hasKey(2l)).andReturn(true);
    EasyMock.expect(ruleTagCache.hasKey(3l)).andReturn(true);
    EasyMock.expect(ruleTagCache.hasKey(4l)).andReturn(true);
    EasyMock.expect(ruleTagCache.hasKey(5l)).andReturn(true);

    // Switch to replay mode
    EasyMock.replay(ruleTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 5);

    assertTrue(elements.get(0).getElementProperties().equals(pair1._2));
    assertTrue(elements.get(0).getEntity().equals(Entity.RULETAG));
    assertTrue(elements.get(0).getAction().equals(Action.UPDATE));

    assertTrue(elements.get(1).getElementProperties().equals(pair2._2));
    assertTrue(elements.get(1).getEntity().equals(Entity.RULETAG));
    assertTrue(elements.get(1).getAction().equals(Action.UPDATE));

    assertTrue(elements.get(2).getElementProperties().equals(pair3._2));
    assertTrue(elements.get(2).getEntity().equals(Entity.RULETAG));
    assertTrue(elements.get(2).getAction().equals(Action.UPDATE));

    assertTrue(elements.get(3).getElementProperties().equals(pair4._2));
    assertTrue(elements.get(3).getEntity().equals(Entity.RULETAG));
    assertTrue(elements.get(3).getAction().equals(Action.UPDATE));

    assertTrue(elements.get(4).getElementProperties().equals(pair5._2));
    assertTrue(elements.get(4).getEntity().equals(Entity.RULETAG));
    assertTrue(elements.get(4).getAction().equals(Action.UPDATE));

    // Verify mock methods were called
    EasyMock.verify(ruleTagCache);
  }

  @Test
  public void processCreate_withNoFields() {
    // Setup Exception
    processCreate.expect(ConfigurationParseException.class);
    processCreate.expectMessage("Creating RuleTag (id = 1) failed. Not enough arguments.");

    // Setup Configuration Instance
    Configuration configuration = getConfBuilderRuleTag(buildRuleTagWtihId(1L)._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(ruleTagCache.hasKey(1L)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(ruleTagCache);

    // Run the code to be tested
    configurationParser.parse(configuration);

    // Verify mock methods were called
    EasyMock.verify(ruleTagCache);
  }

  @Test
  public void processCreate_withMandatoryFields() {
    // Setup Configuration Instance
    Pair<RuleTag,Properties> pair = buildRuleTagWtihPrimFields(1l);
    Configuration configuration = getConfBuilderRuleTag(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(ruleTagCache.hasKey(1l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(ruleTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().equals(pair._2));
    assertTrue(elements.get(0).getEntityId().equals(1L));
    assertTrue(elements.get(0).getEntity().equals(Entity.RULETAG));
    assertTrue(elements.get(0).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(ruleTagCache);
  }

  @Test
  public void processCreate_withAllFields() {
    // Setup Configuration Instance
    Pair<RuleTag,Properties> pair = buildRuleTagWtihAllFields(1l);
    Configuration configuration = getConfBuilderRuleTag(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(ruleTagCache.hasKey(1l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(ruleTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().equals(pair._2));
    assertTrue(elements.get(0).getEntityId().equals(1L));
    assertTrue(elements.get(0).getEntity().equals(Entity.RULETAG));
    assertTrue(elements.get(0).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(ruleTagCache);
  }

  @Test
  public void processCreate_withoutDefaultFields() {
    // Setup Configuration Instance
    Pair<RuleTag,Properties> pair = buildRuleTagWtihoutDefaultFields(1l);
    Configuration configuration = getConfBuilderRuleTag(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(ruleTagCache.hasKey(1l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(ruleTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().equals(pair._2));
    assertTrue(elements.get(0).getEntityId().equals(1L));
    assertTrue(elements.get(0).getEntity().equals(Entity.RULETAG));
    assertTrue(elements.get(0).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(ruleTagCache);
  }

  @Test
  public void processCreate_multipleInstances_withAllFields() {
    // Setup Configuration Instance
    Pair<RuleTag,Properties> pair1 = buildRuleTagWtihAllFields(1l);
    Pair<RuleTag,Properties> pair2 = buildRuleTagWtihAllFields(2l);
    Pair<RuleTag,Properties> pair3 = buildRuleTagWtihAllFields(3l);
    Pair<RuleTag,Properties> pair4 = buildRuleTagWtihAllFields(4l);
    Pair<RuleTag,Properties> pair5 = buildRuleTagWtihAllFields(5l);

    Configuration configuration = getConfBuilderRuleTag(pair1._1,pair2._1, pair3._1, pair4._1, pair5._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(ruleTagCache.hasKey(1l)).andReturn(false);
    EasyMock.expect(ruleTagCache.hasKey(2l)).andReturn(false);
    EasyMock.expect(ruleTagCache.hasKey(3l)).andReturn(false);
    EasyMock.expect(ruleTagCache.hasKey(4l)).andReturn(false);
    EasyMock.expect(ruleTagCache.hasKey(5l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(ruleTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 5);

    assertTrue(elements.get(0).getElementProperties().equals(pair1._2));
    assertTrue(elements.get(0).getEntityId().equals(1L));
    assertTrue(elements.get(0).getEntity().equals(Entity.RULETAG));
    assertTrue(elements.get(0).getAction().equals(Action.CREATE));

    assertTrue(elements.get(1).getElementProperties().equals(pair2._2));
    assertTrue(elements.get(1).getEntityId().equals(2L));
    assertTrue(elements.get(1).getEntity().equals(Entity.RULETAG));
    assertTrue(elements.get(1).getAction().equals(Action.CREATE));

    assertTrue(elements.get(2).getElementProperties().equals(pair3._2));
    assertTrue(elements.get(2).getEntityId().equals(3L));
    assertTrue(elements.get(2).getEntity().equals(Entity.RULETAG));
    assertTrue(elements.get(2).getAction().equals(Action.CREATE));

    assertTrue(elements.get(3).getElementProperties().equals(pair4._2));
    assertTrue(elements.get(3).getEntityId().equals(4L));
    assertTrue(elements.get(3).getEntity().equals(Entity.RULETAG));
    assertTrue(elements.get(3).getAction().equals(Action.CREATE));

    assertTrue(elements.get(4).getElementProperties().equals(pair5._2));
    assertTrue(elements.get(4).getEntityId().equals(5L));
    assertTrue(elements.get(4).getEntity().equals(Entity.RULETAG));
    assertTrue(elements.get(4).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(ruleTagCache);
  }

  @Test
  public void processDelete_NotExistingInstance() {
    // Setup Exception
//    processDelete.expect(ConfigurationParseException.class);
//    processDelete.expectMessage("Deleting RuleTag 1 failed. RuleTag do not exist in the cache.");

    // Setup Configuration Instance
    RuleTag process = buildDeleteRuleTag(1l);
    Configuration configuration = getConfBuilderRuleTag(process);

    // Setup Mock
    // Set expectations
//    EasyMock.expect(ruleTagCache.hasKey(1L)).andReturn(false);

    // Switch to replay mode
//    EasyMock.replay(ruleTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().isEmpty());
    assertTrue(elements.get(0).getEntity().equals(Entity.RULETAG));
    assertTrue(elements.get(0).getAction().equals(Action.REMOVE));

    // Verify mock methods were called
//    EasyMock.verify(ruleTagCache);
  }

  /**
   * At the moment, the parsers returns always a Delte ConfigurationElement.
   * Independent from the id in the cache!
   */
  @Test
  public void processDelete_ExistingInstance() {
    // Setup Configuration Instance
    RuleTag process = buildDeleteRuleTag(1l);
    Configuration configuration = getConfBuilderRuleTag(process);

    // Setup Mock
    // Set expectations
//    EasyMock.expect(ruleTagCache.hasKey(1L)).andReturn(true);

    // Switch to replay mode
//    EasyMock.replay(ruleTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

 // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().isEmpty());
    assertTrue(elements.get(0).getEntity().equals(Entity.RULETAG));
    assertTrue(elements.get(0).getAction().equals(Action.REMOVE));

    // Verify mock methods were called
//    EasyMock.verify(ruleTagCache);
  }

  @Test
  public void processAllOperations_checkOrder() {
    // Setup Configuration Instance
    Pair<RuleTag,Properties> pairUpdate = buildUpdateRuleTagWithAllFields(2l);
    Pair<RuleTag,Properties> pairCreate = buildRuleTagWtihAllFields(3l);
    RuleTag ruleTagDelete = buildDeleteRuleTag(1l);
    Configuration configuration = getConfBuilderRuleTag(pairUpdate._1, pairCreate._1, ruleTagDelete);

    // Setup Mock
    // Set expectations
    EasyMock.expect(ruleTagCache.hasKey(2L)).andReturn(true);
    EasyMock.expect(ruleTagCache.hasKey(3L)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(ruleTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 3);

    // Check ConfigurationElement fields and order
    assertTrue(elements.get(0).getElementProperties().isEmpty());
    assertTrue(elements.get(0).getEntity().equals(Entity.RULETAG));
    assertTrue(elements.get(0).getAction().equals(Action.REMOVE));

    assertTrue(elements.get(1).getElementProperties().equals(pairCreate._2));
    assertTrue(elements.get(1).getEntity().equals(Entity.RULETAG));
    assertTrue(elements.get(1).getAction().equals(Action.CREATE));

    assertTrue(elements.get(2).getElementProperties().equals(pairUpdate._2));
    assertTrue(elements.get(2).getEntity().equals(Entity.RULETAG));
    assertTrue(elements.get(2).getAction().equals(Action.UPDATE));

    // Verify mock methods were called
    EasyMock.verify(ruleTagCache);
  }
}
