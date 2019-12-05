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
package cern.c2mon.server.elasticsearch.supervision;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import cern.c2mon.shared.client.supervision.SupervisionEvent;

/**
 * Converts {@link SupervisionEvent} instances to {@link SupervisionEventDocument}
 * instances.
 *
 * @author Alban Marguet
 */
@Component
public class SupervisionEventDocumentConverter implements Converter<SupervisionEvent, SupervisionEventDocument> {

  @Override
  public SupervisionEventDocument convert(final SupervisionEvent supervisionEvent) {
    SupervisionEventDocument event = new SupervisionEventDocument();

    event.put("id", supervisionEvent.getEntityId());
    event.put("name", supervisionEvent.getName());
    event.put("message", supervisionEvent.getMessage());

    if (supervisionEvent.getEntity() != null) {
      event.put("entity", supervisionEvent.getEntity().name());
    }

    if (supervisionEvent.getEventTime() != null) {
      event.put("timestamp", supervisionEvent.getEventTime().getTime());
    }

    if (supervisionEvent.getStatus() != null) {
      event.put("status", supervisionEvent.getStatus().name());
    }

    return event;
  }
}
