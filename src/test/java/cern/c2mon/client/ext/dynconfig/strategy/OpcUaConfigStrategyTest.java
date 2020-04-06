package cern.c2mon.client.ext.dynconfig.strategy;

import cern.c2mon.client.ext.dynconfig.DynConfigException;
import cern.c2mon.client.ext.dynconfig.query.OpcUaQueryObj;
import cern.c2mon.shared.common.datatag.address.OPCHardwareAddress;
import cern.c2mon.shared.common.datatag.address.impl.OPCHardwareAddressImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;

class OpcUaConfigStrategyTest {
    OpcUaConfigStrategy strategy;

    @BeforeEach
    void setUp() throws DynConfigException {
        URI uri = URI.create("opc.tcp://host/path?itemName=x1&namespace=1");
        OpcUaQueryObj queryObj = new OpcUaQueryObj(uri);
        strategy = new OpcUaConfigStrategy(queryObj);
    }

    @Test
    void getHardwareAddress() {
        OPCHardwareAddressImpl expected = new OPCHardwareAddressImpl("x1");
        expected.setNamespace(1);
        expected.setAddressType(OPCHardwareAddress.ADDRESS_TYPE.STRING);

        Assertions.assertEquals(expected, strategy.getHardwareAddress());
    }
}