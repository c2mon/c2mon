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
package cern.c2mon.daq.common;

import cern.c2mon.daq.tools.equipmentexceptions.EqCommandTagException;
import cern.tim.shared.daq.command.SourceCommandTagValue;

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
