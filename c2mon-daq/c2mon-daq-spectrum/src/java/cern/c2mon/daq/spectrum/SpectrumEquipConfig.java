/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.spectrum;

/**
 * Object for desializing the Json transmitted equipment configuration stuff. To work 
 * correctly with gson library, we have a public parameter-less constructor as well as 
 * getters and setters for everything.
 * 
 * @author mbuttner
 */
public class SpectrumEquipConfig 
{
    private String primaryServer;
    private String secondaryServer;
    private int port;

    //
    // --- CONSTRUCTION --------------------------------------------------------------------
    //
    public SpectrumEquipConfig()
    {
        
    }

    //
    // --- PUBLIC METHODS ------------------------------------------------------------------
    //
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
