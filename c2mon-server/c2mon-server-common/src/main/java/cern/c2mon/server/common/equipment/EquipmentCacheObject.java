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

import cern.c2mon.shared.common.supervision.SupervisionEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * Equipment cache object implementation.
 *
 * @author Mark Brightwell
 */
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class EquipmentCacheObject extends AbstractEquipmentCacheObject implements Equipment, Serializable {

    private static final long serialVersionUID = -7917360710791608270L;

    /**
     * Address parameters used by the handler class to connect to the equipment.
     */
    private String address;

    /**
     * Identifier of the process to which the equipment is attached.
     */
    private Long processId;

    private final SupervisionEntity supervisionEntity = SupervisionEntity.EQUIPMENT;

    /**
     * Ids of all command tag attached to this equipment.
     */
    private LinkedList<Long> commandTagIds = new LinkedList<>();

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

        return equipmentCacheObject;
    }

    public EquipmentCacheObject(long id) {
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

    public EquipmentCacheObject(Long pId, String pName, String pDescription, String pHandlerClassName, Long pStateTagId, Long pAliveTagId, Integer pAliveInterval, Long pCommfaultTagId) {
        super(pId, pName, pDescription, pHandlerClassName, pStateTagId, pAliveTagId, pAliveInterval, pCommfaultTagId);
    }
}
