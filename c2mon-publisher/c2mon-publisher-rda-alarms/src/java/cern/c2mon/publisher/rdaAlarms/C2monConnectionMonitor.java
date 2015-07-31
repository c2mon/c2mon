/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.rdaAlarms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.client.jms.ClientHealthListener;
import cern.c2mon.client.jms.ConnectionListener;
import cern.c2mon.client.core.listener.HeartbeatListener;
import cern.c2mon.shared.client.supervision.Heartbeat;

/**
 * Any possible callback for the connection to the C2MON server. For debugging of the connection
 * during development of the client.
 * 
 * @author mbuttner
 */
public class C2monConnectionMonitor implements ClientHealthListener, ConnectionListener, HeartbeatListener {

    private static Logger LOG = LoggerFactory.getLogger(C2monConnectionMonitor.class);
    

    @Override
    public void onConnection() {
        LOG.info("C2MON server -> onConnection()");        
    }

    @Override
    public void onDisconnection() {
        LOG.warn("C2MON server -> onDisConnection()");        
    }

    @Override
    public void onSlowUpdateListener(String diagnosticMessage) {
        LOG.warn("C2MON server detected slow client: " + diagnosticMessage);        
    }

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
