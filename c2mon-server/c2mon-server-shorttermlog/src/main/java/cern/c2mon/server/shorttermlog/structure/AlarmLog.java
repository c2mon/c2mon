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

package cern.c2mon.server.shorttermlog.structure;

import java.sql.Timestamp;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import cern.c2mon.pmanager.IFallback;
import cern.c2mon.pmanager.fallback.exception.DataFallbackException;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.util.json.GsonFactory;

import com.google.gson.Gson;

/**
 * Bean which represents a Loggable {@link AlarmValue}. 
 * 
 * @author Felix Ehm
 */
@Getter @Setter @EqualsAndHashCode
public class AlarmLog implements IFallback, Loggable {

    private static final Gson GSON = GsonFactory.createGson(); 
    
    private long tagId;
    
    private long alarmId;
    
    private String faultFamily;
    
    private String faultMember;
    
    private int faultCode;
    
    private boolean active;
    
    private int priority;
    
    private String info;
    
    private Timestamp serverTimestamp;

    private Timestamp logDate = null;

    private String timezone;
    
    @Override
    public String getValue() {
        return Boolean.toString(active);
    }

    @Override
    public IFallback getObject(String line) throws DataFallbackException {
        return GSON.fromJson(line, AlarmLog.class);
    }

    @Override
    public String toString() {
        logDate = new Timestamp(System.currentTimeMillis());
        return GSON.toJson(this);
    }

    @Override
    public String getId() {
        return String.valueOf(getAlarmId());
    }
}
