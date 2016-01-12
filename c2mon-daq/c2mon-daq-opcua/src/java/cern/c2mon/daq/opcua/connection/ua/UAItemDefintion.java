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
package cern.c2mon.daq.opcua.connection.ua;

import org.opcfoundation.ua.builtintypes.NodeId;

import cern.c2mon.daq.opcua.connection.common.impl.ItemDefinition;

/**
 * The OPC UA item definition.
 * 
 * @author Andreas Lang
 *
 */
public class UAItemDefintion extends ItemDefinition<NodeId> {

    /**
     * Creates a new UA item definiton.
     * 
     * @param id The id of the definiton.
     * @param address The address of the item.
     */
    public UAItemDefintion(final long id, final NodeId address) {
        super(id, address);
    }
    
    /**
     * Creates a new UA item definiton.
     * 
     * @param id The id of the definiton.
     * @param address The address of the item.
     * @param alternativeAddress The alternative address of the item.
     */
    public UAItemDefintion(final long id, final NodeId address,
            final NodeId alternativeAddress) {
        super(id, address, alternativeAddress);
    }

}
