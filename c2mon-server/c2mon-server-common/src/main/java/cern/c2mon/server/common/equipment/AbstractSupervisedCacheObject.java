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
package cern.c2mon.server.common.equipment;

import cern.c2mon.server.common.AbstractCacheableImpl;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEventImpl;
import cern.c2mon.shared.common.supervision.SupervisionStatus;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;

/**
 * Common part of all cache objects that need supervising by the server. Supervision involves an alive timer, with
 * associated alive and state tags, as well as a current status and status description.
 *
 * @author Mark Brightwell
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@Slf4j
public abstract class AbstractSupervisedCacheObject extends AbstractCacheableImpl implements Supervised {

    private static final long serialVersionUID = -7826198425602484249L;

    /**
     * Default alive interval for all equipment.
     */
    private static final int ALIVE_DEFAULT_INTERVAL = 60000;

    /**
     * Interval in milliseconds at which the alive tag is expected to change.
     */
    private Integer aliveInterval = ALIVE_DEFAULT_INTERVAL;

    /**
     * Unique name of the equipment.
     */
    private String name;

    /**
     * The current status of this supervision equipment.
     */
    @NonNull
    private SupervisionStatus supervisionStatus = SupervisionStatus.DOWN;

    /**
     * Description/reason for the current status.
     */
    @NonNull
    private String statusDescription;

    /**
     * The time of the last update of the supervision status.
     */
    @NonNull
    private Timestamp statusTime;

    /**
     * Identifier of the equipment's state tag.
     */
    private Long stateTagId;

    /**
     * Identifier of the equipment's alive tag (if any).
     */
    private Long aliveTagId;

    /**
     * Constructor.
     *
     * @param id2
     * @param stateTagId2
     */
    protected AbstractSupervisedCacheObject(Long id2, Long stateTagId2) {
        super(id2);
        this.stateTagId = stateTagId2;
    }

    /**
     * Constructor
     *
     * @param id
     * @param name
     * @param stateTagId
     * @param aliveTagId
     * @param aliveInterval
     */
    public AbstractSupervisedCacheObject(Long id, String name, Long stateTagId, Long aliveTagId, int aliveInterval) {
        this(id, name, stateTagId);
        this.aliveTagId = aliveTagId;
        this.aliveInterval = aliveInterval;
    }


    public AbstractSupervisedCacheObject(Long id, String name, Long stateTagId) {
        super(id);
        this.name = name;
        this.stateTagId = stateTagId;
    }

  public AbstractSupervisedCacheObject(long id) {
    super(id);
  }

  /**
     * Clone implementation. All runtime information is frozen in clone, that no longer resides in cache (such as
     * SupervisionStatus).
     *
     * @return clone of cache object
     */
    @Override
    public AbstractSupervisedCacheObject clone() {
        AbstractSupervisedCacheObject cacheObject = (AbstractSupervisedCacheObject) super.clone();

        if (this.statusTime != null) {
          cacheObject.statusTime = (Timestamp) this.statusTime.clone();
        }

        return cacheObject;
    }

    @Override
    public void setSupervision(SupervisionStatus supervisionStatus, String statusDescription, Timestamp statusTime) {
        this.supervisionStatus = supervisionStatus;
        this.statusDescription = statusDescription;
        this.statusTime = statusTime;
    }

    public SupervisionEvent getSupervisionEvent() {
        log.trace("Getting supervision status: " + getSupervisionEntity() + " " + getName() + " is " + getSupervisionStatus());

        Timestamp supervisionTime;
        String supervisionMessage;

        supervisionTime = getStatusTime() != null
          ? getStatusTime()
          : new Timestamp(System.currentTimeMillis());

        supervisionMessage = getStatusDescription() != null
          ? getStatusDescription()
          : getSupervisionEntity() + " " + getName() + " is " + getSupervisionStatus();

        return new SupervisionEventImpl(getSupervisionEntity(), id, getName(), getSupervisionStatus(),
          supervisionTime, supervisionMessage);
    }
}
