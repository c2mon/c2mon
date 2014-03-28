/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.configloader;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.stereotype.Service;

import cern.c2mon.configloader.dao.ConfigLoaderDAO;

/**
 * @author wbuczak
 */
@Service
public class C2MonConfigLoaderMain {

    private static final Logger LOG = LoggerFactory.getLogger(C2MonConfigLoaderMain.class);

    private C2MonConfigLoaderConfig config;

    private ConfigLoaderDAO dao;

    private C2MonConfigLoaderService service;

    private ScheduledExecutorService executor;

    private ScheduledFuture<?> pollerFuture;

    @Autowired
    public void setDAO(ConfigLoaderDAO dao) {
        this.dao = dao;
    }

    @Autowired
    public void setConfig(C2MonConfigLoaderConfig config) {
        this.config = config;
    }

    @Autowired
    public void setService(C2MonConfigLoaderService service) {
        this.service = service;
    }

    public void init() {
        LOG.info("starting and initializing C2MON Configuration loader");

        executor = Executors.newSingleThreadScheduledExecutor();

        pollerFuture = executor.scheduleAtFixedRate(new DbPollerExecutorTask(), 0, config.getDbPollingPeriod(),
                TimeUnit.SECONDS);
    }

    @PreDestroy
    public void predestroy() {
        pollerFuture.cancel(true);
        executor.shutdownNow();
    }

    class DbPollerExecutorTask implements Runnable {

        @Override
        public void run() {

            LOG.debug("executing db poller task");

            try {
                // get the list of not-yet-applied configurations
                List<Configuration> configs = dao.getConfigurationsForLoading();

                LOG.debug("number of configurations to be applied in this iteration: {}", configs.size());

                for (Configuration c : configs) {
                    service.applyConfiguration(c);
                }
            } catch (Exception ex) {
                LOG.error("exception caught trying to apply configuration", ex);
            }

        }// run

    }

    private static void configureLogging() {
        try {
            // Load the log4j configuration - unfortunately pure SLF4J API does not yet provide
            // watch-dog functionality - so we have to use log4j directly
            PropertyConfigurator.configureAndWatch(System.getProperty("log4j.configuration"), 30 * 1000);
        } catch (Exception ex) {
            System.err.println("Unable to load log4j configuration file : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        configureLogging();
        LOG.info("starting C2MON configuration loader process");
        GenericXmlApplicationContext ctx = new GenericXmlApplicationContext();
        ctx.getEnvironment().setDefaultProfiles("PRO");

        // ctx.getEnvironment().setActiveProfiles("TEST");

        ctx.load("/resources/c2mon-configloader-config.xml");

        ctx.refresh();

        C2MonConfigLoaderMain main = ctx.getBean(C2MonConfigLoaderMain.class);
        main.init();
    }

}