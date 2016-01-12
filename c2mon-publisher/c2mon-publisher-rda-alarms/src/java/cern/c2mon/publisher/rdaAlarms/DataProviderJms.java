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
    
    /**
     * Caching. This method is used at startup when a large number of alarms are coming in.
     * We use the array call of the dataprovider to get all data at once, rather than 
     * asking for each alarm individually.
     */
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
