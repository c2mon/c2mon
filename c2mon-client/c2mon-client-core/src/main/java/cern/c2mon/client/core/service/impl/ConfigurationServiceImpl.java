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
package cern.c2mon.client.core.service.impl;

import java.util.*;

import javax.jms.JMSException;

import cern.c2mon.shared.client.configuration.api.device.Device;
import cern.c2mon.shared.client.configuration.api.device.DeviceClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.listener.ClientRequestReportListener;
import cern.c2mon.client.core.configuration.*;
import cern.c2mon.client.core.jms.RequestHandler;
import cern.c2mon.client.core.service.ConfigurationService;
import cern.c2mon.shared.client.alarm.condition.AlarmCondition;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.ConfigurationReportHeader;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.alarm.Alarm;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.equipment.SubEquipment;
import cern.c2mon.shared.client.configuration.api.process.Process;
import cern.c2mon.shared.client.configuration.api.tag.*;
import cern.c2mon.shared.client.process.ProcessNameResponse;
import cern.c2mon.shared.client.tag.TagConfig;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;

@Service("configurationService")
@Slf4j
public class ConfigurationServiceImpl implements ConfigurationService {

  private RequestHandler clientRequestHandler;

  private ConfigurationRequestSender configurationRequestSender;

  private ProcessConfigurationManager processConfigurationManager;

  private EquipmentConfigurationManager equipmentConfigurationManager;

  private SubEquipmentConfigurationManager subEquipmentConfigurationManager;

  private DataTagConfigurationManager dataTagConfigurationManager;

  private RuleTagConfigurationManager ruleTagConfigurationManager;

  private AlarmConfigurationManager alarmConfigurationManager;

  private ControlTagConfigurationManager controlTagConfigurationManager;

  private CommandTagConfigurationManager commandTagConfigurationManager;

  private DeviceClassConfigurationManager deviceClassConfigurationManager;

  private DeviceConfigurationManager deviceConfigurationManager;

  @Autowired
  protected ConfigurationServiceImpl(final @Qualifier("coreRequestHandler") RequestHandler requestHandler,
                                     final ConfigurationRequestSender configurationRequestSender,
                                     ProcessConfigurationManager processConfigurationManager,
                                     DeviceClassConfigurationManager deviceClassConfigurationManager,
                                     EquipmentConfigurationManager equipmentConfigurationManager,
                                     SubEquipmentConfigurationManager subEquipmentConfigurationManager,
                                     DataTagConfigurationManager dataTagConfigurationManager,
                                     RuleTagConfigurationManager ruleTagConfigurationManager,
                                     AlarmConfigurationManager alarmConfigurationManager,
                                     ControlTagConfigurationManager controlTagConfigurationManager,
                                     CommandTagConfigurationManager commandTagConfigurationManager,
                                     DeviceConfigurationManager deviceConfigurationManager) {
    this.clientRequestHandler = requestHandler;
    this.configurationRequestSender = configurationRequestSender;
    this.processConfigurationManager = processConfigurationManager;
    this.equipmentConfigurationManager = equipmentConfigurationManager;
    this.subEquipmentConfigurationManager = subEquipmentConfigurationManager;
    this.dataTagConfigurationManager = dataTagConfigurationManager;
    this.ruleTagConfigurationManager = ruleTagConfigurationManager;
    this.alarmConfigurationManager = alarmConfigurationManager;
    this.controlTagConfigurationManager = controlTagConfigurationManager;
    this.commandTagConfigurationManager = commandTagConfigurationManager;
    this.deviceClassConfigurationManager = deviceClassConfigurationManager;
    this.deviceConfigurationManager = deviceConfigurationManager;
  }

  @Override
  public ConfigurationReport applyConfiguration(final Long configurationId) {
    return clientRequestHandler.applyConfiguration(configurationId);
  }

  @Override
  public ConfigurationReport applyConfiguration(Long configurationId, ClientRequestReportListener reportListener) {
    return clientRequestHandler.applyConfiguration(configurationId, reportListener);
  }

  @Override
  public ConfigurationReport applyConfiguration(Configuration configuration, ClientRequestReportListener listener) {
    return configurationRequestSender.applyConfiguration(configuration, listener);
  }

  @Override
  public Collection<ConfigurationReportHeader> getConfigurationReports() {
    try {
      return clientRequestHandler.getConfigurationReports();
    } catch (JMSException e) {
      log.error("getConfigurationReports() - JMS connection lost -> Could not retrieve configuration reports from the" +
          " C2MON server.", e);
    }
    return new ArrayList<>();
  }

