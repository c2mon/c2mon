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
