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
 * This Exception should be used to inform the driver about errors that occurres
 * while specialized subclass of EquipmentMessageHandler connects to or
 * disconnecting from equipment.
 */
public class EqIOException extends EqException {

    /** Serial version UID */
    private static final long serialVersionUID = -8464314694870885748L;

    /**
     * Creates a new EqIOException.
     * 
     * @param code The error code for the exception.
     * @param descr The description for the exception.
     */
    public EqIOException(final int code, final String descr) {
        super(code, descr);
    }

    /**
     * Creates a new EqIOException.
     * 
     * @param code The error code for the exception.
     */
    public EqIOException(final int code) {
        super(code);
    }

    /**
     * Creates a new EqIOException.
     * 
     * @param descr The description for the exception.
     */
    public EqIOException(final String descr) {
        super(descr);
    }

    /**
     * Creates a new EquipmentIO exception with the exception as content.
     * 
     * @param e The exception to pass.
     */
    public EqIOException(final Throwable e) {
        super(e);
    }

    /**
     * Creates a new EqIOException with the provided message and cause.
     * 
     * @param message The message for the exception.
     * @param cause The cause for the excception.
     */
    public EqIOException(String message, Throwable cause) {
        super(message, cause);
    }

}
