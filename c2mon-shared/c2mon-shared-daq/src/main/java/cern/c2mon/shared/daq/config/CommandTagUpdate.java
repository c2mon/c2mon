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
 * A CommandTag update.
 * @author alang
 *
 */
@Data
public class CommandTagUpdate extends Change implements ITagChange {
    /**
     * The id of the command tag to update.
     */
    private long commandTagId;
    /**
     * The equipment id where the command tag should be updated.
     */
    private long equipmentId;
    /**
     * If set the new name of the command tag.
     */
    private String name;
    /**
     * If set the new source timeout.
     */
    private Integer sourceTimeout;
    /**
     * If set the new number of source retries.
     */
    private Integer sourceRetries;

    /**
     * If set the object containing the changes to the hardware address.
     */
    private HardwareAddressUpdate hardwareAddressUpdate;

    /**
     * Creates a new command tag update
     */
    public CommandTagUpdate() {
    }

    /**
     * This constructor is a kind copy constructor it may be used from
     * subclasses to create an object of this class for serialization to
     * the DAQ core.
     *
     * @param commandTagUpdate The change object to copy.
     */
    public CommandTagUpdate(final CommandTagUpdate commandTagUpdate) {
        setChangeId(commandTagUpdate.getChangeId());
        setCommandTagId(commandTagUpdate.getCommandTagId());
        setEquipmentId(commandTagUpdate.getEquipmentId());
        setName(commandTagUpdate.getName());
        setSourceRetries(commandTagUpdate.getSourceRetries());
        setSourceTimeout(commandTagUpdate.getSourceRetries());
        HardwareAddressUpdate hwAddressUpdate = commandTagUpdate.getHardwareAddressUpdate();
        if (hwAddressUpdate != null)
            setHardwareAddressUpdate(new HardwareAddressUpdate(commandTagUpdate.getHardwareAddressUpdate()));
        for (String remove : commandTagUpdate.getFieldsToRemove()) {
            getFieldsToRemove().add(remove);
        }
    }

    /**
     * Creates a new basic CommandTagUpdate object. After this fields to change
     * should be added.
     *
     * @param changeId The change id of the object which should be unique over
     * all in one message transmitted changes.
     * @param commandTagId The id of the command tag to update.
     * @param equipmentId The id of the equipment the command tag belongs to.
     */
    public CommandTagUpdate(final long changeId, final long commandTagId,
            final long equipmentId) {
        setChangeId(changeId);
        this.commandTagId = commandTagId;
        this.equipmentId = equipmentId;
    }
}
