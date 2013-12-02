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
package cern.c2mon.driver.jec.config;

import cern.c2mon.shared.common.datatag.address.PLCHardwareAddress;
import cern.c2mon.shared.daq.command.ISourceCommandTag;
import cern.c2mon.shared.daq.datatag.ISourceDataTag;

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
