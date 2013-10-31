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
