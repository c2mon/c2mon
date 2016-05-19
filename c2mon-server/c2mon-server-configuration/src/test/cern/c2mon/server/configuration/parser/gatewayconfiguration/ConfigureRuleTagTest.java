/*
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
 */

package cern.c2mon.server.configuration.parser.gatewayconfiguration;

import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.RuleTagCache;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.cache.loading.SequenceDAO;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.configuration.parser.ConfigurationParser;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.server.configuration.parser.util.ConfigurationUtil;
import cern.c2mon.shared.client.configuration.ConfigConstants;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Franz Ritter
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/server/configuration/config/server-configuration-parser-test.xml"})
public class ConfigureRuleTagTest {

  @Autowired
  ConfigurationParser parser;

  @Autowired
  SubEquipmentCache subEquipmentCache;

  @Autowired
  SequenceDAO sequenceDAO;

  @Autowired
  DataTagCache dataTagCache;

  @Autowired
  RuleTagCache ruleTagCache;

  @Rule
  public ExpectedException tagException = ExpectedException.none();


  @Before
  public void resetMocks() {
    EasyMock.reset(sequenceDAO, dataTagCache, ruleTagCache);
  }

  @Test
  public void createRuleTag() {
    // setup Configuration:
    RuleTag tag = RuleTag.create("myRuleTag", Integer.class, "ruleExpression").build();

    List<RuleTag> ruleTagList = new ArrayList<>();
    ruleTagList.add(tag);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(ruleTagList);

    // setUp Mocks:
    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(20L);
    EasyMock.expect(ruleTagCache.hasKey(20L)).andReturn(false);


    EasyMock.replay(sequenceDAO, ruleTagCache);


    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 1);
    assertEquals((long) parsed.get(0).getEntityId(), 20L);
    assertEquals(parsed.get(0).getElementProperties().size(), 6);
    assertEquals(parsed.get(0).getElementProperties().get("dataType"), Integer.class.getName());
    assertEquals(parsed.get(0).getElementProperties().get("isLogged"), "true");
    assertEquals(parsed.get(0).getElementProperties().get("name"), "myRuleTag");
    assertEquals(parsed.get(0).getElementProperties().get("ruleText"), "ruleExpression");
    assertEquals(parsed.get(0).getElementProperties().get("description"), "<no description provided>");
    assertEquals(parsed.get(0).getElementProperties().get("mode"), "1");

