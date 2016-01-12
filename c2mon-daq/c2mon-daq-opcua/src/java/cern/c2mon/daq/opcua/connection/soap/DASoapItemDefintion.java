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
package cern.c2mon.daq.opcua.connection.soap;

import cern.c2mon.daq.opcua.connection.common.impl.ItemDefinition;

/**
 * The SOAP (OPC XML DA) item definition.
 * 
 * @author Andreas Lang
 *
 */
public class DASoapItemDefintion extends ItemDefinition<String> {

    /**
     * Creates a new Soap item definiton.
     * 
     * @param id The id of the definition.
     * @param address The address of the item.
     */
    public DASoapItemDefintion(final long id, final String address) {
        super(id, address);
    }
    
    /**
     * Creates a new Soap item definiton.
     * 
     * @param id The id of the definition.
     * @param address The address of the item.
     * @param alternativeAddress The alternative address of the item.
     */
    public DASoapItemDefintion(
            final long id, final String address,
            final String alternativeAddress) {
        super(id, address, alternativeAddress);
    }

}