  @Override
  public Collection<ConfigurationReport> getConfigurationReports(Long id) {
    try {
      return clientRequestHandler.getConfigurationReports(id);
    } catch (JMSException e) {
      log.error("getConfigurationReports() - JMS connection lost -> Could not retrieve configuration reports from the" +
          " C2MON server.", e);
    }
    return new ArrayList<>();
  }

  @Override
  public Collection<ProcessNameResponse> getProcessNames() {

    try {
      return clientRequestHandler.getProcessNames();
    } catch (JMSException e) {
      log.error("getProcessNames() - JMS connection lost -> Could not retrieve process names from the C2MON server.",
          e);
    }
    return new ArrayList<>();
  }

  @Override
  public Collection<TagConfig> getTagConfigurations(final Collection<Long> tagIds) {

    try {
      // no cache for Tag Configurations => fetch them from the server
      return clientRequestHandler.requestTagConfigurations(tagIds);
    } catch (JMSException e) {
      log.error("getTagConfigurations() - JMS connection lost -> Could not retrieve missing tags from the C2MON " +
          "server.", e);
    }
    return new ArrayList<>();
  }

  @Override
  public String getProcessXml(final String processName) {

    try {
      return clientRequestHandler.getProcessXml(processName);
    } catch (JMSException e) {
      log.error("getProcessXml() - JMS connection lost -> Could not retrieve missing tags from the C2MON server.", e);
    }
    return null;
  }

  @Override
  public ConfigurationReport createProcess(String processName) {
    return processConfigurationManager.createProcess(processName);
  }

  @Override
  public ConfigurationReport createProcess(Process process) {
    return processConfigurationManager.createProcess(process);
  }

  @Override
  public ConfigurationReport updateProcess(Process process) {
    return processConfigurationManager.updateProcess(process);
  }

  @Override
  public ConfigurationReport removeProcessById(Long id) {
    return processConfigurationManager.removeProcessById(id);
  }

  @Override
  public ConfigurationReport removeProcess(String name) {
    return processConfigurationManager.removeProcess(name);
  }

  @Override
  public ConfigurationReport createEquipment(String processName, String name, String handlerClass) {
    return equipmentConfigurationManager.createEquipment(processName, name, handlerClass);
  }

  @Override
  public ConfigurationReport createEquipment(String processName, Equipment equipment) {
    return equipmentConfigurationManager.createEquipment(processName, equipment);
  }

  @Override
  public ConfigurationReport createEquipment(String processName, List<Equipment> equipments) {
    return equipmentConfigurationManager.createEquipment(processName, equipments);
  }

  @Override
  public ConfigurationReport updateEquipment(Equipment equipment) {
    return equipmentConfigurationManager.updateEquipment(equipment);
  }

  @Override
  public ConfigurationReport updateEquipment(List<Equipment> equipments) {
    return equipmentConfigurationManager.updateEquipment(equipments);
  }

  @Override
  public ConfigurationReport removeEquipmentById(Long id) {
    return equipmentConfigurationManager.removeEquipmentById(id);
  }

  @Override
  public ConfigurationReport removeEquipmentById(Set<Long> ids) {
    return equipmentConfigurationManager.removeEquipmentById(ids);
  }

  @Override
  public ConfigurationReport removeEquipment(String equipmentName) {
    return equipmentConfigurationManager.removeEquipment(equipmentName);
  }

  @Override
  public ConfigurationReport removeEquipment(Set<String> equipmentNames) {
    return equipmentConfigurationManager.removeEquipment(equipmentNames);
  }

  @Override
  public ConfigurationReport createSubEquipment(String equipmentName, String name, String handlerClass) {
    return subEquipmentConfigurationManager.createSubEquipment(equipmentName, name, handlerClass);
  }

  @Override
  public ConfigurationReport createSubEquipment(String equipmentName, SubEquipment subEquipment) {
    return subEquipmentConfigurationManager.createSubEquipment(equipmentName, subEquipment);
  }

  @Override
  public ConfigurationReport createSubEquipment(String equipmentName, List<SubEquipment> subEquipments) {
    return subEquipmentConfigurationManager.createSubEquipment(equipmentName, subEquipments);
  }

  @Override
  public ConfigurationReport updateSubEquipment(SubEquipment subEquipment) {
    return subEquipmentConfigurationManager.updateSubEquipment(subEquipment);
  }

  @Override
  public ConfigurationReport updateSubEquipment(List<SubEquipment> subEquipments) {
    return subEquipmentConfigurationManager.updateSubEquipment(subEquipments);
  }

  @Override
  public ConfigurationReport removeSubEquipmentById(Long id) {
    return subEquipmentConfigurationManager.removeSubEquipmentById(id);
  }