    EasyMock.verify(sequenceDAO, ruleTagCache);
  }

  @Test
  public void createRuleTagWithId() {
    // setup Configuration:
    RuleTag tag = RuleTag.create("myRuleTag", Integer.class, "ruleExpression").id(21L).build();

    List<RuleTag> ruleTagList = new ArrayList<>();
    ruleTagList.add(tag);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(ruleTagList);

    // setUp Mocks:
    EasyMock.expect(ruleTagCache.hasKey(21L)).andReturn(false);

    EasyMock.replay(sequenceDAO, ruleTagCache);

    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 1);
    assertEquals((long) parsed.get(0).getEntityId(), 21L);
    assertEquals(parsed.get(0).getElementProperties().size(), 6);
    assertEquals(parsed.get(0).getElementProperties().get("dataType"), Integer.class.getName());
    assertEquals(parsed.get(0).getElementProperties().get("isLogged"), "true");
    assertEquals(parsed.get(0).getElementProperties().get("name"), "myRuleTag");
    assertEquals(parsed.get(0).getElementProperties().get("ruleText"), "ruleExpression");
    assertEquals(parsed.get(0).getElementProperties().get("description"), "<no description provided>");
    assertEquals(parsed.get(0).getElementProperties().get("mode"), "1");

    EasyMock.verify(sequenceDAO, ruleTagCache);
  }


  @Test
  public void createExistingRuleTag() {
    // Setup Exception
    tagException.expect(IllegalArgumentException.class);
    tagException.expectMessage("Error while parsing a 'create' Configuration: Id 21 of the class RuleTag already known to the server");

    RuleTag tag = RuleTag.create("myRuleTag", Integer.class, "ruleExpression").id(21L).build();

    List<RuleTag> ruleTagList = new ArrayList<>();
    ruleTagList.add(tag);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(ruleTagList);

    // setUp Mocks:
    EasyMock.expect(ruleTagCache.hasKey(21L)).andReturn(true);

    // run test
    EasyMock.replay(ruleTagCache);
    parser.parse(config);
    EasyMock.verify(ruleTagCache);
  }

  @Test
  public void updateRuleTagWithName() {
    // setup Configuration:
    RuleTag ruleTag = RuleTag.update("myRuleTag").description("The description").build();

    List<RuleTag> ruleTagList = new ArrayList<>();
    ruleTagList.add(ruleTag);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(ruleTagList);

    // setUp Mocks:
    EasyMock.expect(dataTagCache.get("myRuleTag")).andReturn(new DataTagCacheObject(20L));
    EasyMock.expect(ruleTagCache.hasKey(20L)).andReturn(true);

    EasyMock.replay(ruleTagCache, dataTagCache);

    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 1);
    assertEquals((long) parsed.get(0).getEntityId(), 20L);
    assertEquals(parsed.get(0).getElementProperties().get("description"), "The description");

    EasyMock.verify(ruleTagCache, dataTagCache);
  }

  @Test
  public void updateRuleTagWithId() {
    // setup Configuration:
    RuleTag ruleTag = RuleTag.update(20L).description("The description").build();

    List<RuleTag> ruleTagList = new ArrayList<>();
    ruleTagList.add(ruleTag);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(ruleTagList);

    // setUp Mocks:
    EasyMock.expect(ruleTagCache.hasKey(20L)).andReturn(true);

    EasyMock.replay(ruleTagCache);


    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 1);
    assertEquals((long) parsed.get(0).getEntityId(), 20L);
    assertEquals(parsed.get(0).getElementProperties().get("description"), "The description");

    EasyMock.verify(ruleTagCache);
  }

  @Test
  public void updateNonExistingRuleTag() {
    // Setup Exception
    tagException.expect(ConfigurationParseException.class);
    tagException.expectMessage("Updating of RuleTag (id = 20) failed: The object is unknown to the sever.");

    // setup Configuration:
    RuleTag ruleTag = RuleTag.update(20L).description("The description").build();

    List<RuleTag> ruleTagList = new ArrayList<>();
    ruleTagList.add(ruleTag);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(ruleTagList);

    // setUp Mocks:
    EasyMock.expect(ruleTagCache.hasKey(20L)).andReturn(false);

    // run test
    EasyMock.replay(ruleTagCache);
    parser.parse(config);
    EasyMock.verify(ruleTagCache);
  }

  @Test
  public void deleteRuleTag() {
    // setup Configuration:
    RuleTag ruleTag = RuleTag.update(20L).build();
    ruleTag.setDeleted(true);

    List<RuleTag> ruleTagList = new ArrayList<>();
    ruleTagList.add(ruleTag);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(ruleTagList);

    // setUp Mocks:
    EasyMock.expect(ruleTagCache.hasKey(20L)).andReturn(true);

    EasyMock.replay(ruleTagCache);

    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 1);
    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.REMOVE);
    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.RULETAG);
    assertTrue(parsed.get(0).getElementProperties().isEmpty());

    EasyMock.verify(ruleTagCache);
  }

  @Test
  public void deleteNotExistingRuleTag() {
    // Setup Exception
    tagException.expect(ConfigurationParseException.class);
    tagException.expectMessage("Deleting of RuleTag (id = 20) failed: The object is unknown to the sever.");

    // setup Configuration:
    RuleTag ruleTag = RuleTag.update(20L).build();
    ruleTag.setDeleted(true);

    List<RuleTag> ruleTagList = new ArrayList<>();
    ruleTagList.add(ruleTag);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(ruleTagList);

    // setUp Mocks:
    EasyMock.expect(ruleTagCache.hasKey(20L)).andReturn(false);
    EasyMock.replay(ruleTagCache);

    parser.parse(config);

    EasyMock.verify(ruleTagCache);
  }

}
