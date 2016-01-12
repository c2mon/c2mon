/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/

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
