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

import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.TagFacadeGateway;
import cern.c2mon.server.cache.loading.SequenceDAO;
import cern.c2mon.server.configuration.parser.ConfigurationParser;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.server.configuration.parser.util.ConfigurationUtil;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.alarm.Alarm;
import cern.c2mon.shared.client.configuration.api.alarm.ValueCondition;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Franz Ritter
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/server/configuration/config/server-configuration-parser-test.xml"})
public class ConfigureAlarmsTest {

  @Autowired
  ConfigurationParser parser;

  @Autowired
  TagFacadeGateway tagFacadeGateway;

  @Autowired
  SequenceDAO sequenceDAO;

  @Autowired
  AlarmCache alarmCache;

  @Rule
  public ExpectedException alarmException = ExpectedException.none();


  @Before
  public void resetMocks() {
    EasyMock.reset(tagFacadeGateway, sequenceDAO, alarmCache);
  }

  @Test
  public void createAlarm() {
    // setup Configuration:

    Alarm alarm = Alarm.create("faultFamily", "faultMember", 1337, new ValueCondition(Integer.class, 1)).build();
    alarm.setParentTagId(10L);

    List<Alarm> alarmList = new ArrayList<>();
    alarmList.add(alarm);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(alarmList);

    // setUp Mocks:
    EasyMock.expect(tagFacadeGateway.isInTagCache(10L)).andReturn(true);
    EasyMock.expect(sequenceDAO.getNextAlarmId()).andReturn(20L);
    EasyMock.expect(alarmCache.hasKey(20L)).andReturn(false);

    EasyMock.replay(tagFacadeGateway, sequenceDAO, alarmCache);


    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 1);
    assertEquals((long) parsed.get(0).getEntityId(), 20L);
    assertEquals(parsed.get(0).getElementProperties().get("faultFamily"), "faultFamily");
    assertEquals(parsed.get(0).getElementProperties().get("faultCode"), "1337");
    assertEquals(parsed.get(0).getElementProperties().get("dataTagId"), "10");
    assertEquals(parsed.get(0).getElementProperties().get("faultMember"), "faultMember");
    assertEquals(parsed.get(0).getElementProperties().get("alarmCondition"), new ValueCondition(Integer.class, 1).getXMLCondition());

