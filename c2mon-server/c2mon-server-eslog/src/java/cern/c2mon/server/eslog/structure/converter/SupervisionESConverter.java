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

import cern.c2mon.server.eslog.structure.types.SupervisionES;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import org.springframework.stereotype.Component;

/**
 * @author Alban Marguet
 */
@Component
public class SupervisionESConverter {
  public SupervisionES convertSupervisionEventToSupervisionES(SupervisionEvent supervisionEvent) {
    SupervisionES supervisionES = new SupervisionES();

    if (supervisionEvent == null) {
      return null;
    }

    if (supervisionEvent.getEntity() != null) {
      supervisionES.setEntityName(supervisionEvent.getEntity().name());
    }
    supervisionES.setEntityId(supervisionEvent.getEntityId());
    if (supervisionEvent.getEventTime() != null) {
      supervisionES.setEventTime(supervisionEvent.getEventTime().getTime());
    }
    supervisionES.setMessage(supervisionEvent.getMessage());
    if (supervisionEvent.getStatus() != null) {
      supervisionES.setStatusName(supervisionEvent.getStatus().name());
    }

    return supervisionES;
  }
}
