/******************************************************************************
 * This file is part of the CERN Control and Monitoring (C2MON) platform.
 * 
 * See http://cern.ch/c2mon
 * 
 * Copyright (C) 2005-2013 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: C2MON team, c2mon-support@cern.ch
 *****************************************************************************/
package cern.c2mon.daq.common.impl;

import cern.c2mon.daq.common.ICommandRunner;
import cern.c2mon.daq.common.IEquipmentCommandHandler;
import cern.c2mon.daq.common.messaging.impl.RequestController;

/**
 * Handles the setting of a command runner for one equipment and covers
 * the MessageHandler to the implementation.
 * 
 * @author Andreas Lang
 *
 */
public class EquipmentCommandHandler implements IEquipmentCommandHandler {
    /**
     * The equipmentId to use.
     */
    private long equipmentId;
    /**
     * The request controller to register the command-runners
     */
    private RequestController requestController;
    /**
     * Creates a new command handler.
     * 
     * @param equipmentId The id of the equipment to control.
     * @param requestController The message handler to use.
     */
    public EquipmentCommandHandler(final long equipmentId, final RequestController requestController) {
        this.equipmentId = equipmentId;
        this.requestController = requestController;
    }
    /**
     * Sets the command runner for the controlled equipment.
     * @param commandRunner The command runner to set.
     */
    @Override
    public void setCommandRunner(final ICommandRunner commandRunner) {
        requestController.putCommandRunner(equipmentId, commandRunner);
    }
}
