/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005 - 2010 CERN This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.shared.daq.config;

import java.util.HashMap;
import java.util.Map;
/**
 * A hardware address update.
 * @author alang
 *
 */
public class HardwareAddressUpdate extends ChangePart {
    /**
     * The class name
     */
    private transient String clazz;
    /**
     * Map of java field names and their new values (already parsed to the right
     * type)
     */
    private final Map<String, Object> values = new HashMap<String, Object>();
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

    /**
     * Returns a map of java field names (already parsed to the type of the field.
     * If you like to modify this map you can do this by calling this method and
     * using the methods provided by the map.
     * @return Map of java field names parsed to the right type.
     */
    public Map<String, Object> getChangedValues() {
        return values;
    }

    /**
     * Sets name of the the class of this hardware address.
     * @param clazz The name of the class of this hardware address.
     */
    public void setClazz(final String clazz) {
        this.clazz = clazz;
    }

    /**
     * Gets the name of the class of this hardware address.
     * @return The name of the class of this hardware address.
     */
    public String getClazz() {
        return clazz;
    }

}
