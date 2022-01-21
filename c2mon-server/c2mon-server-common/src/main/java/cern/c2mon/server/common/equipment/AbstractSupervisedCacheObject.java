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

import java.sql.Timestamp;

import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

/**
 * Common part of all cache objects that need supervising by the server. Supervision involves an alive timer, with
 * associated alive and state tags, as well as a current status and status description.
 * 
 * @author Mark Brightwell
 */
public abstract class AbstractSupervisedCacheObject implements Supervised, Cloneable {

    private static final long serialVersionUID = -7826198425602484249L;

    /**
     * Default alive interval for all equipment.
     */
    private static final int ALIVE_DEFAULT_INTERVAL = 60000;

    /**
     * Interval in milliseconds at which the alive tag is expected to change.
     */
    private int aliveInterval = ALIVE_DEFAULT_INTERVAL;

    /**
     * Unique identifier of the equipment.
     */
    @QuerySqlField(name = "ID", index = true)
    private Long id;

    /**
     * Unique name of the equipment.
     */
    @QuerySqlField(name = "NAME", index = true)
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
    @QuerySqlField(name = "ALIVETAGID", index = true)
    private Long aliveTagId;

    
    /**
     * Protected default constructor
     */
    protected AbstractSupervisedCacheObject() {
      // Do nothing
    }

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
    public Supervised clone() {
      try {
        AbstractSupervisedCacheObject cacheObject = (AbstractSupervisedCacheObject) super.clone();

        if (this.statusTime != null) {
          cacheObject.statusTime = (Timestamp) this.statusTime.clone();
        }
        
        return cacheObject;
      } catch (CloneNotSupportedException e) {
        throw new RuntimeException("Exception caught in cloning Supervised object - this should not happen!", e);
      }
    }

    @Override
    public Integer getAliveInterval() {
        return aliveInterval;
    }

    @Override
    public Long getAliveTagId() {
        return aliveTagId;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the process and (re-)sets the associated JMS properties.
     * 
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public Long getStateTagId() {
        return stateTagId;
    }

    @Override
    public String getStatusDescription() {
        return statusDescription;
    }

    @Override
    public SupervisionStatus getSupervisionStatus() {
        return supervisionStatus;
    }

    @Override
    public void setStatusDescription(final String statusDescription) {
        this.statusDescription = statusDescription;
    }

    @Override
    public void setSupervisionStatus(final SupervisionStatus supervisionStatus) {
        this.supervisionStatus = supervisionStatus;
    }

    /**
     * @param stateTagId the stateTagId to set
     */
    public void setStateTagId(final Long stateTagId) {
        this.stateTagId = stateTagId;
    }

    /**
     * @param aliveTagId the aliveTagId to set
     */
    public void setAliveTagId(final Long aliveTagId) {
        this.aliveTagId = aliveTagId;
    }

    /**
     * @param aliveInterval the aliveInterval to set
     */
    public void setAliveInterval(final Integer aliveInterval) {
        this.aliveInterval = aliveInterval;
    }

    @Override
    public Long getId() {
        return id;
    }

    /**
     * @param aliveInterval the aliveInterval to set
     */
    public void setAliveInterval(final int aliveInterval) {
        this.aliveInterval = aliveInterval;
    }

    /**
     * @return the lastChange
     */
    @Override
    public Timestamp getStatusTime() {
        return statusTime;
    }

    /**
     * @param statusTime the lastChange to set
     */
    @Override
    public void setStatusTime(Timestamp statusTime) {
        this.statusTime = statusTime;
    }

}
