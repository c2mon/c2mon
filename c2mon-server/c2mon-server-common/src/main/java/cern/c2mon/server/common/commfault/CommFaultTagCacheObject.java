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
package cern.c2mon.server.common.commfault;


/**
 * Cache object corresponding to entries in the DB CommFaultTag view.
 * 
 * @author Mark Brightwell
 */
public class CommFaultTagCacheObject implements CommFaultTag {
   
    private static final long serialVersionUID = 8760759761176480601L;

    private Long id;

    private Boolean faultValue = Boolean.FALSE; // always FALSE in TIM; TRUE not supported

    private Long equipmentId;

    private String equipmentName;

    private Long aliveTagId;

    private Long stateTagId;

    /**
     * Public default constructor (needed for iBatis).
     */
    public CommFaultTagCacheObject() { 
      // Do nothing
    }
    
    /**
     * Clone not supported so far.
     */
    public CommFaultTagCacheObject clone() throws CloneNotSupportedException {
        return (CommFaultTagCacheObject) super.clone();
    }
    
    /**
     * Constructor setting all fields.
     * 
     * @param id
     * @param faultValue
     * @param equipmentId
     * @param equipmentName
     * @param aliveTagId
     * @param stateTagId
     */
    public CommFaultTagCacheObject(Long id, Long equipmentId, String equipmentName, Long aliveTagId, Long stateTagId) {
        this(id);
        this.equipmentId = equipmentId;
        this.equipmentName = equipmentName;
        this.aliveTagId = aliveTagId;
        this.stateTagId = stateTagId;
    }

    /**
     * Constructor setting minimal set of non-null fields.
     * 
     * @param id
     */
    public CommFaultTagCacheObject(Long id) {
        this();
        this.id = id;
    }

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public Long getEquipmentId() {
        return this.equipmentId;
    }

    public String getEquipmentName() {
        return this.equipmentName;
    }

    public Long getAliveTagId() {
        return this.aliveTagId;
    }

    public Long getStateTagId() {
        return this.stateTagId;
    }

    public boolean hasAliveTag() {
        return this.aliveTagId != null;
    }

    public Boolean getFaultValue() {
        return this.faultValue;
    }

    public Boolean getOkValue() {
        return this.faultValue.booleanValue() ? Boolean.FALSE : Boolean.TRUE;
    }

    /**
     * @param aliveTagId the aliveTagId to set
     */
    public void setAliveTagId(Long aliveTagId) {
        this.aliveTagId = aliveTagId;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @param equipmentId the equipmentId to set
     */
    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
    }

    /**
     * @param equipmentName the equipmentName to set
     */
    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    /**
     * @param stateTagId the stateTagId to set
     */
    public void setStateTagId(Long stateTagId) {
        this.stateTagId = stateTagId;
    }


}
