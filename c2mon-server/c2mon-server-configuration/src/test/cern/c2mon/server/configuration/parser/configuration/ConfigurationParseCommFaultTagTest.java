package cern.c2mon.server.configuration.parser.configuration;

import cern.c2mon.server.cache.*;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.server.configuration.parser.impl.ConfigurationParserImpl;
import cern.c2mon.server.configuration.parser.util.Pair;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigConstants.Entity;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.tag.CommFaultTag;
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

import static cern.c2mon.server.configuration.parser.util.ConfigurationCommFaultTagUtil.*;
import static cern.c2mon.server.configuration.parser.util.ConfigurationUtil.getConfBuilderCommFaultTagE;
import static cern.c2mon.server.configuration.parser.util.ConfigurationUtil.getConfBuilderCommFaultTagUpdate;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:cern/c2mon/server/configuration/config/server-configuration-parser-test.xml" })
public class ConfigurationParseCommFaultTagTest {

  @Autowired
  ConfigurationParserImpl configurationParser;

  @Autowired
  ProcessCache processCache;

  @Autowired
  CommFaultTagCache commFaultTagCache;

  @Autowired
  ControlTagCache statusTagCache;

  @Autowired
  AliveTimerCache aliveTagCache;


  @Autowired
  EquipmentCache equipmentCache;

  @Rule
  public ExpectedException commFaultTagUpdate = ExpectedException.none();

  @Rule
  public ExpectedException commFaultTagCreate = ExpectedException.none();

  @Rule
  public ExpectedException commFaultTagDelete = ExpectedException.none();

  @Before
  public void resetMocks(){
    EasyMock.reset(equipmentCache,processCache,commFaultTagCache, statusTagCache, aliveTagCache);
  }

