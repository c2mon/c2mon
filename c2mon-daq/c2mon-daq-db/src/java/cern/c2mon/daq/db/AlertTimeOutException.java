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
package cern.c2mon.daq.db;

/**
 * This class represents a timeout exception that is thrown if the process waiting for
 * a database alert times out.
 * 
 *  @author Aleksandra Wardzinska
 * */
public class AlertTimeOutException extends Exception {
    
    /**
     */
    private static final long serialVersionUID = -8983041834254433761L;

    /**
     * A default constructor 
     */
    public AlertTimeOutException() {
        super("Connection timed out while waiting for alert.");
    }
    
}

