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
 * This exception should be thrown if any problem with SourceDataTag occurres on
 * the equipment message handler side
 */
public class EqDataTagException extends EqException {

    /** Serial version UID */
    private static final long serialVersionUID = 4040974979146654502L;

    /**
     * Creates a new EqDataTagException.
     * 
     * @param code The error code for the exception.
     * @param descr The description for the exception.
     */
    public EqDataTagException(final int code, final String descr) {
        super(code, descr);
    }

    /**
     * Creates a new EqDataTagException.
     * 
     * @param code The error code for the exception.
     */
    public EqDataTagException(final int code) {
        super(code);
    }

    /**
     * Creates a new EqDataTagException.
     * 
     * @param descr The description for the exception.
     */
    public EqDataTagException(final String descr) {
        super(descr);
    }

}
