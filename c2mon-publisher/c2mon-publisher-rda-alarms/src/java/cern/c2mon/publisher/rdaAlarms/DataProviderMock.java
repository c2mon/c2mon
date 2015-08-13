/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.rdaAlarms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DataProviderMock implements DataProviderIntf {

    private HashMap<String,String> alarmSources = new HashMap<String,String>();
    private ArrayList<String> sources = new ArrayList<String>();
    
    public DataProviderMock() {
        sources.add(TestBaseClass.EXISTING_SOURCE_ID);
        sources.add(TestBaseClass.EXISTING_SOURCE_ID_2);
        alarmSources.put(TestBaseClass.EXISTING_ALARM_ID, TestBaseClass.EXISTING_SOURCE_ID);
        
        // this one comes on the fly!
        // alarmSources.put(TestBaseClass.EXISTING_ALARM_ID_2, TestBaseClass.EXISTING_SOURCE_ID_2);
    }

    public void removeSource(String sourceId) {
        sources.remove(sourceId);
        alarmSources.remove(sourceId);
    }


    
    @Override
    public String getSource(String alarmId) throws Exception {
        // use case 1.: find the source in the cache
        String sourceId = alarmSources.get(alarmId);
        
        // use case 2.: find it in the external source (DB or JMS)
        if (sourceId == null && alarmId.equals(TestBaseClass.EXISTING_ALARM_ID_2)) {
            sourceId = TestBaseClass.EXISTING_SOURCE_ID_2;
            alarmSources.put(alarmId, sourceId);            
        }
        return sourceId;
    }

    @Override
    public void close() {
        // not needed in the mock
    }

    @Override
    public Collection<String> getSourceNames() throws Exception {
        return sources;
    }

    @Override
    public ConcurrentHashMap<String, String> initSourceMap(Set<String> alarmIds) throws Exception {
        ConcurrentHashMap<String, String> sourceMap = new ConcurrentHashMap<String, String>();
        for (String alarmId : alarmIds) {
            String source = alarmSources.get(alarmId);
            if (source != null) {
                sourceMap.put(alarmId, source);
            }
        }
        return sourceMap;
    }

}
