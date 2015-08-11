/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.rdaAlarms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.phoenix.core.Alarm;
import cern.phoenix.remote.AlarmDataProvider;
import cern.phoenix.remote.RemoteModuleFactory;

/**
 * Implementation to obtain the source of an alarm from the C2MON alarms data provider.
 * 
 * @author mbuttner
 */
public class DataProviderJms implements DataProviderInterface {

    private static final Logger LOG = LoggerFactory.getLogger(DataProviderJms.class);

    private RemoteModuleFactory alarmProviderFactory;
    private AlarmDataProvider provider;

    //
    // --- CONSTRUCTION --------------------------------------------------------------------------
    //
    public DataProviderJms() {
        LOG.info("Creating the remote JMS data provider interface ...");
        alarmProviderFactory = new RemoteModuleFactory();
        alarmProviderFactory.init();

        provider = alarmProviderFactory.getDataProvider();
        LOG.info("Ready.");
    }

    //
    // --- Overrides DataProviderInterface -------------------------------------------------------
    //
    @Override
    public void close() {
        LOG.info("Closed.");
    }

    @Override
    public String getSource(String alarmId) throws Exception {
        LOG.trace("Request source name for alarm {} ...", alarmId);
        String source = null;
        Alarm alarm = provider.getAlarmDefinition(alarmId);
        if (alarm != null) {
            source = alarm.getSourceName();
        }
        LOG.debug("{} -> {}", alarmId, source);
        return source;
    }

}
