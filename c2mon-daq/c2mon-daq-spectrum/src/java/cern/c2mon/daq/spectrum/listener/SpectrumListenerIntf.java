/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.spectrum.listener;

import java.util.Queue;

import cern.c2mon.daq.spectrum.SpectrumEquipConfig;
import cern.c2mon.daq.spectrum.SpectrumEvent;
import cern.c2mon.daq.spectrum.SpectrumEventProcessor;

/**
 * Interface for Spectrum connections, used by the real class as well as the
 * mock for testing.
 * 
 * @author mbuttner
 */
public interface SpectrumListenerIntf extends Runnable {

    void setConfig(SpectrumEquipConfig config);

    void shutdown();

    void setQueue(Queue<SpectrumEvent> queue);

    void setProcessor(SpectrumEventProcessor proc);

}