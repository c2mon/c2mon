/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.spectrum.listener;

import cern.c2mon.daq.spectrum.SpectrumEventProcessor;

/**
 * Interface for Spectrum connections, used by the real class as well as the
 * mock for testing.
 * 
 * @author mbuttner
 */
public interface SpectrumListenerIntf extends Runnable {

    void shutdown();

    void setProcessor(SpectrumEventProcessor proc);

}