    EasyMock.verify(tagFacadeGateway, sequenceDAO, alarmCache);
  }

  @Test
  public void createAlarmWithId() {

    // setup Configuration:

    Alarm alarm = Alarm.create("faultFamily", "faultMember", 1337, new ValueCondition(Integer.class, 1)).id(21L).build();
    alarm.setParentTagId(10L);

    List<Alarm> alarmList = new ArrayList<>();
    alarmList.add(alarm);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(alarmList);

    // setUp Mocks:
    EasyMock.expect(alarmCache.hasKey(21L)).andReturn(false);
    EasyMock.expect(tagFacadeGateway.isInTagCache(10L)).andReturn(true);

    EasyMock.replay(tagFacadeGateway, sequenceDAO, alarmCache);


    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 1);
    assertEquals((long) parsed.get(0).getEntityId(), 21L);
    assertEquals(parsed.get(0).getElementProperties().get("faultFamily"), "faultFamily");
    assertEquals(parsed.get(0).getElementProperties().get("faultCode"), "1337");
    assertEquals(parsed.get(0).getElementProperties().get("dataTagId"), "10");
    assertEquals(parsed.get(0).getElementProperties().get("faultMember"), "faultMember");
    assertEquals(parsed.get(0).getElementProperties().get("alarmCondition"), new ValueCondition(Integer.class, 1).getXMLCondition());

    EasyMock.verify(tagFacadeGateway, sequenceDAO, alarmCache);
  }

  @Test
  public void createAlarmWithNonExistingTag() {
    // Setup Exception
    alarmException.expect(ConfigurationParseException.class);
    alarmException.expectMessage("Creating of a new Alarm (id = 21) failed: No Tag with the id 10 found");

    // setup Configuration:
    Alarm alarm = Alarm.create("faultFamily", "faultMember", 1337, new ValueCondition(Integer.class, 1)).id(21L).build();
    alarm.setParentTagId(10L);

    List<Alarm> alarmList = new ArrayList<>();
    alarmList.add(alarm);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(alarmList);

    // setUp Mocks:
    EasyMock.expect(alarmCache.hasKey(21L)).andReturn(false);
    EasyMock.expect(tagFacadeGateway.isInTagCache(10L)).andReturn(false);

    // run test
    EasyMock.replay(tagFacadeGateway, sequenceDAO, alarmCache);
    parser.parse(config);
    EasyMock.verify(tagFacadeGateway, sequenceDAO, alarmCache);
  }

  @Test
  public void createExistingAlarm() {
    // Setup Exception
    alarmException.expect(IllegalArgumentException.class);
    alarmException.expectMessage("Error while parsing a 'create' Configuration: Id 21 of the class Alarm already known to the server");

    // setup Configuration:
    Alarm alarm = Alarm.create("faultFamily", "faultMember", 1337, new ValueCondition(Integer.class, 1)).id(21L).build();
    alarm.setParentTagId(10L);

    List<Alarm> alarmList = new ArrayList<>();
    alarmList.add(alarm);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(alarmList);

    // setUp Mocks:
    EasyMock.expect(tagFacadeGateway.isInTagCache(10L)).andReturn(true);
    EasyMock.expect(alarmCache.hasKey(21L)).andReturn(true);


    // run test
    EasyMock.replay(tagFacadeGateway, sequenceDAO, alarmCache);
    parser.parse(config);
    EasyMock.verify(tagFacadeGateway, sequenceDAO, alarmCache);
  }

  @Test
  public void updateAlarm() {
    // setup Configuration:
    Alarm alarm = Alarm.update(20L).alarmCondition(new ValueCondition(Integer.class, 2)).build();

    List<Alarm> alarmUpdateList = new ArrayList<>();
    alarmUpdateList.add(alarm);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(alarmUpdateList);

    // setUp Mocks:
    EasyMock.expect(alarmCache.hasKey(20L)).andReturn(true);

    EasyMock.replay(tagFacadeGateway, sequenceDAO, alarmCache);


    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 1);
    assertEquals((long) parsed.get(0).getEntityId(), 20L);
    assertEquals(parsed.get(0).getElementProperties().get("alarmCondition"), new ValueCondition(Integer.class, 2).getXMLCondition());

    EasyMock.verify(tagFacadeGateway, sequenceDAO, alarmCache);
  }

  @Test
  public void updateNonExistingAlarm() {
    // Setup Exception
    alarmException.expect(ConfigurationParseException.class);
    alarmException.expectMessage("Updating of Alarm (id = 20) failed: The object is unknown to the sever.");

    // setup Configuration:
    Alarm alarm = Alarm.update(20L).alarmCondition(new ValueCondition(Integer.class, 2)).build();

    List<Alarm> alarmUpdateList = new ArrayList<>();
    alarmUpdateList.add(alarm);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(alarmUpdateList);

    // setUp Mocks:
    EasyMock.expect(alarmCache.hasKey(20L)).andReturn(false);

    // run test
    EasyMock.replay(tagFacadeGateway, sequenceDAO, alarmCache);
    parser.parse(config);
    EasyMock.verify(tagFacadeGateway, sequenceDAO, alarmCache);
  }

  @Test
  public void deleteAlarm() {
    // setup Configuration:
    Alarm alarm = Alarm.builder().id(20L).deleted(true).build();

    List<Alarm> alarmUpdateList = new ArrayList<>();
    alarmUpdateList.add(alarm);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(alarmUpdateList);

    // setUp Mocks:
    EasyMock.expect(alarmCache.hasKey(20L)).andReturn(true);

    EasyMock.replay(tagFacadeGateway, sequenceDAO, alarmCache);


    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 1);
    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.REMOVE);
    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.ALARM);
    assertTrue(parsed.get(0).getElementProperties().isEmpty());

    EasyMock.verify(tagFacadeGateway, sequenceDAO, alarmCache);
  }

  @Test
  public void deleteNotExistingAlarm() {
    // Setup Exception
    alarmException.expect(ConfigurationParseException.class);
    alarmException.expectMessage("Deleting of Alarm (id = 20) failed: The object is unknown to the sever.");

    // setup Configuration:
    Alarm alarm = Alarm.builder().id(20L).deleted(true).build();

    List<Alarm> alarmUpdateList = new ArrayList<>();
    alarmUpdateList.add(alarm);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(alarmUpdateList);

    // setUp Mocks:
    EasyMock.expect(alarmCache.hasKey(20L)).andReturn(false);
    EasyMock.replay(tagFacadeGateway, sequenceDAO, alarmCache);

    parser.parse(config);

    EasyMock.verify(tagFacadeGateway, sequenceDAO, alarmCache);
  }

}
