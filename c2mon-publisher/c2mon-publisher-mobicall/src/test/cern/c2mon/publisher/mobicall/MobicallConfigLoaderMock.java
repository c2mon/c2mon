/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.mobicall;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For test purpose. Creates a fake alarm for Mobicall notification with alarm id FF:FM:1. All other
 * "find" requests should fail.
 * 
 * @author mbuttner
 */
public class MobicallConfigLoaderMock implements MobicallConfigLoaderIntf {

    private static final Logger LOG = LoggerFactory.getLogger(MobicallConfigLoaderMock.class);
    
    private ConcurrentHashMap<String, MobicallAlarm> alarms;

    public MobicallConfigLoaderMock() {
        alarms = new ConcurrentHashMap<String, MobicallAlarm>();
        
        MobicallAlarm ma = new MobicallAlarm("FF:FM:1");
        ma.setSystemName("FF");
        ma.setIdentifier("FM");
        ma.setFaultCode(1);
        ma.setNotificationId("111");
        ma.setProblemDescription("Dummy problem description");
        alarms.put("FF:FM:1", ma);
    }
    
    @Override
    public void loadConfig() {
        LOG.info("Request for config load received");
    }

    @Override
    public MobicallAlarm find(String alarmId) {
        return alarms.get(alarmId);
    }

    @Override
    public void close() {
        // No need for this in the mock
    }

}
