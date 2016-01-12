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

package cern.c2mon.daq.spectrum;

/**
 * Dataholder for configuration data, which will be initialized from the Spring context for
 * classes extending this one (like the SpectrumEventProcessor does).
 * 
 * @author mbuttner
 */
public abstract class SpectrumConfig {
    
    private String primaryServer;
    private String secondaryServer;
    private int port;
    
    //
    // --- PUBLIC METHODS ---------------------------------------------------------------------
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
