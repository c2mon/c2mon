/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005 - 2011 CERN This program is free software; you can
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
package cern.c2mon.driver.common.conf.equipment;

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
