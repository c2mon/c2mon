/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005 - 2011 CERN This program is free software; you can
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
package cern.c2mon.shared.daq.datatag;

import java.sql.Timestamp;

import cern.c2mon.shared.common.datatag.ValueChangeMonitor;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
/**
 * The Source Data Tag interface which is used to cover most of the information
 * for the implementation layer.
 * 
 * @author Andreas Lang
 *
 */
public interface ISourceDataTag {
    /**
     * Gets the id of the data tag.
     * @return The id of the data tag.
     */
    Long getId();
    /**
     * Gets the name of the data tag.
     * @return The name of the data tag.
     */
    String getName();
    /**
     * Gets the data tag data type.
     * @return The data tag data type.
     */
    String getDataType();
    /**
     * Gets the numeric representation of the data type.
     * @return The numeric representation of the data type. See {@link TagDataType}
     * constants for details.
     */
    int getDataTypeNumeric();
    /**
     * Gets the value deadband type.
     * @return The value deadband type.
     */
    short getValueDeadbandType();
    /**
     * Gets the value deadband.
     * @return The value deadband.
     */
    float getValueDeadband();
    /**
     * Gets the timedeadband of this tag. This is the maximum time the DAQ
     * sends updates to the server.
     * 
     * @deprecated This should only be used from the DAQ core. Equipment 
     * specific implementations which also provide a timedeadband mechanism
     * should use a value from their hardware address.
     * @return The time deadband of this tag.
     */
    int getTimeDeadband();
    /**
     * Gets the hardware address of this tag.
     * @return The hardware address of this tag or null if there is none.
     */
    HardwareAddress getHardwareAddress();
    /**
     * Gets the current value of this tag (might be null).
     * @return The current value of this tag.
     */
    SourceDataTagValue getCurrentValue();
    
    /**
     * Checks if tag has VCM registered
     * @return true if value check monitor is defined, false otherwise
     */
    boolean hasValueCheckMonitor();
        
    /**
     * Gets the value check monitor object, if defined
     * @return value check monitor object's reference 
     */
    ValueChangeMonitor getValueCheckMonitor();
    
    /**
     * Invalidate the current value of a SourceDataTag The invalidate method
     * will always return a SourceDataTagValue object, unless
     * <UL>
     * <LI>the quality object passed as a parameter is null
     * <LI>the quality code of the SourceDataQuality object is OK
     * <LI>the timestamp passed as a parameter is older than the timestamp of
     * the current value
     * </UL>
     * No deadband checks are applied for invalidation.
     * @param quality The quality of the source data tag value.
     * @param timestamp Timestamp for the invalidation.
     * @return The SourceDataTagValue to send to the server.
     */
    SourceDataTagValue invalidate(final SourceDataQuality quality, final Timestamp timestamp);
    /**
     * Invalidate the current value of a SourceDataTag The invalidate method
     * will always return a SourceDataTagValue object, unless
     * <UL>
     * <LI>the quality object passed as a parameter is null
     * <LI>the quality code of the SourceDataQuality object is OK
     * <LI>the timestamp passed as a parameter is older than the timestamp of
     * the current value
     * </UL>
     * No deadband checks are applied for invalidation.
     * @param quality The quality of the source data tag value.
     * @return The SourceDataTagValue to send to the server.
     */
    SourceDataTagValue invalidate(final SourceDataQuality quality);
    
}