  @Test
  public void commFaultTagUpdate_withNoFields() {
    // Setup Configuration Instance
    Configuration configuration = getConfBuilderCommFaultTagUpdate(buildCommFaultTagWithId(1L)._1);

//    DataTag.builder().address(new DataTagAddress(new PLCHardwareAddressImpl(0, 0, 0, 0, 0, 0.0f, "")));

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(commFaultTagCache.hasKey(1L)).andReturn(true);


    // Switch to replay mode
    EasyMock.replay(equipmentCache,processCache,commFaultTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertFalse(elements.size() > 0);

    // Verify mock methods were called
    EasyMock.verify(equipmentCache,processCache,commFaultTagCache);
  }

  @Test
  public void commFaultTagUpdate_notExistingInstance() {
    // Setup Exception
    commFaultTagUpdate.expect(ConfigurationParseException.class);
    commFaultTagUpdate.expectMessage("Creating CommFaultTag (id = 1) failed. Not enough arguments.");

    // Setup Configuration Instance
    Pair<CommFaultTag,Properties> pair = buildUpdateCommFaultTagWithSomeFields(1l);
    Configuration configuration = getConfBuilderCommFaultTagUpdate(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(commFaultTagCache.hasKey(1l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(equipmentCache,processCache,commFaultTagCache);

    // Run the code to be tested
   configurationParser.parse(configuration);

    // Verify mock methods were called
   EasyMock.verify(equipmentCache,processCache,commFaultTagCache);
  }

  @Test
  public void commFaultTagUpdate_withSomeFields() {
    // Setup Configuration Instance
    Pair<CommFaultTag,Properties> pair = buildUpdateCommFaultTagWithSomeFields(1l);
    Configuration configuration = getConfBuilderCommFaultTagUpdate(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1l)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(commFaultTagCache.hasKey(1l)).andReturn(true);

    // Switch to replay mode
    EasyMock.replay(equipmentCache,processCache,commFaultTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().equals(pair._2));
    assertTrue(elements.get(0).getEntity().equals(Entity.CONTROLTAG));
    assertTrue(elements.get(0).getAction().equals(Action.UPDATE));

    // Verify mock methods were called
    EasyMock.verify(equipmentCache,processCache,commFaultTagCache);
  }

  @Test
  public void commFaultTagUpdate_withAllFields() {
    // Setup Configuration Instance
    Pair<CommFaultTag,Properties> pair = buildUpdateCommFaultTagWithAllFields(1l);
    Configuration configuration = getConfBuilderCommFaultTagUpdate(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(commFaultTagCache.hasKey(1l)).andReturn(true);

    // Switch to replay mode
    EasyMock.replay(equipmentCache,processCache,commFaultTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().equals(pair._2));
    assertTrue(elements.get(0).getEntity().equals(Entity.CONTROLTAG));
    assertTrue(elements.get(0).getAction().equals(Action.UPDATE));

    // Verify mock methods were called
    EasyMock.verify(equipmentCache,processCache,commFaultTagCache);
  }


  @Test
  public void commFaultTagCreate_withNoFields() {
    // Setup Exception
    commFaultTagCreate.expect(ConfigurationParseException.class);
    commFaultTagCreate.expectMessage("Creating CommFaultTag (id = 1) failed. Not enough arguments.");

    // Setup Configuration Instance
    Configuration configuration = getConfBuilderCommFaultTagE(buildCommFaultTagWithId(1L)._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(1L)).andReturn(false);
    EasyMock.expect(commFaultTagCache.hasKey(1l)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(1L)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(equipmentCache,processCache,commFaultTagCache);

    // Run the code to be tested
    configurationParser.parse(configuration);

    // Verify mock methods were called
    EasyMock.verify(equipmentCache,processCache,commFaultTagCache);
  }

  @Test
  public void commFaultTagCreate_withMandatoryFields() {
    // Setup Configuration Instance
    Pair<CommFaultTag,Properties> pair = buildCommFaultTagWithPrimFields(1l);
    Configuration configuration = getConfBuilderCommFaultTagE(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(1L)).andReturn(false);
    EasyMock.expect(commFaultTagCache.hasKey(1l)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(1L)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(equipmentCache,processCache,commFaultTagCache, statusTagCache, aliveTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 4);
    assertTrue(elements.get(1).getElementProperties().equals(pair._2));
    assertTrue(elements.get(1).getEntityId().equals(1L));
    assertTrue(elements.get(1).getEntity().equals(Entity.CONTROLTAG));
    assertTrue(elements.get(1).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(equipmentCache,processCache,commFaultTagCache, statusTagCache, aliveTagCache);
  }

  @Test
  public void commFaultTagCreate_withNotExistingSupClass() {
    commFaultTagCreate.expect(ConfigurationParseException.class);
    commFaultTagCreate.expectMessage("Creating Process (id = 1) failed. Not enough arguments.");

    // Setup Configuration Instance
    Pair<CommFaultTag,Properties> pair = buildCommFaultTagWithPrimFields(1l);
    Configuration configuration = getConfBuilderCommFaultTagE(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(equipmentCache,processCache,commFaultTagCache);

    // Run the code to be tested
    configurationParser.parse(configuration);

    // Verify mock methods were called
    EasyMock.verify(equipmentCache,processCache,commFaultTagCache);
  }

  @Test
  public void commFaultTagCreate_withAllFields() {
    // Setup Configuration Instance
    Pair<CommFaultTag,Properties> pair = buildCommFaultTagWithAllFields(1l);
    Configuration configuration = getConfBuilderCommFaultTagE(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(1L)).andReturn(false);
    EasyMock.expect(commFaultTagCache.hasKey(1l)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(1L)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(equipmentCache,processCache,commFaultTagCache, statusTagCache, aliveTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 4);
    assertTrue(elements.get(1).getElementProperties().equals(pair._2));
    assertTrue(elements.get(1).getEntityId().equals(1L));
    assertTrue(elements.get(1).getEntity().equals(Entity.CONTROLTAG));
    assertTrue(elements.get(1).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(equipmentCache,processCache,commFaultTagCache, statusTagCache, aliveTagCache);
  }

  @Test
  public void commFaultTagCreate_withoutDefaultFields() {
    // Setup Configuration Instance
    Pair<CommFaultTag,Properties> pair = buildCommFaultTagWithoutDefaultFields(1l);
    Configuration configuration = getConfBuilderCommFaultTagE(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(1L)).andReturn(false);
    EasyMock.expect(commFaultTagCache.hasKey(1l)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(1L)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(equipmentCache,processCache,commFaultTagCache, statusTagCache, aliveTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 4);
    assertTrue(elements.get(1).getElementProperties().equals(pair._2));
    assertTrue(elements.get(1).getEntityId().equals(1L));
    assertTrue(elements.get(1).getEntity().equals(Entity.CONTROLTAG));
    assertTrue(elements.get(1).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(equipmentCache,processCache,commFaultTagCache, statusTagCache, aliveTagCache);
  }

}
