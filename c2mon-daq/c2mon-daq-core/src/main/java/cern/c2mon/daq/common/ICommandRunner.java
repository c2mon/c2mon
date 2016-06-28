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

import cern.c2mon.daq.tools.equipmentexceptions.EqCommandTagException;
import cern.c2mon.shared.daq.command.SourceCommandTagValue;

/**
 * Interface for a command runner.
 * 
 * @author Andreas Lang
 *
 */
public interface ICommandRunner {
    
    /**
     * This method is called if a command should be executed.
     * 
     * @param sourceCommandTagValue Defines the command to run.
     * @throws EqCommandTagException If the command fails an EqCommandTagException
     * should be thrown.
     * @return A String with additional information about the successful command.
     * The caller must be able to deal with null values.
     */
    String runCommand(final SourceCommandTagValue sourceCommandTagValue) throws EqCommandTagException;

}
