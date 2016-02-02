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
package cern.c2mon.shared.common.datatag;

import java.sql.Timestamp;
import java.util.Map;

import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.common.type.TagDataType;
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
    @Deprecated
    int getTimeDeadband();
    /**
     * Gets the hardware address of this tag.
     * @return The hardware address of this tag or null if there is none.
     */
    HardwareAddress getHardwareAddress();

  /**
   * Gets the addressParmeters of this tag. If the address parameters are set the HardwareAddress should be null.
   * @return
   */
  Map<String, String> getAddressParameters();

    /**
     * Gets the current value of this tag (might be null).
     * @return The current value of this tag.
     */
    SourceDataTagValue getCurrentValue();

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