  @Override
  public ConfigurationReport removeSubEquipmentById(Set<Long> ids) {
    return subEquipmentConfigurationManager.removeSubEquipmentById(ids);
  }

  @Override
  public ConfigurationReport removeSubEquipment(String subEquipmentName) {
    return subEquipmentConfigurationManager.removeSubEquipment(subEquipmentName);
  }

  @Override
  public ConfigurationReport removeSubEquipment(Set<String> subEquipmentNames) {
    return subEquipmentConfigurationManager.removeSubEquipment(subEquipmentNames);
  }

  @Override
  public ConfigurationReport createDataTag(String equipmentName, String name, Class<?> dataType, DataTagAddress
      address) {
    return dataTagConfigurationManager.createDataTag(equipmentName, name, dataType, address);
  }

  @Override
  public ConfigurationReport createDataTag(String equipmentName, DataTag dataTag) {
    return dataTagConfigurationManager.createDataTag(equipmentName, dataTag);
  }

  @Override
  public ConfigurationReport createDataTags(String equipmentName, List<DataTag> tags) {
    return dataTagConfigurationManager.createDataTags(equipmentName, tags);
  }

  @Override
  public ConfigurationReport updateDataTag(DataTag tag) {
    return dataTagConfigurationManager.updateDataTag(tag);
  }

  @Override
  public ConfigurationReport updateDataTags(List<DataTag> tags) {
    return dataTagConfigurationManager.updateDataTags(tags);
  }

  @Override
  public ConfigurationReport removeDataTagsById(Set<Long> ids) {
    return dataTagConfigurationManager.removeDataTagsById(ids);
  }

  @Override
  public ConfigurationReport removeDataTagById(Long id) {
    return dataTagConfigurationManager.removeDataTagById(id);
  }

  @Override
  public ConfigurationReport removeDataTag(String name) {
    return dataTagConfigurationManager.removeDataTag(name);
  }

  @Override
  public ConfigurationReport removeDataTags(Set<String> tagNames) {
    return dataTagConfigurationManager.removeDataTags(tagNames);
  }

  @Override
  public ConfigurationReport createCommandTag(String equipmentName, String name, Class<?> dataType, HardwareAddress
      hardwareAddress, Integer clientTimeout, Integer execTimeout, Integer sourceTimeout, Integer sourceRetries,
                                              String rbacClass, String rbacDevice, String rbacProperty) {

    return commandTagConfigurationManager.createCommandTag(equipmentName, name, dataType, hardwareAddress,
        clientTimeout, execTimeout, sourceTimeout, sourceRetries, rbacClass, rbacDevice, rbacProperty);
  }

  @Override
  public ConfigurationReport createCommandTag(String equipmentName, CommandTag commandTag) {
    return commandTagConfigurationManager.createCommandTag(equipmentName, commandTag);
  }

  @Override
  public ConfigurationReport createCommandTags(String equipmentName, List<CommandTag> tags) {
    return commandTagConfigurationManager.createCommandTags(equipmentName, tags);
  }

  @Override
  public ConfigurationReport updateCommandTag(CommandTag tag) {
    return commandTagConfigurationManager.updateCommandTag(tag);
  }

  @Override
  public ConfigurationReport updateCommandTags(List<CommandTag> tags) {
    return commandTagConfigurationManager.updateCommandTags(tags);
  }

  @Override
  public ConfigurationReport removeCommandTagsById(Set<Long> ids) {
    return commandTagConfigurationManager.removeCommandTagsById(ids);
  }

  @Override
  public ConfigurationReport removeCommandTagById(Long id) {
    return commandTagConfigurationManager.removeCommandTagById(id);
  }

  @Override
  public ConfigurationReport removeCommandTag(String name) {
    return commandTagConfigurationManager.removeCommandTag(name);
  }

  @Override
  public ConfigurationReport removeCommandTags(Set<String> tagNames) {
    return commandTagConfigurationManager.removeCommandTags(tagNames);
  }

  @Override
  public ConfigurationReport updateAliveTag(AliveTag tag) {
    return controlTagConfigurationManager.updateAliveTag(tag);
  }

  @Override
  public ConfigurationReport updateAliveTags(List<AliveTag> tags) {
    return controlTagConfigurationManager.updateAliveTags(tags);
  }

  @Override
  public ConfigurationReport removeAliveTag(String name) {
    return controlTagConfigurationManager.removeAliveTag(name);
  }

  @Override
  public ConfigurationReport updateCommFaultTag(CommFaultTag tag) {
    return controlTagConfigurationManager.updateCommFaultTag(tag);
  }

