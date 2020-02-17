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

import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.shared.common.supervision.SupervisionEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collection;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AliveTag extends ControlTag {

    private static final long serialVersionUID = 215188677282763819L;

    /**
     * Interval at which the alive tag must be updated. This interval includes the ALIVE_TOLERANCE_FACTOR in order to
     * make sure that slight delays in data transmission or minor interruptions do not cause BigBrother to invalidate
     * data.
     */
    private Integer aliveInterval;

    /**
     * Name of the equipment or process represented by the alive tag. This information is used to generate
     * human-readable error messages.
     */
    private String supervisedName;

    /**
     * CommFaultTag of the equipment/process to which the aliveTag is attached.
     */
    private Long commFaultTagId;

    /**
     * Identifier of the state tag of the equipment/process to which the alive tag is attached.
     */
    private Long stateTagId;

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


    public AliveTag(Long id) {
        super(id, null, null);
    }

    /**
     * Constructor used in iBatis sqlmap.
     */
    public AliveTag(final Long aliveTagId, final Long supervisedId, final String supervisedName,
                    final String aliveType, Long commFaultTagId, final Long stateTagId, final Integer aliveInterval) {
        this(aliveTagId,supervisedId, supervisedName,
          SupervisionEntity.parse(aliveType), commFaultTagId, stateTagId, aliveInterval);
    }

    public AliveTag(final Long aliveTagId, final Long supervisedId, final String supervisedName,
                    final SupervisionEntity supervisionEntity, Long commFaultTagId, final Long stateTagId, final Integer aliveInterval) {
        super(aliveTagId, supervisedId, supervisionEntity);
        this.commFaultTagId = commFaultTagId;
        setValue(false);
        this.supervisedName = supervisedName;
        this.stateTagId = stateTagId;
        this.aliveInterval = aliveInterval;
    }

    public boolean setValueAndGetDifferent(boolean active){
        boolean different = getValue() != active;
        super.setValue(active);
        return different;
    }
}
