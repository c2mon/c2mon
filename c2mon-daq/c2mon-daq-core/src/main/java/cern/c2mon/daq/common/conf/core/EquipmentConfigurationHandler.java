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
package cern.c2mon.daq.common.conf.core;

import cern.c2mon.daq.common.conf.equipment.ICommandTagChanger;
import cern.c2mon.daq.common.conf.equipment.IDataTagChanger;
import cern.c2mon.daq.common.conf.equipment.IEquipmentConfigurationChanger;
import cern.c2mon.daq.common.conf.equipment.IEquipmentConfigurationHandler;
import cern.c2mon.shared.common.process.IEquipmentConfiguration;

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
   * @return the Equipment Unit name
   */
  public String getEquipmentName() {
      return this.getEquipmentConfiguration().getName();
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
