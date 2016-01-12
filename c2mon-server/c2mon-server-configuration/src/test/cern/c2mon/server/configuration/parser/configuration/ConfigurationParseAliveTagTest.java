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
import cern.c2mon.shared.client.configuration.api.tag.AliveTag;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Properties;

import static cern.c2mon.server.configuration.parser.util.ConfigurationAliveTagUtil.*;
import static cern.c2mon.server.configuration.parser.util.ConfigurationUtil.getConfBuilderAliveTagP;
import static cern.c2mon.server.configuration.parser.util.ConfigurationUtil.getConfBuilderAliveTagUpdate;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:cern/c2mon/server/configuration/config/server-configuration-parser-test.xml" })
public class ConfigurationParseAliveTagTest {

  @Autowired
  ConfigurationParserImpl configurationParser;

  @Autowired
  ProcessCache processCache;

  @Autowired
  AliveTimerCache aliveTagCache;

  @Autowired
  ControlTagCache controlTagCache;

  @Rule
  public ExpectedException aliveTagUpdate = ExpectedException.none();

  @Rule
  public ExpectedException aliveTagCreate = ExpectedException.none();

  @Rule
  public ExpectedException aliveTagDelete = ExpectedException.none();

  @Before
  public void resetMocks(){
    EasyMock.reset(processCache,controlTagCache ,aliveTagCache);
  }

  @Test
  public void aliveTagUpdate_withNoFields() {
    // Setup Configuration Instance
    Configuration configuration = getConfBuilderAliveTagUpdate(buildAliveTagWtihId(1L)._1);

//    DataTag.builder().address(new DataTagAddress(new PLCHardwareAddressImpl(0, 0, 0, 0, 0, 0.0f, "")));

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(aliveTagCache.hasKey(1L)).andReturn(true);


    // Switch to replay mode
    EasyMock.replay(processCache);
    EasyMock.replay(aliveTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertFalse(elements.size() > 0);

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(aliveTagCache);
  }

  @Test
  public void aliveTagUpdate_notExistingInstance() {
    // Setup Exception
    aliveTagUpdate.expect(ConfigurationParseException.class);
    aliveTagUpdate.expectMessage("Creating AliveTag (id = 1) failed. Not enough arguments.");

    // Setup Configuration Instance
    Pair<AliveTag,Properties> pair = buildUpdateAliveTagWithSomeFields(1l);
    Configuration configuration = getConfBuilderAliveTagUpdate(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(aliveTagCache.hasKey(1l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache);
    EasyMock.replay(aliveTagCache);

    // Run the code to be tested
   configurationParser.parse(configuration);

    // Verify mock methods were called
   EasyMock.verify(processCache);
   EasyMock.verify(aliveTagCache);
  }

  @Test
  public void aliveTagUpdate_withSomeFields() {
    // Setup Configuration Instance
    Pair<AliveTag,Properties> pair = buildUpdateAliveTagWithSomeFields(1l);
    Configuration configuration = getConfBuilderAliveTagUpdate(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1l)).andReturn(true);
    EasyMock.expect(aliveTagCache.hasKey(1l)).andReturn(true);

    // Switch to replay mode
    EasyMock.replay(processCache);
    EasyMock.replay(aliveTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().equals(pair._2));
    assertTrue(elements.get(0).getEntity().equals(Entity.CONTROLTAG));
    assertTrue(elements.get(0).getAction().equals(Action.UPDATE));

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(aliveTagCache);
  }

  @Test
  public void aliveTagUpdate_withAllFields() {
    // Setup Configuration Instance
    Pair<AliveTag,Properties> pair = buildUpdateAliveTagWithAllFields(1l);
    Configuration configuration = getConfBuilderAliveTagUpdate(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(aliveTagCache.hasKey(1l)).andReturn(true);

    // Switch to replay mode
    EasyMock.replay(processCache);
    EasyMock.replay(aliveTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().equals(pair._2));
    assertTrue(elements.get(0).getEntity().equals(Entity.CONTROLTAG));
    assertTrue(elements.get(0).getAction().equals(Action.UPDATE));

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(aliveTagCache);
  }


  @Test
  public void aliveTagCreate_withNoFields() {
    // Setup Exception
    aliveTagCreate.expect(ConfigurationParseException.class);
    aliveTagCreate.expectMessage("Creating AliveTag (id = 1) failed. Not enough arguments.");

    // Setup Configuration Instance
    Configuration configuration = getConfBuilderAliveTagP(buildAliveTagWtihId(1L)._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(false);
    EasyMock.expect(controlTagCache.hasKey(1L)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(1L)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache,controlTagCache ,aliveTagCache);

    // Run the code to be tested
    configurationParser.parse(configuration);

    // Verify mock methods were called
    EasyMock.verify(processCache,controlTagCache ,aliveTagCache);
  }

  @Test
  public void aliveTagCreate_withMandatoryFields() {
    // Setup Configuration Instance
    Pair<AliveTag,Properties> pair = buildAliveTagWithPrimFields(1l);
    Configuration configuration = getConfBuilderAliveTagP(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(false);
    EasyMock.expect(controlTagCache.hasKey(1L)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(1l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache,controlTagCache ,aliveTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 3);
    assertTrue(elements.get(1).getElementProperties().equals(pair._2));
    assertTrue(elements.get(1).getEntityId().equals(1L));
    assertTrue(elements.get(1).getEntity().equals(Entity.CONTROLTAG));
    assertTrue(elements.get(1).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(processCache,controlTagCache ,aliveTagCache);
  }

  @Test
  public void aliveTagCreate_withNotExistingSupClass() {
    aliveTagCreate.expect(ConfigurationParseException.class);
    aliveTagCreate.expectMessage("Creating Process (id = 1) failed. Not enough arguments.");

    // Setup Configuration Instance
    Pair<AliveTag,Properties> pair = buildAliveTagWithPrimFields(1l);
    Configuration configuration = getConfBuilderAliveTagUpdate(pair._1);

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
  public void aliveTagCreate_withAllFields() {
    // Setup Configuration Instance
    Pair<AliveTag,Properties> pair = buildAliveTagWithAllFields(1l);
    Configuration configuration = getConfBuilderAliveTagP(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(false);
    EasyMock.expect(controlTagCache.hasKey(1L)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(1l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache,controlTagCache ,aliveTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 3);
    assertTrue(elements.get(1).getElementProperties().equals(pair._2));
    assertTrue(elements.get(1).getEntityId().equals(1L));
    assertTrue(elements.get(1).getEntity().equals(Entity.CONTROLTAG));
    assertTrue(elements.get(1).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(processCache,controlTagCache ,aliveTagCache);
  }

  @Test
  public void aliveTagCreate_withoutDefaultFields() {
    // Setup Configuration Instance
    Pair<AliveTag,Properties> pair = buildAliveTagWithoutDefaultFields(1l);
    Configuration configuration = getConfBuilderAliveTagP(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(false);
    EasyMock.expect(controlTagCache.hasKey(1L)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(1l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache,controlTagCache ,aliveTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 3);
    assertTrue(elements.get(1).getElementProperties().equals(pair._2));
    assertTrue(elements.get(1).getEntityId().equals(1L));
    assertTrue(elements.get(1).getEntity().equals(Entity.CONTROLTAG));
    assertTrue(elements.get(1).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(processCache,controlTagCache ,aliveTagCache);
  }


}
