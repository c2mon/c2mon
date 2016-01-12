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

package cern.c2mon.configloader.impl;

import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cern.c2mon.configloader.C2MonConfigLoaderService;
import cern.c2mon.configloader.Configuration;
import cern.c2mon.configloader.dao.ConfigLoaderDAO;
import cern.c2mon.configloader.requestor.ServerReconfigurationRequestor;
import cern.c2mon.shared.client.configuration.ConfigConstants.Status;
import cern.c2mon.shared.client.configuration.ConfigurationReport;

/**
 * @author wbuczak
 */
@Service
public class C2MonConfigLoaderServiceImpl implements C2MonConfigLoaderService {

    private static final Logger LOG = LoggerFactory.getLogger(C2MonConfigLoaderServiceImpl.class);
    private static final Logger APPLIED_CONFIG_LOG = LoggerFactory.getLogger("configLog");

    @Resource
    private ConfigLoaderDAO dao;

    @Resource
    @Qualifier("reconfigurationRequestor")
    private ServerReconfigurationRequestor requestor;

    @Value("#{configLoaderConfiguration.getLoaderUserName()}")
    private String userName;

    @Override
    public void applyConfiguration(Configuration conf) {
        ConfigurationReport report = null;
        try {
            report = requestor.applyConfiguration(conf);
            dao.update(conf.getId(), userName);
            log(conf, report);
        } catch (Exception ex) {
            LOG.error("exception caught trying to apply configuration", ex);
            log(conf, ex);
        }
    }

    private void log(Configuration conf, Exception ex) {
        APPLIED_CONFIG_LOG.info("{} [ {} ]  {} {} {} {} {} {}",
                new Object[] { userName, conf.getId(), "FAILED", ex.getMessage(), conf.getName(),
                        conf.getDescription(), conf.getAuthor(), conf.getCreateTimestampStr() });
    }

    private void log(Configuration conf, ConfigurationReport report) {

        
        
        if (report != null) {
            
            if (report.getStatus().equals(Status.FAILURE)) {
                APPLIED_CONFIG_LOG.warn("{} [ {} ]  status={} : \"{}\" descr={} author={}", 
                   new Object [] {userName, conf.getId(), report.getStatus().toString(), report.getStatusDescription(), conf.getDescription(), conf.getAuthor()});
                
            } else  {
            
                APPLIED_CONFIG_LOG.info(
                    "{} [ {} ]  {} {} {} {} {} {}",
                    new Object[] { userName, conf.getId(), report.getStatus().toString(), daqsToRestart(report),
                            conf.getName(), conf.getDescription(), conf.getAuthor(), conf.getCreateTimestampStr() });
            }
        } else {
            APPLIED_CONFIG_LOG.info(
                    "{} [ {} ]  {} {} {} {} {} {}",
                    new Object[] { userName, "???", daqsToRestart(null), conf.toString(), conf.getName(),
                            conf.getDescription(), conf.getAuthor(), conf.getCreateTimestampStr() });
        }

    }

    private static String daqsToRestart(final ConfigurationReport report) {
        if (report == null)
            return "";

        Set<String> daqs = report.getProcessesToReboot();
        return daqs.toString();
    }

}
