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

import org.springframework.core.convert.converter.Converter;

import cern.c2mon.server.common.alarm.Alarm;

/**
 * Converts {@link Alarm} to {@link AlarmDocument} excluding values.
 */
public class BaseAlarmDocumentConverter implements Converter<Alarm, AlarmDocument> {

  /**
   * Convert given {@link Alarm} to {@link AlarmDocument}.
   *
   * @param alarm the alarm.
   * @return the alarm document.
   */
  @Override
  public AlarmDocument convert(final Alarm alarm) {
    AlarmDocument document = new AlarmDocument();

    document.put("id", alarm.getId());
    document.put("faultFamily", alarm.getFaultFamily());
    document.put("faultMember", alarm.getFaultMember());
    document.put("faultCode", alarm.getFaultCode());
    document.put("metadata", alarm.getMetadata().toMap());

    return document;
  }
}
