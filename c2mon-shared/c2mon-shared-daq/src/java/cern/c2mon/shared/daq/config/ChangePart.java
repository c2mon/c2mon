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
package cern.c2mon.shared.daq.config;

import java.util.ArrayList;
import java.util.List;

/**
 * A part of a change event which means an object which has
 * maybe list of fields to remove.
 * @author alang
 *
 */
public abstract class ChangePart {

    /**
     * List of fields to remove.
     */
    private List<String> fieldsToRemove = new ArrayList<String>();

    /**
     * Adds a name of a field which should be removed.
     * @param javaFieldName The (java) name of the field to remove.
     */
    public void addFieldToRemove(final String javaFieldName) {
        fieldsToRemove.add(javaFieldName);
    }

    /**
     * Returns the list of field names to remove.
     * @return The list of field names to remove.
     */
    public List<String> getFieldsToRemove() {
        return fieldsToRemove;
    }
}
