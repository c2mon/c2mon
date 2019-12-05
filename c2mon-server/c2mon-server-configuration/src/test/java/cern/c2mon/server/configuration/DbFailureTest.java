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

import cern.c2mon.server.cache.*;
import cern.c2mon.server.cache.config.CacheModule;
import cern.c2mon.server.cache.dbaccess.*;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModuleRef;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.configuration.config.ConfigurationModule;
import cern.c2mon.server.configuration.config.ProcessCommunicationManagerMock;
import cern.c2mon.server.configuration.handler.impl.ProcessConfigHandlerImpl;
import cern.c2mon.server.configuration.junit.ConfigurationCachePopulationRule;
import cern.c2mon.server.configuration.junit.ConfigurationDatabasePopulationRule;
import cern.c2mon.server.daq.config.DaqModule;
import cern.c2mon.server.daq.update.JmsContainerManagerImpl;
import cern.c2mon.server.rule.config.RuleModule;
import cern.c2mon.server.supervision.config.SupervisionModule;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
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
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    CommonModule.class,
    CacheModule.class,
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
  public ConfigurationCachePopulationRule configurationCachePopulationRule;

  private IMocksControl mockControl = EasyMock.createNiceControl();

  @Autowired
  private ProcessConfigHandlerImpl processConfigHandler;

  @Autowired
  private ConfigurationLoader configurationLoader;

  @Autowired
  private DataTagCache dataTagCache;

  @Autowired
  private DataTagMapper dataTagMapper;

  @Autowired
  private ControlTagCache controlTagCache;

  @Autowired
  private ControlTagMapper controlTagMapper;

  @Autowired
  private CommandTagCache commandTagCache;

  @Autowired
  private CommandTagMapper commandTagMapper;

  @Autowired
  private RuleTagCache ruleTagCache;

  @Autowired
  private RuleTagMapper ruleTagMapper;

  @Autowired
  private EquipmentCache equipmentCache;

  @Autowired
  private EquipmentMapper equipmentMapper;

  @Autowired
  private SubEquipmentCache subEquipmentCache;

  @Autowired
  private SubEquipmentMapper subEquipmentMapper;

  @Autowired
  private ProcessCache processCache;

  @Autowired
  private ProcessMapper processMapper;

  @Autowired
  private AliveTimerCache aliveTimerCache;

  @Autowired
  private CommFaultTagCache commFaultTagCache;

  @Autowired
  private AlarmCache alarmCache;

  @Autowired
  private AlarmMapper alarmMapper;

  @Autowired
  private ProcessFacade processFacade;

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
    ProcessConfigTransacted processConfigTransacted = mockControl.createMock(ProcessConfigTransacted.class);
    processConfigHandler.setProcessConfigTransacted(processConfigTransacted);
    processConfigTransacted.remove(EasyMock.isA(Process.class), EasyMock.isA(ConfigurationElementReport.class));
    EasyMock.expectLastCall().andThrow(new RuntimeException("fake exception thrown"));

    mockControl.replay();

    ConfigurationReport report = configurationLoader.applyConfiguration(28);
    assertTrue(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    //check all is removed except Process & its Control tags
    //check process is still here
    assertTrue(processCache.hasKey(50L));
    assertNotNull(processMapper.getItem(50L));
    //equipment is gone
    assertFalse(equipmentCache.hasKey(150L));
    assertNull(equipmentMapper.getItem(150L));
    //rules gone
    assertFalse(ruleTagCache.hasKey(60010L));
    assertNull(ruleTagMapper.getItem(60010L));
    assertFalse(ruleTagCache.hasKey(60002L));
    assertNull(ruleTagMapper.getItem(60002L));
    //tags
    assertFalse(dataTagCache.hasKey(200002L));
    assertNull(dataTagMapper.getItem(200002L));
    assertFalse(dataTagCache.hasKey(200003L));
    assertNull(dataTagMapper.getItem(200003L));
    //process control tags are still here!
    assertTrue(controlTagCache.hasKey(1220L));
    assertNotNull(controlTagMapper.getItem(1220L));
    assertTrue(controlTagCache.hasKey(1221L));
    assertNotNull(controlTagMapper.getItem(1221L));
    //equipment control tags are gone
    assertFalse(controlTagCache.hasKey(1222L));
    assertNull(controlTagMapper.getItem(1222L));
    assertFalse(controlTagCache.hasKey(1223L));
    assertNull(controlTagMapper.getItem(1223L));
    //equipment commfault is gone
    assertFalse(commFaultTagCache.hasKey(1223L));
    //process alive is still here (may be active or not, depending on status when removed)
    assertTrue(aliveTimerCache.hasKey(1221L));
    //alarms all gone
    assertFalse(alarmCache.hasKey(350000L));
    assertNull(alarmMapper.getItem(350000L));
    assertFalse(alarmCache.hasKey(350001L));
    assertNull(alarmMapper.getItem(350001L));

    mockControl.verify();
  }
}
