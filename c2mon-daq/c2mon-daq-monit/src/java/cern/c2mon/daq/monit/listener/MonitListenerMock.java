/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

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
    // --- Implements SpectrumListenerIntf --------------------------------------------------------
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
