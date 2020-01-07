///*******************************************************************************
// * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
// *
// * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
// * C2MON is free software: you can redistribute it and/or modify it under the
// * terms of the GNU Lesser General Public License as published by the Free
// * Software Foundation, either version 3 of the license.
// *
// * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
// * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
// * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
// * more details.
// *
// * You should have received a copy of the GNU Lesser General Public License
// * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
// ******************************************************************************/
//
//package cern.c2mon.server.configuration.parser.configuration;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Properties;
//
//import org.easymock.EasyMock;
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.rules.ExpectedException;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//
//import cern.c2mon.server.cache.AlarmCache;
//import cern.c2mon.server.cache.TagFacadeGateway;
//import cern.c2mon.server.cache.loading.SequenceDAO;
//import cern.c2mon.server.configuration.parser.ConfigurationParser;
//import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
//import cern.c2mon.shared.client.configuration.ConfigConstants;
//import cern.c2mon.shared.client.configuration.ConfigurationElement;
//import cern.c2mon.shared.client.configuration.api.Configuration;
//import cern.c2mon.shared.client.configuration.api.alarm.Alarm;
//
//import static cern.c2mon.server.configuration.parser.util.ConfigurationAlarmUtil.buildCreateAllFieldsAlarm;
//import static cern.c2mon.server.configuration.parser.util.ConfigurationAlarmUtil.buildCreateBasicAlarm;
//import static cern.c2mon.server.configuration.parser.util.ConfigurationAlarmUtil.buildDeleteAlarm;
//import static cern.c2mon.server.configuration.parser.util.ConfigurationAlarmUtil.buildUpdateAlarmWithAllFields;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//
///**
// * @author Franz Ritter
// */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = ConfigurationParserTestMocks.class)
//public class ConfigureAlarmsTest {
//
//  @Autowired
//  ConfigurationParser parser;
//
//  @Autowired
//  TagFacadeGateway tagFacadeGateway;
//
//  @Autowired
//  SequenceDAO sequenceDAO;
//
//  @Autowired
//  AlarmCache alarmCache;
//
//  @Rule
//  public ExpectedException alarmException = ExpectedException.none();
//
//
//  @Before
//  public void resetMocks() {
//    EasyMock.reset(tagFacadeGateway, sequenceDAO, alarmCache);
//  }
//
//  @Test
//  public void createAlarm() {
//    // setup Configuration:
//
//    Properties expectedProps = new Properties();
//    Alarm tag = buildCreateBasicAlarm(expectedProps);
//
//    List<Alarm> alarmList = Arrays.asList(tag);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(alarmList);
//
//    // setUp Mocks:
//    EasyMock.expect(tagFacadeGateway.isInTagCache(100L)).andReturn(true);
//    EasyMock.expect(sequenceDAO.getNextAlarmId()).andReturn(200L);
//    EasyMock.expect(alarmCache.hasKey(200L)).andReturn(false);
//
//    EasyMock.replay(alarmCache, sequenceDAO, tagFacadeGateway);
//
//    List<ConfigurationElement> parsed = parser.parse(config);
//
//    assertEquals(parsed.size(), 1);
//    assertEquals((long) parsed.get(0).getEntityId(), 200L);
//    assertTrue(parsed.get(0).getEntity().equals(ConfigConstants.Entity.ALARM));
//    assertTrue(parsed.get(0).getAction().equals(ConfigConstants.Action.CREATE));
//    parsed.get(0).getElementProperties().remove("name"); // Can be ignored
//    assertEquals(parsed.get(0).getElementProperties(), expectedProps);
//
//    EasyMock.verify(alarmCache, sequenceDAO, tagFacadeGateway);
//  }
//
//  @Test
//  public void createAlarmWithAllFields() {
//    Properties expectedProps = new Properties();
//    Alarm alarm = buildCreateAllFieldsAlarm(201L, expectedProps);
//
//    List<Alarm> alarmList = Arrays.asList(alarm);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(alarmList);
//
//    // setUp Mocks:
//    EasyMock.expect(tagFacadeGateway.isInTagCache(100L)).andReturn(true);
//    EasyMock.expect(alarmCache.hasKey(201L)).andReturn(false);
//
//    EasyMock.replay(alarmCache, tagFacadeGateway);
//
//    List<ConfigurationElement> parsed = parser.parse(config);
//
//    assertEquals((long) parsed.get(0).getEntityId(), 201L);
//    parsed.get(0).getElementProperties().remove("name"); // Can be ignored
//    assertEquals(parsed.get(0).getElementProperties(), expectedProps);
//    assertTrue(parsed.get(0).getEntity().equals(ConfigConstants.Entity.ALARM));
//    assertTrue(parsed.get(0).getAction().equals(ConfigConstants.Action.CREATE));
//
//    EasyMock.verify(alarmCache, tagFacadeGateway);
//  }
//
//  @Test
//  public void createMultipleAlarmWithAllFields() {
//    Properties expectedProps1 = new Properties();
//    Properties expectedProps2 = new Properties();
//    Properties expectedProps3 = new Properties();
//
//    Alarm alarm1 = buildCreateAllFieldsAlarm(201L, expectedProps1);
//    Alarm alarm2 = buildCreateAllFieldsAlarm(202L, expectedProps2);
//    Alarm alarm3 = buildCreateAllFieldsAlarm(203L, expectedProps3);
//
//    List<Alarm> alarmList = Arrays.asList(alarm1, alarm2, alarm3);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(alarmList);
//
//    // setUp Mocks:
//    EasyMock.expect(tagFacadeGateway.isInTagCache(100L)).andReturn(true);
//    EasyMock.expect(alarmCache.hasKey(201L)).andReturn(false);
//
//    EasyMock.expect(tagFacadeGateway.isInTagCache(100L)).andReturn(true);
//    EasyMock.expect(alarmCache.hasKey(202L)).andReturn(false);
//
//    EasyMock.expect(tagFacadeGateway.isInTagCache(100L)).andReturn(true);
//    EasyMock.expect(alarmCache.hasKey(203L)).andReturn(false);
//
//
//    EasyMock.replay(alarmCache, tagFacadeGateway);
//
//    // run the parsing
//    List<ConfigurationElement> elements = parser.parse(config);
//
//    // Assert stuff
//    assertTrue(elements.size() == 3);
//
//    elements.get(0).getElementProperties().remove("name"); // Can be ignored, as only a method and not a field
//    assertEquals(elements.get(0).getElementProperties(), expectedProps1);
//    assertTrue(elements.get(0).getEntityId().equals(201L));
//    assertTrue(elements.get(0).getEntity().equals(ConfigConstants.Entity.ALARM));
//    assertTrue(elements.get(0).getAction().equals(ConfigConstants.Action.CREATE));
//
//    elements.get(1).getElementProperties().remove("name"); // Can be ignored, as only a method and not a field
//    assertEquals(elements.get(1).getElementProperties(), expectedProps2);
//    assertTrue(elements.get(1).getEntityId().equals(202L));
//    assertTrue(elements.get(1).getEntity().equals(ConfigConstants.Entity.ALARM));
//    assertTrue(elements.get(1).getAction().equals(ConfigConstants.Action.CREATE));
//
//    elements.get(2).getElementProperties().remove("name"); // Can be ignored, as only a method and not a field
//    assertEquals(elements.get(2).getElementProperties(), expectedProps3);
//    assertTrue(elements.get(2).getEntityId().equals(203L));
//    assertTrue(elements.get(2).getEntity().equals(ConfigConstants.Entity.ALARM));
//    assertTrue(elements.get(2).getAction().equals(ConfigConstants.Action.CREATE));
//
//    EasyMock.verify(alarmCache, tagFacadeGateway);
//  }
//
//  @Test
//  public void createAlarmWithNonExistentTag() {
//    // Setup Exception
//    alarmException.expect(ConfigurationParseException.class);
//
//    Alarm alarm = buildCreateBasicAlarm(null);
//    alarm.setId(200L);
//
//    List<Alarm> alarmList = Arrays.asList(alarm);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(alarmList);
//
//    // setUp Mocks:
//    EasyMock.expect(tagFacadeGateway.isInTagCache(100L)).andReturn(false);
//
//    // run test
//    EasyMock.replay(tagFacadeGateway);
//    parser.parse(config);
//    EasyMock.verify(tagFacadeGateway);
//  }
//
//  @Test
//  public void createExistingAlarm() {
//    // Setup Exception
//    alarmException.expect(ConfigurationParseException.class);
//
//    Alarm alarm = buildCreateBasicAlarm(null);
//    alarm.setId(200L);
//
//    List<Alarm> alarmList = Arrays.asList(alarm);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(alarmList);
//
//    // setUp Mocks:
//    EasyMock.expect(tagFacadeGateway.isInTagCache(100L)).andReturn(true);
//    EasyMock.expect(alarmCache.hasKey(200L)).andReturn(true);
//
//    // run test
//    EasyMock.replay(alarmCache, tagFacadeGateway);
//    parser.parse(config);
//    EasyMock.verify(alarmCache, tagFacadeGateway);
//  }
//
//  @Test
//  public void updateAlarmWithAllFields() {
//    // setup Configuration:
//    Properties expectedProps = new Properties();
//    Alarm alarm = buildUpdateAlarmWithAllFields(200L, expectedProps);
//
//    List<Alarm> alarmUpdateList = Arrays.asList(alarm);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(alarmUpdateList);
//
//    // setUp Mocks:
//    EasyMock.expect(alarmCache.hasKey(200L)).andReturn(true);
//
//    EasyMock.replay(alarmCache);
//
//    List<ConfigurationElement> parsed = parser.parse(config);
//
//    assertEquals((long) parsed.get(0).getEntityId(), 200L);
//    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.ALARM);
//    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.UPDATE);
//    assertEquals(expectedProps, parsed.get(0).getElementProperties());
//
//    EasyMock.verify(alarmCache);
//  }
//
//  @Test
//  public void updateNonExistentAlarm() {
//    // Setup Exception
//    //alarmException.expect(ConfigurationParseException.class);
//
//    // setup Configuration:
//    Alarm alarm = Alarm.update(200L).updateMetadata(null, null).build();
//
//    List<Alarm> alarmUpdateList = Arrays.asList(alarm);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(alarmUpdateList);
//
//    // setUp Mocks:
//    EasyMock.expect(alarmCache.hasKey(200L)).andReturn(false);
//
//    // run test
//    EasyMock.replay(alarmCache);
//    parser.parse(config);
//    EasyMock.verify(alarmCache);
//  }
//
//  @Test
//  public void deleteAlarm() {
//    // setup Configuration:
//    Alarm alarm = buildDeleteAlarm(200L);
//
//    List<Alarm> alarmRemoveList = Arrays.asList(alarm);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(alarmRemoveList);
//
//    // setUp Mocks:
//    EasyMock.expect(alarmCache.hasKey(200L)).andReturn(true);
//
//    EasyMock.replay(alarmCache);
//
//    List<ConfigurationElement> parsed = parser.parse(config);
//
//    assertEquals(1, parsed.size());
//    assertEquals((long) parsed.get(0).getEntityId(), 200L);
//    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.REMOVE);
//    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.ALARM);
//    assertTrue(parsed.get(0).getElementProperties().isEmpty());
//
//    EasyMock.verify(alarmCache);
//  }
//
//  @Test
//  public void deleteNonExistentAlarm() {
//    // setup Configuration:
//    Alarm alarm = buildDeleteAlarm(200L);
//
//    List<Alarm> alarmRemoveList = Arrays.asList(alarm);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(alarmRemoveList);
//
//    // setUp Mocks:
//    EasyMock.expect(alarmCache.hasKey(200L)).andReturn(false);
//    EasyMock.replay(alarmCache);
//
//    assertEquals(0, parser.parse(config).size());
//    EasyMock.verify(alarmCache);
//  }
//}
