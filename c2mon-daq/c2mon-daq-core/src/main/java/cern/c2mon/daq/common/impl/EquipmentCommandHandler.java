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
