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
package cern.c2mon.server.common.equipment;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;

import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionEntity;

/**
 * Equipment cache object implementation.
 *
 * @author Mark Brightwell
 */
public class EquipmentCacheObject extends AbstractEquipmentCacheObject implements Equipment, Cloneable, Serializable {

    private static final long serialVersionUID = -7917360710791608270L;

    /**
     * Address parameters used by the handler class to connect to the equipment.
     */
    private String address;

    /**
     * Identifier of the process to which the equipment is attached.
     */
    private Long processId;

    /**
     * Collection of the subequipments of this equipment. LinkedList since never access elements in the middle, and no
     * resizing is done on adding.
     */
    private LinkedList<Long> subEquipmentIds = new LinkedList<Long>();

    /**
     * Ids of all command tag attached to this equipment.
     */
    private LinkedList<Long> commandTagIds = new LinkedList<Long>();

    /**
     * Clone method that can be used by the server core and other modules to obtain their own copy of the equipment
     * cache object.
     *
     * @return a clone of the equipment
     */
    @Override
    @SuppressWarnings("unchecked")
    public EquipmentCacheObject clone() {
        EquipmentCacheObject equipmentCacheObject = (EquipmentCacheObject) super.clone();
        equipmentCacheObject.commandTagIds = (LinkedList<Long>) this.commandTagIds.clone();
        equipmentCacheObject.subEquipmentIds = (LinkedList<Long>) this.subEquipmentIds.clone();

        return equipmentCacheObject;
    }

    /**
     * Public constructor.
     */
    public EquipmentCacheObject() {
    }

    /**
     * Constructor (used in reconfiguration module).
     *
     * @param id the equipment id
     */
    public EquipmentCacheObject(final Long id) {
        super(id);
    }

    /**
     * Constructor setting the minimal set of fields that can be expected to be non-null for all equipment cache objects
     * circulating in the server.
     * <p>
     * Is used to construct fake cache objects (functionality not implemented so far)
     *
     * @param id
     * @param name
     * @param handlerClass
     * @param stateTagId
     */
    public EquipmentCacheObject(Long id, String name, String handlerClass, Long stateTagId) {
        super(id, name, handlerClass, stateTagId);
    }

    public EquipmentCacheObject(final Long pId, final String pName, final String pDescription,
            final String pHandlerClassName, final String pEquipmentAddress, final Long pStateTagId,
            final Long pAliveTagId, final int pAliveInterval, final Long pCommfaultTagId, final Long pProcessId) {
        super(pId, pName, pDescription, pHandlerClassName, pStateTagId, pAliveTagId, pAliveInterval, pCommfaultTagId);
        this.address = pEquipmentAddress;
        this.processId = pProcessId;
    }

    /**
     * Get the address configuration for the EquipmentMessageHandler to be able to communicate with the equipment.
     */
    public String getAddress() {
        return this.address;
    }

    /**
     * Get the identifier of the DAQ process to which the equipment is attached.
     *
     * @return the identifier of the DAQ process to which the equipment is attached
     */
    @Override
    public Long getProcessId() {
        return this.processId;
    }

    @Override
    public Collection<Long> getSubEquipmentIds() {
        return subEquipmentIds;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @param processId the processId to set
     */
    public void setProcessId(Long processId) {
        this.processId = processId;
    }

    /**
     * @param subEquipmentIds the subEquipmentIds to set
     */
    public void setSubEquipmentIds(Collection<Long> subEquipmentIds) {
        this.subEquipmentIds = (LinkedList<Long>) subEquipmentIds;
    }

    @Override
    public Collection<Long> getCommandTagIds() {
        return commandTagIds;
    }

    /**
     * Setter methods
     *
     * @param commandTagIds the commandTagIds to set
     */
    public void setCommandTagIds(final Collection<Long> commandTagIds) {
        this.commandTagIds = (LinkedList<Long>) commandTagIds;
    }

    @Override
    public SupervisionEntity getSupervisionEntity() {
        return SupervisionEntity.EQUIPMENT;
    }

}
