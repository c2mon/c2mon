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
package cern.c2mon.client.core.service;

import cern.c2mon.client.common.listener.ClientRequestReportListener;
import cern.c2mon.client.core.ConfigurationService;
import cern.c2mon.client.core.configuration.*;
import cern.c2mon.client.jms.RequestHandler;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.ConfigurationReportHeader;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.alarm.Alarm;
import cern.c2mon.shared.client.configuration.api.alarm.AlarmCondition;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.equipment.SubEquipment;
import cern.c2mon.shared.client.configuration.api.process.Process;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import cern.c2mon.shared.client.configuration.api.tag.RuleTag;
import cern.c2mon.shared.client.configuration.api.tag.Tag;
import cern.c2mon.shared.client.process.ProcessNameResponse;
import cern.c2mon.shared.client.tag.TagConfig;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service("configurationService")
@Slf4j
public class ConfigurationServiceImpl implements ConfigurationService {

  /**
   * Provides methods for requesting tag information from the C2MON server
   */
  private RequestHandler clientRequestHandler;

  private ConfigurationRequestSender configurationRequestSender;

  private ProcessConfiguration processConfiguration;

  private EquipmentConfiguration equipmentConfiguration;

  private SubEquipmentConfiguration subEquipmentConfiguration;

  private DataTagConfiguration dataTagConfiguration;

  private RuleTagConfiguration ruleTagConfiguration;

  private AlarmConfiguration alarmConfiguration;

