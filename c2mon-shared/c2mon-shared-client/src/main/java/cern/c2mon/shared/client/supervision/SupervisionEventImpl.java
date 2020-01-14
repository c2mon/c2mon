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
package cern.c2mon.shared.client.supervision;

import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionEntity;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus;
import cern.c2mon.shared.util.json.GsonFactory;
import com.google.gson.Gson;
import lombok.Data;

import java.sql.Timestamp;

/**
 * <p>Implementation of the SupervisionEvent. Corresponds to the
 * objects stored in the Supervision log table. The sqlmap subpackage
 * contains an iBatis file for querying the DB.
 *
 * <p>Supervision event corresponding to a row in the supervision
 * log table. Can mark a change in the status or a confirmation
 * of the current status.
 *
 * @author Mark Brightwell
 *
 */
@Data
public class SupervisionEventImpl implements SupervisionEvent {

  /** Serial version UID */
  private static final long serialVersionUID = 4404796977086131652L;

  /** Gson parser singleton for that class */
  private static transient Gson gson = null;

  /**
   * The entity concerned (Process, Equipment, SubEquipment)
   */
  private SupervisionEntity entity;

  /**
   * The id of the entity (unique for the entity).
   */
  private long entityId;

  /** The name of the entity */
  private String name;

  /**
   * The new status of the entity.
   */
  private SupervisionStatus status;

  /**
   * The time of this change/confirmation of status.
   */
  private Timestamp eventTime;

  /**
   * Additional info (should only be used for informing the user, not
   * for specifying event; if necessary introduce a new status!).
   */
  private String message;

  /**
   * Constructor used by iBatis and Gson
   */
  @SuppressWarnings("unused")
  private SupervisionEventImpl() {
    // Do nothing
  };

  /**
   * Constructor.
   * @param entity The entity for which this supervision event is created
   * @param entityId The id of the entity
   * @param status one state from the <code>SupervisionStatus</code> enumeration
   * @param eventTime time of the event
   * @param  message Free text for describing this event
   */
  public SupervisionEventImpl(final SupervisionEntity entity,
                              final Long entityId,
                              final String name,
                              final SupervisionStatus status,
                              final Timestamp eventTime,
                              final String message) {
    this.entity = entity;
    this.entityId = entityId;
    this.name = name;
    this.status = status;
    this.eventTime = eventTime;
    this.message = message;
  }

  /**
   * @return The Gson parser singleton instance to serialize/deserialize Json
   * messages of that class
   */
  private static synchronized Gson getGson() {
    if (gson == null) {
      gson = GsonFactory.createGson();
    }

    return gson;
  }


  /**
   * Generates out of this class instance a JSON message
   * @return The serialized JSON representation of this class instance
   */
  public final String toJson() {
    return getGson().toJson(this);
  }


  /**
   * Deserialized the JSON string into a <code>SupervisionEvent</code> object instance
   * @param json A JSON string representation of a <code>SupervisionEventImpl</code> class
   * @return The deserialized <code>TransferTagValue</code> instance of the JSON message
   */
  public static final SupervisionEvent fromJson(final String json) {
    return getGson().fromJson(json, SupervisionEventImpl.class);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((entity == null) ? 0 : entity.hashCode());
    result = prime * result + Long.hashCode(entityId);
    result = prime * result + ((status == null) ? 0 : status.hashCode());
    return result;
  }

  /**
   * Compares all fields expect the free text message and the
   * event time.
   * @param obj The SupervisionEvent to compare with
   * @return <code>true</code>, if the two object are equal
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SupervisionEventImpl other = (SupervisionEventImpl) obj;
    if (entity == null) {
      if (other.entity != null)
        return false;
    } else if (!entity.equals(other.entity))
      return false;
    if (entityId != other.entityId)
      return false;
    if (status == null) {
      if (other.status != null)
        return false;
    } else if (!status.equals(other.status))
      return false;
    return true;
  }


  @Override
  public SupervisionEvent clone() throws CloneNotSupportedException {
    SupervisionEventImpl clone = (SupervisionEventImpl) super.clone();
    if (eventTime != null) {
      clone.eventTime = (Timestamp) eventTime.clone();
    }
    return clone;
  }
}
