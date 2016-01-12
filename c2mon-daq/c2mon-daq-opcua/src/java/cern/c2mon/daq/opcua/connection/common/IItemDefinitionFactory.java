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
package cern.c2mon.daq.opcua.connection.common;

import cern.c2mon.daq.opcua.connection.common.impl.ItemDefinition;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
/**
 * Factory to create item definitions based on a HardwareAddress.
 * 
 * @author Andreas Lang
 *
 * @param <ID> The item definition type which is created from this factory. It
 * has to extend the {@link ItemDefinition} object.
 */
public interface IItemDefinitionFactory<ID extends ItemDefinition< ? > > {

    /**
     * Creates a new ItemAddress.
     * 
     * @param id The id of the definition to create.
     * @param hardwareAddress The HardwareAddress which contains the
     * configuration supplied from the core.
     * @return The created ItemDefinition object.
     */
    ID createItemDefinition(long id, final HardwareAddress hardwareAddress);

}
