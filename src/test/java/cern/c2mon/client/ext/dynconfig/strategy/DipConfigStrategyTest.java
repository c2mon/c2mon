package cern.c2mon.client.ext.dynconfig.strategy;

import cern.c2mon.client.ext.dynconfig.DynConfigException;
import cern.c2mon.client.ext.dynconfig.query.DipQueryObj;
import cern.c2mon.shared.common.datatag.address.impl.DIPHardwareAddressImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;

class DipConfigStrategyTest {
    DipConfigStrategy strategy = new DipConfigStrategy();

    @BeforeEach
    void setUp() throws DynConfigException {
        URI uri = URI.create("dip://host/path?itemName=x1");
        DipQueryObj queryObj = new DipQueryObj(uri);
        strategy = new DipConfigStrategy(queryObj);
    }

    @Test
    void getHardwareAddress() {
        DIPHardwareAddressImpl expected = new DIPHardwareAddressImpl("x1");

        Assertions.assertEquals(expected, strategy.getHardwareAddress());
    }
}