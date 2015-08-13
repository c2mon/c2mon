/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.rdaAlarms;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
public class DataProviderJms implements DataProviderIntf {

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

        provider = alarmProviderFactory.getAlarmDataProvider();
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
    public Collection<String> getSourceNames() {
        return provider.getSourceNames();        
    }
    
    @Override
    public ConcurrentHashMap<String, String> initSourceMap(Set<String> alarmIds) {
        LOG.info("Preparing the alarmEquip map ...");
        int counter = 0;
        ConcurrentHashMap<String,String> result = new ConcurrentHashMap<String,String>();
        Map<String, Alarm> alarmDefs = provider.getAlarmDefinitions(alarmIds);
        for (String alarmId : alarmIds) {
            counter++;
            Alarm alarm = alarmDefs.get(alarmId);
            if (alarm != null) {
                result.put(alarmId, alarm.getSourceName());
            }
        }            
        LOG.info("Cached source name for {} alarms.", counter);
        return result;
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
