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
 * A update event for the equipment unit.
 * @author alang
 *
 */
@Data
public class EquipmentConfigurationUpdate extends Change {
    /**
     * Equipment id
     */
    private long equipmentId;
    /**
     * The changed name or null if unchanged
     */
    private String name;
    /**
     * The changed commfault tag id or null if unchanged.
     */
    private Long commfaultTagId;
    /**
     * The changed commfault tag value or null if unchanged.
     */
    private Boolean commfaultTagValue;
    /**
     * The changed alive tag id or null if unchanged.
     */
    private Long aliveTagId;
    /**
     * The changed alive interval or null if unchanged.
     */// TODO Why is this long? AliveInterval is Int in EquipmentCacheObject
    private Long aliveInterval;

    /**
     * the cahgned equipment address
     */
    private String equipmentAddress;

    /**
     * Creates a new equipment change.
     */
    public EquipmentConfigurationUpdate() {
    }

    /**
     * This constructor is a kind copy constructor it may be used from
     * subclasses to create an object of this class for serialization to
     * the DAQ core.
     *
     * @param equipmentConfigurationUpdate The change object to copy.
     */
    public EquipmentConfigurationUpdate(
            final EquipmentConfigurationUpdate equipmentConfigurationUpdate) {
        setAliveInterval(equipmentConfigurationUpdate.getAliveInterval());
        setAliveTagId(equipmentConfigurationUpdate.getAliveTagId());
        setChangeId(equipmentConfigurationUpdate.getChangeId());
        setCommfaultTagId(equipmentConfigurationUpdate.getCommfaultTagId());
        setCommfaultTagValue(equipmentConfigurationUpdate.getCommfaultTagValue());
        setEquipmentId(equipmentConfigurationUpdate.getEquipmentId());
        setName(equipmentConfigurationUpdate.getName());
        setEquipmentAddress(equipmentConfigurationUpdate.getEquipmentAddress());
        for (String remove : equipmentConfigurationUpdate.getFieldsToRemove()) {
            getFieldsToRemove().add(remove);
        }
    }

    /**
     * Creates a new equipment change.
     * @param changeId The change id of the new change.
     * @param equipmentId The id of the equipment to change.
     */
    public EquipmentConfigurationUpdate(final long changeId, final long equipmentId) {
        setChangeId(changeId);
        this.equipmentId = equipmentId;
    }

}
