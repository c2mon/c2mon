/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.almon.address;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import cern.c2mon.daq.almon.address.AlarmTripplet;
import cern.c2mon.daq.almon.address.AlarmType;
import cern.c2mon.daq.almon.address.AlmonHardwareAddress;
import cern.c2mon.daq.almon.address.AlmonHardwareAddressFactory;
import cern.c2mon.daq.almon.address.impl.AlmonHardwareAddressImpl;

/**
 * @author wbuczak
 */
public class AlmonHardwareAddressTest {

    @Test
    public void testBasicAlarmTripplet() {
        AlarmTripplet at = new AlarmTripplet("ITM.CRFBU", "RFLNP", 1);
        AlarmTripplet at2 = new AlarmTripplet("ITM.CRFBU", "RFLNP", 1);

        assertNotNull(at);
        assertEquals("ITM.CRFBU", at.getFaultFamily());
        assertEquals("RFLNP", at.getFaultMember());
        assertEquals(1, at.getFaultCode());

        assertNotSame(at, at2);
        assertEquals(at, at2);

    }

    @Test
    public void testJsonAlarmTripplet() {
        final String json = " { \"faultFamily\": \"ITM.CRFBU\",\"faultMember\":\"RFLNP\",\"faultCode\": \"1\"}";

        AlarmTripplet at = AlarmTripplet.fromJson(json);

        assertNotNull(at);
        assertEquals("ITM.CRFBU", at.getFaultFamily());
        assertEquals("RFLNP", at.getFaultMember());
        assertEquals(1, at.getFaultCode());

        assertEquals("ITM.CRFBU:RFLNP:1", at.toString());

        AlarmTripplet at2 = AlarmTripplet.fromJson(at.toJson());
        assertNotSame(at, at2);
        assertEquals(at, at2);

        assertEquals(at.toJson(), at2.toJson());

    }

    @Test
    public void testBasicAlmonHardwareAddress() {

        AlarmTripplet at = new AlarmTripplet("ITM.CRFBU", "RFLNP", 1);

        AlmonHardwareAddress hw = new AlmonHardwareAddressImpl(AlarmType.GM, "RFLNP", "ALARM", "value", at);
        assertNotNull(hw);
        assertEquals(AlarmType.GM, hw.getType());
        assertEquals("RFLNP", hw.getDevice());
        assertEquals("ALARM", hw.getProperty());
        assertEquals("value", hw.getField());
        assertEquals(at, hw.getAlarmTripplet());
    }

    @Test
    public void testJsonAlarmHardwareAddress() {

        final String json = " { \"device\": \"RFLNP\", \"property\": \"ALARM\", \"type\": \"GM\", \"field\": \"value\","
                + "\"alarmTripplet\": { \"faultFamily\": \"ITM.CRFBU\",\"faultMember\":\"RFLNP\",\"faultCode\": \"1\"}}";

        AlmonHardwareAddressImpl.fromJson(json);
        AlmonHardwareAddressImpl.fromJson(json);
        AlmonHardwareAddressImpl.fromJson(json);

        AlmonHardwareAddressImpl hw = AlmonHardwareAddressImpl.fromJson(json);
        hw = AlmonHardwareAddressImpl.fromJson(json);

        // System.out.println(hw.getDevice());

        System.out.println(hw.toString());

        assertNotNull(hw);
        assertEquals(AlarmType.GM, hw.getType());
        assertEquals("RFLNP", hw.getDevice());
        assertEquals("ALARM", hw.getProperty());
        assertEquals("value", hw.getField());

        AlmonHardwareAddressImpl hw2 = AlmonHardwareAddressImpl.fromJson(hw.toJson());

        System.out.println(hw2.toString());

        assertNotSame(hw, hw2);
        assertEquals(hw, hw2);

        assertEquals(hw.toJson(), hw2.toJson());

        AlmonHardwareAddress hw3 = AlmonHardwareAddressFactory.fromJson(json);

        assertEquals(hw, hw3);

        AlmonHardwareAddress hw4 = null;
        try {
            hw4 = AlmonHardwareAddressFactory.fromJson("{ incorrect: json}}");
            fail("IllegalArgumentException was expected");
        } catch (IllegalArgumentException ex) {
            assertNull(hw4);
        }

    }

}
