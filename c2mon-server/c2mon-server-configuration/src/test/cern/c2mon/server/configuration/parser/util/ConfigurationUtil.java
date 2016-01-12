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
package cern.c2mon.server.configuration.parser.util;

import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.alarm.Alarm;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.equipment.SubEquipment;
import cern.c2mon.shared.client.configuration.api.process.Process;
import cern.c2mon.shared.client.configuration.api.tag.*;

import static cern.c2mon.server.configuration.parser.util.ConfigurationAliveTagUtil.builderAliveTagWithPrimFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationCommFaultTagUtil.builderCommFaultTagWithPrimFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationEquipmentUtil.builderEquipmentWithPrimFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationProcessUtil.builderProcessWithPrimFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationStatusTagUtil.builderStatusTagWithPrimFields;

public class ConfigurationUtil {

  public static Configuration.ConfigurationBuilder getConfBuilder() {
    return Configuration.builder().application("configuration test - application").name("configuration test name");
  }

  public static Configuration getConfBuilderProcess(Process... elements) {
    Configuration.ConfigurationBuilder tempBuild = getConfBuilder();
    for (Process element : elements) {
      tempBuild.process(element);
    }
    return tempBuild.build();
  }

  public static Configuration getConfBuilderEquipment(Equipment... elements) {
    Process.ProcessBuilder tempBuild = Process.builder().id(1L);
    for (Equipment element : elements) {
      tempBuild.equipment(element);
    }
    return getConfBuilder().process(tempBuild.build()).build();
  }

  public static Configuration getConfBuilderSubEquipment(SubEquipment... elements) {
    Equipment.EquipmentBuilder tempBuild = Equipment.builder().id(1L);
    for (SubEquipment element : elements) {
      tempBuild.subEquipment(element);
    }
    return getConfBuilder().process(Process.builder().id(1L).equipment(tempBuild.build()).build()).build();
  }

  public static Configuration getConfBuilderSubEquipment(Long equipId, Long processId, SubEquipment... elements) {
    Equipment.EquipmentBuilder tempBuild = Equipment.builder().id(equipId);
    for (SubEquipment element : elements) {
      tempBuild.subEquipment(element);
    }
    return getConfBuilder().process(Process.builder().id(processId).equipment(tempBuild.build()).build()).build();
  }

  public static Configuration getConfBuilderRuleTag(RuleTag... elements) {
    Configuration.ConfigurationBuilder tempBuild = getConfBuilder();
    for (RuleTag element : elements) {
      tempBuild.rule(element);
    }
    return tempBuild.build();
  }


  /**
   * Retrieve a ConfigurationObject, which holds only a list of CommFaultTags attached to a Process.
   * The configuration also provides a create status and alive tag.
   *
   * @param elements List of CommFaultTags which are build in the ConfigurationObject
   * @return final ConfigurationObject with the CommFaultTag objects
   */
  public static Configuration getConfBuilderCommFaultTagE(CommFaultTag... elements) {
    AliveTag.AliveTagBuilder alive = builderAliveTagWithPrimFields(1L, "equipment", 1L)._1;
    StatusTag.StatusTagBuilder status = builderStatusTagWithPrimFields(1L, "equipment", 1L)._1;
    Equipment.EquipmentBuilder equipment = builderEquipmentWithPrimFields(1L, 1L, 1L, 1L, 1L)._1;
    equipment.aliveTag(alive.build()).statusTag(status.build());
    for (CommFaultTag element : elements) {
      equipment.commFaultTag(element);
    }
    return getConfBuilder().process(Process.builder().id(1L).equipment(equipment.build()).build()).build();
  }

  /**
   * Retrieve a ConfigurationObject, which holds only a list of CommFaultTags attached to a Process
   *
   * @param elements List of CommFaultTags which are build in the ConfigurationObject
   * @return final ConfigurationObject with the CommFaultTag objects
   */
  public static Configuration getConfBuilderCommFaultTagUpdate(CommFaultTag... elements) {
    Equipment.EquipmentBuilder tempBuild = Equipment.builder().id(1L);
    for (CommFaultTag element : elements) {
      tempBuild.commFaultTag(element);
    }
    return getConfBuilder().process(Process.builder().id(1L).equipment(tempBuild.build()).build()).build();
  }

