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

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.eslog.structure.types.EsAlarm;
import cern.c2mon.shared.common.metadata.Metadata;

/**
 * Convert an Alarm to an {@link EsAlarm} for ElasticSearch writing.
 *
 * @author Alban Marguet
 */
@Component
public class EsAlarmLogConverter implements Converter<Alarm, EsAlarm> {

  /**
   * Converts an Alarm to an {@link EsAlarm} by getting all its data.
   */
  @Override
  public EsAlarm convert(final Alarm alarm) {
    EsAlarm esAlarm = new EsAlarm();

    if (alarm == null || alarm.getTagId() == null) {
      return null;
    }

    esAlarm.setTagId(alarm.getTagId());
    esAlarm.setId(alarm.getId());

    esAlarm.setActive(alarm.isActive());
    esAlarm.setActiveNumeric(alarm.isActive() ? 1 : 0);
    
    esAlarm.setFaultFamily(alarm.getFaultFamily());
    esAlarm.setFaultMember(alarm.getFaultMember());
    esAlarm.setFaultCode(alarm.getFaultCode());

    esAlarm.setTimestamp(alarm.getTimestamp().getTime());

    esAlarm.setInfo(alarm.getInfo());

    esAlarm.getMetadata().putAll(retrieveMetadata(alarm));

    return esAlarm;
  }

  private Map<String, String> retrieveMetadata(final Alarm alarm) {
    final Metadata metadata = alarm.getMetadata();
    if (metadata == null) {
      return Collections.emptyMap();
    }

    return metadata.getMetadata().entrySet().stream()
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().toString()));
  }

}