/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.spectrum;

import java.util.Queue;

/**
 * Interface for Spectrum connections, used by the real class as well as the
 * mock for testing.
 * 
 * @author mbuttner
 */
public interface SpectrumListenerIntf extends Runnable {

    void setConfig(SpectrumEquipConfig config);

    void shutdown();

    void setQueue(Queue<Event> queue);

}