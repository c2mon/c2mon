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

import lombok.Data;

/**
 * Command tag remove event.
 * @author alang
 *
 */
@Data
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
}
