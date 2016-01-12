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

import cern.c2mon.shared.common.command.SourceCommandTag;

/**
 * Command tag add event
 * @author alang
 *
 */
public class CommandTagAdd extends Change implements ITagChange {
    /**
     * The equipmentId to add the command tag.
     */
    private long equipmentId;
    
    /**
     * The command tag to add.
     */
    private SourceCommandTag sourceCommandTag;
    
    /**
     * Creates a new command tag add event
     */
    public CommandTagAdd() {
        
    }
    
    /**
     * This constructor is a kind copy constructor it may be used from
     * subclasses to create an object of this class for serialization to
     * the DAQ core.
     * 
     * @param commandTagAdd The change object to copy.
     */
    public CommandTagAdd(final CommandTagAdd commandTagAdd) {
        setEquipmentId(commandTagAdd.getEquipmentId());
        setChangeId(commandTagAdd.getChangeId());
        setSourceCommandTag(commandTagAdd.getSourceCommandTag());
    }
    
    /**
     * Creates a new fully initialized CommandTagAdd object.
     * @param changeId The change id of the object which should be unique over
     * all in one message transmitted changes.
     * @param equipmentId The equipmentId to add the data tag to.
     * @param sourceCommandTag The SourceCommandTag to add.
     */
    public CommandTagAdd(final long changeId, final long equipmentId, 
            final SourceCommandTag sourceCommandTag) {
        setChangeId(changeId);
        this.equipmentId = equipmentId;
        this.sourceCommandTag = sourceCommandTag;
    }

    /**
     * @return the sourceCommandTag
     */
    public SourceCommandTag getSourceCommandTag() {
        return sourceCommandTag;
    }

    /**
     * @param sourceCommandTag the sourceCommandTag to set
     */
    public void setSourceCommandTag(final SourceCommandTag sourceCommandTag) {
        this.sourceCommandTag = sourceCommandTag;
    }

    /**
     * @return the equipmentId
     */
    public long getEquipmentId() {
        return equipmentId;
    }

    /**
     * @param equipmentId the equipmentId to set
     */
    public void setEquipmentId(final long equipmentId) {
        this.equipmentId = equipmentId;
    }
}
