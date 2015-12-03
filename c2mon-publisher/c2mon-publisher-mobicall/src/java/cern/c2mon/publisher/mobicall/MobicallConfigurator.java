/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.mobicall;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MobicallConfigurator implements Runnable {
    
    public static final int LATENCY = 5; // in seconds, = sleep slice and wait time to let the thread complete on stop
    public static final int DEFAULT_REFRESH_INTERVAL = 5; // update alarm definitions once per n minutes

    private static final Logger LOG = LoggerFactory.getLogger(MobicallConfigurator.class);
    
    private MobicallConfigLoaderIntf loader;
    private int refreshInterval;
    private boolean cont;
    
    
    public MobicallConfigurator(MobicallConfigLoaderIntf loader) {
        this.loader = loader;
        refreshInterval = Integer.parseInt(System.getProperty("mobicall.config.refresh", "" + DEFAULT_REFRESH_INTERVAL));
    }
    
    @Override
    public void run() {
        cont = true;
        long lastLoadTs = 0;
        int failures = 0;
        int success = 0;
        while (cont) {
            if (System.currentTimeMillis() - (30 * 1000) < lastLoadTs) {
                LOG.warn("Skipped a request to reload configuration, was too fast!");
                failures++;
                if (failures > 10) {
                    LOG.warn("Too many failures, stopping the reconfiguration thread!");                    
                    cont = false;
                }
            } else {
                LOG.warn("Time to reload config ...");
                loader.loadConfig();
                lastLoadTs = System.currentTimeMillis();
                success++;
                if (success > 10) {
                    failures = 0;
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
    
    public void stop() {
        cont = false;
    }
    
}