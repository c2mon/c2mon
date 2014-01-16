/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project. See http://ts-project-tim.web.cern.ch
 * Copyright (C) 2005-2010 CERN. This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details. You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA. Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.common.equipment;

import java.util.Collection;
import java.util.LinkedList;

import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionEntity;

/**
 * Equipment cache object implementation.
 * 
 * @author Mark Brightwell
 */
public class EquipmentCacheObject extends AbstractEquipmentCacheObject implements Equipment, Cloneable {

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
     * Collection of ids for all the datatags attached to this equipment (not control tags?) (recall subequipments have
     * no datatags attached to them).
     */
    private LinkedList<Long> dataTagIds = new LinkedList<Long>();

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
    @SuppressWarnings("unchecked")
    public EquipmentCacheObject clone() {
        EquipmentCacheObject equipmentCacheObject = (EquipmentCacheObject) super.clone();
        equipmentCacheObject.commandTagIds = (LinkedList<Long>) this.commandTagIds.clone();
        equipmentCacheObject.dataTagIds = (LinkedList<Long>) this.dataTagIds.clone();
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

    public void setDataTagIds(Collection<Long> dataTagIds) {
        this.dataTagIds = (LinkedList<Long>) dataTagIds;
    }

    @Override
    public Collection<Long> getDataTagIds() {
        return dataTagIds;
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
