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

import cern.c2mon.shared.daq.command.SourceCommandTag;

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
