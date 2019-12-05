/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.server.elasticsearch.alarm;

import org.springframework.stereotype.Component;

import cern.c2mon.server.common.alarm.Alarm;

/**
 * Converts {@link Alarm} instances to {@link AlarmDocument} instances with values included.
 *
 * @author Alban Marguet
 * @author Justin Lewis Salmon
 */
@Component
public class AlarmValueDocumentConverter extends BaseAlarmDocumentConverter {

  @Override
  public AlarmDocument convert(Alarm alarm) {
    AlarmDocument document = super.convert(alarm);
    document.put("tagId", alarm.getTagId());
    document.put("active", alarm.isActive());
    document.put("activeNumeric", alarm.isActive() ? 1 : 0);
    document.put("info", alarm.getInfo());
    document.put("timestamp", alarm.getTimestamp().getTime());
    document.put("sourceTimestamp", alarm.getSourceTimestamp().getTime());
    document.put("oscillating", alarm.isOscillating());
    return document;
  }
}
