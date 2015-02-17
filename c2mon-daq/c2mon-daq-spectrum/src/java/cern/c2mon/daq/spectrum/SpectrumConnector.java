/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.spectrum;

public class SpectrumConnector {

    private SpectrumConnector() {
        
    }
    
    public static SpectrumListenerIntf getListener()
    {
        String connectionMode = System.getProperty("spectrum.mode", "prod");
        if (connectionMode.equals("test")) {
            return new SpectrumListenerMock();
        }
        return SpectrumListener.getInstance();            
    }
    
}
