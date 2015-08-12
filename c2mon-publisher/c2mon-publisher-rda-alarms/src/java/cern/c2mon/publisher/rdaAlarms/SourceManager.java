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
    // really a singleton
    private SourceManager() {
        timer = new Timer();
        timer.scheduleAtFixedRate(this, FREQ, FREQ);
    }
    
    // factory method used by other classes to find the reference, and by the spring config
    // to create the instance
    public static SourceManager getSourceManager() {
        if (sourceMgr == null) {
            sourceMgr = new SourceManager();
        }
        return sourceMgr;
    }

    // property setter used by the Spring config
    public void setDataProvider(DataProviderInterface dpi) {
        this.dpi = dpi;        
    }

    //
    // --- JMX --------------------------------------------------------------------------------
    //    
    @ManagedAttribute
    public int getSourceCount() {
        return sourceCount;
    }

    @ManagedAttribute
    public long getAlarmCount() {
        return alarmCount;
    }

    //
    // --- PUBLIC METHODS ---------------------------------------------------------------------
    //    
    /**
     * @return <code>AcquiredData</code> for RDA publishing 
     */
    AcquiredData getSources() {
        return sources;
    }
    
    /**
     * @param name of the alarm source for which the RDA property is looked for.
     * @return <code>RdaAlarmPropery</code> providing all known alarms for the source
     */
    public RdaAlarmProperty findProperty(final String name) {
        return properties.get(name);
    }
    
    /**
     * To be called when application closes to properly release resources.
     */
    public void close() {
        dpi.close();
        timer.cancel();
    }

    public String getSourceNameForAlarm(String alarmId) {
        return alarmEquip.get(alarmId);
    }
    
    /**
     * The method allows the RdaAlarmsPublisher to find the property corresponding to a given
     * alarm. The properties for all sources are supposed to exist, because they are either
     * created on startup or during periodic update.
     * 
     * @param alarmId <code>String</code> alarm identifier (triplet)
     * @return <code>RdaAlarmPropery</code> providing all known alarms for the source of the alarm
     */
    public RdaAlarmProperty findProp(String alarmId) {
        String sourceName = alarmEquip.get(alarmId);
        if (sourceName != null) {
            return properties.get(sourceName);
        }
        return null;
    }
    

    /**
     * For efficiency: the method loads all source definitions and updates internal strucutres
     * accordingly. For the active alarms, the mapping to their source is also filled in. This
     * allows to use array calls on startup rather than a 1 by one query.
     * 
     * @param activeAlarms <code>Collection<AlarmValue></code> list of active alarms
     * @throws Exception if the call to the dataprovider fails
     */
    public void initialize(Collection<AlarmValue> activeAlarms) throws Exception { 
        Data sd = DataFactory.createData();
        sources = new AcquiredData(sd);
        for (String source : dpi.getSourceNames()) {
            addSource(source);
        }        
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
        sources.getData().append(source, System.currentTimeMillis());
        sourceCount++;
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
                    addSource(sourceId);
                }
            }
            
            // 2. remove old stuff
            for (String sourceId : properties.keySet()) {
                if (!sourceDefs.contains(sourceId)) {
                    sources.getData().remove(sourceId);
                    properties.remove(sourceId);
                    alarmEquip.remove(sourceId);
                    sourceCount--;
                }
            }
            
        } catch (Exception e) {
            LOG.error("Failed to update list of sources", e);
        }                
        LOG.info("Declared {} sources.", sourceCount);
    }


}
