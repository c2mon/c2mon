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
package cern.c2mon.daq.common.conf.equipment;

import cern.c2mon.shared.common.process.IEquipmentConfiguration;

/**
 * Helper class to simplify checks for equipment implementations.
 * 
 * @author Andreas Lang
 *
 */
public class EquipmentConfigurationChangerHelper {
    
    private EquipmentConfigurationChangerHelper() {
    }

    /**
     * Checks if the alive tag interval of the equipment has changed.
     * @param equipmentConfiguration The new equipment configuration.
     * @param oldEquipmentConfiguration The old equipment configuration.
     * @return True if the the interval has changed else false.
     */
    public static boolean hasAliveTagIntervalChanged(
            final IEquipmentConfiguration equipmentConfiguration,
            final IEquipmentConfiguration oldEquipmentConfiguration) {
        return equipmentConfiguration.getAliveTagInterval()
                != oldEquipmentConfiguration.getAliveTagInterval();
    }

    /**
     * Checks if the address of the equipment has changed.
     * @param equipmentConfiguration The new equipment configuration.
     * @param oldEquipmentConfiguration The old equipment configuration.
     * @return True if the the address has changed else false.
     */
    public static boolean hasAddressChanged(
            final IEquipmentConfiguration equipmentConfiguration, 
            final IEquipmentConfiguration oldEquipmentConfiguration) {
        boolean hasChanged;
        if (oldEquipmentConfiguration.getAddress() == null) {
            if (equipmentConfiguration.getAddress() == null)
                hasChanged = false;
            else
                hasChanged = true;
        }
        else {
            hasChanged = !oldEquipmentConfiguration.getAddress().equals(equipmentConfiguration.getAddress());
        }
        return hasChanged;
    }

}
