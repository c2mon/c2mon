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

import java.util.Properties;

import cern.c2mon.shared.client.alarm.condition.ValueAlarmCondition;
import cern.c2mon.shared.client.configuration.api.alarm.Alarm;
import cern.c2mon.shared.client.metadata.Metadata;

/**
 * Utility class which provides builder methods for different Alarm objects.
 * All methods imply that the Alarm is build as instance od a DataTag
 */
public class ConfigurationAlarmUtil {

  /**
   * Expected generated id is 200.
   * Expected parent id is 100.
   */
  public static Alarm buildCreateBasicAlarm(Properties properties) {
    if (properties == null) {
      properties = new Properties();
    }

    Alarm alarm = Alarm.create("faultFamily", "faultMember", 1337, new ValueAlarmCondition(1)).dataTagId(100L).build();

    properties.setProperty("faultFamily", "faultFamily");
    properties.setProperty("faultMember", "faultMember");
    properties.setProperty("faultCode", "1337");
    properties.setProperty("alarmCondition", new ValueAlarmCondition(1).getXMLCondition());
    properties.setProperty("dataTagId", "100");

    return alarm;
  }


  /**
   * Expected parent id is 10.
   */
  public static Alarm buildCreateAllFieldsAlarm(Long id, Properties properties) {
    if (properties == null) {
      properties = new Properties();
    }

    Alarm alarm = Alarm.create("faultFamily", "faultMember", 1337, new ValueAlarmCondition(1))
        .id(id)
        .addMetadata("testMetadata", 11)
        .dataTagId(100L)
        .build();

    properties.setProperty("faultFamily", "faultFamily");
    properties.setProperty("faultMember", "faultMember");
    properties.setProperty("faultCode", "1337");
    properties.setProperty("dataTagId", "100");
    properties.setProperty("alarmCondition", new ValueAlarmCondition(1).getXMLCondition());
    Metadata metadata = new Metadata();
    metadata.addMetadata("testMetadata",11);
    properties.setProperty("metadata", Metadata.toJSON(metadata));

    return alarm;
  }

  public static Alarm buildUpdateAlarmWithAllFields(Long id, Properties properties) {
    if (properties == null) {
      properties = new Properties();
    }

    Alarm alarm = Alarm.update(id)
        .alarmCondition(new ValueAlarmCondition(2))
        .updateMetadata("testMetadata", 12)
        .build();

    properties.setProperty("alarmCondition", new ValueAlarmCondition(2).getXMLCondition());
    Metadata metadata = new Metadata();
    metadata.addMetadata("testMetadata",12);
    metadata.setUpdate(true);
    properties.setProperty("metadata", Metadata.toJSON(metadata));

    return alarm;
  }

  public static Alarm buildDeleteAlarm(Long id) {
    Alarm deleteAlarm = new Alarm();
    deleteAlarm.setId(id);
    deleteAlarm.setDeleted(true);

    return deleteAlarm;
  }
}
