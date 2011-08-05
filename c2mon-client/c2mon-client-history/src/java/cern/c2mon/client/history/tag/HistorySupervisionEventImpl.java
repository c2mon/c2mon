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
package cern.c2mon.client.history.tag;

import java.sql.Timestamp;

import cern.c2mon.client.common.history.HistorySupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionConstants.SupervisionEntity;
import cern.c2mon.shared.client.supervision.SupervisionConstants.SupervisionStatus;

/**
 * This class implement the {@link SupervisionEvent} and is used when the event
 * is retrieved from the history
 * 
 * @author vdeila
 * 
 */
public class HistorySupervisionEventImpl implements HistorySupervisionEvent {

  /** The entity for which this supervision event is created */
  private final SupervisionEntity entity;
  /** The id of the entity */
  private final Long entityId;
  /** one state from the <code>SupervisionStatus</code> enumeration */
  private final SupervisionStatus status;
  /** time of the event */
  private final Timestamp eventTime;
  /** Free text for describing this event */
  private final String message;

  /**
   * Constructor.
   * 
   * @param entity
   *          The entity for which this supervision event is created
   * @param entityId
   *          The id of the entity
   * @param status
   *          one state from the <code>SupervisionStatus</code> enumeration
   * @param eventTime
   *          time of the event
   * @param message
   *          Free text for describing this event
   */
  public HistorySupervisionEventImpl(final SupervisionEntity entity, final Long entityId, final SupervisionStatus status, final Timestamp eventTime,
      final String message) {
    this.entity = entity;
    this.entityId = entityId;
    this.status = status;
    this.eventTime = eventTime;
    this.message = message;
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

  /**
   * @return the status
   */
  public SupervisionStatus getStatus() {
    return status;
  }

  /**
   * @return the eventTime
   */
  public Timestamp getEventTime() {
    return eventTime;
  }

  /**
   * @return the message
   */
  public String getMessage() {
    return message;
  }

  
  /**
   * 
   * @return a clone of this
   */
  @Override
  public SupervisionEvent clone() {
    return new HistorySupervisionEventImpl(entity, entityId, status, eventTime, message);
  }

  /**
   * @return the time of when this update should execute
   */
  @Override
  public Timestamp getExecutionTimestamp() {
    return getEventTime();
  }

}
