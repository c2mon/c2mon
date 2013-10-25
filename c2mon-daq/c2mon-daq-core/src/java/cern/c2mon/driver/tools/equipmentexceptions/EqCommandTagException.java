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
package cern.c2mon.driver.tools.equipmentexceptions;

/**
 * This Exception should be used to informing the driver about errors that
 * Occurres while specialized subclass of EquipmentMessageHandler executes a
 * command
 */
public class EqCommandTagException extends EqException {

    /** Serial version UID */
    private static final long serialVersionUID = -6291868467392281457L;

    /**
     * Creates a new EqCommandTagException.
     * 
     * @param code The error code for the exception.
     * @param descr The description for the exception.
     */
    public EqCommandTagException(final int code, final String descr) {
        super(code, descr);
    }

    /**
     * Creates a new EqCommandTagException.
     * 
     * @param code The error code for the exception.
     */
    public EqCommandTagException(final int code) {
        super(code);
    }

    /**
     * Creates a new EqCommandTagException.
     * 
     * @param descr The description for the exception.
     */
    public EqCommandTagException(final String descr) {
        super(descr);
    }
    
    /**
     * Creates a new EqCommandTagException.
     * 
     * @param descr The description for the exception.
     * @param e The thrown exception.
     */
    public EqCommandTagException(final String descr, final Throwable e) {
        super(descr, e);
    }

}