  /**
   * Retrieve a ConfigurationObject, which holds only a list of AliveTag attached to a Process
   *
   * @param elements List of AliveTags which are build in the ConfigurationObject
   * @return final ConfigurationObject with the AliveTag objects
   */
  public static Configuration getConfBuilderAliveTagP(AliveTag... elements) {
//    Process.ProcessBuilder tempBuild = Process.builder().id(1L);
    StatusTag.StatusTagBuilder status = builderStatusTagWithPrimFields(1L, "process", 1L)._1;
    Process.ProcessBuilder process = builderProcessWithPrimFields(1L, 1L, 1L)._1;
    process.statusTag(status.build());
    for (AliveTag element : elements) {
      process.aliveTag(element);
    }
    return getConfBuilder().process(process.build()).build();
  }

  /**
   * Retrieve a ConfigurationObject, which holds only a AliveTag attached to a Equipment.
   * This method should only used for updates.
   *
   * @param element AliveTag which is attached to the ConfigurationObject
   * @return final ConfigurationObject with the AliveTag object
   */
  public static Configuration getConfBuilderAliveTagEUpdate(Long equipmentId, Long processId, AliveTag element) {
    Equipment.EquipmentBuilder tempBuild = Equipment.builder().id(equipmentId);
    tempBuild.aliveTag(element);
    return getConfBuilder().process(Process.builder().id(processId).equipment(tempBuild.build()).build()).build();
  }

  /**
   * Retrieve a ConfigurationObject, which holds only a StatusTag attached to a Equipment.
   * This method should only used for updates.
   *
   * @param element StatusTag which is attached to the ConfigurationObject
   * @return final ConfigurationObject with the StatusTag object
   */
  public static Configuration getConfBuilderStatusTagEUpdate(Long equipmentId, Long processId, StatusTag element) {
    Equipment.EquipmentBuilder tempBuild = Equipment.builder().id(equipmentId);
    tempBuild.statusTag(element);
    return getConfBuilder().process(Process.builder().id(processId).equipment(tempBuild.build()).build()).build();
  }

  /**
   * Retrieve a ConfigurationObject, which holds only a CommFaultTag attached to a Equipment.
   * This method should only used for updates.
   *
   * @param element CommFaultTag which is attached to the ConfigurationObject
   * @return final ConfigurationObject with the CommFaultTag object
   */
  public static Configuration getConfBuilderCommFaultTagEUpdate(Long equipmentId, Long processId, CommFaultTag element) {
    Equipment.EquipmentBuilder tempBuild = Equipment.builder().id(equipmentId);
    tempBuild.commFaultTag(element);
    return getConfBuilder().process(Process.builder().id(processId).equipment(tempBuild.build()).build()).build();
  }

  /**
   * Retrieve a ConfigurationObject, which holds only a list of AliveTag attached to a Process
   *
   * @param elements List of AliveTags which are build in the ConfigurationObject
   * @return final ConfigurationObject with the AliveTag objects
   */
  public static Configuration getConfBuilderAliveTagUpdate(AliveTag... elements) {
    Process.ProcessBuilder tempBuild = Process.builder().id(1L);
    for (AliveTag element : elements) {
      tempBuild.aliveTag(element);
    }
    return getConfBuilder().process(tempBuild.build()).build();
  }

  public static Configuration getConfBuilderStatusTagP(StatusTag... elements) {
    AliveTag.AliveTagBuilder alive = builderAliveTagWithPrimFields(1L, "process", 1L)._1;
    Process.ProcessBuilder process = builderProcessWithPrimFields(1L, 1L, 1L)._1;
    process.aliveTag(alive.build());
    for (StatusTag element : elements) {
      process.statusTag(element);
    }
    return getConfBuilder().process(process.build()).build();
  }

