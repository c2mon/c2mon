/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.spectrum;

/**
 * Test/Production switch for the Spectrum connection. If the system property
 * spectrum.mode is set to "test", the factory method will return an instance of
 * the mock. Otherwise, an instance of the real Spectrum listener is returned.
 * 
 * @author mbuttner
 */
public class SpectrumConnector {

    //
    // --- CONSTRUCTION ----------------------------------------------------
    //
    private SpectrumConnector() {        
    }

    //
    // --- PUBLIC METHODS --------------------------------------------------
    //
    public static SpectrumListenerIntf getListener()
    {
        String connectionMode = System.getProperty("spectrum.mode", "prod");
        if (connectionMode.equals("test")) {
            return new SpectrumListenerMock();
        }
        if (connectionMode.equals("jms")) {
            return new SpectrumListenerJms();
        }
        return SpectrumListener.getInstance();            
    }
    
}
