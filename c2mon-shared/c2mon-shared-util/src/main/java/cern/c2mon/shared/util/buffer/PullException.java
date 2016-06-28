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
package cern.c2mon.shared.util.buffer;

/**
 * Exception in calling the callback function to pull data from
 * the SynchroBuffer.
 * @author F.Calderini
 */
public class PullException extends Exception {

    /**
     * Creates new <code>PullException</code> without detail message.
     */
    public PullException() {
        super();
    }


    /**
     * Constructs an <code>PullException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public PullException(String msg) {
        super(msg);
    }
}
