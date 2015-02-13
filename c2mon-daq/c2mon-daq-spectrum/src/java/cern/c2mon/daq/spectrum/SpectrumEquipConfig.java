/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.spectrum;

public class SpectrumEquipConfig 
{
    private String primaryServer;
    private String secondaryServer;
    private int port;
    
    public SpectrumEquipConfig()
    {
        
    }

    public String getPrimaryServer() {
        return primaryServer;
    }

    public void setPrimaryServer(String primaryServer) {
        this.primaryServer = primaryServer;
    }

    public String getSecondaryServer() {
        return secondaryServer;
    }

    public void setSecondaryServer(String secondaryServer) {
        this.secondaryServer = secondaryServer;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
