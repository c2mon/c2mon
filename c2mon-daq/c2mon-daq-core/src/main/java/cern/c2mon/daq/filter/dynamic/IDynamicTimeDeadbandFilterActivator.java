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
package cern.c2mon.daq.filter.dynamic;


import java.util.Map;

import cern.c2mon.shared.common.datatag.SourceDataTag;

/**
 * Implementations of this class control the activation and inactivation of the
 * dynamic time deadband filtering based on the occurrence of a tag.
 * 
 * @author alang
 *
 */
public interface IDynamicTimeDeadbandFilterActivator {

    /**
     * This should be called if a new tag is send to the server (or should be 
     * send to the server if time deadband filtering would be inactive).
     * The implementing class should base the filtering on how often this 
     * method is called. 
     * @param tagID The id of the called tag.
     */
    void newTagValueSent(long tagID);

    /**
     * Returns the currently used data tag table.
     * @return The current data tag table.
     */
    Map<Long, SourceDataTag> getDataTagMap();
    
    /**
     * Adds a data tag to be controlled by this activator.
     * @param sourceDataTag The data tag that should be controlled 
     * by this activator.
     */
    void addDataTag(SourceDataTag sourceDataTag);
    
    /**
     * Removes a data tag from the control of this activator.
     * Implementing classes should make sure time deadband filtering is 
     * deactivated when releasing a tag from their control.
     * @param sourceDataTag The data tag to remove from he control of this
     * activator.
     */
    void removeDataTag(SourceDataTag sourceDataTag);

    /**
     * This method removes all data tags from the time deadband activator.
     */
    void clearDataTags();
    
}
