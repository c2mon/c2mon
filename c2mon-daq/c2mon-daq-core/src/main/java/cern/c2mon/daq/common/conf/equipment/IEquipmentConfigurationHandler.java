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
 * Interface for an equipment configuration handler which is used
 * to restrict visibility and access to the configuration.
 * 
 * @author Andreas Lang
 *
 */
public interface IEquipmentConfigurationHandler {
    
    /**
     * Gets the id of the equipment this handler accesses.
     * @return The id of the equipment this handler accesses.
     */
    long getEquipmentId();
    
    /**
     * Gets the equipment configuration.
     * @return The EquipmentConfiguration object of this handler.
     */
    IEquipmentConfiguration getEquipmentConfiguration();
    
    /**
     * Sets the data tag changer of this handler. This method should be called
     * from the implementation layer. If not set a standard implementation
     * will be used which will set the REBOOT state in the reports if the change
     * is important for the equipment.
     * 
     * @param dataTagChanger The data tag changer to set.
     */
    void setDataTagChanger(final IDataTagChanger dataTagChanger);
    
    /**
     * Sets the command tag changer of this handler. This method should be called
     * from the implementation layer. If not set a standard implementation
     * will be used which will set the REBOOT state in the reports if the change
     * is important for the equipment.
     * 
     * @param commandTagChanger The command tag changer to set.
     */
    void setCommandTagChanger(final ICommandTagChanger commandTagChanger);
    
    /**
     * Sets the equipment configuration changer for this handler. If not set a standard implementation
     * will be used which will set the REBOOT state in the reports if the change
     * is important for the equipment.
     * 
     * @param equipmentConfigurationChanger The equipment configuration changer
     * for this handler.
     */
    void setEquipmentConfigurationChanger(
            final IEquipmentConfigurationChanger equipmentConfigurationChanger);
    // TODO process configuration changer if needed for implementation.

}
