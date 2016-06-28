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

package cern.c2mon.pmanager.fallback.exception;

/**
 * Represents any failure occurred to the system's disc
 * @author mruizgar
 *
 */
public class SystemDiskSpaceException extends Exception {

    /**
     * Unique string for identifying the class
     */
    private static final long serialVersionUID = 1127685173371614176L;

    /**
     * Constructor to create an Exception with an explanatory message inside
     * @param message The message that explains what have occurred
     */
    public SystemDiskSpaceException(final String message) {
        super("SystemDiskSpaceException: " + message);
    }
    
    /**
     * Default constructor
     */    
    public SystemDiskSpaceException() {
        super();
    }
}
