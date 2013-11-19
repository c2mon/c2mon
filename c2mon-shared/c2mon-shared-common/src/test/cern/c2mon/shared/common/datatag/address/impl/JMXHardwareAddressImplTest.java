/*
 * $Id $
 * 
 * $Date$ $Revision$ $Author$
 * 
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.shared.common.datatag.address.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;

import org.junit.Test;

import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.common.datatag.address.HardwareAddressFactory;
import cern.c2mon.shared.common.datatag.address.JMXHardwareAddress;
import cern.c2mon.shared.common.datatag.address.JMXHardwareAddress.ReceiveMethod;

public class JMXHardwareAddressImplTest {

    @Test
    public void testConstructors() {
        try {
            new JMXHardwareAddressImpl("java.lang:type=Runtime", "java.vm.version");
        } catch (ConfigurationException ex) {
            fail("ConfigurationException NOT expected");
        }

        try {
            new JMXHardwareAddressImpl("java.lang:type=Runtime", "java.vm.version");
        } catch (ConfigurationException ex) {
            fail("ConfigurationException NOT expected");
        }

        try {
            new JMXHardwareAddressImpl("java.lang:type=Runtime", "java.vm.version", "notification");
        } catch (ConfigurationException ex) {
            fail("ConfigurationException NOT expected");
        }

        try {
            new JMXHardwareAddressImpl(null, "java.vm.version");
            fail("ConfigurationException expected");
        } catch (ConfigurationException ex) {
        }

        try {
            new JMXHardwareAddressImpl("", "java.vm.version");
            fail("ConfigurationException expected");
        } catch (ConfigurationException ex) {
        }
        try {
            new JMXHardwareAddressImpl("java.lang:type=Runtime", "cacheSize", "unknown");
            fail("ConfigurationException expected");
        } catch (ConfigurationException ex) {
        }

    }

    @Test
    public void testGetAttribute() throws Exception {
        JMXHardwareAddress addr1 = new JMXHardwareAddressImpl("java.lang:type=Runtime", "java.vm.version");
        assertEquals("java.vm.version", addr1.getAttribute());
    }

    @Test
    public void testGetCallMethod() throws Exception {
        JMXHardwareAddress addr1 = new JMXHardwareAddressImpl("java.lang:type=Runtime", "java.vm.version", "method1",
                "notification");
        JMXHardwareAddress addr2 = new JMXHardwareAddressImpl("java.lang:type=Runtime", "java.vm.version");
        assertEquals("method1", addr1.getCallMethod());
        assertTrue(addr1.hasCallMethod());
        assertTrue(addr1.hasAttribute());
        assertEquals(null, addr2.getCallMethod());
    }

    @Test
    public void testGetReceiveMethod() throws Exception {
        JMXHardwareAddress addr1 = new JMXHardwareAddressImpl("java.lang:type=Runtime", "java.vm.version");
        assertEquals(ReceiveMethod.poll, addr1.getReceiveMethod());
        assertFalse(addr1.hasCallMethod());
        JMXHardwareAddress addr2 = new JMXHardwareAddressImpl("java.lang:type=Runtime", "cacheSize", "notification");
        assertEquals(ReceiveMethod.notification, addr2.getReceiveMethod());
    }

    @Test
    public void testToConfigXMLHardwareAddress() throws Exception {
        JMXHardwareAddress addr1 = new JMXHardwareAddressImpl("java.lang:type=Runtime", "java.vm.version");
        JMXHardwareAddress addr2 = (JMXHardwareAddress) HardwareAddressFactory.getInstance().fromConfigXML(
                addr1.toConfigXML());
        assertEquals(addr1, addr2);

        JMXHardwareAddress addr3 = new JMXHardwareAddressImpl("cern.mypackage.mybean=Test", "TestAttribute",null,5,"myCompositeAttribute",null,"notification");
        //System.out.println(addr3.toConfigXML());        
        JMXHardwareAddress addr4 = (JMXHardwareAddress) HardwareAddressFactory.getInstance().fromConfigXML(                
                addr3.toConfigXML());
        //System.out.println();
        //System.out.println(addr4.toConfigXML());
        
        assertEquals(addr3, addr4);
    }

    @Test
    public void testEquals() throws Exception {
        JMXHardwareAddress addr1 = new JMXHardwareAddressImpl("java.lang:type=Runtime", "java.vm.version", "method1",
                "poll");
        JMXHardwareAddress addr2 = new JMXHardwareAddressImpl("java.lang:type=Runtime", "java.vm.version", "method1",
                "poll");

        JMXHardwareAddress addr3 = new JMXHardwareAddressImpl("java.lang:type=Runtime", "java.vm.version");
        JMXHardwareAddress addr4 = new JMXHardwareAddressImpl("java.lang:type=Runtime", "java.vm.version");
   
        assertEquals(addr1, addr2);

        assertEquals(addr3,addr4);

        assertEquals(addr1, addr2);      
    }

    @Test
    public void testHashCode() throws Exception {
        JMXHardwareAddress addr1 = new JMXHardwareAddressImpl("java.lang:type=Runtime", "java.vm.version", "method1",
                "poll");
        JMXHardwareAddress addr2 = new JMXHardwareAddressImpl("java.lang:type=Runtime", "java.vm.version", "method1",
                "poll");
        JMXHardwareAddress addr3 = new JMXHardwareAddressImpl("java.lang:type=Runtime", "java.vm.version",
                "some-method2", "poll");

        HashMap<HardwareAddress, Integer> map = new HashMap<HardwareAddress, Integer>();
        map.put(addr1, 1);
        map.put(addr2, 2);

        assertEquals(1, map.size());

        map.put(addr3, 3);

        assertEquals(2, map.size());
    }
}
