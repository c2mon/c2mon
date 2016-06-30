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

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * A datatag update POJO.
 *
 * <p>If adding a field to this class, make sure the setter
 * adjusts the isEmpty field.
 *
 * @author alang
 *
 */
@Data
public class DataTagUpdate extends Change implements ITagChange {

    /**
     * Records if this update contains a field to update or not.
     */
    @JsonIgnore
    private transient boolean empty = true;
    /**
     * The data tag id.
     */
    private long dataTagId;
    /**
     * The equipment id where the command tag should be updated.
     */
    private long equipmentId;
    /**
     * The changes to the data tag address or null if unchanged.
     */
    private DataTagAddressUpdate dataTagAddressUpdate;
    /**
     * The new name of the data tag or null if unchanged.
     */
    private String name;
    /**
     * The new mode of the data tag or null if unchanged.
     */
    private Short mode;
    /**
     * The new data type of the data tag or null if unchanged.
     */
    private String dataType;
    /**
     * The new min-value of the datatag or null if unchanged.
     */
    private Number minValue;
    /**
     * The new max-value of the data tag or null if unchanged.
     */
    private Number maxValue;

    /**
     * Creates a new data tag update change.
     */
    public DataTagUpdate() {
    }

    /**
     * This constructor is a kind copy constructor it may be used from
     * subclasses to create an object of this class for serialization to
     * the DAQ core.
     *
     * @param dataTagUpdate The change object to copy.
     */
    public DataTagUpdate(final DataTagUpdate dataTagUpdate) {
        setChangeId(dataTagUpdate.getChangeId());
        setDataTagAddressUpdate(new DataTagAddressUpdate(dataTagUpdate.getDataTagAddressUpdate()));
        setDataTagId(dataTagUpdate.getDataTagId());
        setDataType(dataTagUpdate.getDataType());
        setEquipmentId(dataTagUpdate.getEquipmentId());
        setMaxValue(dataTagUpdate.getMaxValue());
        setMinValue(dataTagUpdate.getMinValue());
        setMode(dataTagUpdate.getMode());
        setName(dataTagUpdate.getName());
        for (String remove : dataTagUpdate.getFieldsToRemove()) {
            getFieldsToRemove().add(remove);
        }
    }

    /**
     * Creates a new data tag update change.
     * @param changeId The change id of the new change.
     * @param dataTagId The data tag id to change.
     * @param equipmentId The equipment id to which the data tag belongs.
     */
    public DataTagUpdate(final long changeId, final long dataTagId,
            final long equipmentId) {
        setChangeId(changeId);
        this.dataTagId = dataTagId;
        this.equipmentId = equipmentId;
    }

    /**
     * @param dataTagAddressUpdate the dataTagAddressUpdate to set
     */
    public void setDataTagAddressUpdate(final DataTagAddressUpdate dataTagAddressUpdate) {
        setEmpty(false);
        this.dataTagAddressUpdate = dataTagAddressUpdate;
    }
    /**
     * @param dataType the dataType to set
     */
    public void setDataType(final String dataType) {
        this.dataType = dataType;
        setEmpty(false);
    }
    /**
     * @param minValue the minValue to set
     */
    public void setMinValue(final Number minValue) {
        this.minValue = minValue;
        setEmpty(false);
    }
    /**
     * @param maxValue the maxValue to set
     */
    public void setMaxValue(final Number maxValue) {
        this.maxValue = maxValue;
        setEmpty(false);
    }
}
