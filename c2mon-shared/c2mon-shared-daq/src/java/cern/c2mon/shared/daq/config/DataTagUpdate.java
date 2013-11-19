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
 * A datatag update POJO.
 * 
 * <p>If adding a field to this class, make sure the setter
 * adjusts the isEmpty field.
 * 
 * @author alang
 *
 */
@SuppressWarnings("unchecked")
public class DataTagUpdate extends Change implements ITagChange {
  
    /**
     * Records if this update contains a field to update or not.
     */
    private transient boolean isEmpty = true;    
    /**
     * The data tag id. 
     */
    private long dataTagId;
    /**
     * The equipment id where the command tag should be updated.
     */
    private long equipmentId;
    /**
     * The changes to the data tag address or null if unchanged.
     */
    private DataTagAddressUpdate dataTagAddressUpdate;
    /**
     * The new name of the data tag or null if unchanged.
     */
    private String name;
    /**
     * The new mode of the data tag or null if unchanged.
     */
    private Short mode;
    /**
     * The new data type of the data tag or null if unchanged.
     */
    private String dataType;
    /**
     * The new min-value of the datatag or null if unchanged.
     */
    private Comparable minValue;
    /**
     * The new max-value of the data tag or null if unchanged.
     */
    private Comparable maxValue;
    
    /**
     * Creates a new data tag update change.
     */
    public DataTagUpdate() {
    }
    
    /**
     * This constructor is a kind copy constructor it may be used from
     * subclasses to create an object of this class for serialization to
     * the DAQ core.
     * 
     * @param dataTagUpdate The change object to copy.
     */
    public DataTagUpdate(final DataTagUpdate dataTagUpdate) {
        setChangeId(dataTagUpdate.getChangeId());
        setDataTagAddressUpdate(new DataTagAddressUpdate(dataTagUpdate.getDataTagAddressUpdate()));
        setDataTagId(dataTagUpdate.getDataTagId());
        setDataType(dataTagUpdate.getDataType());
        setEquipmentId(dataTagUpdate.getEquipmentId());
        setMaxValue(dataTagUpdate.getMaxValue());
        setMinValue(dataTagUpdate.getMinValue());
        setMode(dataTagUpdate.getMode());
        setName(dataTagUpdate.getName());
        for (String remove : dataTagUpdate.getFieldsToRemove()) {
            getFieldsToRemove().add(remove);
        }
    }
    
    /**
     * Creates a new data tag update change.
     * @param changeId The change id of the new change.
     * @param dataTagId The data tag id to change.
     * @param equipmentId The equipment id to which the data tag belongs.
     */
    public DataTagUpdate(final long changeId, final long dataTagId,
            final long equipmentId) {
        setChangeId(changeId);
        this.dataTagId = dataTagId;
        this.equipmentId = equipmentId;
    }
    
    /**
     * @return the dataTagId
     */
    public Long getDataTagId() {
        return dataTagId;
    }
    /**
     * @param dataTagId the dataTagId to set
     */
    public void setDataTagId(final Long dataTagId) {
        this.dataTagId = dataTagId;
    }
    /**
     * @return the dataTagAddressUpdate or null if unchanged.
     */
    public DataTagAddressUpdate getDataTagAddressUpdate() {
        return dataTagAddressUpdate;
    }
    /**
     * @param dataTagAddressUpdate the dataTagAddressUpdate to set
     */
    public void setDataTagAddressUpdate(
            final DataTagAddressUpdate dataTagAddressUpdate) {
        setEmpty(false);
        this.dataTagAddressUpdate = dataTagAddressUpdate;
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
        setEmpty(false);
    }
    /**
     * @return the mode or null if unchanged.
     */
    public Short getMode() {      
        return mode;
    }
    /**
     * @param mode the mode to set
     */
    public void setMode(final Short mode) {      
        this.mode = mode;
        setEmpty(false);
    }
    /**
     * @return the dataType or null if unchanged.
     */
    public String getDataType() {
        return dataType;
    }
    /**
     * @param dataType the dataType to set
     */
    public void setDataType(final String dataType) {
        this.dataType = dataType;
        setEmpty(false);
    }
    /**
     * @return the minValue or null if unchanged.
     */
    public Comparable getMinValue() {
        return minValue;
    }
    /**
     * @param minValue the minValue to set
     */
    public void setMinValue(final Comparable minValue) {     
        this.minValue = minValue;
        setEmpty(false);
    }
    /**
     * @return the maxValue or null if unchanged.
     */
    public Comparable getMaxValue() {
        return maxValue;
    }
    /**
     * @param maxValue the maxValue to set
     */
    public void setMaxValue(final Comparable maxValue) {
        this.maxValue = maxValue;
        setEmpty(false);
    }

    /**
     * Sets the equipment id of the data tag.
     * @param equipmentId The equipment id of the data tag.
     */
    public void setEquipmentId(final long equipmentId) {
        this.equipmentId = equipmentId;
    }

    /**
     * Returns the equipment id of the data tag.
     * @return The equipment id of the data tag.
     */
    public long getEquipmentId() {
        return equipmentId;
    }

    /**
     * Sets the isEmpty field
     * @param isEmpty empty flag to set
     */
    private void setEmpty(final boolean isEmpty) {
      this.isEmpty = isEmpty;
    }

    /**
     * Returns true if no fields were set in this update object,
     * in which case the server does not need to send it down to
     * the DAQ.
     * 
     * <p>For use on server side.
     * 
     * @return true if no fields are set
     */
    public boolean isEmpty() {
      return isEmpty;
    }
    
}
