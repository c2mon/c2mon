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
package cern.c2mon.daq.opcua.connection.common.impl;
/**
 * Exception which is not solvable without a configuration change.
 * 
 * @author Andreas Lang
 *
 */
public class OPCCriticalException extends RuntimeException {

    /**
     * Default serial version id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new OPCCriticalException.
     */
    public OPCCriticalException() {
        super();
    }

    /**
     * Creates a new OPCCriticalException.
     * 
     * @param message The message of the exception.
     */
    public OPCCriticalException(final String message) {
        super(message);
    }

    /**
     * Creates a new OPCCriticalException.
     * 
     * @param cause Throwable which is the cause of this exception.
     */
    public OPCCriticalException(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new OPCCriticalException.
     * 
     * @param message The message of the exception.
     * @param cause Throwable which is the cause of this exception.
     */
    public OPCCriticalException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
