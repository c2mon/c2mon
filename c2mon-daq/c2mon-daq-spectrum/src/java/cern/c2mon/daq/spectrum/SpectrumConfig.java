/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.spectrum;

/**
 * Classes needing to be configured with the Spectrum connection parameters should extends this one.
 * The server names and ports can than be configured by Spring.
 * 
 * @author mbuttner
 */
public abstract class SpectrumConfig {
    
    private String primaryServer;
    private String secondaryServer;
    private int port;
    
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
