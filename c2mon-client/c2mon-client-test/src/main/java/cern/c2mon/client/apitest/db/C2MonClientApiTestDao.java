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
package cern.c2mon.client.apitest.db;

import java.util.List;

import cern.c2mon.client.apitest.CommandDef;
import cern.c2mon.client.apitest.EquipmentDef;
import cern.c2mon.client.apitest.MetricDef;

public interface C2MonClientApiTestDao {

    /**
     * This method returns all registered metrics for a given process
     * 
     * @return
     */

    List<MetricDef> getProcessMetrics(String processName);

    /**
     * This method returns all registered metrics for a given equipment
     * 
     * @return
     */
    List<MetricDef> getEquipmentMetrics(String equipmentName);

    /**
     * This method returns a list of equipments defined for a given list of processes
     * 
     * @param processNames
     * @return
     */
    List<EquipmentDef> getEquipments(String... processNames);
    
    
    /**
     * This method returns a list of registered commands for given computer
     * @return
     */
    List<CommandDef> getRegisteredCommands(String computerName);    

}
