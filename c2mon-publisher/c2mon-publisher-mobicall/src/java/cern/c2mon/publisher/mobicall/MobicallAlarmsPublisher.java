/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project. See http://ts-project-tim.web.cern.ch
 * Copyright (C) 2004 - 2012 CERN. This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA. Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.publisher.mobicall;

import java.text.SimpleDateFormat;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import cern.c2mon.client.jms.AlarmListener;
import cern.c2mon.shared.client.alarm.AlarmValue;

/**
 */
@ManagedResource(objectName = "cern.c2mon.publisher.rdaAlarms:name=RdaAlarmsPublisher", description = "Rda publisher for DIAMON alarm client")
public final class MobicallAlarmsPublisher implements Runnable, AlarmListener {

    static final Logger LOG = LoggerFactory.getLogger(MobicallAlarmsPublisher.class);
    @SuppressWarnings("unused")
    private static final SimpleDateFormat df = new SimpleDateFormat("dd.MM.YYYY HH:MM:SS");

    private static MobicallAlarmsPublisher publisher;
    
    private VCM received;
    private VCM processed;
    private long rejected;

    private Thread daemonThread;
    private C2monConnectionIntf c2mon;
    private volatile boolean running = false;
    
    //
    // --- CONSTRUCTION ----------------------------------------------------------------
    //
    private MobicallAlarmsPublisher() {
        LOG.warn("Publisher instance created ...");
    }

    public static MobicallAlarmsPublisher getPublisher() {
        if (publisher == null) {
            publisher = new MobicallAlarmsPublisher();
        }
        return publisher;
    }

    //
    // --- PUBLIC GETTERS / SETTERS ----------------------------------------------------
    // ... some of them are used by Spring at bean construction time ...
    public void setC2mon(C2monConnectionIntf c2mon) {
        this.c2mon = c2mon;
        this.c2mon.setListener(this);
    }
    
    public C2monConnectionIntf getC2mon() {
        return this.c2mon;
    }
        
    public void setReceived(VCM received) {
        this.received = received;
    }
    
    public void setProcessed(VCM processed) {
        this.processed = processed;
    }
    
    static String getAlarmId(AlarmValue av) {
        return av.getFaultFamily() + ":" + av.getFaultMember() + ":" + av.getFaultCode();
    }
    
    //
    // --- JMX -------------------------------------------------------------------------
    //

    @ManagedAttribute
    public long getRejected() {
        return this.rejected;
    }

    @ManagedAttribute
    public boolean isRunning()
    {
        return this.running;
    }


    //
    // --- DAEMON -----------------------------------------------------------------------
    //
    /**
     * This method has to be called in order to start the RDA publisher
     */
    public void start() {
        daemonThread = new Thread(this);
        daemonThread.start();
    }

    @Override
    public void run() {
        try {
            LOG.info("Starting RDA device server");

            c2mon.start();
            Collection<AlarmValue> activeAlarms = c2mon.getActiveAlarms();
            LOG.info("... now listening to incoming alarms.");
            
            c2mon.connectListener();
            for (AlarmValue av : activeAlarms) {
                this.onAlarmUpdate(av);
            }
            LOG.info("Started with initial selection of " + activeAlarms.size() + " alarms.");
            // everything ready, start the RDA server for publishung
            running = true;
        } catch (Exception e) {
            LOG.error("A major problem occured while running the RDA server. Stopping publisher!", e);
        }
    }

    public void join() throws InterruptedException {
        this.daemonThread.join();
    }

    public void shutdown() {        
        LOG.info("Stopping C2MON ...");
        c2mon.stop();
        try {
            daemonThread.join();
            running = false;
        } catch (InterruptedException e) {
            LOG.warn("InterruptedException caught", e);
        }
        LOG.info("RDA publisher stopped.");
    }

    //
    // --- Implements AlarmListener -----------------------------------------------------------
    //
    @Override
    public void onAlarmUpdate(AlarmValue av) {

        String alarmId = getAlarmId(av);
        LOG.debug(" RECEIVED    > " + alarmId + " is active:" + av.isActive());
        received.increment();
        // TODO
        processed.increment();
        LOG.debug(" PROCESSED    > " + alarmId);

    }



}
