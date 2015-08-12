/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.configloader.requestor.impl;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedResource;

import cern.c2mon.client.common.listener.ClientRequestReportListener;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.C2monTagManager;
import cern.c2mon.configloader.Configuration;
import cern.c2mon.configloader.requestor.ServerReconfigurationRequestor;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.request.ClientRequestErrorReport;
import cern.c2mon.shared.client.request.ClientRequestProgressReport;

/**
 * @author wbuczak
 */
@ManagedResource(objectName = "cern.c2mon.configloader.requestor:name=ReconfigurationRequestor", description = "c2mon configuration loader requestor")
public class ReconfigurationRequestorImpl implements ServerReconfigurationRequestor {

    private static final Logger LOG = LoggerFactory.getLogger(ReconfigurationRequestorImpl.class);

    /**
     * Tag manager
     */
    private C2monTagManager tagManager;

    @PostConstruct
    public void init() {
        LOG.info("Initializing ReconfigurationRequestor..");

        try {
            LOG.info("Starting C2MON Service Gateway...");
            C2monServiceGateway.startC2monClientSynchronous();
            tagManager = C2monServiceGateway.getTagManager();
            LOG.info("TagManager instance started: " + (tagManager == null ? "NULL" : "OK"));

            LOG.info("alarm monitor sender's initialization done");
        } catch (Exception e) {
            LOG.error("ReconfigurationRequestor initialization failed!", e);

        }

    }

    @Override
    public synchronized ConfigurationReport applyConfiguration(final Configuration conf) {
        LOG.info("applying : {}", conf.getId());

        ConfigurationReport report = tagManager.applyConfiguration(conf.getId(),  new ClientRequestReportListener() {
            
            @Override
            public void onProgressReportReceived(ClientRequestProgressReport arg0) {
                LOG.debug("Progress " + conf.getId() + " " + arg0.getCurrentProgressPart() + "/" + arg0.getTotalProgressParts());
                
            }
            
            @Override
            public void onErrorReportReceived(ClientRequestErrorReport arg0) {
                LOG.warn(conf.getId() + "  " + arg0.getErrorMessage()+ " : " + arg0.getRequestExecutionStatus().toString());
                
            }
        });

        LOG.debug("report :   " + conf.getId() + " "
                + (report == null ? "NULL" : report.getStatus()));

        if (report == null)
            LOG.warn("Received NULL Configuration report for configuration id: {}", conf.getId());
        return report;
    }

}