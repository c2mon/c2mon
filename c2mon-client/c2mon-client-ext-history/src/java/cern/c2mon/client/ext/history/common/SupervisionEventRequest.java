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
package cern.c2mon.client.ext.history.common;

import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionEntity;

/**
 * This is passed as an argument to
 * {@link HistoryProvider#getSupervisionEvents(java.util.Collection)} when
 * requesting supervision events
 * 
 * @author vdeila
 * 
 */
public class SupervisionEventRequest {
  /** The id of the event to request */
  private final Long id;

  /** The entity of the event to request */
  private final SupervisionEntity entity;

  /**
   * 
   * @param id
   *          The id of the event to request
   * @param entity
   *          The entity of the event to request
   */
  public SupervisionEventRequest(final Long id, final SupervisionEntity entity) {
    this.id = id;
    this.entity = entity;
  }

  /**
   * @return The id of the event to request
   */
  public Long getId() {
    return this.id;
  }

  /**
   * @return The entity of the event to request
   */
  public SupervisionEntity getEntity() {
    return this.entity;
  }

}
