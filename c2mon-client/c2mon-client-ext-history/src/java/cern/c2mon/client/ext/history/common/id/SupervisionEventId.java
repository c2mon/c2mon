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
package cern.c2mon.client.ext.history.common.id;

import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionEntity;

/**
 * This class is used to identify a {@link SupervisionEvent}
 * 
 * @author vdeila
 * 
 */
public class SupervisionEventId extends HistoryUpdateId {

  /** the entity */
  private final SupervisionEntity entity;

  /** the entity id */
  private final Long entityId;

  /**
   * 
   * @param entity
   *          the entity
   * @param entityId
   *          the entity id
   */
  public SupervisionEventId(final SupervisionEntity entity, final Long entityId) {
    this.entity = entity;
    this.entityId = entityId;
  }

  /**
   * @return the entity
   */
  public SupervisionEntity getEntity() {
    return entity;
  }

  /**
   * @return the entityId
   */
  public Long getEntityId() {
    return entityId;
  }

  @Override
  public String toString() {
    return String.format("SupervisionEvent %s %s", this.entityId.toString(), this.entity.toString());
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((entity == null) ? 0 : entity.hashCode());
    result = prime * result + ((entityId == null) ? 0 : entityId.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    SupervisionEventId other = (SupervisionEventId) obj;
    if (entity == null) {
      if (other.entity != null) {
        return false;
      }
    }
    else if (!entity.equals(other.entity)) {
      return false;
    }
    if (entityId == null) {
      if (other.entityId != null) {
        return false;
      }
    }
    else if (!entityId.equals(other.entityId)) {
      return false;
    }
    return true;
  }

}
