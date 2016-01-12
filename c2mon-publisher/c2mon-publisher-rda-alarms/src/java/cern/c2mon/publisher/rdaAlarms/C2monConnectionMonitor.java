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

package cern.c2mon.publisher.rdaAlarms;

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
        C2monServiceGateway.getSupervisionManager().addClientHealthListener(this);
        C2monServiceGateway.getSupervisionManager().addConnectionListener(this);
        C2monServiceGateway.getSupervisionManager().addHeartbeatListener(this);
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
