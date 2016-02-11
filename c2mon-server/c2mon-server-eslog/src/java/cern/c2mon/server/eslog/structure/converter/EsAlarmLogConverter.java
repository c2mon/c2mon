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
package cern.c2mon.server.eslog.structure.converter;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.eslog.structure.types.EsAlarm;
import cern.c2mon.shared.common.metadata.Metadata;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * Convert an Alarm to an {@link EsAlarm} for ElasticSearch writing.
 * @author Alban Marguet
 */
@Component
public class EsAlarmLogConverter {

  /** Converts an Alarm to an {@link EsAlarm} by getting all its data. */
  public EsAlarm convertAlarmToAlarmES(Alarm alarm) {
    EsAlarm EsAlarm = new EsAlarm();

    if (alarm == null || alarm.getTagId() == null) {
      return null;
    }

    EsAlarm.setTagId(alarm.getTagId());
    EsAlarm.setAlarmId(alarm.getId());

    EsAlarm.setActive(alarm.isActive());
    EsAlarm.setActivity(String.valueOf(alarm.isActive()));

    if (EsAlarm.isActive()) {
      EsAlarm.setActiveNumeric(1);
    }
    else {
      EsAlarm.setActiveNumeric(0);
    }

    EsAlarm.setFaultFamily(alarm.getFaultFamily());
    EsAlarm.setFaultMember(alarm.getFaultMember());
    EsAlarm.setFaultCode(alarm.getFaultCode());

    Timestamp alarmTimestamp = alarm.getTimestamp();
    if (alarmTimestamp != null) {
      EsAlarm.setServerTimestamp(alarm.getTimestamp().getTime());
    }

    EsAlarm.setInfo(alarm.getInfo());

    retrieveMetadata(EsAlarm, alarm);

    return EsAlarm;
  }

  private void retrieveMetadata(EsAlarm EsAlarm, Alarm alarm) {
    Metadata metadata = alarm.getMetadata();
    Map<String, String> metadataMap = new HashMap<>();
    if (metadata != null) {
      for (String key : metadata.getMetadata().keySet()) {
        Object value = metadata.getMetadata().get(key);
        metadataMap.put(key, value.toString());
      }
    }
    EsAlarm.setMetadata(metadataMap);
  }
}