  public static Configuration getConfBuilderStatusTagUpdate(StatusTag... elements) {
    Process.ProcessBuilder tempBuild = Process.builder().id(1L);
    for (StatusTag element : elements) {
      tempBuild.statusTag(element);
    }
    return getConfBuilder().process(tempBuild.build()).build();
  }


  /**
   * Retrieve a ConfigurationObject, which holds only a list of DataTag attached to a Equipment
   *
   * @param elements List of DataTag which are build in the ConfigurationObject
   * @return final ConfigurationObject with the DataTag objects
   */
  @SafeVarargs
  public static Configuration getConfBuilderDataTagE(DataTag<Number>... elements) {
    Equipment.EquipmentBuilder tempBuild = Equipment.builder().id(1L);
    for (DataTag<Number> element : elements) {
      tempBuild.dataTag(element);
    }
    return getConfBuilder().process(Process.builder().id(1L).equipment(tempBuild.build()).build()).build();
  }

  @SafeVarargs
  public static Configuration getConfBuilderDataTagE(Long equipmentId, Long processId, DataTag<Number>... elements) {
    Equipment.EquipmentBuilder tempBuild = Equipment.builder().id(equipmentId);
    for (DataTag<Number> element : elements) {
      tempBuild.dataTag(element);
    }
    return getConfBuilder().process(Process.builder().id(processId).equipment(tempBuild.build()).build()).build();
  }

  @SafeVarargs
  public static Configuration getConfBuilderDataTagS(Long subEquipmentId, Long equipmentId, Long processId, DataTag<Number>... elements) {
    Equipment.EquipmentBuilder tempBuildE = Equipment.builder().id(equipmentId);
    SubEquipment.SubEquipmentBuilder tempBuildS = SubEquipment.builderSubEquipment().id(subEquipmentId);
    for (DataTag<Number> element : elements) {
      tempBuildS.dataTag(element);
    }
    return getConfBuilder().process(Process.builder().id(processId).equipment(tempBuildE.subEquipment(tempBuildS.build()).build()).build()).build();
  }

  public static Configuration getConfBuilderCommandTag(Long equipmentId, Long processId, CommandTag... elements) {
    Equipment.EquipmentBuilder tempBuild = Equipment.builder().id(equipmentId);
    for (CommandTag element : elements) {
      tempBuild.commandTag(element);
    }
    return getConfBuilder().process(Process.builder().id(processId).equipment(tempBuild.build()).build()).build();
  }

  /**
   * Retrieve a ConfigurationObject, which holds only a list of Alarms attached to Process -> Equipment -> SubEquipment -> DataTag
   *
   * @param elements List of Alarm which are build in the ConfigurationObject
   * @return final ConfigurationObject with the DataTag objects
   */
  public static Configuration getConfBuilderAlarm(Alarm... elements) {
    DataTag.DataTagBuilder tempBuild = DataTag.builder().id(1L);
    for (Alarm element : elements) {
      tempBuild.alarm(element);
    }
    return getConfBuilder().process(
        Process.builder().id(1L).equipment(
            Equipment.builder().id(1L).dataTag(
                tempBuild.build()).build()).build()).build();
  }

  /**
   * Retrieve a ConfigurationObject, which holds only a list of Alarms attached to Process -> Equipment -> SubEquipment -> DataTag
   * Determines also the ids of the parents
   *
   * @param elements List of Alarm which are build in the ConfigurationObject
   * @return final ConfigurationObject with the DataTag objects
   */
  public static Configuration getConfBuilderAlarm(Long datatTagId, Long equipmentId, Long processId, Alarm... elements) {
    DataTag.DataTagBuilder tempBuild = DataTag.builder().id(datatTagId);
    for (Alarm element : elements) {
      tempBuild.alarm(element);
    }
    return getConfBuilder().process(
        Process.builder().id(processId).equipment(
            Equipment.builder().id(equipmentId).dataTag(
                tempBuild.build()).build()).build()).build();
  }
}
