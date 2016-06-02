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
package cern.c2mon.daq.opcua;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;

import cern.c2mon.daq.opcua.connection.common.AbstractOPCUAAddressParser.AddressKeys;
import cern.c2mon.daq.opcua.connection.common.impl.OPCUADefaultAddress;
import cern.c2mon.daq.opcua.connection.common.impl.OPCUADefaultAddressParser;

public class OPCAddressParserTest {

    private OPCUADefaultAddressParser parser = new OPCUADefaultAddressParser();

    @Test
    public void testParseAddressSingleCorrect() throws URISyntaxException {
        String addressString = AddressKeys.URI + "=dcom://testhost:1234/testpath;"
            + AddressKeys.user + "=user@domain;" + AddressKeys.password + "=password;"
            + AddressKeys.serverTimeout + "=314;" + AddressKeys.serverRetryTimeout + "=1337";
        List<OPCUADefaultAddress> addresses =
            parser.createOPCAddressFromAddressString(addressString);
        assertEquals(1, addresses.size());
        OPCUADefaultAddress address = addresses.get(0);
        assertEquals("dcom", address.getProtocol());
        assertEquals("domain", address.getDomain());
        assertEquals("password", address.getPassword());
        assertEquals(1337, address.getServerRetryTimeout());
        assertEquals(314, address.getServerTimeout());
        assertEquals(new URI("dcom://testhost:1234/testpath"), address.getUri());
        assertEquals("dcom://testhost:1234/testpath", address.getUriString());
        assertEquals("user", address.getUser());
        assertEquals(true, address.isAliveWriterEnabled());
    }

    @Test
    public void testParseAddressDoubleCorrect() throws URISyntaxException {
        String addressString = AddressKeys.URI + "=dcom://testhost:1234/testpath,"
            + "http://testhost2:1234/testpath2;" + AddressKeys.user + "=user@domain,user2;"
            + AddressKeys.password + "=password, password2;"
            + AddressKeys.serverTimeout + "=314;" + AddressKeys.serverRetryTimeout + "=1337;"
            + AddressKeys.aliveWriter + "=false";
        List<OPCUADefaultAddress> addresses =
            parser.createOPCAddressFromAddressString(addressString);
        assertEquals(2, addresses.size());
        OPCUADefaultAddress address = addresses.get(0);
        assertEquals("dcom", address.getProtocol());
        assertEquals("domain", address.getDomain());
        assertEquals("password", address.getPassword());
        assertEquals(1337, address.getServerRetryTimeout());
        assertEquals(314, address.getServerTimeout());
        assertEquals(new URI("dcom://testhost:1234/testpath"), address.getUri());
        assertEquals("dcom://testhost:1234/testpath", address.getUriString());
        assertEquals("user", address.getUser());
        assertEquals(false, address.isAliveWriterEnabled());

        address = addresses.get(1);
        assertEquals("http", address.getProtocol());
        assertNull(address.getDomain());
        assertEquals("password2", address.getPassword());
        assertEquals(1337, address.getServerRetryTimeout());
        assertEquals(314, address.getServerTimeout());
        assertEquals(new URI("http://testhost2:1234/testpath2"), address.getUri());
        assertEquals("http://testhost2:1234/testpath2", address.getUriString());
        assertEquals("user2", address.getUser());
        assertEquals(false, address.isAliveWriterEnabled());
    }

    @Test(expected=Exception.class)
    public void testParseAddressWrongURI() {
        String addressString = AddressKeys.URI + "=://wrongURI?,"
            + "http://testhost2:1234/testpath2;" + AddressKeys.user + "=user@domain,user2;"
            + AddressKeys.password + "=password, password2;"
            + AddressKeys.serverTimeout + "=314;" + AddressKeys.serverRetryTimeout + "=1337";
        parser.createOPCAddressFromAddressString(addressString);
    }
}
