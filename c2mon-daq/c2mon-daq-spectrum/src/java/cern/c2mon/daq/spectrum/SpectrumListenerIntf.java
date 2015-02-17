/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.spectrum;

import java.util.Queue;

public interface SpectrumListenerIntf extends Runnable {

    public abstract void setConfig(SpectrumEquipConfig config);

    public abstract void shutdown();

    public abstract Queue<Event> getQueue();


}