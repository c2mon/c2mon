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
package cern.c2mon.daq.jec.config;

import cern.c2mon.shared.common.command.ISourceCommandTag;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.address.PLCHardwareAddress;

/**
 * Interface for classes implementing the ability to handle the tag configuration.
 * 
 * @author Andreas Lang
 *
 */
public interface IJECTagConfigurationController {
    
    /**
     * Checks if the provided hardware address is within the configured PLC
     * address space.
     * 
     * @param hardwareAddress The hardware address to check.
     * @return True if the hardware address is in range else false.
     */
    boolean isInAddressRange(final PLCHardwareAddress hardwareAddress);

    /**
     * Configures a source data tag.
     * 
     * @param sourceDataTag The source data tag to configure.
     */
    void configureDataTag(final ISourceDataTag sourceDataTag);

    /**
     * Configures a source command tag.
     * 
     * @param sourceCommandTag The source command tag to configure.
     */
    void configureCommandTag(final ISourceCommandTag sourceCommandTag);
    
    /**
     * Clears all configures tags from the configuration.
     */
    void clearTagConfiguration();
    
    /**
     * Removes a data tag from the configuration.
     * 
     * @param sourceDataTag The source data tag to remove from the configuration.
     */
    void removeDataTag(final ISourceDataTag sourceDataTag);
    
    /**
     * Removes a source command tag from the configuration.
     * 
     * @param sourceCommandTag The source command tag to remove from the configuration.
     */
    void removeCommandTag(final ISourceCommandTag sourceCommandTag);
    
}
