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


import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.shared.common.supervision.SupervisionEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Cache object corresponding to entries in the DB CommFaultTag view.
 *
 * @author Mark Brightwell
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class CommFaultTag extends ControlTag {

    private static final long serialVersionUID = 876075976116480601L;

    private String equipmentName;

    private Long aliveTagId;

    private Long stateTagId;

    /**
     * Constructor setting all fields.
     *
     * @param id
     * @param equipmentId
     * @param equipmentName
     * @param aliveTagId
     * @param stateTagId
     */
    public CommFaultTag(@NonNull Long id, @NonNull Long equipmentId, String equipmentName, String equipmentType, Long aliveTagId, Long stateTagId) {
        super(id, equipmentId, SupervisionEntity.parse(equipmentType));
        this.equipmentName = equipmentName;
        this.aliveTagId = aliveTagId;
        this.stateTagId = stateTagId;
    }

    /**
     * Clone not supported so far.
     */
    @Override
    public CommFaultTag clone() {
        return (CommFaultTag) super.clone();
    }

}
