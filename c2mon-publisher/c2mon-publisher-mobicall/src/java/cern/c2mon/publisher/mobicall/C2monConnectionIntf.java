/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.mobicall;

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
