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
package cern.c2mon.client.core.configuration.impl;

import cern.c2mon.client.core.configuration.ConfigurationRequestSender;
import cern.c2mon.client.core.configuration.AlarmConfiguration;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.alarm.Alarm;
import cern.c2mon.shared.client.configuration.api.alarm.AlarmCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cern.c2mon.client.core.configuration.util.ConfigurationUtil.*;

/**
 * Created by fritter on 13/05/16.
 */
@Service("alarmConfiguration")
public class AlarmConfigurationImpl implements AlarmConfiguration {

  private ConfigurationRequestSender configurationRequestSender;

  @Autowired
  AlarmConfigurationImpl(ConfigurationRequestSender configurationRequestSender) {
    this.configurationRequestSender = configurationRequestSender;
  }


  @Override
  public ConfigurationReport createAlarm(Long tagId, AlarmCondition alarmCondition, String faultFamily, String faultMember, Integer faultCode) {

    return createAlarm(tagId, Alarm.create(faultFamily, faultMember, faultCode, alarmCondition).build());
  }

  @Override
  public ConfigurationReport createAlarm(Long tagId, Alarm alarm) {

    Map<Long, Alarm> createAlarmMap = new HashMap<>();
    createAlarmMap.put(tagId, alarm);

    return createAlarmsById(createAlarmMap);
  }

  @Override
  public ConfigurationReport createAlarmsById(Map<Long, Alarm> alarms) {

    List<Alarm> createAlarms = new ArrayList();

    for (Map.Entry<Long, Alarm> entry : alarms.entrySet()) {
      Alarm createAlarm = entry.getValue();
      createAlarm.setParentTagId(entry.getKey());
      createAlarms.add(createAlarm);
    }

    // validate alarm configuration
    validateIsCreate(createAlarms);

    Configuration config = new Configuration();
    config.setConfigurationItems(createAlarms);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport createAlarm(String tagName, AlarmCondition alarmCondition, String faultFamily, String faultMember, Integer faultCode) {

    return createAlarm(tagName, Alarm.create(faultFamily, faultMember, faultCode, alarmCondition).build());
  }

  @Override
  public ConfigurationReport createAlarm(String tagName, Alarm alarm) {

    Map<String, Alarm> createAlarmMap = new HashMap<>();
    createAlarmMap.put(tagName, alarm);

    return createAlarmsByName(createAlarmMap);
  }

  @Override
  public ConfigurationReport createAlarmsByName(Map<String, Alarm> alarms) {

    List<Alarm> createAlarms = new ArrayList();

    for (Map.Entry<String, Alarm> entry : alarms.entrySet()) {
      Alarm createAlarm = entry.getValue();
      createAlarm.setParentTagName(entry.getKey());
      createAlarms.add(createAlarm);
    }

    // validate alarm configuration
    validateIsCreate(createAlarms);

    Configuration config = new Configuration();
    config.setConfigurationItems(createAlarms);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport updateAlarm(Alarm updateAlarm) {

    List<Alarm> updateAlarms = new ArrayList<>();
    updateAlarms.add(updateAlarm);

    return updateAlarms(updateAlarms);
  }

  @Override
  public ConfigurationReport updateAlarms(List<Alarm> alarms) {
    // validate alarm configuration
    validateIsUpdate(alarms);

    Configuration config = new Configuration();
    config.setConfigurationItems(alarms);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport removeAlarm(Long id) {

    List<Long> dummyAlarmIdList = new ArrayList<>();
    dummyAlarmIdList.add(id);

    return removeAlarms(dummyAlarmIdList);
  }

  @Override
  public ConfigurationReport removeAlarms(List<Long> ids) {

    List<Alarm> alarmsToDelete = new ArrayList<>();

    for (Long id : ids) {

      alarmsToDelete.add(Alarm.builder().id(id).deleted(true).build());
    }

    Configuration config = new Configuration();
    config.setConfigurationItems(alarmsToDelete);

    return configurationRequestSender.applyConfiguration(config, null);
  }
}
