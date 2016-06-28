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
package cern.c2mon.shared.common.command;

import cern.c2mon.shared.common.datatag.address.HardwareAddress;
/**
 * Interface for a SourceCommandTag this is used to cover the real source command
 * tag to the implementation layer. This gives us more freedom to change things
 * in the future.
 * 
 * @author Andreas Lang
 *
 */
public interface ISourceCommandTag {

    /**
     * Get the unique numeric identifier of the command tag.
     * 
     * @return the unique numeric identifier of the command tag.
     */
    Long getId();

    /**
     * Get the unique name of the command tag.
     * 
     * @return the unique name of the command tag.
     */
    String getName();

    /**
     * Returns the hardware address of this tag.
     * 
     * @return The hardware address of this tag.
     */
    HardwareAddress getHardwareAddress();
    
    /**
     * Returns the source timeout of this command. This is the time the
     * DAQ will wait until he considers a command send to the hardware as failed.
     * 
     * @deprecated This value is not intended to be used from the equipment
     * specific implementations. If they have also need a timeout they
     * should use a value from their hardware address.
     * @return The source timeout of this command.
     */
    int getSourceTimeout();

}
