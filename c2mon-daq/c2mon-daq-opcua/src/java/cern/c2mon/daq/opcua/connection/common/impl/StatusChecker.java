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
package cern.c2mon.daq.opcua.connection.common.impl;

import java.util.TimerTask;

import cern.c2mon.daq.opcua.connection.common.IOPCEndpoint;

public abstract class StatusChecker extends TimerTask {
    
    private IOPCEndpoint endpoint;
    
    public StatusChecker(final IOPCEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public void run() {
        try {
            endpoint.checkConnection();
        } catch (OPCCommunicationException e) {
            onOPCCommunicationException(endpoint, e);
        } catch (OPCCriticalException e) {
            onOPCCriticalException(endpoint, e);
        } catch (Exception e) {
            onOPCUnknownException(endpoint, e);
        }

    }

    public abstract void onOPCUnknownException(
            final IOPCEndpoint endpoint, final Exception e);


    public abstract void onOPCCriticalException(
            final IOPCEndpoint endpoint, final OPCCriticalException e);


    public abstract void onOPCCommunicationException(
            final IOPCEndpoint endpoint, final OPCCommunicationException e);

}
