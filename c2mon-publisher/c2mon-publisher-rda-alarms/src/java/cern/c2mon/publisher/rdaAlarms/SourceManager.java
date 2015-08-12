/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.rdaAlarms;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.cmw.data.Data;
import cern.cmw.data.DataFactory;
import cern.cmw.rda3.common.data.AcquiredData;

@ManagedResource(objectName = "cern.c2mon.publisher.rdaAlarms:name=SourceManager", description = "Alarm Soure Manager")
public class SourceManager extends TimerTask {

    public static final long FREQ = 15 * 60 * 1000; // run once per 15mn
    
    private static SourceManager sourceMgr;    
    private static final Logger LOG = LoggerFactory.getLogger(SourceManager.class);
    
    private ConcurrentHashMap<String, String> alarmEquip = new ConcurrentHashMap<String, String>();
    private Map<String, RdaAlarmProperty> properties = new HashMap<String, RdaAlarmProperty>();    
    private AcquiredData sources;

    private int sourceCount;
    private long alarmCount;
    
    private DataProviderInterface dpi;
    private Timer timer;
    
    //
    // --- CONSTRUCTION -----------------------------------------------------------------------
    //
    private SourceManager() {
        timer = new Timer();
        timer.scheduleAtFixedRate(this, FREQ, FREQ);
    }
    
    public static SourceManager getSourceManager() {
        if (sourceMgr == null) {
            sourceMgr = new SourceManager();
        }
        return sourceMgr;
    }

    public void setDataProvider(DataProviderInterface dpi) {
        this.dpi = dpi;        
    }
    
    //
    // --- PUBLIC METHODS ---------------------------------------------------------------------
    //
    @ManagedAttribute
    public int getSourceCount() {
        return sourceCount;
    }

    @ManagedAttribute
    public long getAlarmCount() {
        return alarmCount;
    }
    
    public DataProviderInterface getDataProvider() {
        return dpi;
    }
    
    AcquiredData getSources() {
        return sources;
    }
    

    public void close() {
        dpi.close();
        timer.cancel();
    }

    public String getSourceNameForAlarm(String alarmId) {
        return alarmEquip.get(alarmId);
    }
    
    public RdaAlarmProperty findOrCreateProp(String alarmId) {
        String sourceName = alarmEquip.get(alarmId);
        if (sourceName == null) {
            try {
                LOG.info("Source for {} not yet known, asking data provider ... ", alarmId);
                sourceName = dpi.getSource(alarmId);
                if (sourceName != null) {
                    alarmEquip.put(alarmId, sourceName);
                    if (!exists(sourceName)) {
                        return addSource(sourceName);
                    }
                }
            } catch (Exception e) {
                LOG.warn(alarmId + " not found by data provider, ignored. (" + e.getMessage() + ")");
            }
        }
        return properties.get(sourceName);
    }
    
    public RdaAlarmProperty findProperty(final String name) {
        return properties.get(name);
    }

    
    public void initialize(Collection<AlarmValue> activeAlarms) throws Exception { 
        Data sd = DataFactory.createData();
        for (String source : dpi.getSourceNames()) {
            RdaAlarmProperty property = new RdaAlarmProperty(source);
            properties.put(source, property);
            sourceCount++;
            sd.append(source, System.currentTimeMillis());
        }        
        sources = new AcquiredData(sd);
        LOG.info("Declared {} sources.", sourceCount);
        
        // Use arraycall to init the alarm/source map
        Set<String> alarmIds = new HashSet<String>();
        for (AlarmValue av : activeAlarms) {    
            alarmCount++;
            alarmIds.add(RdaAlarmsPublisher.getAlarmId(av));
        }
        alarmEquip = dpi.initSourceMap(alarmIds);
    }
    
    //
    // --- PRIVAE METHODS ---------------------------------------------------------------------
    //
    private boolean exists(String sourceId) {
        return properties.containsKey(sourceId);
    }

    private RdaAlarmProperty addSource(String source) {
        RdaAlarmProperty property = new RdaAlarmProperty(source);
        properties.put(source, property);        
        return property;
    }

    //
    // --- Implements TimerTask ----------------------------------------------------------------
    //
    @Override
    public void run() {
        LOG.info("Running source definition garbage collector ...");

        try {
            Collection<String> sourceDefs = dpi.getSourceNames();
            
            // 1. add new stuff
            for (String sourceId : sourceDefs) {
                if (!this.exists(sourceId)) {
                    RdaAlarmProperty property = new RdaAlarmProperty(sourceId);
                    properties.put(sourceId, property);
                    sourceCount++;
                    sources.getData().append(sourceId, System.currentTimeMillis());
                }
            }
            
            // 2. remove old stuff
            for (String sourceId : properties.keySet()) {
                if (!sourceDefs.contains(sourceId)) {
                    sources.getData().remove(sourceId);
                    sourceCount--;
                    properties.remove(sourceId);
                    alarmEquip.remove(sourceId);
                }
            }
            
        } catch (Exception e) {
            LOG.error("Failed to update list of sources", e);
        }                
        LOG.info("Declared {} sources.", sourceCount);
    }


}
