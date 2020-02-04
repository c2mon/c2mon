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

package cern.c2mon.server.configuration.util;

import cern.c2mon.server.configuration.parser.util.ConfigurationProcessUtil;
import cern.c2mon.server.configuration.parser.util.ConfigurationRuleTagUtil;
import cern.c2mon.shared.client.alarm.condition.ValueAlarmCondition;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.alarm.Alarm;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.equipment.SubEquipment;
import cern.c2mon.shared.client.configuration.api.process.Process;
import cern.c2mon.shared.client.configuration.api.tag.*;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.address.impl.SimpleHardwareAddressImpl;

/**
 * Created by fritter on 27/05/16.
 */
public class TestConfigurationProvider {
  /**
   * Process
   * id: 5
   * statusTagId: 100
   * aliveTagId: 101
   */
  public static Configuration createProcess(){
    Configuration configuration = new Configuration();
    Process process = Process.create("P_INI_TEST").id(5L)
        .statusTag(StatusTag.create("P:STATUS").id(100L).build())
        .aliveTag(AliveTag.create("P:ALIVE").id(101L).build(), 60000).build();

    configuration.addEntity(process);
    return configuration;
  }

  public static Configuration deleteProcess(){
    Configuration configuration = new Configuration();
    Process process = ConfigurationProcessUtil.buildDeleteProcess(5L);
    configuration.addEntity(process);
    return configuration;
  }

  public static Configuration deleteRuleTag(){
    Configuration configuration = new Configuration();
    RuleTag rule = ConfigurationRuleTagUtil.buildDeleteRuleTag(1500L);
    configuration.addEntity(rule);
    return configuration;
  }

  public static Configuration deleteDataTag(){
    Configuration configuration = new Configuration();
    DataTag dataTag = new DataTag();
    dataTag.setId(1000L);
    dataTag.setDeleted(true);
    configuration.addEntity(dataTag);
    return configuration;
  }

  /**
   * Equipment
   * id: 15
   * statusTagId: 200
   * commFaultTagId: 201
   */
  public static Configuration createEquipment(){
    Configuration configuration = new Configuration();
    Equipment equipment = Equipment.create("E_INI_TEST", "handlerClass").id(15L)
        .statusTag(StatusTag.create("E:STATUS").id(200L).build())
        .commFaultTag(CommFaultTag.create("E:Comm").id(201L).build()).build();
    equipment.setProcessId(5L);

    configuration.addEntity(equipment);
    return configuration;
  }

  /**
   * SubEquipment
   * id: 26
   * statusTagId: 300
   * commFaultTagId: 301
   * aliveTagId: 302
   */
  public static Configuration createSubEquipment(){
    Configuration configuration = new Configuration();
    SubEquipment subEquipment = SubEquipment.create("SE_INI_TEST").id(25L)
        .statusTag(StatusTag.create("SE:STATUS").id(300L).build())
        .commFaultTag(CommFaultTag.create("SE:Comm").id(301L).build())
        .aliveTag(AliveTag.create("SE:Alive").id(302L).address(new DataTagAddress()).build(), 60000).build();
    subEquipment.setEquipmentId(15L);

    configuration.addEntity(subEquipment);
    return configuration;
  }

  /**
   * DataTag
   * id: 1000
   */
  public static Configuration createEquipmentDataTag(Long parentId){
    Configuration configuration = new Configuration();
    DataTag dataTag = DataTag.create("DATA_INI_TEST",Integer.class, new DataTagAddress()).equipmentId(parentId).id(1000L).build();

    configuration.addEntity(dataTag);
    return configuration;
  }

  /**
   * DataTag
   * id: 1000
   */
  public static Configuration createSubEquipmentDataTag(Long parentId){
    Configuration configuration = new Configuration();
    DataTag dataTag = DataTag.create("DATA_INI_TEST",Integer.class, new DataTagAddress()).subEquipmentId(parentId).id(1000L).build();

    configuration.addEntity(dataTag);
    return configuration;
  }

  /**
   * RuleTag
   * id: 1500
   * datTagId: 1000
   */
  public static Configuration createRuleTag(){
    Configuration configuration = new Configuration();
    RuleTag ruleTag = RuleTag.create("RuleTag", Integer.class, "(#1000 < 0)|(#1000 > 200)[1],true[0]").id(1500L).build();

    configuration.addEntity(ruleTag);
    return configuration;
  }

  /**
   * CommandTag
   * id: 500
   * equipmentId: 15
   */
  public static Configuration createCommandTag(){
    Configuration configuration = new Configuration();
    CommandTag commandTag = CommandTag.create("CommandTag", Integer.class, new SimpleHardwareAddressImpl("testAddress"),
        30000, 6000, 200, 2, "RBAC class", "RBAC device", "RBAC property").id(500L).equipmentId(15L).build();

    configuration.addEntity(commandTag);
    return configuration;
  }

  /**
   * Alarm
   * id: 2000
   * datTagId: 1000
   */
  public static Configuration createAlarm(){
    Configuration configuration = new Configuration();
    Alarm alarm = Alarm.create("faultFamily", "faultMember", 1337, new ValueAlarmCondition(1))
        .id(2000L)
        .dataTagId(1000L).build();

    configuration.addEntity(alarm);
    return configuration;
  }


}
