/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.mobicall;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The background tasks, which drives the reload of the configuration. It is not much more than a 
 * periodic call to a method of a given interface, with some safety mechanism:
 * - it can be safely stopped through the stop() method. The task will exit within LATENCY seconds
 * - the interval between config loads can be configued in minutes, default is 5
 * - in case of unexpected interruptions of the sleep method, and consecutive "fast" attempts to
 *   reload, the calls will be discarded if the request is closer than 30s to the previous one
 * - after 10 failures, the task will completely exit (unless we have 10 successes since the
 *   first failure)
 * 
 * @author mbuttner
 */
public class MobicallConfigurator implements Runnable {

    // in seconds, = sleep slice and wait time to let the thread complete on stop
    public static final int LATENCY = 5;

    // update alarm definitions once per n minutes
    public static final int DEFAULT_REFRESH_INTERVAL = 5;

    private static final Logger LOG = LoggerFactory.getLogger(MobicallConfigurator.class);

    private MobicallConfigLoaderIntf loader;
    private int refreshInterval;
    private boolean cont;

    //
    // --- CONSTRUCTION ---------------------------------------------------------------------------------
    //
    public MobicallConfigurator(MobicallConfigLoaderIntf loader) {
        this.loader = loader;
        refreshInterval = Integer
                .parseInt(System.getProperty("mobicall.config.refresh", "" + DEFAULT_REFRESH_INTERVAL));
    }

    //
    // --- PUBLIC METHODS -------------------------------------------------------------------------------
    //
    public void stop() {
        cont = false;
    }

    //
    // --- Implements Runnable --------------------------------------------------------------------------
    //
    @Override
    public void run() {
        cont = true;
        long lastLoadTs = 0;
        int failures = 0;
        int success = 0;
        
        while (cont) {
            
            // no reconfig less than 30s after the previous one
            if (System.currentTimeMillis() - (30 * 1000) < lastLoadTs) {
                LOG.warn("Skipped a request to reload configuration, was too fast!");
                failures++;
                
                // the 10th failure = stop it, could harm the system
                if (failures > 10) {
                    LOG.warn("Too many failures, stopping the reconfiguration thread!");
                    cont = false;
                }
                
            } else {                
                LOG.warn("Time to reload config ...");
                loader.loadConfig();
                lastLoadTs = System.currentTimeMillis();
                if (failures > 0) {
                    success++;
                    if (success > 10) {
                        failures = 0;
                        success = 0;
                    }
                }
                LOG.warn("Config reloaded OK.");
            }
            if (cont) {
                try {
                    int slept = 0;
                    while (cont && slept < refreshInterval * 60) {
                        Thread.sleep(LATENCY * 1000);
                        slept += LATENCY;
                    }
                } catch (InterruptedException e) {
                    LOG.warn("Sleep in MobicallConfigurator thread interrupted");
                }
            }
        }
    }


}