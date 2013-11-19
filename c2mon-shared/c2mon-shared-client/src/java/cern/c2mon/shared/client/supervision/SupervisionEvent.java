/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.shared.client.supervision;

import java.io.Serializable;
import java.sql.Timestamp;

import cern.c2mon.shared.client.request.ClientRequestResult;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionEntity;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus;

/**
 * A SupervisionEvent marks a change or confirmation of the status
 * of a given entity.
 * 
 * <p>Supervision events thrown by the supervision module are logged
 * in the supervision log table. The current status of an entity can
 * also be requested through the SupervisionFacade; it is returned
 * as a SupervisionEvent. 
 * 
 * <p>Notice that only the free text message should ever return
 * null.
 * 
 * @author Mark Brightwell
 *
 */
public interface SupervisionEvent extends ClientRequestResult, Cloneable, Serializable {

  /**
   * Returns the entity type to which the status applied.
   * @return the entity type (Process, Equipment or SubEquipment)
   */
  SupervisionEntity getEntity();

  /**
   * Returns the id of the entity to which the status applies.
   * @return the unique id; never returns null
   */
  Long getEntityId();

  /**
   * Returns the status of the entity concerned (the new or
   * confirmed status valid until a new event arrives).
   * @return the current/new status; never returns null
   */
  SupervisionStatus getStatus();

  /**
   * Returns the timestamp when this event is generated. This
   * can either correspond to the time a change was detected
   * to the entity status, or simply the time the current
   * status was checked, following a request.
   * 
   * @return the time at which the event object was created;
   *        never returns null
   */
  Timestamp getEventTime();

  /**
   * Returns an optional message describing the reason for the
   * new status.
   * @return an optional text message; can be null
   */
  String getMessage();
  
  /**
   * Clones the instance of that interface
   * @return A clone of the object
   * @throws CloneNotSupportedException Thrown in case that one of the
   *         fields of the interface implementation is not cloneable.
   */
  SupervisionEvent clone() throws CloneNotSupportedException;

}