  /**
   * Default Constructor, used by Spring to instantiate the Singleton service
   *
   * @param requestHandler Provides methods for requesting tag information from the C2MON server
   */
  @Autowired
  protected ConfigurationServiceImpl(final @Qualifier("coreRequestHandler") RequestHandler requestHandler, final ConfigurationRequestSender configurationRequestSender,
                                     ProcessConfiguration processConfiguration, EquipmentConfiguration equipmentConfiguration, SubEquipmentConfiguration subEquipmentConfiguration,
                                     DataTagConfiguration dataTagConfiguration, RuleTagConfiguration ruleTagConfiguration, AlarmConfiguration alarmConfiguration) {
    this.clientRequestHandler = requestHandler;
    this.configurationRequestSender = configurationRequestSender;
    this.processConfiguration = processConfiguration;
    this.equipmentConfiguration = equipmentConfiguration;
    this.subEquipmentConfiguration = subEquipmentConfiguration;
    this.dataTagConfiguration = dataTagConfiguration;
    this.ruleTagConfiguration = ruleTagConfiguration;
    this.alarmConfiguration = alarmConfiguration;
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
      log.error("getConfigurationReports() - JMS connection lost -> Could not retrieve configuration reports from the C2MON server.", e);
    }
    return new ArrayList<>();
  }

  @Override
  public Collection<ConfigurationReport> getConfigurationReports(Long id) {
    try {
      return clientRequestHandler.getConfigurationReports(id);
    } catch (JMSException e) {
      log.error("getConfigurationReports() - JMS connection lost -> Could not retrieve configuration reports from the C2MON server.", e);
    }
    return new ArrayList<>();
  }

  @Override
  public Collection<ProcessNameResponse> getProcessNames() {

    try {
      return clientRequestHandler.getProcessNames();
    } catch (JMSException e) {
      log.error("getProcessNames() - JMS connection lost -> Could not retrieve process names from the C2MON server.", e);
    }
    return new ArrayList<>();
  }

  @Override
  public Collection<TagConfig> getTagConfigurations(final Collection<Long> tagIds) {

    try {
      // no cache for Tag Configurations => fetch them from the server
      return clientRequestHandler.requestTagConfigurations(tagIds);
    } catch (JMSException e) {
      log.error("getTagConfigurations() - JMS connection lost -> Could not retrieve missing tags from the C2MON server.", e);
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

  public ConfigurationReport createProcess(String processName) {
    return processConfiguration.createProcess(processName);
  }

  public ConfigurationReport createProcess(Process process) {
    return processConfiguration.createProcess(process);
  }

  @Override
  public ConfigurationReport updateProcess(Process process) {
    return processConfiguration.updateProcess(process);
  }

  @Override
  public ConfigurationReport removeProcess(Long id) {
    return processConfiguration.removeProcess(id);
  }

  @Override
  public ConfigurationReport removeProcess(String name) {
    return processConfiguration.removeProcess(name);
  }

  @Override
  public ConfigurationReport createEquipment(Long processId, String name, String handlerClass) {
    return equipmentConfiguration.createEquipment(processId, name, handlerClass);
  }

  @Override
  public ConfigurationReport createEquipment(Long processId, Equipment equipment) {
    return equipmentConfiguration.createEquipment(processId, equipment);
  }

  @Override
  public ConfigurationReport createEquipments(Long processId, List<Equipment> equipments) {
    return equipmentConfiguration.createEquipments(processId, equipments);
  }

  @Override
  public ConfigurationReport createEquipment(String processName, String name, String handlerClass) {
    return equipmentConfiguration.createEquipment(processName, name, handlerClass);
  }

  @Override
  public ConfigurationReport createEquipment(String processName, Equipment equipment) {
    return equipmentConfiguration.createEquipment(processName, equipment);
  }

  @Override
  public ConfigurationReport createEquipments(String processName, List<Equipment> equipments) {
    return equipmentConfiguration.createEquipments(processName, equipments);
  }

  @Override
  public ConfigurationReport updateEquipment(Equipment equipment) {
    return equipmentConfiguration.updateEquipment(equipment);
  }

  @Override
  public ConfigurationReport updateEquipments(List<Equipment> equipments) {
    return equipmentConfiguration.updateEquipments(equipments);
  }

  @Override
  public ConfigurationReport removeEquipment(Long id) {
    return equipmentConfiguration.removeEquipment(id);
  }

  @Override
  public ConfigurationReport removeEquipments(List<Long> ids) {
    return equipmentConfiguration.removeEquipments(ids);
  }

  @Override
  public ConfigurationReport removeEquipment(String equipmentName) {
    return equipmentConfiguration.removeEquipment(equipmentName);
  }

  @Override
  public ConfigurationReport removeEquipmentsByName(List<String> equipmentNames) {
    return equipmentConfiguration.removeEquipmentsByName(equipmentNames);
  }

  @Override
  public ConfigurationReport createSubEquipment(Long equipmentId, String name, String handlerClass) {
    return subEquipmentConfiguration.createSubEquipment(equipmentId, name, handlerClass);
  }

  @Override
  public ConfigurationReport createSubEquipment(Long equipmentId, SubEquipment subEquipment) {
    return subEquipmentConfiguration.createSubEquipment(equipmentId, subEquipment);
  }

  @Override
  public ConfigurationReport createSubEquipments(Long equipmentId, List<SubEquipment> subEquipments) {
    return subEquipmentConfiguration.createSubEquipments(equipmentId, subEquipments);
  }

  @Override
  public ConfigurationReport createSubEquipment(String equipmentName, String name, String handlerClass) {
    return subEquipmentConfiguration.createSubEquipment(equipmentName, name, handlerClass);
  }

  @Override
  public ConfigurationReport createSubEquipment(String equipmentName, SubEquipment subEquipment) {
    return subEquipmentConfiguration.createSubEquipment(equipmentName, subEquipment);
  }

  @Override
  public ConfigurationReport createSubEquipments(String equipmentName, List<SubEquipment> subEquipments) {
    return subEquipmentConfiguration.createSubEquipments(equipmentName, subEquipments);
  }

  @Override
  public ConfigurationReport updateSubEquipment(SubEquipment subEquipment) {
    return subEquipmentConfiguration.updateSubEquipment(subEquipment);
  }

  @Override
  public ConfigurationReport updateSubEquipments(List<SubEquipment> subEquipments) {
    return subEquipmentConfiguration.updateSubEquipments(subEquipments);
  }

  @Override
  public ConfigurationReport removeSubEquipment(Long id) {
    return subEquipmentConfiguration.removeSubEquipment(id);
  }

  @Override
  public ConfigurationReport removeSubEquipments(List<Long> ids) {
    return subEquipmentConfiguration.removeSubEquipments(ids);
  }

  @Override
  public ConfigurationReport removeSubEquipment(String subEquipmentName) {
    return subEquipmentConfiguration.removeSubEquipment(subEquipmentName);
  }

  @Override
  public ConfigurationReport removeSubEquipmentsByName(List<String> subEquipmentNames) {
    return subEquipmentConfiguration.removeSubEquipmentsByName(subEquipmentNames);
  }

  @Override
  public ConfigurationReport createDataTag(Long equipmentId, String name, Class<?> dataType, DataTagAddress address) {
    return dataTagConfiguration.createDataTag(equipmentId, name, dataType, address);
  }

  @Override
  public ConfigurationReport createDataTag(Long equipmentId, DataTag dataTag) {
    return dataTagConfiguration.createDataTag(equipmentId, dataTag);
  }

  @Override
  public ConfigurationReport createDataTags(Long equipmentId, List<DataTag> tags) {
    return dataTagConfiguration.createDataTags(equipmentId, tags);
  }

  @Override
  public ConfigurationReport createDataTag(String equipmentName, String name, Class<?> dataType, DataTagAddress address) {
    return dataTagConfiguration.createDataTag(equipmentName, name, dataType, address);
  }

  @Override
  public ConfigurationReport createDataTag(String equipmentName, DataTag dataTag) {
    return dataTagConfiguration.createDataTag(equipmentName, dataTag);
  }

  @Override
  public ConfigurationReport createDataTags(String equipmentName, List<DataTag> tags) {
    return dataTagConfiguration.createDataTags(equipmentName, tags);
  }

  @Override
  public ConfigurationReport updateTag(Tag tag) {
    return dataTagConfiguration.updateTag(tag);
  }

  @Override
  public ConfigurationReport updateTags(List<Tag> tags) {
    return dataTagConfiguration.updateTags(tags);
  }

  @Override
  public ConfigurationReport removeTag(Long id) {
    return dataTagConfiguration.removeTag(id);
  }

  @Override
  public ConfigurationReport removeTags(List<Long> ids) {
    return dataTagConfiguration.removeTags(ids);
  }

  @Override
  public ConfigurationReport removeTag(String name) {
    return dataTagConfiguration.removeTag(name);
  }

  @Override
  public ConfigurationReport removeTagsByName(List<String> tagNames) {
    return dataTagConfiguration.removeTagsByName(tagNames);
  }

  @Override
  public ConfigurationReport createRule(String ruleExpression, String name, Class<?> dataType) {
    return ruleTagConfiguration.createRule(ruleExpression, name, dataType);
  }

  @Override
  public ConfigurationReport createRule(RuleTag createRuleTag) {
    return ruleTagConfiguration.createRule(createRuleTag);
  }

  @Override
  public ConfigurationReport createRules(List<RuleTag> ruleTags) {
    return ruleTagConfiguration.createRules(ruleTags);
  }

  @Override
  public ConfigurationReport createAlarm(Long tagId, AlarmCondition alarmCondition, String faultFamily, String faultMember, Integer faultCode) {
    return alarmConfiguration.createAlarm(tagId, alarmCondition, faultFamily, faultMember, faultCode);
  }

  @Override
  public ConfigurationReport createAlarm(Long tagId, Alarm alarm) {
    return alarmConfiguration.createAlarm(tagId, alarm);
  }

  @Override
  public ConfigurationReport createAlarmsById(Map<Long, Alarm> alarms) {
    return alarmConfiguration.createAlarmsById(alarms);
  }

  @Override
  public ConfigurationReport createAlarm(String tagName, AlarmCondition alarmCondition, String faultFamily, String faultMember, Integer faultCode) {
    return alarmConfiguration.createAlarm(tagName, alarmCondition, faultFamily, faultMember, faultCode);
  }

  @Override
  public ConfigurationReport createAlarm(String tagName, Alarm alarm) {
    return alarmConfiguration.createAlarm(tagName, alarm);
  }

  @Override
  public ConfigurationReport createAlarmsByName(Map<String, Alarm> alarms) {
    return alarmConfiguration.createAlarmsByName(alarms);
  }

  @Override
  public ConfigurationReport updateAlarm(Alarm updateAlarm) {
    return alarmConfiguration.updateAlarm(updateAlarm);
  }

  @Override
  public ConfigurationReport updateAlarms(List<Alarm> alarms) {
    return alarmConfiguration.updateAlarms(alarms);
  }

  @Override
  public ConfigurationReport removeAlarm(Long id) {
    return alarmConfiguration.removeAlarm(id);
  }

  @Override
  public ConfigurationReport removeAlarms(List<Long> ids) {
    return alarmConfiguration.removeAlarms(ids);
  }
}
