/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * <p/>
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * <p/>
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.eslog.structure.converter;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.eslog.structure.types.AlarmES;
import cern.c2mon.server.eslog.structure.types.TagES;
import cern.c2mon.shared.common.metadata.Metadata;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * Convert an Alarm to an AlarmES for ElasticSearch writing.
 * @author Alban Marguet
 */
@Component
public class AlarmESLogConverter {
  public AlarmES convertAlarmToAlarmES(Alarm alarm) {
    AlarmES alarmES = new AlarmES();

    if (alarm == null || alarm.getTagId() == null) {
      return null;
    }

    alarmES.setTagId(alarm.getTagId());
    alarmES.setAlarmId(alarm.getId());

    alarmES.setActive(alarm.isActive());
    alarmES.setActivity(String.valueOf(alarm.isActive()));

    if (alarmES.isActive()) {
      alarmES.setActiveNumeric(1);
    }
    else {
      alarmES.setActiveNumeric(0);
    }

    alarmES.setFaultFamily(alarm.getFaultFamily());
    alarmES.setFaultMember(alarm.getFaultMember());
    alarmES.setFaultCode(alarm.getFaultCode());

    Timestamp alarmTimestamp = alarm.getTimestamp();
    if (alarmTimestamp != null) {
      alarmES.setServerTimestamp(alarm.getTimestamp().getTime());
    }

    alarmES.setInfo(alarm.getInfo());

    retrieveMetadata(alarmES, alarm);

    return alarmES;
  }

  private void retrieveMetadata(AlarmES alarmES, Alarm alarm) {
    Metadata metadata = alarm.getMetadata();
    Map<String, String> metadataMap = new HashMap<>();
    if (metadata != null) {
      for (String key : metadata.getMetadata().keySet()) {
        Object value = metadata.getMetadata().get(key);
        if (value instanceof String) {
          metadataMap.put(key, (String) value);
        }
      }
    }
    alarmES.setMetadata(metadataMap);
  }
}