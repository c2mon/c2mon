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
package cern.c2mon.server.elasticsearch.alarm;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.shared.common.metadata.Metadata;

/**
 * Converts {@link Alarm} instances to {@link AlarmDocument} instances.
 *
 * @author Alban Marguet
 * @author Justin Lewis Salmon
 */
@Component
public class AlarmDocumentConverter implements Converter<Alarm, AlarmDocument> {

  @Override
  public AlarmDocument convert(final Alarm alarm) {
    AlarmDocument document = new AlarmDocument();

    document.put("id", alarm.getId());
    document.put("tagId", alarm.getTagId());
    document.put("active", alarm.isActive());
    document.put("activeNumeric", alarm.isActive() ? 1 : 0);
    document.put("faultFamily", alarm.getFaultFamily());
    document.put("faultMember", alarm.getFaultMember());
    document.put("faultCode", alarm.getFaultCode());
    document.put("timestamp", alarm.getTimestamp().getTime());
    document.put("info", alarm.getInfo());
    document.put("metadata", getMetadata(alarm));

    return document;
  }

  private Map<String, Object> getMetadata(Alarm alarm) {
    Metadata metadata = alarm.getMetadata();

    if (metadata != null) {
      return metadata.getMetadata().entrySet().stream().collect(Collectors.toMap(
          Map.Entry::getKey,
          e -> e.getValue() == null ? null : e.getValue()
      ));
    }

    return Collections.emptyMap();
  }
}
