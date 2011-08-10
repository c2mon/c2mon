/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.client.common.history.id;

import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.tim.shared.common.supervision.SupervisionConstants.SupervisionEntity;

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
