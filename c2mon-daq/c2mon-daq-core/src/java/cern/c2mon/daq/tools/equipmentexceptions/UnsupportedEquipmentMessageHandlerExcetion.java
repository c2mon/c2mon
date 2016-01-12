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
package cern.c2mon.daq.tools.equipmentexceptions;

/**
 * This Exception is used in case the configuration XML contains definition of
 * unsupported specialization of EquipmentMessageHandler
 */
public class UnsupportedEquipmentMessageHandlerExcetion extends EqException {

    /** Serial version UID */
    private static final long serialVersionUID = -1010010606309120448L;

    /**
     * Default constructor.
     */
    public UnsupportedEquipmentMessageHandlerExcetion() {

    }

    /**
     * Creates a new unsupported equipment message handler exception
     * with the provided info string.
     * 
     * @param info Information about the exception.
     */
    public UnsupportedEquipmentMessageHandlerExcetion(final String info) {
        super(info);
    }

    /**
     * Creates a new unsupported equipment message handler exception
     * with the provided error code.
     * 
     * @param code Exception error code.
     */
    public UnsupportedEquipmentMessageHandlerExcetion(final int code) {
        super(code);
    }
}
