/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.monit.listener;

import cern.c2mon.daq.monit.MonitEventProcessor;

/**
 * 
 * @author mbuttner
 */
public interface MonitListenerIntf {

    void connect();
    void disconnect();

    void setProcessor(MonitEventProcessor proc);

}