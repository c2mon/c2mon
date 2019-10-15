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
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * Common part of all cache objects that need supervising by the server. Supervision involves an alive timer, with
 * associated alive and state tags, as well as a current status and status description.
 *
 * @author Mark Brightwell
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
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
    private SupervisionStatus supervisionStatus = SupervisionStatus.DOWN;

    /**
     * Description/reason for the current status.
     */
    private String statusDescription;

    /**
     * The time of the last update of the supervision status.
     */
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
        this(id2);
    }

    /**
     * Constructor.
     *
     * @param id2
     */
    protected AbstractSupervisedCacheObject(final Long id) {
        this();
        this.id = id;
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

    /**
     * Constructor.
     *
     * @param id
     * @param name
     * @param stateTagId
     */
    public AbstractSupervisedCacheObject(Long id, String name, Long stateTagId) {
        this(id);
        this.name = name;
        this.stateTagId = stateTagId;
    }

    /**
     * Clone implementation. All runtime information is frozen in clone, that no longer resides in cache (such as
     * SupervisionStatus).
     *
     * @return clone of cache object
     */
    @Override
    public AbstractSupervisedCacheObject clone() throws CloneNotSupportedException {
        AbstractSupervisedCacheObject cacheObject = (AbstractSupervisedCacheObject) super.clone();

        if (this.statusTime != null) {
          cacheObject.statusTime = (Timestamp) this.statusTime.clone();
        }

        return cacheObject;
    }
}
