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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DataProviderMock implements DataProviderIntf {

    private HashMap<String,String> alarmSources = new HashMap<String,String>();
    private ArrayList<String> sources = new ArrayList<String>();
    
    public DataProviderMock() {
        sources.add(TestBaseClass.SOURCE_ID);
        sources.add(TestBaseClass.SOURCE_ID_bis);
        alarmSources.put(TestBaseClass.ALARM_ID, TestBaseClass.SOURCE_ID);
        alarmSources.put(TestBaseClass.ALARM_ID_bis, TestBaseClass.SOURCE_ID);
        
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
        if (sourceId == null && alarmId.equals(TestBaseClass.SAMPLE_ALARM_ID)) {
            sourceId = TestBaseClass.SOURCE_ID_bis;
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
