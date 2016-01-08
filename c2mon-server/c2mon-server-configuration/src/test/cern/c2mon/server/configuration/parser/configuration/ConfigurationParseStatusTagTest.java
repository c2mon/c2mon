package cern.c2mon.server.configuration.parser.configuration;

import cern.c2mon.server.cache.AliveTimerCache;
import cern.c2mon.server.cache.ControlTagCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.server.configuration.parser.impl.ConfigurationParserImpl;
import cern.c2mon.server.configuration.parser.util.Pair;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigConstants.Entity;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.Configuration;
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

import java.util.List;
import java.util.Properties;

import static cern.c2mon.server.configuration.parser.util.ConfigurationStatusTagUtil.*;
import static cern.c2mon.server.configuration.parser.util.ConfigurationUtil.getConfBuilderStatusTagP;
import static cern.c2mon.server.configuration.parser.util.ConfigurationUtil.getConfBuilderStatusTagUpdate;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:cern/c2mon/server/configuration/config/server-configuration-parser-test.xml" })
public class ConfigurationParseStatusTagTest {

  @Autowired
  ConfigurationParserImpl configurationParser;

  @Autowired
  ProcessCache processCache;

  @Autowired
  ControlTagCache statusTagCache;

  @Autowired
  AliveTimerCache aliveTagCache;
  
  @Rule
  public ExpectedException statusTagUpdate = ExpectedException.none();

  @Rule
  public ExpectedException statusTagCreate = ExpectedException.none();

  @Rule
  public ExpectedException statusTagDelete = ExpectedException.none();

  @Before
  public void resetMocks(){
    EasyMock.reset(processCache,statusTagCache ,aliveTagCache);
  }

  @Test
  public void statusTagUpdate_withNoFields() {
    // Setup Configuration Instance
    Configuration configuration = getConfBuilderStatusTagUpdate(buildStatusTagWithId(1L)._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(statusTagCache.hasKey(1L)).andReturn(true);


    // Switch to replay mode
    EasyMock.replay(processCache);
    EasyMock.replay(statusTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertFalse(elements.size() > 0);

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(statusTagCache);
  }

  @Test
  public void statusTagUpdate_notExistingInstance() {
    // Setup Exception
    statusTagUpdate.expect(ConfigurationParseException.class);
    statusTagUpdate.expectMessage("Creating StatusTag (id = 1) failed. Not enough arguments.");

    // Setup Configuration Instance
    Pair<StatusTag,Properties> pair = buildUpdateStatusTagWithSomeFields(1l);
    Configuration configuration = getConfBuilderStatusTagUpdate(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(statusTagCache.hasKey(1l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache);
    EasyMock.replay(statusTagCache);

    // Run the code to be tested
   configurationParser.parse(configuration);

    // Verify mock methods were called
   EasyMock.verify(processCache);
   EasyMock.verify(statusTagCache);
  }

  @Test
  public void statusTagUpdate_withSomeFields() {
    // Setup Configuration Instance
    Pair<StatusTag,Properties> pair = buildUpdateStatusTagWithSomeFields(1l);
    Configuration configuration = getConfBuilderStatusTagUpdate(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1l)).andReturn(true);
    EasyMock.expect(statusTagCache.hasKey(1l)).andReturn(true);

    // Switch to replay mode
    EasyMock.replay(processCache);
    EasyMock.replay(statusTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().equals(pair._2));
    assertTrue(elements.get(0).getEntity().equals(Entity.CONTROLTAG));
    assertTrue(elements.get(0).getAction().equals(Action.UPDATE));

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(statusTagCache);
  }

  @Test
  public void statusTagUpdate_withAllFields() {
    // Setup Configuration Instance
    Pair<StatusTag,Properties> pair = buildUpdateStatusTagWithAllFields(1l);
    Configuration configuration = getConfBuilderStatusTagUpdate(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(statusTagCache.hasKey(1l)).andReturn(true);

    // Switch to replay mode
    EasyMock.replay(processCache);
    EasyMock.replay(statusTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().equals(pair._2));
    assertTrue(elements.get(0).getEntity().equals(Entity.CONTROLTAG));
    assertTrue(elements.get(0).getAction().equals(Action.UPDATE));

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(statusTagCache);
  }


  @Test
  public void statusTagCreate_withNoFields() {
    // Setup Exception
    statusTagCreate.expect(ConfigurationParseException.class);
    statusTagCreate.expectMessage("Creating StatusTag (id = 1) failed. Not enough arguments.");

    // Setup Configuration Instance
    Configuration configuration = getConfBuilderStatusTagP(buildStatusTagWithId(1L)._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(1l)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(1L)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache,statusTagCache ,aliveTagCache);

    // Run the code to be tested
    configurationParser.parse(configuration);

    // Verify mock methods were called
    EasyMock.verify(processCache,statusTagCache ,aliveTagCache);
  }

  @Test
  public void statusTagCreate_withMandatoryFields() {
    // Setup Configuration Instance
    Pair<StatusTag,Properties> pair = buildStatusTagWithPrimFields(1l);
    Configuration configuration = getConfBuilderStatusTagP(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(1l)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(1L)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache,statusTagCache ,aliveTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 3);
    assertTrue(elements.get(0).getElementProperties().equals(pair._2));
    assertTrue(elements.get(0).getEntityId().equals(1L));
    assertTrue(elements.get(0).getEntity().equals(Entity.CONTROLTAG));
    assertTrue(elements.get(0).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(processCache,statusTagCache ,aliveTagCache);
  }

  @Test
  public void statusTagCreate_withNotExistingSupClass() {
    statusTagCreate.expect(ConfigurationParseException.class);
    statusTagCreate.expectMessage("Creating Process (id = 1) failed. Not enough arguments.");

    // Setup Configuration Instance
    Pair<StatusTag,Properties> pair = buildStatusTagWithPrimFields(1l);
    Configuration configuration = getConfBuilderStatusTagUpdate(pair._1);

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
  public void statusTagCreate_withAllFields() {
    // Setup Configuration Instance
    Pair<StatusTag,Properties> pair = buildStatusTagWithAllFields(1l);
    Configuration configuration = getConfBuilderStatusTagP(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(1l)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(1L)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache,statusTagCache ,aliveTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 3);
    assertTrue(elements.get(0).getElementProperties().equals(pair._2));
    assertTrue(elements.get(0).getEntityId().equals(1L));
    assertTrue(elements.get(0).getEntity().equals(Entity.CONTROLTAG));
    assertTrue(elements.get(0).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(processCache,statusTagCache ,aliveTagCache);
  }

  @Test
  public void statusTagCreate_withoutDefaultFields() {
    // Setup Configuration Instance
    Pair<StatusTag,Properties> pair = buildStatusTagWithoutDefaultFields(1l);
    Configuration configuration = getConfBuilderStatusTagP(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(1l)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(1L)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache,statusTagCache ,aliveTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 3);
    assertTrue(elements.get(0).getElementProperties().equals(pair._2));
    assertTrue(elements.get(0).getEntityId().equals(1L));
    assertTrue(elements.get(0).getEntity().equals(Entity.CONTROLTAG));
    assertTrue(elements.get(0).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(processCache,statusTagCache ,aliveTagCache);
  }


}
