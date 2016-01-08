package cern.c2mon.server.configuration.parser.configuration;

import static cern.c2mon.server.configuration.parser.util.ConfigurationProcessUtil.*;
import static cern.c2mon.server.configuration.parser.util.ConfigurationUtil.getConfBuilderProcess;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Properties;

import cern.c2mon.server.cache.AliveTimerCache;
import cern.c2mon.server.cache.ControlTagCache;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.shared.client.configuration.api.tag.StatusTag;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.configuration.parser.impl.ConfigurationParserImpl;
import cern.c2mon.server.configuration.parser.util.Pair;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigConstants.Entity;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.process.Process;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:cern/c2mon/server/configuration/config/server-configuration-parser-test.xml" })
public class ConfigurationParseProcessTest {

  @Autowired
  ConfigurationParserImpl configurationParser;

  @Autowired
  ProcessCache processCache;

  @Autowired
  EquipmentCache equipmentCache;

  @Autowired
  ControlTagCache statusTagCache;

  @Autowired
  AliveTimerCache aliveTagCache;


  @Rule
  public ExpectedException processUpdate = ExpectedException.none();

  @Rule
  public ExpectedException processCreate = ExpectedException.none();

  @Rule
  public ExpectedException processDelete = ExpectedException.none();

  @Before
  public void resetMocks(){
    EasyMock.reset(processCache);
    EasyMock.reset(equipmentCache);
    EasyMock.reset(statusTagCache, aliveTagCache);
  }

  @Test
  public void processUpdate_withNoFields() {
    // Setup Configuration Instance
    Configuration configuration = getConfBuilderProcess(buildProcessWtihId(1L)._1);

//    DataTag.builder().address(new DataTagAddress(new PLCHardwareAddressImpl(0, 0, 0, 0, 0, 0.0f, "")));

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);

    // Switch to replay mode
    EasyMock.replay(processCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertFalse(elements.size() > 0);

    // Verify mock methods were called
    EasyMock.verify(processCache);
  }

  @Test
  public void processUpdate_notExistingInstance() {
    // Setup Exception
    processUpdate.expect(ConfigurationParseException.class);
    processUpdate.expectMessage("Creating Process (id = 1) failed. Not enough arguments.");

    // Setup Configuration Instance
    Pair<Process,Properties> pair = buildUpdateProcessWtihSomeFields(1l);
    Configuration configuration = getConfBuilderProcess(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache);

    // Run the code to be tested
   configurationParser.parse(configuration);

    // Verify mock methods were called
    EasyMock.verify(processCache);
  }

  @Test
  public void processUpdate_withSomeFields() {
    // Setup Configuration Instance
    Pair<Process,Properties> pair = buildUpdateProcessWtihSomeFields(1l);
    Configuration configuration = getConfBuilderProcess(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1l)).andReturn(true);

    // Switch to replay mode
    EasyMock.replay(processCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().equals(pair._2));
    assertTrue(elements.get(0).getEntity().equals(Entity.PROCESS));
    assertTrue(elements.get(0).getAction().equals(Action.UPDATE));

