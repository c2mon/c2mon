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
package cern.c2mon.shared.daq.config;


import lombok.Data;

/**
 * The equipment unit remove event.
 *
 * @author wbuczak
 */
@Data
public class EquipmentUnitRemove extends Change {

    /**
     * Equipment id
     */
    private long equipmentId;

    /**
     * Creates a new equipment unit remove event this is internally used.
     */
    public EquipmentUnitRemove() {
    }

    /**
     * This constructor is a kind copy constructor it may be used from subclasses to create an object of this class for
     * serialization to the DAQ core.
     *
     * @param eqUnitRemove The update object to copy.
     */
    public EquipmentUnitRemove(final EquipmentUnitRemove eqUnitRemove) {
        setChangeId(eqUnitRemove.getChangeId());
        setEquipmentId(eqUnitRemove.equipmentId);
    }

    /**
     * Creates a new equipment unit remove change.
     *
     * @param changeId The change id of the new change.
     * @param equipmentId The id of the equipment to add the data tag to.
     */
    public EquipmentUnitRemove(final Long changeId, final long equipmentId) {
        setChangeId(changeId);
        setEquipmentId(equipmentId);
    }
}
