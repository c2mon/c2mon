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

import cern.c2mon.shared.common.process.EquipmentConfiguration;
import cern.c2mon.shared.daq.config.ChangeReport;

/**
 * Interface to be implemented from parts of the core which are interested in
 * changes to the EquipmentConfiguration
 * 
 * @author Andreas Lang
 *
 */
public interface ICoreEquipmentConfigurationChanger {
    /**
     * This is called when an equipment unit update occurred.
     * 
     * @param equipmentConfiguration The updated equipment configuration.
     * @param oldEquipmentConfiguration The former equipment configuration.
     * @param changeReport The change report which needs to be filled.
     */
    void onUpdateEquipmentConfiguration(
            final EquipmentConfiguration equipmentConfiguration,
            final EquipmentConfiguration oldEquipmentConfiguration,
            final ChangeReport changeReport);
}