    // Verify mock methods were called
    EasyMock.verify(processCache);
  }

  @Test
  public void processUpdate_withAllFields() {
    // Setup Configuration Instance
    Pair<Process,Properties> pair = buildUpdateProcessWtihAllFields(1l);
    Configuration configuration = getConfBuilderProcess(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1l)).andReturn(true);

    // Switch to replay mode
    EasyMock.replay(processCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().equals(pair._2));
    assertTrue(elements.get(0).getEntity().equals(Entity.PROCESS));
    assertTrue(elements.get(0).getAction().equals(Action.UPDATE));

    // Verify mock methods were called
    EasyMock.verify(processCache);
  }

  @Test
  public void processUpdate_multipleInstances_withAllFields() {
    // Setup Configuration Instance
    Pair<Process,Properties> pair1 = buildUpdateProcessWtihAllFields(1l);
    Pair<Process,Properties> pair2 = buildUpdateProcessWtihAllFields(2l);
    Pair<Process,Properties> pair3 = buildUpdateProcessWtihAllFields(3l);
    Pair<Process,Properties> pair4 = buildUpdateProcessWtihAllFields(4l);
    Pair<Process,Properties> pair5 = buildUpdateProcessWtihAllFields(5l);

    Configuration configuration = getConfBuilderProcess(pair1._1,pair2._1, pair3._1, pair4._1, pair5._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1l)).andReturn(true);
    EasyMock.expect(processCache.hasKey(2l)).andReturn(true);
    EasyMock.expect(processCache.hasKey(3l)).andReturn(true);
    EasyMock.expect(processCache.hasKey(4l)).andReturn(true);
    EasyMock.expect(processCache.hasKey(5l)).andReturn(true);

    // Switch to replay mode
    EasyMock.replay(processCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 5);

    assertTrue(elements.get(0).getElementProperties().equals(pair1._2));
    assertTrue(elements.get(0).getEntity().equals(Entity.PROCESS));
    assertTrue(elements.get(0).getAction().equals(Action.UPDATE));

    assertTrue(elements.get(1).getElementProperties().equals(pair2._2));
    assertTrue(elements.get(1).getEntity().equals(Entity.PROCESS));
    assertTrue(elements.get(1).getAction().equals(Action.UPDATE));

    assertTrue(elements.get(2).getElementProperties().equals(pair3._2));
    assertTrue(elements.get(2).getEntity().equals(Entity.PROCESS));
    assertTrue(elements.get(2).getAction().equals(Action.UPDATE));

    assertTrue(elements.get(3).getElementProperties().equals(pair4._2));
    assertTrue(elements.get(3).getEntity().equals(Entity.PROCESS));
    assertTrue(elements.get(3).getAction().equals(Action.UPDATE));

    assertTrue(elements.get(4).getElementProperties().equals(pair5._2));
    assertTrue(elements.get(4).getEntity().equals(Entity.PROCESS));
    assertTrue(elements.get(4).getAction().equals(Action.UPDATE));

    // Verify mock methods were called
    EasyMock.verify(processCache);
  }

  @Test
  public void processCreate_withNoFields() {
    // Setup Exception
    processCreate.expect(ConfigurationParseException.class);
    processCreate.expectMessage("Creating Process (id = 1) failed. Not enough arguments.");

    // Setup Configuration Instance
    Configuration configuration = getConfBuilderProcess(buildProcessWtihId(1L)._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache);

    // Run the code to be tested
    configurationParser.parse(configuration);

    // Verify mock methods were called
    EasyMock.verify(processCache);
  }

  @Test
  public void processCreate_withMandatoryFields() {
    // Setup Configuration Instance
    Pair<Process,Properties> pair = buildProcessWtihPrimFields(1l);
    Configuration configuration = getConfBuilderProcess(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1l)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(0l)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(1l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache,statusTagCache, aliveTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 3);
    assertTrue(elements.get(2).getElementProperties().equals(pair._2));
    assertTrue(elements.get(2).getEntityId().equals(1L));
    assertTrue(elements.get(2).getEntity().equals(Entity.PROCESS));
    assertTrue(elements.get(2).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(processCache,statusTagCache, aliveTagCache);
  }

  @Test
  public void processCreate_withAllFields() {
    // Setup Configuration Instance
    Pair<Process,Properties> pair = buildProcessWtihAllFields(1l);
    Configuration configuration = getConfBuilderProcess(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1l)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(0l)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(1l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache, statusTagCache, aliveTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 3);
    assertTrue(elements.get(2).getElementProperties().equals(pair._2));
    assertTrue(elements.get(2).getEntityId().equals(1L));
    assertTrue(elements.get(2).getEntity().equals(Entity.PROCESS));
    assertTrue(elements.get(2).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(processCache, statusTagCache, aliveTagCache);
  }

  @Test
  public void processCreate_withoutDefaultFields() {
    // Setup Configuration Instance
    Pair<Process,Properties> pair = buildProcessWtihoutDefaultFields(1l);
    Configuration configuration = getConfBuilderProcess(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1l)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(0l)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(1l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache, statusTagCache, aliveTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 3);
    assertTrue(elements.get(2).getElementProperties().equals(pair._2));
    assertTrue(elements.get(2).getEntityId().equals(1L));
    assertTrue(elements.get(2).getEntity().equals(Entity.PROCESS));
    assertTrue(elements.get(2).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(processCache, statusTagCache, aliveTagCache);
  }

  @Test
  public void processCreate_multipleInstances_withAllFields() {
    // Setup Configuration Instance
    Pair<Process,Properties> pair1 = buildProcessWtihAllFields(1l);
    Pair<Process,Properties> pair2 = buildProcessWtihAllFields(2l);
    Pair<Process,Properties> pair3 = buildProcessWtihAllFields(3l);
    Pair<Process,Properties> pair4 = buildProcessWtihAllFields(4l);
    Pair<Process,Properties> pair5 = buildProcessWtihAllFields(5l);

    Configuration configuration = getConfBuilderProcess(pair1._1,pair2._1, pair3._1, pair4._1, pair5._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1l)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(0l)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(1l)).andReturn(false);

    EasyMock.expect(processCache.hasKey(2l)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(0l)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(1l)).andReturn(false);

    EasyMock.expect(processCache.hasKey(3l)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(0l)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(1l)).andReturn(false);

    EasyMock.expect(processCache.hasKey(4l)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(0l)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(1l)).andReturn(false);

    EasyMock.expect(processCache.hasKey(5l)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(0l)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(1l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache, statusTagCache, aliveTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 15);

    assertTrue(elements.get(10).getElementProperties().equals(pair1._2));
    assertTrue(elements.get(10).getEntityId().equals(1L));
    assertTrue(elements.get(10).getEntity().equals(Entity.PROCESS));
    assertTrue(elements.get(10).getAction().equals(Action.CREATE));

    assertTrue(elements.get(11).getElementProperties().equals(pair2._2));
    assertTrue(elements.get(11).getEntityId().equals(2L));
    assertTrue(elements.get(11).getEntity().equals(Entity.PROCESS));
    assertTrue(elements.get(11).getAction().equals(Action.CREATE));

    assertTrue(elements.get(12).getElementProperties().equals(pair3._2));
    assertTrue(elements.get(12).getEntityId().equals(3L));
    assertTrue(elements.get(12).getEntity().equals(Entity.PROCESS));
    assertTrue(elements.get(12).getAction().equals(Action.CREATE));

    assertTrue(elements.get(13).getElementProperties().equals(pair4._2));
    assertTrue(elements.get(13).getEntityId().equals(4L));
    assertTrue(elements.get(13).getEntity().equals(Entity.PROCESS));
    assertTrue(elements.get(13).getAction().equals(Action.CREATE));

    assertTrue(elements.get(14).getElementProperties().equals(pair5._2));
    assertTrue(elements.get(14).getEntityId().equals(5L));
    assertTrue(elements.get(14).getEntity().equals(Entity.PROCESS));
    assertTrue(elements.get(14).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(processCache, statusTagCache, aliveTagCache);
  }

  @Test
  public void processCreate_withNewControlTag() {
    processCreate.expect(ConfigurationParseException.class);
    processCreate.expectMessage("Not possible to create a ControlTag for Process or Equipment when the parent already exists.");

    // Setup Configuration Instance
    Process process = buildUpdateProcessNewControlTag(1l);
    Configuration configuration = getConfBuilderProcess(process);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(0l)).andReturn(true);
    EasyMock.expect(aliveTagCache.hasKey(1l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache,aliveTagCache);

    // runt test
    configurationParser.parse(configuration);


    // Verify mock methods were called
    EasyMock.verify(processCache,aliveTagCache);
  }

  @Test
  public void processDelete_NotExistingInstance() {
    // Setup Exception
//    processDelete.expect(ConfigurationParseException.class);
//    processDelete.expectMessage("Deleting Process 1 failed. Process do not exist in the cache.");

    // Setup Configuration Instance
    Process process = buildDeleteProcess(1l);
    Configuration configuration = getConfBuilderProcess(process);

    // Setup Mock
    // Set expectations
//    EasyMock.expect(processCache.hasKey(1L)).andReturn(false);

    // Switch to replay mode
//    EasyMock.replay(processCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().isEmpty());
    assertTrue(elements.get(0).getEntity().equals(Entity.PROCESS));
    assertTrue(elements.get(0).getAction().equals(Action.REMOVE));

    // Verify mock methods were called
//    EasyMock.verify(processCache);
  }

  /**
   * At the moment, the parsers returns always a Delte ConfigurationElement.
   * Independent from the id in the cache!
   */
  @Test
  public void processDelete_ExistingInstance() {
    // Setup Configuration Instance
    Process process = buildDeleteProcess(1l);
    Configuration configuration = getConfBuilderProcess(process);

    // Setup Mock
    // Set expectations
//    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);

    // Switch to replay mode
//    EasyMock.replay(processCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

 // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().isEmpty());
    assertTrue(elements.get(0).getEntity().equals(Entity.PROCESS));
    assertTrue(elements.get(0).getAction().equals(Action.REMOVE));

    // Verify mock methods were called
//    EasyMock.verify(processCache);
  }

  @Test
  public void processAllOperations_checkOrder() {
    // Setup Configuration Instance
    Pair<Process,Properties> pairUpdate = buildUpdateProcessWtihAllFields(2l);
    Pair<Process,Properties> pairCreate = buildProcessWtihAllFields(3l);
    Process processDelete = buildDeleteProcess(1l);
    Configuration configuration = getConfBuilderProcess(pairUpdate._1, pairCreate._1, processDelete);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(2L)).andReturn(true);
    EasyMock.expect(processCache.hasKey(3L)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(0l)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(1l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache, statusTagCache, aliveTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 5);

    // Check ConfigurationElement fields and order
    assertTrue(elements.get(0).getElementProperties().isEmpty());
    assertTrue(elements.get(0).getEntity().equals(Entity.PROCESS));
    assertTrue(elements.get(0).getAction().equals(Action.REMOVE));

    assertTrue(elements.get(3).getElementProperties().equals(pairCreate._2));
    assertTrue(elements.get(3).getEntity().equals(Entity.PROCESS));
    assertTrue(elements.get(3).getAction().equals(Action.CREATE));

    assertTrue(elements.get(4).getElementProperties().equals(pairUpdate._2));
    assertTrue(elements.get(4).getEntity().equals(Entity.PROCESS));
    assertTrue(elements.get(4).getAction().equals(Action.UPDATE));

    // Verify mock methods were called
    EasyMock.verify(processCache, statusTagCache, aliveTagCache);
  }
}
