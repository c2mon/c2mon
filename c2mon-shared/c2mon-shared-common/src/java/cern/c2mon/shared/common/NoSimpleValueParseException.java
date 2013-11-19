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
package cern.c2mon.shared.common;
/**
 * Exception which is thrown if you try to parse a non simple value with the 
 * method for simple values.
 * 
 * @author Andreas Lang
 *
 */
public class NoSimpleValueParseException extends Exception {
    /**
     * Standard message.
     */
    private static final String MESSAGE = "A value which should be parsed as a simple value was no simple value.";

    /**
     * Serial version ID.
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new NoSimpleValueParseException.
     */
    public NoSimpleValueParseException() {
        super(MESSAGE);
    }

}
