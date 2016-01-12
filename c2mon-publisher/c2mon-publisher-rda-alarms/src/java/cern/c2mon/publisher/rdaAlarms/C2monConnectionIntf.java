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

import javax.jms.JMSException;

import cern.c2mon.client.jms.AlarmListener;
import cern.c2mon.shared.client.alarm.AlarmValue;

/**
 * This interface restricts the C2MON API to what we need in the RDA publisher, and by the
 * way defines what needs to be mocked for testing.
 * 
 * @author mbuttner
 */
public interface C2monConnectionIntf {
        
    void setListener(AlarmListener listener);       // callback for incoming alarm events
    void start() throws Exception;                  // start the connection
    void connectListener() throws JMSException;     // signals that the listener is now ready to receive event
    void stop();                                    // stop the C2MON connection
    
    Collection<AlarmValue> getActiveAlarms();       // retrieve the initial list of active alarms
    public int getQuality(long alarmTagId);         // check underlying datatag for an alarm (see below)

    /**
     * Use this to check the return value of getQuality(). An alarm is valid if the underlying datatag
     * exists and is valid. Otherwise, the alarm should be removed from the publisher, or have a special
     * status.
     * Ex.: if (qual &amp; Quality.EXISTING == Quality.EXISTING) is true if the data tag is existing
     */
    public static class Quality {
        public static final int EXISTING =1;
        public static final int VALID =2;        
    }


}
