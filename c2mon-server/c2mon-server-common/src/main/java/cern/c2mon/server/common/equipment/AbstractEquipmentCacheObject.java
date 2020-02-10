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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Abstract class representing an equipment to be monitored. It contains all the common fields for the
 * SubEquipmentCacheObject and EquipmentCacheObject classes.
 *
 * @author Mark Brightwell
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractEquipmentCacheObject extends AbstractSupervisedCacheObject implements AbstractEquipment {

    private static final long serialVersionUID = -5739956036131280454L;

    /**
     * Free-text description of the equipment.
     */
    private String description;

    /**
     * Fully qualified name of the EquipmentMessageHandler subclass to be used by the DAQ to connect to the equipment.
     */
    private String handlerClassName;

    /**
     * Identifier of the equipment's communication fault tag.
     */
    private Long commFaultTagId;

    /**
     * Value of the commfault tag that will be interpreted as an equipment problem. This value is the same for ALL
     * equipment.
     */
    private static final Boolean COMM_FAULT_TAG_VALUE = Boolean.FALSE;

    /**
     * Constructor setting the minimal set of fields that can be expected to be non-null for all (sub-)equipment cache
     * objects circulating in the server. Is used to construct fake cache objects.
     *
     * @param id id
     * @param name name
     * @param handlerClass class name
     * @param stateTagId state tag id
     */
    public AbstractEquipmentCacheObject(final Long id, final String name, final String handlerClass, final Long stateTagId) {
        super(id, name, stateTagId);
        this.setHandlerClassName(handlerClass);
    }

    /**
     * Creates a MonitoringEquipment object containing the provided information
     *
     * @param pId Identifier of the equipment
     * @param pName Name of the equipment
     * @param pDescription Description of the equipment
     * @param pHandlerClassName Class that handles the equipment
     * @param pStateTagId Tag that represents the state of the equipment
     * @param pAliveTagId Alive tag indicating that the equipment is running
     * @param pAliveInterval Interval for the alive tag
     * @param pCommfaultTagId Tag indicating the state of the communication with the equipment
     */
    public AbstractEquipmentCacheObject(final Long pId, final String pName, final String pDescription,
            final String pHandlerClassName, final Long pStateTagId, final Long pAliveTagId, final Integer pAliveInterval,
            final Long pCommfaultTagId) {
        super(pId, pName, pStateTagId, pAliveTagId, pAliveInterval);
        this.description = pDescription;
        this.handlerClassName = pHandlerClassName;
        this.commFaultTagId = pCommfaultTagId;
    }

    public AbstractEquipmentCacheObject(long id) {
        super(id);
    }

    /**
     * Clone implementation. All runtime information is frozen in clone, that no longer resides in cache (such as
     * SupervisionStatus).
     */
    @Override
    public AbstractEquipmentCacheObject clone() {
        return (AbstractEquipmentCacheObject) super.clone();
    }


    /**
     * Get the value of the equipment's communication fault tag that will be interpreted as a communication fault.
     *
     * @return the "bad" value of the equipment's communication fault tag
     */
    public Boolean getCommFaultTagValue() {
        return COMM_FAULT_TAG_VALUE;
    }

}
