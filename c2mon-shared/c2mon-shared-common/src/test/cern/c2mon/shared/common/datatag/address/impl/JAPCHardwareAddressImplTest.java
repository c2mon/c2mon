package cern.c2mon.shared.common.datatag.address.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import cern.c2mon.shared.common.datatag.address.HardwareAddressFactory;
import cern.c2mon.shared.common.datatag.address.JAPCHardwareAddress;
import cern.c2mon.shared.common.datatag.address.JAPCHardwareAddress.COMMAND_TYPE;
import cern.c2mon.shared.common.datatag.address.impl.JAPCHardwareAddressImpl;

public class JAPCHardwareAddressImplTest {

    @Test
    public void testToConfigXMLHardwareAddress() throws Exception {
        JAPCHardwareAddress addr1 = new JAPCHardwareAddressImpl("device", "property", "field", "GET", "contextfield",
                "filterkey=filterval");

        JAPCHardwareAddress addr2 = (JAPCHardwareAddress) HardwareAddressFactory.getInstance().fromConfigXML(
                addr1.toConfigXML());
        assertNotSame(addr1, addr2);
        assertEquals(addr1, addr2);

        // System.out.println(addr1.toConfigXML());

        addr1 = new JAPCHardwareAddressImpl("device", "property", "field", "SET", null);

        addr2 = (JAPCHardwareAddress) HardwareAddressFactory.getInstance().fromConfigXML(addr1.toConfigXML());
        assertNotSame(addr1, addr2);
        assertEquals(addr1, addr2);

        assertFalse(addr1.hasContextField());
        assertFalse(addr1.hasFilter());

        // System.out.println(addr1.toConfigXML());

        StringBuilder strAddr = new StringBuilder(
                "<HardwareAddress class=\"cern.c2mon.shared.common.datatag.address.impl.JAPCHardwareAddressImpl\">");
        strAddr.append("<device-name>device</device-name>").append("<property-name>property</property-name>")
                .append("<data-field-name>field</data-field-name>").append("<command-type>SET</command-type>")
                .append("</HardwareAddress>");

        addr2 = (JAPCHardwareAddress) HardwareAddressFactory.getInstance().fromConfigXML(strAddr.toString());
        assertEquals(COMMAND_TYPE.SET, addr2.getCommandType());
        assertEquals(null, addr2.getContextField());
        assertFalse(addr2.hasContextField());

        strAddr = new StringBuilder(
                "<HardwareAddress class=\"cern.c2mon.shared.common.datatag.address.impl.JAPCHardwareAddressImpl\">");
        strAddr.append("<device-name>device</device-name>").append("<property-name>property</property-name>")
                .append("<data-field-name>field</data-field-name>").append("<command-type>get</command-type>")
                .append("<context-field>contextfield</context-field>").append("</HardwareAddress>");

        addr2 = (JAPCHardwareAddress) HardwareAddressFactory.getInstance().fromConfigXML(strAddr.toString());
        assertEquals(COMMAND_TYPE.GET, addr2.getCommandType());
        assertEquals("contextfield", addr2.getContextField());
        assertTrue(addr2.hasContextField());
        assertFalse(addr2.hasFilter());

        strAddr = new StringBuilder(
                "<HardwareAddress class=\"cern.c2mon.shared.common.datatag.address.impl.JAPCHardwareAddressImpl\">");
        strAddr.append("<device-name>device</device-name>").append("<property-name>property</property-name>")
                .append("<data-field-name><![CDATA[field&part2]]></data-field-name>").append("</HardwareAddress>");

        addr2 = (JAPCHardwareAddress) HardwareAddressFactory.getInstance().fromConfigXML(strAddr.toString());
        assertEquals(COMMAND_TYPE.UNKNOWN, addr2.getCommandType());
        assertEquals(null, addr2.getContextField());

        // make sure that CDATAs can be used in the data-field-name
        assertEquals("field&part2", addr2.getDataFieldName());

        String addr3 = "<HardwareAddress class=\"cern.c2mon.shared.common.datatag.address.impl.JAPCHardwareAddressImpl\"><protocol>yami</protocol><service>yami</service><device-name>DIAMON.CLIC.CFV-UA43-CIBL4</device-name><property-name>Acquisition</property-name><data-field-name>ntp.avg</data-field-name></HardwareAddress>";
        addr2 = (JAPCHardwareAddress) HardwareAddressFactory.getInstance().fromConfigXML(addr3.toString());
        assertFalse(addr2.hasFilter());
        assertFalse(addr2.hasContextField());
    }
}
