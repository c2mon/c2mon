package cern.c2mon.driver.opcua.connection.common.impl;

import java.util.TimerTask;

import cern.c2mon.driver.opcua.connection.common.IOPCEndpoint;

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
