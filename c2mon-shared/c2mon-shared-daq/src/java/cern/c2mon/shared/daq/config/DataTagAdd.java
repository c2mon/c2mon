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

import cern.c2mon.shared.common.datatag.SourceDataTag;

/**
 * The data tag add event.
 * @author alang
 *
 */
public class DataTagAdd extends Change implements ITagChange {
    /**
     * The equipmentId to add the command tag.
     */
    private long equipmentId;
    /**
     * The source data tag to add.
     */
    private SourceDataTag sourceDataTag;
    
    /**
     * Creates a new data tag add event this is internally used.
     */
    public DataTagAdd() {
    }
    
    /**
     * This constructor is a kind copy constructor it may be used from
     * subclasses to create an object of this class for serialization to
     * the DAQ core.
     * 
     * @param dataTagAdd The update object to copy.
     */
    public DataTagAdd(final DataTagAdd dataTagAdd) {
        setChangeId(dataTagAdd.getChangeId());
        setEquipmentId(dataTagAdd.getEquipmentId());
        setSourceDataTag(dataTagAdd.getSourceDataTag());
    }
    
    /**
     * Creates a new data tag add change.
     * 
     * @param changeId The change id of the new change.
     * @param equipmentId The id of the equipment to add the data tag to.
     * @param sourceDataTag The source data tag to add.
     */
    public DataTagAdd(final Long changeId, final long equipmentId,
            final SourceDataTag sourceDataTag) {
        setChangeId(changeId);
        this.equipmentId = equipmentId;
        this.sourceDataTag = sourceDataTag;
    }

    /**
     * @return the sourceDataTag to be added
     */
    public SourceDataTag getSourceDataTag() {
        return sourceDataTag;
    }

    /**
     * @param sourceDataTag the sourceDataTag to set
     */
    public void setSourceDataTag(final SourceDataTag sourceDataTag) {
        this.sourceDataTag = sourceDataTag;
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
