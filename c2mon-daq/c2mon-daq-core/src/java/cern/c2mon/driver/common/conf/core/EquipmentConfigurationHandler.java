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
package cern.c2mon.driver.common.conf.core;

import cern.c2mon.driver.common.conf.equipment.ICommandTagChanger;
import cern.c2mon.driver.common.conf.equipment.IDataTagChanger;
import cern.c2mon.driver.common.conf.equipment.IEquipmentConfiguration;
import cern.c2mon.driver.common.conf.equipment.IEquipmentConfigurationChanger;
import cern.c2mon.driver.common.conf.equipment.IEquipmentConfigurationHandler;

/**
 * Implementation of an equipment configuration handler which in fact wraps methods
 * of the configuration controller by inserting the equipment id it controls.
 * 
 * @author Andreas Lang
 *
 */
public class EquipmentConfigurationHandler implements IEquipmentConfigurationHandler {
    /**
     * The configuration controller to access.
     */
    private ConfigurationController configurationController;
    
    /**
     * The id of the equipment this handler controls.
     */
    private long equipmentId;
    
    /**
     * Creates a new configuration handler which accesses the provided 
     * configuration controller with the provided equipmentId.
     * @param equipmentId The equipment which this handler should access.
     * @param configurationController The configuration controller to use.
     */
    public EquipmentConfigurationHandler(
            final long equipmentId,
            final ConfigurationController configurationController) {
        this.configurationController = configurationController;
        this.equipmentId = equipmentId;
        configurationController.putImplementationCommandTagChanger(equipmentId, new DefaultCommandTagChanger());
        configurationController.putImplementationDataTagChanger(equipmentId, new DefaultDataTagChanger());
        configurationController.putImplementationEquipmentConfigurationChanger(equipmentId, new DefaultEquipmentConfigurationChanger());
        
    }
    
    /**
     * Gets the equipment configuration.
     * @return The EquipmentConfiguration object of this handler.
     */
    @Override
    public long getEquipmentId() {
        return equipmentId;
    }
    
    /**
     * Gets the equipment configuration.
     * @return The EquipmentConfiguration object of this handler.
     */
    @Override
    public IEquipmentConfiguration getEquipmentConfiguration() {
        return configurationController.getProcessConfiguration().getEquipmentConfiguration(getEquipmentId());
    }

    /**
     * Sets the commandTagChanger for the controlled equipment. If not set a standard implementation
     * will be used which will set the REBOOT state in the reports if the change
     * is important for the equipment.
     * 
     * @param commandTagChanger The command tag changer object to set.
     */
    @Override
    public void setCommandTagChanger(final ICommandTagChanger commandTagChanger) {
        configurationController.putImplementationCommandTagChanger(getEquipmentId(), commandTagChanger);
    }

    /**
     * Sets the data tag changer for the controlled equipment. If not set a standard implementation
     * will be used which will set the REBOOT state in the reports if the change
     * is important for the equipment.
     * 
     * @param dataTagChanger The data tag changer to set.
     */
    @Override
    public void setDataTagChanger(final IDataTagChanger dataTagChanger) {
        configurationController.putImplementationDataTagChanger(getEquipmentId(), dataTagChanger);
    }

    /**
     * Sets the equipment configuration changer for the controlled equipment.
     * If not set a standard implementation will be used which will set the 
     * REBOOT state in the reports if the change is important for the equipment.
     * 
     * @param equipmentConfigurationChanger The equipment configuration changer
     * to set.
     */
    @Override
    public void setEquipmentConfigurationChanger(
            final IEquipmentConfigurationChanger equipmentConfigurationChanger) {
        configurationController
            .putImplementationEquipmentConfigurationChanger(
                    equipmentId, equipmentConfigurationChanger);
        
    }

}
