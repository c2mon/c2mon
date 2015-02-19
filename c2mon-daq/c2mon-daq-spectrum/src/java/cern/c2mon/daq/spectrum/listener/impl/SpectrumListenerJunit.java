/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.spectrum.listener.impl;

import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.daq.spectrum.SpectrumAlarm;
import cern.c2mon.daq.spectrum.SpectrumEquipConfig;
import cern.c2mon.daq.spectrum.SpectrumEvent;
import cern.c2mon.daq.spectrum.SpectrumEventProcessor;
import cern.c2mon.daq.spectrum.listener.SpectrumListenerIntf;

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
    public void setConfig(SpectrumEquipConfig config) {
        //
    }

    @Override
    public void shutdown() {
        //
    }

    @Override
    public void setQueue(Queue<SpectrumEvent> queue) {
        LOG.info("Listener enabled.");
        this.queue = queue;
    }

    @Override
    public void setProcessor(SpectrumEventProcessor proc) {
        this.proc = proc;
    }

}
