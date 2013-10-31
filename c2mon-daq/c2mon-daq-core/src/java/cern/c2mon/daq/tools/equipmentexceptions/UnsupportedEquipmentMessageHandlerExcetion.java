/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2010 CERN This program is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
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
