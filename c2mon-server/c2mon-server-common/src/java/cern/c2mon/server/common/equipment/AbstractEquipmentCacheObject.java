/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project. See http://ts-project-tim.web.cern.ch
 * Copyright (C) 2005-2011 CERN. This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details. You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA. Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.common.equipment;

/**
 * Abstract class representing an equipment to be monitored. It contains all the common fields for the
 * SubEquipmentCacheObject and EquipmentCacheObject classes.
 * 
 * @author Mark Brightwell
 */
public abstract class AbstractEquipmentCacheObject extends AbstractSupervisedCacheObject implements AbstractEquipment,
        Cloneable {

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

    protected AbstractEquipmentCacheObject() {
    }

    /**
     * Constructor. Used during runtime (re-)configuration.
     * 
     * @param id the unique id of the equipment
     */
    protected AbstractEquipmentCacheObject(final Long id) {
        super(id);
    }

    /**
     * Constructor setting the minimal set of fields that can be expected to be non-null for all (sub-)equipment cache
     * objects circulating in the server. Is used to construct fake cache objects.
     * 
     * @param id id
     * @param name name
     * @param handlerClass class name
     * @param stateTagId state tag id
     */
    public AbstractEquipmentCacheObject(final Long id, final String name, final String handlerClass,
            final Long stateTagId) {
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
            final String pHandlerClassName, final Long pStateTagId, final Long pAliveTagId, final int pAliveInterval,
            final Long pCommfaultTagId) {
        super(pId, pName, pStateTagId, pAliveTagId, pAliveInterval);
        this.description = pDescription;
        this.handlerClassName = pHandlerClassName;
        this.commFaultTagId = pCommfaultTagId;
    }

    /**
     * Clone implementation. All runtime information is frozen in clone, that no longer resides in cache (such as
     * SupervisionStatus).
     */
    public AbstractEquipment clone() {
        return (AbstractEquipmentCacheObject) super.clone();
    }

    /**
     * Get the free-text description of the equipment.
     * 
     * @return a free-text description of the equipment
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Get the fully qualified name of the EquipmentMessageHandler subclass used to connect to the equipment by the DAQ
     * process.
     * 
     * @return the fully qualified class name of the EquipmentMessageHandler subclass used to communicate with the
     *         equipment
     */
    public String getHandlerClassName() {
        return this.handlerClassName;
    }

    /**
     * Get the identifier of the equipment's communication fault tag.
     * 
     * @return the identifier of the equipment's communication fault tag
     */
    public Long getCommFaultTagId() {
        return this.commFaultTagId;
    }

    /**
     * Get the value of the equipment's communication fault tag that will be interpreted as a communication fault.
     * 
     * @return the "bad" value of the equipment's communication fault tag
     */
    public Boolean getCommFaultTagValue() {
        return COMM_FAULT_TAG_VALUE;
    }

    /**
     * @param description the description to set
     */
    public final void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param handlerClassName the handlerClassName to set
     */
    public final void setHandlerClassName(String handlerClassName) {
        this.handlerClassName = handlerClassName;
    }

    /**
     * @param commFaultTagId the commFaultTagId to set
     */
    public final void setCommFaultTagId(Long commFaultTagId) {
        this.commFaultTagId = commFaultTagId;
    }

}
