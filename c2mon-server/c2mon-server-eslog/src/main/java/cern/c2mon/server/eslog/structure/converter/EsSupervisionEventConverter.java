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

import cern.c2mon.server.eslog.structure.types.EsSupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Converts a SupervisionEvent to a {@link EsSupervisionEvent} used in ElasticSearch.
 *
 * @author Alban Marguet
 */
@Component
public class EsSupervisionEventConverter implements Converter<SupervisionEvent, EsSupervisionEvent> {

  /**
   * A supervisionEvent becomes a {@link EsSupervisionEvent}.
   */
  @Override
  public EsSupervisionEvent convert(final SupervisionEvent supervisionEvent) {
    if (supervisionEvent == null) {
      return null;
    }

    EsSupervisionEvent esSupervisionEvent = new EsSupervisionEvent();

    if (supervisionEvent.getEntity() != null) {
      esSupervisionEvent.setEntityName(supervisionEvent.getEntity().name());
    }
    esSupervisionEvent.setEntityId(supervisionEvent.getEntityId());

    if (supervisionEvent.getEventTime() != null) {
      esSupervisionEvent.setEventTime(supervisionEvent.getEventTime().getTime());
    }
    esSupervisionEvent.setMessage(supervisionEvent.getMessage());

    if (supervisionEvent.getStatus() != null) {
      esSupervisionEvent.setStatusName(supervisionEvent.getStatus().name());
    }

    return esSupervisionEvent;
  }
}