/*******************************************************************************
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
 ******************************************************************************/

package cern.c2mon.server.configuration.parser.configuration;

import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.RuleTagCache;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.cache.loading.SequenceDAO;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.configuration.parser.ConfigurationParser;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.tag.RuleTag;
import cern.c2mon.shared.client.configuration.api.tag.Tag;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static cern.c2mon.server.configuration.parser.util.ConfigurationRuleTagUtil.*;
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

    Properties expectedProps = new Properties();
    RuleTag tag = buildCreateBasicRuleTag(expectedProps);

    List<RuleTag> dataTagList = Arrays.asList(tag);

    Configuration config = new Configuration(1L);
    config.setEntities(dataTagList);

    // setUp Mocks:
    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(100L);
    EasyMock.expect(ruleTagCache.hasKey(100L)).andReturn(false);

    EasyMock.replay(sequenceDAO, ruleTagCache);

    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 1);
    assertEquals((long) parsed.get(0).getEntityId(), 100L);
    assertTrue(parsed.get(0).getEntity().equals(ConfigConstants.Entity.RULETAG));
    assertTrue(parsed.get(0).getAction().equals(ConfigConstants.Action.CREATE));
    assertEquals(parsed.get(0).getElementProperties(), expectedProps);

    EasyMock.verify(sequenceDAO, ruleTagCache);
  }

  @Test
  public void createRuleTagWithAllFields() {
    Properties expectedProps = new Properties();
    RuleTag tag = buildCreateAllFieldsRuleTag(101L, expectedProps);

    List<RuleTag> ruleTagList = Arrays.asList(tag);

    Configuration config = new Configuration(1L);
    config.setEntities(ruleTagList);

    // setUp Mocks:
    EasyMock.expect(ruleTagCache.hasKey(101L)).andReturn(false);

    EasyMock.replay(ruleTagCache);

    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 1);
    assertEquals((long) parsed.get(0).getEntityId(), 101L);
    assertEquals(parsed.get(0).getElementProperties(), expectedProps);
    assertTrue(parsed.get(0).getEntity().equals(ConfigConstants.Entity.RULETAG));
    assertTrue(parsed.get(0).getAction().equals(ConfigConstants.Action.CREATE));

    EasyMock.verify(ruleTagCache);
  }

  @Test
  public void createMultipleRuleTagWithAllFields() {
    Properties expectedProps1 = new Properties();
    Properties expectedProps2 = new Properties();
    Properties expectedProps3 = new Properties();

    RuleTag tag1 = buildCreateAllFieldsRuleTag(101L, expectedProps1);
    RuleTag tag2 = buildCreateAllFieldsRuleTag(102L, expectedProps2);
    RuleTag tag3 = buildCreateAllFieldsRuleTag(103L, expectedProps3);

    List<RuleTag> ruleTagList = Arrays.asList(tag1, tag2, tag3);

    Configuration config = new Configuration(1L);
    config.setEntities(ruleTagList);

    // setUp Mocks:
    EasyMock.expect(ruleTagCache.hasKey(101L)).andReturn(false);
    EasyMock.expect(ruleTagCache.hasKey(102L)).andReturn(false);
    EasyMock.expect(ruleTagCache.hasKey(103L)).andReturn(false);

    EasyMock.replay(ruleTagCache);

    // run the parsing
    List<ConfigurationElement> elements = parser.parse(config);

    // Assert stuff
    assertTrue(elements.size() == 3);

    assertEquals(elements.get(0).getElementProperties(), expectedProps1);
    assertTrue(elements.get(0).getEntityId().equals(101L));
    assertTrue(elements.get(0).getEntity().equals(ConfigConstants.Entity.RULETAG));
    assertTrue(elements.get(0).getAction().equals(ConfigConstants.Action.CREATE));

    assertEquals(elements.get(1).getElementProperties(), expectedProps2);
    assertTrue(elements.get(1).getEntityId().equals(102L));
    assertTrue(elements.get(1).getEntity().equals(ConfigConstants.Entity.RULETAG));
    assertTrue(elements.get(1).getAction().equals(ConfigConstants.Action.CREATE));

    assertEquals(elements.get(2).getElementProperties(), expectedProps3);
    assertTrue(elements.get(2).getEntityId().equals(103L));
    assertTrue(elements.get(2).getEntity().equals(ConfigConstants.Entity.RULETAG));
    assertTrue(elements.get(2).getAction().equals(ConfigConstants.Action.CREATE));


    EasyMock.verify(ruleTagCache);
  }

  @Test
  public void createExistingRuleTag() {
    // Setup Exception
    tagException.expect(ConfigurationParseException.class);
    tagException.expectMessage("Error while parsing a 'create' Configuration: Id 21 of the class RuleTag already known to the server");

    RuleTag tag = RuleTag.create("myRuleTag", Integer.class, "ruleExpression").id(21L).build();

    List<RuleTag> ruleTagList = Arrays.asList(tag);

    Configuration config = new Configuration(1L);
    config.setEntities(ruleTagList);

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
    Properties expectedProps = new Properties();
    RuleTag ruleTag = buildUpdateRuleTagWithSomeFields("myRuleTag", expectedProps);

    List<Tag> tagUpdateList = Arrays.asList(ruleTag);

    Configuration config = new Configuration(1L);
    config.setEntities(tagUpdateList);

    // setUp Mocks:
    EasyMock.expect(dataTagCache.get("myRuleTag")).andReturn(new DataTagCacheObject(20L));
    EasyMock.expect(ruleTagCache.hasKey(20L)).andReturn(true);

    EasyMock.replay(dataTagCache, ruleTagCache);

    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals((long) parsed.get(0).getEntityId(), 20L);
    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.RULETAG);
    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.UPDATE);
    assertEquals(parsed.get(0).getElementProperties(), expectedProps);

    EasyMock.verify(dataTagCache, ruleTagCache);
  }

  @Test
  public void updateRuleTagWithId() {

    // setup Configuration:
    Properties expectedProps = new Properties();
    RuleTag ruleTag = buildUpdateRuleTagWithSomeFields(100L, expectedProps);

    List<Tag> tagUpdateList = Arrays.asList(ruleTag);

    Configuration config = new Configuration(1L);
    config.setEntities(tagUpdateList);

    // setUp Mocks:
    EasyMock.expect(ruleTagCache.hasKey(100L)).andReturn(true);

    EasyMock.replay(ruleTagCache);

    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals((long) parsed.get(0).getEntityId(), 100L);
    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.RULETAG);
    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.UPDATE);
    assertEquals(parsed.get(0).getElementProperties(), expectedProps);

    EasyMock.verify(ruleTagCache);
  }

  @Test
  public void updateRuleTagWithAllFields() {
    // setup Configuration:
    Properties expectedProps = new Properties();
    RuleTag ruleTag = buildUpdateRuleTagWithAllFields(100L, expectedProps);

    List<Tag> tagUpdateList = Arrays.asList(ruleTag);

    Configuration config = new Configuration(1L);
    config.setEntities(tagUpdateList);

    // setUp Mocks:
    EasyMock.expect(ruleTagCache.hasKey(100L)).andReturn(true);

    EasyMock.replay(ruleTagCache);

    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals((long) parsed.get(0).getEntityId(), 100L);
    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.RULETAG);
    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.UPDATE);
    assertEquals(parsed.get(0).getElementProperties(), expectedProps);

    EasyMock.verify(ruleTagCache);
  }

  @Test
  public void updateNonExistingRuleTag() {
    // Setup Exception
    tagException.expect(ConfigurationParseException.class);
    tagException.expectMessage("Updating of RuleTag (id = 20) failed: The object is unknown to the sever.");

    // setup Configuration:
    RuleTag ruleTag = RuleTag.update(20L).description("The description").build();

    List<Tag> tagUpdateList = Arrays.asList(ruleTag);

    Configuration config = new Configuration(1L);
    config.setEntities(tagUpdateList);

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
    RuleTag ruleTag = buildDeleteRuleTag(20L);

    List<Tag> tagUpdateList = Arrays.asList(ruleTag);

    Configuration config = new Configuration(1L);
    config.setEntities(tagUpdateList);

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
    RuleTag ruleTag = new RuleTag();
    ruleTag.setId(20L);
    ruleTag.setDeleted(true);

    List<Tag> tagUpdateList = Arrays.asList(ruleTag);

    Configuration config = new Configuration(1L);
    config.setEntities(tagUpdateList);

    // setUp Mocks:
    EasyMock.expect(ruleTagCache.hasKey(20L)).andReturn(false);
    EasyMock.replay(ruleTagCache);

    parser.parse(config);

    EasyMock.verify(ruleTagCache);
  }
}
