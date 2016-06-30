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

import javax.xml.bind.annotation.XmlValue;


/**
 * The equipment unit add event.
 *
 * @author wbuczak
 */
@Data
public class EquipmentUnitAdd extends Change {
    /**
     * the equipment's unique identifier
     */
    private long equipmentId;

    /**
     * this string contains a CDATA section with EquipmentUnit XML block
     */
    @XmlValue
    private String equipmentUnitXml;

    /**
     * Creates a new equipment unit add event this is internally used.
     */
    public EquipmentUnitAdd() {
    }

    /**
     * This constructor is a kind copy constructor it may be used from subclasses to create an object of this class for
     * serialization to the DAQ core.
     *
     * @param eqUnitAdd The update object to copy.
     */
    public EquipmentUnitAdd(final EquipmentUnitAdd eqUnitAdd) {
        setChangeId(eqUnitAdd.getChangeId());
        this.equipmentId = eqUnitAdd.equipmentId;
        this.equipmentUnitXml = eqUnitAdd.equipmentUnitXml;
    }

    /**
     * Creates a new equipment unit add change.
     *
     * @param changeId The change id of the new change.
     * @param equipmentId The equipment identifier
     * @param equipmentUnitXml the XML configuration of the equipment (EquipmentUnit block)
     */
    public EquipmentUnitAdd(final Long changeId, final long equipmentId, final String equipmentUnitXml) {
        setChangeId(changeId);
        this.equipmentId = equipmentId;
        this.equipmentUnitXml = equipmentUnitXml;
    }
}
