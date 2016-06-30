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
 * A data tag address update.
 * @author alang
 *
 */
@Data
public class DataTagAddressUpdate extends ChangePart {
    /**
     * The new guaranteedDelivery value or null if unchanged.
     */
    private Boolean guaranteedDelivery;
    /**
     * The new priority or null if unchanged.
     */
    private Integer priority;
    /**
     * The new time deadband or null if unchanged.
     */
    private Integer timeDeadband;
    /**
     * The new time to live or null if unchanged.
     */
    private Integer timeToLive;
    /**
     * The new value deadband type or null if unchanged.
     */
    private Short valueDeadbandType;
    /**
     * The new value deadband or null if unchanged.
     */
    private Float valueDeadband;
    /**
     * The new hardware address or null if unchanged.
     */
    private HardwareAddressUpdate hardwareAddressUpdate;

    /**
     * Default constructor.
     */
    public DataTagAddressUpdate() {
    }

    /**
     * This constructor is a kind copy constructor it may be used from
     * subclasses to create an object of this class for serialization to
     * the DAQ core.
     *
     * @param dataTagAddressUpdate The update object to copy.
     */
    public DataTagAddressUpdate(final DataTagAddressUpdate dataTagAddressUpdate) {
        setGuaranteedDelivery(dataTagAddressUpdate.getGuaranteedDelivery());
        HardwareAddressUpdate hwAddressUpdate = dataTagAddressUpdate.getHardwareAddressUpdate();
        if (hwAddressUpdate != null)
            setHardwareAddressUpdate(new HardwareAddressUpdate());
        setPriority(dataTagAddressUpdate.getPriority());
        setTimeDeadband(dataTagAddressUpdate.getTimeDeadband());
        setTimeToLive(dataTagAddressUpdate.getTimeToLive());
        setValueDeadband(dataTagAddressUpdate.getValueDeadband());
        setValueDeadbandType(dataTagAddressUpdate.getValueDeadbandType());
        for (String remove : dataTagAddressUpdate.getFieldsToRemove()) {
            getFieldsToRemove().add(remove);
        }
    }
}
