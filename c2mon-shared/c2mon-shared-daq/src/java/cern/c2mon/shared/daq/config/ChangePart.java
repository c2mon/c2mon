/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005 - 2010 CERN This program is free software; you can
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
