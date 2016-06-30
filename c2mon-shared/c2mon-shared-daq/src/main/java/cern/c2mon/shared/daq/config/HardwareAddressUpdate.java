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

import java.util.HashMap;
import java.util.Map;
/**
 * A hardware address update.
 * @author alang
 *
 */
@Data
public class HardwareAddressUpdate extends ChangePart {
    /**
     * The class name
     */
    private transient String clazz;
    /**
     * Map of java field names and their new values (already parsed to the right
     * type)
     */
    private final Map<String, Object> changedValues = new HashMap<>();
    /**
     * Creates a new HardwareAddressUpdate (internal constructor)
     */
    public HardwareAddressUpdate() {
    }

    /**
     * Creates a new HardwareAddressUpdate with the provided class.
     * @param clazz The name of the HardwareAdddress class.
     */
    public HardwareAddressUpdate(final String clazz) {
        this.clazz = clazz;
    }

    /**
     * This constructor is a kind copy constructor it may be used from
     * subclasses to create an object of this class for serialization to
     * the DAQ core.
     *
     * @param hardwareAddressUpdate The update object to copy.
     */
    public HardwareAddressUpdate(final HardwareAddressUpdate hardwareAddressUpdate) {
        setClazz(hardwareAddressUpdate.getClazz());
        for (String key : hardwareAddressUpdate.getChangedValues().keySet()) {
            getChangedValues().put(key, hardwareAddressUpdate.getChangedValues().get(key));
        }
        for (String remove : hardwareAddressUpdate.getFieldsToRemove()) {
            getFieldsToRemove().add(remove);
        }
    }
}
