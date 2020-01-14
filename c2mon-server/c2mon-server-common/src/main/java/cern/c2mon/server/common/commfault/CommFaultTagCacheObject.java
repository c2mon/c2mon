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
package cern.c2mon.server.common.commfault;


import cern.c2mon.server.common.AbstractCacheableImpl;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

/**
 * Cache object corresponding to entries in the DB CommFaultTag view.
 *
 * @author Mark Brightwell
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class CommFaultTagCacheObject extends AbstractCacheableImpl implements CommFaultTag {

    private static final long serialVersionUID = 8760759761176480601L;

    private final Boolean faultValue = Boolean.FALSE; // always FALSE in TIM; TRUE not supported

    private long equipmentId;

    private String equipmentName;

    private Long aliveTagId;

    private Long stateTagId;

    private Timestamp eventTimestamp;

    /**
     * Constructor setting all fields.
     *
     * @param id
     * @param equipmentId
     * @param equipmentName
     * @param aliveTagId
     * @param stateTagId
     */
    public CommFaultTagCacheObject(Long id, Long equipmentId, String equipmentName, Long aliveTagId, Long stateTagId) {
        super(id);
        this.equipmentId = equipmentId;
        this.equipmentName = equipmentName;
        this.aliveTagId = aliveTagId;
        this.stateTagId = stateTagId;
    }

    /**
     * Clone not supported so far.
     */
    @Override
    public CommFaultTagCacheObject clone() {
        return (CommFaultTagCacheObject) super.clone();
    }

    public boolean hasAliveTag() {
        return this.aliveTagId != null;
    }

    public Boolean getOkValue() {
        return this.faultValue ? Boolean.FALSE : Boolean.TRUE;
    }

}
