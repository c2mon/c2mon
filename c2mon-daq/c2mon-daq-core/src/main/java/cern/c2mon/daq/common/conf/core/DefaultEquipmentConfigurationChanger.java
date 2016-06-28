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

import cern.c2mon.daq.common.conf.equipment.EquipmentConfigurationChangerHelper;
import cern.c2mon.daq.common.conf.equipment.IEquipmentConfigurationChanger;
import cern.c2mon.shared.common.process.IEquipmentConfiguration;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ChangeReport.CHANGE_STATE;
/**
 * This is a default implementation. of the equipment configuration changer.
 * It returns for every change which might affect the implementation
 * layer the status REBOOT.
 * 
 * @author Andreas Lang
 *
 */
public class DefaultEquipmentConfigurationChanger implements IEquipmentConfigurationChanger {

    /**
     * Called after an update of the equipment configuration. Sets the state
     * of the report to REBOOT if any change is made which might affect
     * equipment specific things.
     * 
     * @param equipmentConfiguration The equipment configuration.
     * @param oldEquipmentConfiguration The equipment configuration before the changes.
     * @param changeReport The report to fill.
     */
    @Override
    public void onUpdateEquipmentConfiguration(final IEquipmentConfiguration equipmentConfiguration, 
            final IEquipmentConfiguration oldEquipmentConfiguration,
            final ChangeReport changeReport) {
        if (EquipmentConfigurationChangerHelper.hasAddressChanged(equipmentConfiguration, oldEquipmentConfiguration)) {
            changeReport.appendError("Address changes might affect " 
                    + "equipment specific behavior. But no changer is " 
                    + "registered. You have to restart the DAQ to apply "
                    + "the changes.");
            changeReport.setState(CHANGE_STATE.REBOOT);
        }
        if (EquipmentConfigurationChangerHelper.hasAliveTagIntervalChanged(equipmentConfiguration, oldEquipmentConfiguration)) {
            changeReport.appendError("Equipment alive tag interval changes might affect " 
                    + "equipment specific behavior. But no changer is " 
                    + "registered. You have to restart the DAQ to apply "
                    + "the changes.");
            changeReport.setState(CHANGE_STATE.REBOOT);
        }
        if (!changeReport.getState().equals(CHANGE_STATE.REBOOT)) {
            changeReport.setState(CHANGE_STATE.SUCCESS);
        }
    }

}
