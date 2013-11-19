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
/**
 * Command tag remove event.
 * @author alang
 *
 */
public class CommandTagRemove extends Change implements ITagChange {
    /**
     * The id of the command tag to remove.
     */
    private long commandTagId;
    
    /**
     * The equipment id where the command tag should be removed.
     */
    private long equipmentId;
    
    /**
     * Creates a new command tag remove event.
     */
    public CommandTagRemove() {
    }
    
    /**
     * This constructor is a kind copy constructor it may be used from
     * subclasses to create an object of this class for serialization to
     * the DAQ core.
     * 
     * @param commandTagRemove The change object to copy.
     */
    public CommandTagRemove(final CommandTagRemove commandTagRemove) {
        setChangeId(commandTagRemove.getChangeId());
        setCommandTagId(commandTagRemove.getCommandTagId());
        setEquipmentId(commandTagRemove.getEquipmentId());
    }
    
    /**
     * Creates a new fully initialized CommandTagRemove object.
     * @param changeId The change id of the object which should be unique over
     * all in one message transmitted changes.
     * @param commandTagId The id of the command tag to remove
     * @param equipmentId The id of the equipment to remove the command tag from.
     */
    public CommandTagRemove(final long changeId, 
            final long commandTagId, final long equipmentId) {
        setChangeId(changeId);
        this.commandTagId = commandTagId;
        this.equipmentId = equipmentId;
    }

    /**
     * @return the commandTagId
     */
    public long getCommandTagId() {
        return commandTagId;
    }

    /**
     * @param commandTagId the commandTagId to set
     */
    public void setCommandTagId(final long commandTagId) {
        this.commandTagId = commandTagId;
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
