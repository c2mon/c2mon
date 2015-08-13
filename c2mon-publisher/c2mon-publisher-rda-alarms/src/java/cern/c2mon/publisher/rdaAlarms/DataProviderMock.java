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
        sources.add("TSOURCE");
        alarmSources.put("FF:FM:1", "TSOURCE");
    }
    
    @Override
    public String getSource(String alarmId) throws Exception {
        return alarmSources.get(alarmId);
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