  @Override
  public ConfigurationReport updateCommFaultTags(List<CommFaultTag> tags) {
    return controlTagConfigurationManager.updateCommFaultTags(tags);
  }

  @Override
  public ConfigurationReport updateStatusTag(StatusTag tag) {
    return controlTagConfigurationManager.updateStatusTag(tag);
  }

  @Override
  public ConfigurationReport updateStatusTags(List<StatusTag> tags) {
    return controlTagConfigurationManager.updateStatusTags(tags);
  }

  @Override
  public ConfigurationReport createRule(String ruleExpression, String name, Class<?> dataType) {
    return ruleTagConfigurationManager.createRule(ruleExpression, name, dataType);
  }

  @Override
  public ConfigurationReport createRule(RuleTag createRuleTag) {
    return ruleTagConfigurationManager.createRule(createRuleTag);
  }

  @Override
  public ConfigurationReport createRules(List<RuleTag> ruleTags) {
    return ruleTagConfigurationManager.createRules(ruleTags);
  }

  @Override
  public ConfigurationReport updateRuleTag(RuleTag tag) {
    return ruleTagConfigurationManager.updateRuleTag(tag);
  }

  @Override
  public ConfigurationReport updateRuleTags(List<RuleTag> tags) {
    return ruleTagConfigurationManager.updateRuleTags(tags);
  }

  @Override
  public ConfigurationReport removeRuleTagsById(Set<Long> ids) {
    return ruleTagConfigurationManager.removeRuleTagsById(ids);
  }

  @Override
  public ConfigurationReport removeRuleTagById(Long id) {
    return ruleTagConfigurationManager.removeRuleTagById(id);
  }

  @Override
  public ConfigurationReport removeRuleTag(String name) {
    return ruleTagConfigurationManager.removeRuleTag(name);
  }

  @Override
  public ConfigurationReport removeRuleTags(Set<String> tagNames) {
    return ruleTagConfigurationManager.removeRuleTags(tagNames);
  }

  @Override
  public ConfigurationReport createAlarm(String tagName, AlarmCondition alarmCondition, String faultFamily, String
      faultMember, Integer faultCode) {
    return alarmConfigurationManager.createAlarm(tagName, alarmCondition, faultFamily, faultMember, faultCode);
  }

  @Override
  public ConfigurationReport createAlarm(String tagName, Alarm alarm) {
    return alarmConfigurationManager.createAlarm(tagName, alarm);
  }

  @Override
  public ConfigurationReport createAlarms(Map<String, Alarm> alarms) {
    return alarmConfigurationManager.createAlarms(alarms);
  }

  @Override
  public ConfigurationReport updateAlarm(Alarm updateAlarm) {
    return alarmConfigurationManager.updateAlarm(updateAlarm);
  }

  @Override
  public ConfigurationReport updateAlarms(List<Alarm> alarms) {
    return alarmConfigurationManager.updateAlarms(alarms);
  }

  @Override
  public ConfigurationReport removeAlarm(Long id) {
    return alarmConfigurationManager.removeAlarm(id);
  }

  @Override
  public ConfigurationReport removeAlarms(Set<Long> ids) {
    return alarmConfigurationManager.removeAlarms(ids);
  }

  @Override
  public ConfigurationReport createDeviceClass(String deviceClassName) {
    return deviceClassConfigurationManager.createDeviceClass(deviceClassName);
  }

  @Override
  public ConfigurationReport createDeviceClass(DeviceClass deviceClass) {
    return deviceClassConfigurationManager.createDeviceClass(deviceClass);
  }

  @Override
  public ConfigurationReport removeDeviceClassById(Long id) {
    return deviceClassConfigurationManager.removeDeviceClassById(id);
  }

  @Override
  public ConfigurationReport removeDeviceClass(String name) {
    return deviceClassConfigurationManager.removeDeviceClass(name);
  }

  @Override
  public ConfigurationReport createDevice(String deviceName, String deviceClassName) {
    return deviceConfigurationManager.createDevice(deviceName, deviceClassName);
  }

  @Override
  public ConfigurationReport createDevice(String deviceName, long deviceClassId) {
    return deviceConfigurationManager.createDevice(deviceName, deviceClassId);
  }

  @Override
  public ConfigurationReport createDevice(Device device) {
    return deviceConfigurationManager.createDevice(device);
  }

  @Override
  public ConfigurationReport removeDeviceById(Long id) {
    return deviceConfigurationManager.removeDeviceById(id);
  }

  @Override
  public ConfigurationReport removeDevice(String name) {
    return deviceConfigurationManager.removeDevice(name);
  }

}

