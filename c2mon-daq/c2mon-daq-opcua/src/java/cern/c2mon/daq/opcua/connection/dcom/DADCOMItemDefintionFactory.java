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
package cern.c2mon.daq.opcua.connection.dcom;

import cern.c2mon.daq.opcua.connection.common.impl.ClassicItemDefinitionFactory;

/**
 * Creates DADCOMItemDefinitons.
 * 
 * @author Andreas Lang
 *
 */
public class DADCOMItemDefintionFactory 
        extends ClassicItemDefinitionFactory<DADCOMItemDefintion>  {

    /**
     * Creates a DADCOMItemDefintion.
     * 
     * @param id The id of the defintion.
     * @param opcItemName the item name/address of the defintion.
     * @return The new definition.
     */
    @Override
    public DADCOMItemDefintion createItemDefinition(
            final long id, final String opcItemName) {
        return new DADCOMItemDefintion(id, opcItemName.trim());
    }

    /**
     * Creates a DADCOMItemDefintion.
     * 
     * @param id The id of the defintion.
     * @param opcItemName the item name/address of the defintion.
     * @param redundantOpcItemName the redundant item name/address of the
     * defintion.
     * @return The new definition.
     */
    @Override
    public DADCOMItemDefintion createItemDefinition(
            final long id, final String opcItemName,
            final String redundantOpcItemName) {
        return new DADCOMItemDefintion(id, opcItemName.trim(), redundantOpcItemName.trim());
    }

}
