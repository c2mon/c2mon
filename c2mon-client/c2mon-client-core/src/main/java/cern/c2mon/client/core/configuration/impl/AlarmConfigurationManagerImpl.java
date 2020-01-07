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

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.core.configuration.AlarmConfigurationManager;
import cern.c2mon.client.core.configuration.ConfigurationRequestSender;
import cern.c2mon.shared.client.alarm.condition.AlarmCondition;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.alarm.Alarm;

import static cern.c2mon.client.core.configuration.util.ConfigurationUtil.validateIsCreate;
import static cern.c2mon.client.core.configuration.util.ConfigurationUtil.validateIsUpdate;

/**
 * @author Franz Ritter
 */
@Service("alarmConfigurationManager")
public class AlarmConfigurationManagerImpl implements AlarmConfigurationManager {

  private ConfigurationRequestSender configurationRequestSender;

  @Autowired
  AlarmConfigurationManagerImpl(ConfigurationRequestSender configurationRequestSender) {
    this.configurationRequestSender = configurationRequestSender;
  }

  @Override
  public ConfigurationReport createAlarm(String tagName, AlarmCondition alarmCondition, String faultFamily, String faultMember, Integer faultCode) {

    return createAlarm(tagName, Alarm.create(faultFamily, faultMember, faultCode, alarmCondition).build());
  }

  @Override
  public ConfigurationReport createAlarm(String tagName, Alarm alarm) {

    Map<String, Alarm> createAlarmMap = new HashMap<>();
    createAlarmMap.put(tagName, alarm);

    return createAlarms(createAlarmMap);
  }

  @Override
  public ConfigurationReport createAlarms(Map<String, Alarm> alarms) {

    List<Alarm> createAlarms = new ArrayList();

    for (Map.Entry<String, Alarm> entry : alarms.entrySet()) {
      Alarm createAlarm = entry.getValue();
      createAlarm.setDataTagName(entry.getKey());
      createAlarms.add(createAlarm);
    }

    // validate alarm configuration
    validateIsCreate(createAlarms);

    Configuration config = new Configuration();
    config.setEntities(createAlarms);

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
    config.setEntities(alarms);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport removeAlarm(Long id) {

    Set<Long> dummyAlarmIdList = new HashSet<>();
    dummyAlarmIdList.add(id);

    return removeAlarms(dummyAlarmIdList);
  }

  @Override
  public ConfigurationReport removeAlarms(Set<Long> ids) {

    List<Alarm> alarmsToDelete = new ArrayList<>();

    for (Long id : ids) {
      Alarm deleteAlarm = new Alarm();
      deleteAlarm.setId(id);
      deleteAlarm.setDeleted(true);

      alarmsToDelete.add(deleteAlarm);
    }

    Configuration config = new Configuration();
    config.setEntities(alarmsToDelete);

    return configurationRequestSender.applyConfiguration(config, null);
  }
}
