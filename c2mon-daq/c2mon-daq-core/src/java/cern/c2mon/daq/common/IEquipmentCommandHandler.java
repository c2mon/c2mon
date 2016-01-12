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
package cern.c2mon.daq.common;

/**
 * Handles the registering of a command runner for one equipment.
 * 
 * @author Andreas Lang
 *
 */
public interface IEquipmentCommandHandler {
    
    /**
     * Sets the command runner of this handler. This method should be called
     * from the implementation layer. If not set the core will assume that the
     * implementation does not support running commands.
     * 
     * @param commandRunner The command runner to set.
     */
    void setCommandRunner(final ICommandRunner commandRunner);
}
