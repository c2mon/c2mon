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
 * A CommandTag update.
 * @author alang
 *
 */
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
     * @return the name or null if unchanged.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return the sourceTimeout or null if unchanged.
     */
    public Integer getSourceTimeout() {
        return sourceTimeout;
    }

    /**
     * @param sourceTimeout the sourceTimeout to set
     */
    public void setSourceTimeout(final Integer sourceTimeout) {
        this.sourceTimeout = sourceTimeout;
    }

    /**
     * @return the sourceRetries or null if unchanged.
     */
    public Integer getSourceRetries() {
        return sourceRetries;
    }

    /**
     * @param sourceRetries the sourceRetries to set
     */
    public void setSourceRetries(final Integer sourceRetries) {
        this.sourceRetries = sourceRetries;
    }

    /**
     * @return the hardwareAddressUpdate or null if unchanged.
     */
    public HardwareAddressUpdate getHardwareAddressUpdate() {
        return hardwareAddressUpdate;
    }

    /**
     * @param hardwareAddressUpdate the hardwareAddressUpdate to set
     */
    public void setHardwareAddressUpdate(final HardwareAddressUpdate hardwareAddressUpdate) {
        this.hardwareAddressUpdate = hardwareAddressUpdate;
    }

    /**
     * Sets the equipment id.
     * @param equipmentId The equipment id.
     */
    public void setEquipmentId(final long equipmentId) {
        this.equipmentId = equipmentId;
    }

    /**
     * Gets the equipment id of the tag to update.
     * @return The equipment id of the tag to update.
     */
    public long getEquipmentId() {
        return equipmentId;
    }
    
}
