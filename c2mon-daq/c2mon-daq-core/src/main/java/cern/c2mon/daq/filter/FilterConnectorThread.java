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
package cern.c2mon.daq.filter;

import cern.c2mon.daq.common.messaging.JmsLifecycle;

/**
 * The thread used for connecting to the JMS filter broker.
 * This runs on a separate thread to allow the DAQ to start up,
 * even if the filter JMS broker is not working.
 * 
 * @author mbrightw
 *
 */
public class FilterConnectorThread extends Thread {
    
    /**
     * The FilterMessageSender this thread must connect to JMS.
     */
    private JmsLifecycle filterMessageSender;
    
    /**
     * The constructor.
     * 
     * @param filterMessageSender the FilterMessageSender to connect to JMS
     */
    public FilterConnectorThread(JmsLifecycle filterMessageSender) {
        super("FilterConnectorThread");
        this.filterMessageSender = filterMessageSender;
    }
    
    /**
     * The method that runs when the thread is started.
     */
    public void run() {
        filterMessageSender.connect();
    }
}
