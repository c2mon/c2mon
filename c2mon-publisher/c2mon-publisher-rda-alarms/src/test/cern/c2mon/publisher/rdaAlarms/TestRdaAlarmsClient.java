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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.japc.AcquiredParameterValue;
import cern.japc.MapParameterValue;
import cern.japc.Parameter;
import cern.japc.ParameterException;
import cern.japc.ParameterValueListener;
import cern.japc.Selector;
import cern.japc.SimpleParameterValue;
import cern.japc.SubscriptionHandle;
import cern.japc.factory.ParameterFactory;
import cern.japc.factory.ParameterValueFactory;

@RunWith(SpringJUnit4ClassRunner.class)
public class TestRdaAlarmsClient extends TestBaseClass implements ParameterValueListener {

    static C2monConnectionMock c2mon;
    
    //
    // --- TESTS ------------------------------------------------------------------------
    //
    @Test
    public void testSimpleGet() throws Exception {
        startTestPublisher();
        Thread.sleep(1 * 1000);
        c2mon.activateAlarm("FF", "FM", 1);
        Parameter param = ParameterFactory.newInstance().newParameter("DMN.RDA.ALARMS.TEST", SOURCE_ID);
        AcquiredParameterValue avalue = param.getValue(null);
        MapParameterValue value = (MapParameterValue) avalue.getValue();
        getLogger().info("Initial status of alarm: {} ", value.toString());   
        assertEquals(RdaAlarmsProperty.AlarmState.ACTIVE.toString(), value.getString(ALARM_ID));
    }
    
    @Test
    public void testSimpleSet() throws Exception {
        startTestPublisher();
        Thread.sleep(1 * 1000);
        Parameter param = ParameterFactory.newInstance().newParameter(RDA_TEST_DEVICE, SOURCE_ID);
        AcquiredParameterValue avalue = param.getValue(null);
        MapParameterValue value = (MapParameterValue) avalue.getValue();
        try {
            param.setValue(null, value);
            fail();
        } catch (ParameterException pe) {
            getLogger().info("The SET operation produced a {} as expected", pe.getClass().getName());
        }
        
    }

    Object lock = new Object();
    int updatesReceived = 0;
    
    @Test
    public void testSubscribe() throws Exception {
        startTestPublisher();
        Thread.sleep(1 * 1000);
        c2mon.activateAlarm("FF", "FM", 1);

        Map<String, SimpleParameterValue> filterParams = new HashMap<String, SimpleParameterValue>();        
        filterParams.put(ALARM_ID, ParameterValueFactory.newParameterValue(ALARM_ID));        
        MapParameterValue filter = ParameterValueFactory.newParameterValue(filterParams);
        Selector selector = ParameterValueFactory.newSelector("5000", filter, true);           

        Parameter p1 = ParameterFactory.newInstance().newParameter(RDA_TEST_DEVICE, SOURCE_ID);
        SubscriptionHandle sh1 = p1.createSubscription(selector, this);            
        sh1.startMonitoring();            
        
        Thread.sleep(1000);
        c2mon.terminateAlarm("FF", "FM", 1);
        Thread.sleep(1000);
        c2mon.activateAlarm("FF", "FM", 1);

        synchronized(lock) {
            getLogger().info("Waiting ...");
            lock.wait();
            getLogger().info("Ok, completing now");
        }
        assertEquals(3, updatesReceived);
    }

    //
    // --- Implements ParameterValueListener -----------------------------------------------------
    //
    @Override
    public void exceptionOccured(String arg0, String arg1, ParameterException arg2) {
        getLogger().warn("JAPC exception notified: {}, {}", arg0, arg1, arg2);
        synchronized(lock) {
            lock.notifyAll();
        }
    }

    @Override
    public synchronized void valueReceived(String param, AcquiredParameterValue avalue) {
        getLogger().info("Received: {} -> {}", param, avalue.toString());
        
        updatesReceived++;

        MapParameterValue value = (MapParameterValue) avalue.getValue();
        getLogger().info("UPDATE {}:", updatesReceived);
        for (String sourceId : value.getNames()) {
            getLogger().info(" - {} - {}", sourceId, value.getString(sourceId));
        }
        
        if (updatesReceived >= 2)
        {
            getLogger().info("Received the number of expected updates, unlocking the caller");
            synchronized(lock) {
                lock.notifyAll();
            }        
        }
    }
    
    //
    // --- SETUP & CLEANUP --------------------------------------------------------------
    //
    @Before
    public void retrieveBeans() {
        c2mon = applicationContext.getBean("c2mon", C2monConnectionMock.class);        
    }

    @AfterClass
    public static void cleanup() {
        stopTestPublisher();
    }

    
}
