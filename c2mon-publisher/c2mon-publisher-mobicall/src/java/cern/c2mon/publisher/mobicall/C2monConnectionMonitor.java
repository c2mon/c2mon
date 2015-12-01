/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.mobicall;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.listener.HeartbeatListener;
import cern.c2mon.client.jms.ClientHealthListener;
import cern.c2mon.client.jms.ConnectionListener;
import cern.c2mon.shared.client.supervision.Heartbeat;

/**
 * Logger for the events on the C2MON connection. There is no logic or actions attached to this,
 * but in case of problems, a trace will appear in the log files. 
 * 
 * Usage: call C2monConnectionMonitor.start(); (just once, further calls will be ignored)
 * 
 * @author mbuttner
 */
class C2monConnectionMonitor implements ClientHealthListener, ConnectionListener, HeartbeatListener {

    private static C2monConnectionMonitor instance;
    private static final Logger LOG = LoggerFactory.getLogger(C2monConnectionMonitor.class);
    //
    // --- CONSTRUCTION -----------------------------------------------------------------------------
    //
    public static void start() {
        if (instance == null) {
            instance = new C2monConnectionMonitor();
        }
    }
    
    private C2monConnectionMonitor() {
        C2monServiceGateway.getSupervisionService().addClientHealthListener(this);
        C2monServiceGateway.getSupervisionService().addConnectionListener(this);
        C2monServiceGateway.getSupervisionService().addHeartbeatListener(this);
    }
    
    //
    // --- Implements ConnectionListener -----------------------------------------------------
    //
    @Override
    public void onConnection() {
        LOG.info("C2MON server -> onConnection()");
    }

    @Override
    public void onDisconnection() {
        LOG.warn("C2MON server -> onDisConnection()");
    }

    //
    // --- Implements ClientHealthListener ---------------------------------------------------
    //
    @Override
    public void onSlowUpdateListener(String diagnosticMessage) {
        LOG.warn("C2MON server detected slow client: " + diagnosticMessage);
    }

    //
    // --- Implements HeartbeatListener -------------------------------------------------------
    //
    @Override
    public void onHeartbeatReceived(Heartbeat pHeartbeat) {
        LOG.debug("C2MON server -> onHeartbeatReceived()");
    }

    @Override
    public void onHeartbeatExpired(Heartbeat pHeartbeat) {
        LOG.warn("C2MON server -> onHeartbeatExpired()");
    }

    @Override
    public void onHeartbeatResumed(Heartbeat pHeartbeat) {
        LOG.info("C2MON server -> onHeartbeatResumed()");
    }

}