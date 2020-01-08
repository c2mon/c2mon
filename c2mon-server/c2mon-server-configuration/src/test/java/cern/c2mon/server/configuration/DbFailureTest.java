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
package cern.c2mon.server.configuration;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.config.CacheConfigModuleRef;
import cern.c2mon.server.cache.dbaccess.*;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModuleRef;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.configuration.config.ConfigurationModule;
import cern.c2mon.server.configuration.config.ProcessCommunicationManagerMock;
import cern.c2mon.server.configuration.handler.transacted.ProcessConfigHandler;
import cern.c2mon.server.configuration.junit.ConfigurationDatabasePopulationRule;
import cern.c2mon.server.daq.config.DaqModule;
import cern.c2mon.server.daq.update.JmsContainerManagerImpl;
import cern.c2mon.server.rule.config.RuleModule;
import cern.c2mon.server.supervision.config.SupervisionModule;
import cern.c2mon.server.test.CachePopulationRule;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.common.command.CommandTag;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * Tests the ConfigHandler's when DB persitence fails, for instance when a constraint
 * if violated. Checks the cache is left in a consistent state.
 *
 * @author Mark Brightwell
 *
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    CommonModule.class,
    CacheConfigModuleRef.class,
    CacheDbAccessModule.class,
    CacheLoadingModuleRef.class,
    SupervisionModule.class,
    ConfigurationModule.class,
    DaqModule.class,
    RuleModule.class,
    ProcessCommunicationManagerMock.class
})
public class DbFailureTest {

  @Rule
  @Autowired
  public ConfigurationDatabasePopulationRule populationRule;

  @Rule
  @Autowired
  public CachePopulationRule configurationCachePopulationRule;

  private IMocksControl mockControl = EasyMock.createNiceControl();

  @Autowired
  private ProcessConfigHandler processConfigHandler;

  @Autowired
  private ConfigurationLoader configurationLoader;

  @Autowired
  private C2monCache<DataTag> dataTagCache;

  @Autowired
  private DataTagMapper dataTagMapper;

//  @Autowired
//  private ControlTagCache controlTagCache;

  @Autowired
  private ControlTagMapper controlTagMapper;

  @Autowired
  private C2monCache<CommandTag> commandTagCache;

  @Autowired
  private CommandTagMapper commandTagMapper;

  @Autowired
  private C2monCache<RuleTag> ruleTagCache;

  @Autowired
  private RuleTagMapper ruleTagMapper;

  @Autowired
  private C2monCache<Equipment> equipmentCache;

  @Autowired
  private EquipmentMapper equipmentMapper;

  @Autowired
  private C2monCache<SubEquipment> subEquipmentCache;

  @Autowired
  private SubEquipmentMapper subEquipmentMapper;

  @Autowired
  private C2monCache<Process> processCache;

  @Autowired
  private ProcessMapper processMapper;

  @Autowired
  private C2monCache<AliveTag> aliveTimerCache;

  @Autowired
  private C2monCache<CommFaultTag> commFaultTagCache;

  @Autowired
  private C2monCache<Alarm> alarmCache;

  @Autowired
  private AlarmMapper alarmMapper;

  @Autowired
  private JmsContainerManagerImpl jmsContainerManager;

  @Before
  public void init() {
    mockControl.reset();
  }

  @After
  public void cleanUp() {
    // Make sure the JmsContainerManager is stopped, otherwise the
    // DefaultMessageListenerContainers inside will keep trying to connect to
    // a JMS broker (which will not be running)
    jmsContainerManager.stop();
  }

  /**
   * Tests the system is left in the correct consistent state if the removal of
   * the Process from the DB fails.
   */
  @Test
  @DirtiesContext
  public void testDBPersistenceFailure() {
    //reset ProcessConfigTransacted to mock
    ProcessConfigHandler processConfigTransacted = mockControl.createMock(ProcessConfigHandler.class);
    processConfigTransacted.remove(EasyMock.isA(Long.class), EasyMock.isA(ConfigurationElementReport.class));
    EasyMock.expectLastCall().andThrow(new RuntimeException("fake exception thrown"));

    mockControl.replay();

    ConfigurationReport report = configurationLoader.applyConfiguration(28);
    assertTrue(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    //check all is removed except Process & its Control tags
    //check process is still here
    assertTrue(processCache.containsKey(50L));
    assertNotNull(processMapper.getItem(50L));
    //equipment is gone
    assertFalse(equipmentCache.containsKey(150L));
    assertNull(equipmentMapper.getItem(150L));
    //rules gone
    assertFalse(ruleTagCache.containsKey(60010L));
    assertNull(ruleTagMapper.getItem(60010L));
    assertFalse(ruleTagCache.containsKey(60002L));
    assertNull(ruleTagMapper.getItem(60002L));
    //tags
    assertFalse(dataTagCache.containsKey(200002L));
    assertNull(dataTagMapper.getItem(200002L));
    assertFalse(dataTagCache.containsKey(200003L));
    assertNull(dataTagMapper.getItem(200003L));
    //process control tags are still here!
//    assertTrue(controlTagCache.containsKey(1220L));
    assertNotNull(controlTagMapper.getItem(1220L));
//    assertTrue(controlTagCache.containsKey(1221L));
    assertNotNull(controlTagMapper.getItem(1221L));
    //equipment control tags are gone
//    assertFalse(controlTagCache.containsKey(1222L));
    assertNull(controlTagMapper.getItem(1222L));
//    assertFalse(controlTagCache.containsKey(1223L));
    assertNull(controlTagMapper.getItem(1223L));
    //equipment commfault is gone
    assertFalse(commFaultTagCache.containsKey(1223L));
    //process alive is still here (may be active or not, depending on status when removed)
    assertTrue(aliveTimerCache.containsKey(1221L));
    //alarms all gone
    assertFalse(alarmCache.containsKey(350000L));
    assertNull(alarmMapper.getItem(350000L));
    assertFalse(alarmCache.containsKey(350001L));
    assertNull(alarmMapper.getItem(350001L));

    mockControl.verify();
  }
}
