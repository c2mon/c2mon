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

package cern.c2mon.daq.spectrum.listener;

import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.daq.spectrum.SpectrumAlarm;
import cern.c2mon.daq.spectrum.SpectrumEvent;
import cern.c2mon.daq.spectrum.SpectrumEventProcessor;

/**
 * When this listener is enabled, it is possible to write directly to the EventProcessor
 * queue. Used for JUnit tests to submit messages and check their result in the system
 * 
 * @author mbuttner
 */
public class SpectrumListenerJunit implements SpectrumListenerIntf {

    private static final Logger LOG = LoggerFactory.getLogger(SpectrumListenerJunit.class);
    private Queue<SpectrumEvent> queue;
    
    private static SpectrumListenerJunit listener;
    private SpectrumEventProcessor proc;
    
    //
    // --- CONSTRUCTION ---------------------------------------------------------------------------
    //
    public SpectrumListenerJunit() {
        listener = this;
    }
    
    public static SpectrumListenerJunit getListener() {
        return listener;
    }
    
    //
    // --- PUBLIC METHODS -------------------------------------------------------------------------
    //
    public void sendMessage(String server, String content) {
        LOG.info("{} >> {}", server, content);
        queue.add(new SpectrumEvent(server, content));
    }
    
    public SpectrumAlarm getAlarm(String hostname) {
        return proc.getAlarm(hostname);
    }
    
    //
    // --- Implements Runnable --------------------------------------------------------------------
    //
    @Override
    public void run() {
        //
    }

    //
    // --- Implements SpectrumListenerIntf --------------------------------------------------------
    //

    @Override
    public void shutdown() {
        //
    }

    @Override
    public void setProcessor(SpectrumEventProcessor proc) {
        this.proc = proc;
        this.queue = proc.getQueue();
        LOG.info("Listener enabled.");
    }

}
