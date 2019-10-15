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
package cern.c2mon.server.common.alive;

import cern.c2mon.server.common.AbstractCacheableImpl;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AliveTimerCacheObject extends AbstractCacheableImpl implements AliveTimer {

    private static final long serialVersionUID = 2151886747282763819L;

    /**
     * Interval at which the alive tag must be updated. This interval includes the ALIVE_TOLERANCE_FACTOR in order to
     * make sure that slight delays in data transmission or minor interruptions do not cause BigBrother to invalidate
     * data.
     */
    private Integer aliveInterval;

    /**
     * Type of alive tag: either ALIVE_TYPE_PROCESS, ALIVE_TYPE_EQUIPMENT or ALIVE_TYPE_SUBEQUIPMENT The aliveType, in
     * combination with the relatedId allows BigBrother to determine what action to take if the alive expires.
     */
    private String aliveType;

    /**
     * Contains the description of the aliveTimer type
     */
    private String aliveTypeDescription;

    /**
     * Identifier of the equipment or process represented by the alive tag. The relatedId, in combination with the
     * aliveType allows BigBrother to determine what action to take if the alive expires.
     */
    private Long relatedId;

    /**
     * Name of the equipment or process represented by the alive tag. This information is used by BigBrother to generate
     * human-readable error messages.
     */
    private String relatedName;

    /**
     * Identifier of the state tag of the equipment/process to which the alive tag is attached.
     */
    private Long relatedStateTagId;

    /**
     * Timestamp of the last update of the alive tag. This information is used to periodically check which alive timers
     * have expired.
     */
    private long lastUpdate;

    /**
     * All dependent alive timers. In the case of a "process alive", this collection will contain the ids of the alive
     * timers representing equipment attached to the DAQ process. If a process alive expires, BigBrother will stop the
     * alive timers of all equipment attached to the process as no data can possibly be acquired from the equipment if
     * the DAQ process is stopped. TODO are these used??? remove if not necessary
     */
    private Collection<Long> dependentAliveTimerIds = null;

    /**
     * Flag indicating whether the alive timer is active or not.
     */
    private boolean active = false;

    // ---------------------------------------------------------------------------
    // CONSTRUCTORS
    // ---------------------------------------------------------------------------

    /**
     * Constructor setting minimal set of non-null fields.
     */
    public AliveTimerCacheObject(Long id) {
        this.id = id;
    }

    /**
     * Constructor used in iBatis sqlmap.
     *
     * @param aliveTagId
     * @param relatedId
     * @param relatedName
     * @param relatedStateTagId
     * @param aliveType
     * @param aliveInterval
     */
    public AliveTimerCacheObject(final Long aliveTagId, final Long relatedId, final String relatedName,
            final Long relatedStateTagId, final String aliveType, final Integer aliveInterval) {
        this(aliveTagId);
        this.relatedId = relatedId;
        this.relatedName = relatedName;
        this.relatedStateTagId = relatedStateTagId;
        setAliveType(aliveType); // also sets alive description field
        this.aliveInterval = aliveInterval;
    }

    @Override
    public AliveTimerCacheObject clone() throws CloneNotSupportedException {
        AliveTimerCacheObject aliveTimer = (AliveTimerCacheObject) super.clone();
        if (this.dependentAliveTimerIds != null)
            aliveTimer.dependentAliveTimerIds = new ArrayList<>(this.dependentAliveTimerIds);

        return aliveTimer;
    }

    public synchronized Timestamp getUpdateTimestamp() {
        return new Timestamp(this.lastUpdate);
    }

    /**
     * Check whether this alive timer is related to a process alive tag.
     *
     * @return true if this alive timer is related to a process alive tag.
     */
    @Override
    public synchronized boolean isProcessAliveType() {
        return (getAliveType().equals(AliveTimer.ALIVE_TYPE_PROCESS));
    }

    /**
     * Check whether this alive timer is related to an equipemnt alive tag.
     *
     * @return true if this alive timer is related to an equipemnt alive tag.
     */
    @Override
    public synchronized boolean isEquipmentAliveType() {
        return (getAliveType().equals(AliveTimer.ALIVE_TYPE_EQUIPMENT));
    }

    /**
     * Check whether this alive timer is related to a subequipemnt alive tag.
     *
     * @return true if this alive timer is related to an subequipent alive tag.
     */
    public synchronized boolean isSubEquipmentAliveType() {
        return (getAliveType().equals(AliveTimer.ALIVE_TYPE_SUBEQUIPMENT));
    }

    /**
     * Sets the aliveType and accordingly the alive type description field.
     *
     * @param aliveType the aliveType to set
     */
    public void setAliveType(String aliveType) {
        this.aliveType = aliveType.trim();
        if (getAliveType().equals(AliveTimer.ALIVE_TYPE_PROCESS)) {
            this.aliveTypeDescription = AliveTimer.PROCESS_MSG;
        } else {
            if (getAliveType().equals(AliveTimer.ALIVE_TYPE_EQUIPMENT)) {
                this.aliveTypeDescription = AliveTimer.EQUIPMENT_MSG;
            } else {
                this.aliveTypeDescription = AliveTimer.SUBEQUIPMENT_MSG;
            }
        }
    }

    /**
     * @return the relatedAliveTimerIds
     */
    protected synchronized Collection<Long> getRelatedAliveTimerIds() {
        return dependentAliveTimerIds;
    }
}
