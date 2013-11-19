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
 * A data tag remove event
 * @author alang
 *
 */
public class DataTagRemove extends Change implements ITagChange {
    /**
     * The id of the data tag to remove.
     */
    private long dataTagId;
    
    /**
     * The equipment id where the data tag should be removed.
     */
    private long equipmentId;
    
    /**
     * Creates a new Data tag remove change.
     */
    public DataTagRemove() {
    }
    
    /**
     * This constructor is a kind copy constructor it may be used from
     * subclasses to create an object of this class for serialization to
     * the DAQ core.
     * 
     * @param dataTagRemove The update object to copy.
     */
    public DataTagRemove(final DataTagRemove dataTagRemove) {
        setChangeId(dataTagRemove.getChangeId());
        setDataTagId(dataTagRemove.getDataTagId());
        setEquipmentId(dataTagRemove.getEquipmentId());
    }
    
    /**
     * Creates a new Data tag remove change.
     * 
     * @param changeId The change id of the new change
     * @param dataTagId The id of the data tag to remove.
     * @param equipmentId The id of the equipment to remove the data tag from.
     */
    public DataTagRemove(final Long changeId, final long dataTagId, 
            final long equipmentId) {
        setChangeId(changeId);
        this.dataTagId = dataTagId;
        this.equipmentId = equipmentId;
    }

    /**
     * @return the dataTagId
     */
    public long getDataTagId() {
        return dataTagId;
    }

    /**
     * @param dataTagId the dataTagId to set
     */
    public void setDataTagId(final long dataTagId) {
        this.dataTagId = dataTagId;
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
