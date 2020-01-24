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
import cern.c2mon.server.cache.dbaccess.*;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.configuration.handler.transacted.ProcessConfigHandler;
import cern.c2mon.server.configuration.junit.ConfigRuleChain;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.common.command.CommandTag;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;

import javax.inject.Inject;

import static org.junit.Assert.*;

/**
 * Tests the ConfigHandler's when DB persitence fails, for instance when a constraint
 * if violated. Checks the cache is left in a consistent state.
 *
 * @author Mark Brightwell
 *
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class DbFailureTest extends ConfigurationCacheTest {

  @Rule
  @Inject
  public ConfigRuleChain configRuleChain;

  private IMocksControl mockControl = EasyMock.createNiceControl();

  @Inject
  private ProcessConfigHandler processConfigHandler;

  @Inject
  private ConfigurationLoader configurationLoader;

  @Inject
  private C2monCache<DataTag> dataTagCache;

  @Inject
  private DataTagMapper dataTagMapper;

  @Inject
  private C2monCache<CommandTag> commandTagCache;

  @Inject
  private CommandTagMapper commandTagMapper;

  @Inject
  private C2monCache<RuleTag> ruleTagCache;

  @Inject
  private RuleTagMapper ruleTagMapper;

  @Inject
  private C2monCache<Equipment> equipmentCache;

  @Inject
  private EquipmentMapper equipmentMapper;

  @Inject
  private C2monCache<SubEquipment> subEquipmentCache;

  @Inject
  private SubEquipmentMapper subEquipmentMapper;

  @Inject
  private C2monCache<Process> processCache;

  @Inject
  private ProcessMapper processMapper;

  @Inject
  private C2monCache<AliveTag> aliveTimerCache;

  @Inject
  private C2monCache<CommFaultTag> commFaultTagCache;

  @Inject
  private C2monCache<Alarm> alarmCache;

  @Inject
  private AlarmMapper alarmMapper;

  @Before
  public void init() {
    mockControl.reset();
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
