/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.configloader.requestor.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.configloader.Configuration;
import cern.c2mon.configloader.dao.ConfigLoaderTestDAO;
import cern.c2mon.configloader.requestor.TestReconfigurationRequestor;
import cern.c2mon.shared.client.configuration.ConfigConstants.Status;
import cern.c2mon.shared.client.configuration.ConfigurationReport;

/**
 * This reconfiguration requestor is used for test purposes only. It records all reconfiguration requests in its
 * internal cache, which can later be used for validation in the tests
 * 
 * @author wbuczak
 */
public class DummyReconfigurationRequestorImpl implements TestReconfigurationRequestor {

    private List<Configuration> configurations = new ArrayList<>();

    ConfigLoaderTestDAO dao;

    @Autowired
    public void setDAO(ConfigLoaderTestDAO dao) {
        this.dao = dao;
    }

    @Override
    public ConfigurationReport applyConfiguration(Configuration configuration) {
        try {
            dao.setAppliedFlag(configuration.getId());
        } finally {
            synchronized (this) {
                configurations.add(configuration);
            }
        }

        ConfigurationReport report = new ConfigurationReport(true, null);
        report.setStatus(Status.OK);
        return report;
    }

    @Override
    public synchronized List<Configuration> getReconfigurationRequests() {
        return Collections.unmodifiableList(configurations);
    }

}