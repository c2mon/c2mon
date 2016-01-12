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

package cern.c2mon.daq.monit.listener;

import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.daq.monit.MonitEventProcessor;
import cern.c2mon.daq.monit.MonitUpdateEvent;

/**
 * When this listener is enabled, it is possible to write directly to the EventProcessor
 * queue. Used for JUnit tests to submit messages and check their result in the system
 * 
 * @author mbuttner
 */
public class MonitListenerMock implements MonitListenerIntf {

    private static final Logger LOG = LoggerFactory.getLogger(MonitListenerMock.class);
    private Queue<MonitUpdateEvent> queue;
    
    private static MonitListenerMock listener;
    private MonitEventProcessor proc;
    
    //
    // --- CONSTRUCTION ---------------------------------------------------------------------------
    //
    public MonitListenerMock() {
        listener = this;
    }
    
    public static MonitListenerMock getListener() {
        return listener;
    }
    
    //
    // --- PUBLIC METHODS -------------------------------------------------------------------------
    //
    public void sendMessage(String server, String content) {
        LOG.info("{} >> {}", server, content);
//        queue.add(new MonitUpdateEvent(server, content));
    }
        

    //
    // --- Implements MonitListenerIntf --------------------------------------------------------
    //
    @Override
    public void connect() {
        //
    }

    @Override
    public void disconnect() {
        //
    }

    @Override
    public void setProcessor(MonitEventProcessor proc) {
        this.proc = proc;
        this.queue = proc.getQueue();
        LOG.info("Listener enabled.");
    }

}
