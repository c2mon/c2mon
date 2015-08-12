/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.rdaAlarms;

import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.listener.HeartbeatListener;
import cern.c2mon.client.jms.ClientHealthListener;
import cern.c2mon.client.jms.ConnectionListener;
import cern.c2mon.shared.client.supervision.Heartbeat;

class C2monConnectionMonitor implements ClientHealthListener, ConnectionListener, HeartbeatListener {

    private static C2monConnectionMonitor instance;
    
    //
    // --- CONSTRUCTION -----------------------------------------------------------------------------
    //
    public static void start() {
        if (instance == null) {
            instance = new C2monConnectionMonitor();
        }
    }
    
    private C2monConnectionMonitor() {
        C2monServiceGateway.getSupervisionManager().addClientHealthListener(this);
        C2monServiceGateway.getSupervisionManager().addConnectionListener(this);
        C2monServiceGateway.getSupervisionManager().addHeartbeatListener(this);
    }
    
    //
    // --- Implements ConnectionListener -----------------------------------------------------
    //
    @Override
    public void onConnection() {
        RdaAlarmsPublisher.LOG.info("C2MON server -> onConnection()");
    }

    @Override
    public void onDisconnection() {
        RdaAlarmsPublisher.LOG.warn("C2MON server -> onDisConnection()");
    }

    //
    // --- Implements ClientHealthListener ---------------------------------------------------
    //
    @Override
    public void onSlowUpdateListener(String diagnosticMessage) {
        RdaAlarmsPublisher.LOG.warn("C2MON server detected slow client: " + diagnosticMessage);
    }

    //
    // --- Implements HeartbeatListener -------------------------------------------------------
    //
    @Override
    public void onHeartbeatReceived(Heartbeat pHeartbeat) {
        RdaAlarmsPublisher.LOG.debug("C2MON server -> onHeartbeatReceived()");
    }

    @Override
    public void onHeartbeatExpired(Heartbeat pHeartbeat) {
        RdaAlarmsPublisher.LOG.warn("C2MON server -> onHeartbeatExpired()");
    }

    @Override
    public void onHeartbeatResumed(Heartbeat pHeartbeat) {
        RdaAlarmsPublisher.LOG.info("C2MON server -> onHeartbeatResumed()");
    }

}