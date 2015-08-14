/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.rdaAlarms;

import java.util.Collection;

import javax.jms.JMSException;

import cern.c2mon.client.jms.AlarmListener;
import cern.c2mon.shared.client.alarm.AlarmValue;

public interface C2monConnectionIntf {

        
    void setListener(AlarmListener listener);
    void start() throws Exception;
    void connectListener() throws JMSException;
    void stop();
    
    Collection<AlarmValue> getActiveAlarms();
    public int getQuality(long alarmTagId);

    /**
     * Use this to check hte return value of getQuality()
     * Ex.: if (qual &amp; Quality.EXISTING == Quality.EXISTING) is true if the data tag is existing
     */
    public static class Quality {
        public static final int EXISTING =1;
        public static final int VALID =2;        
    }


}
