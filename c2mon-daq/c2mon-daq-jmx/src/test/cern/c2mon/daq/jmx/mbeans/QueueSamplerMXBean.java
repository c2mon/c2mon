/**
 * Copyright (c) 2013 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.jmx.mbeans;

/**
 * MXBean used for test purposes only
 * 
 * @author wbuczak
 */
public interface QueueSamplerMXBean {
    QueueSample getQueueSample();

    void updateMetricInsideMap(Integer newValue);

    void clearQueue();
}
