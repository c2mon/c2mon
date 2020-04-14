package cern.c2mon.client.ext.dynconfig.strategy;

import cern.c2mon.client.ext.dynconfig.DynConfigException;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.common.datatag.address.OPCHardwareAddress;
import cern.c2mon.shared.common.datatag.address.impl.OPCHardwareAddressImpl;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class OpcUaConfigStrategyTest {

    ITagConfigStrategy strategy;

    @Test
    void createStrategyWithoutItemNameShouldThrowError() {
        URI uri = URI.create("opc.tcp://host/path?commandPulseLength=2");
        assertThrows(DynConfigException.class, () -> new OpcUaConfigStrategy(uri));
    }

    @Test
    void prepareTagConfigurationsShouldReturnProperAddressClass() throws DynConfigException {
        HardwareAddress actual = createHardwareAddressFrom("itemName=x&commandPulseLength=2");
        assertTrue(actual instanceof OPCHardwareAddress);
    }


    @Test
    void prepareTagConfigurationsWithPulseShouldSetPulse() throws DynConfigException {
        OPCHardwareAddressImpl expected = new OPCHardwareAddressImpl("x", 2);
        HardwareAddress actual = createHardwareAddressFrom("itemName=x&commandPulseLength=2");
        assertEquals(expected, actual);
    }

    @Test
    void prepareTagConfigurationsWithNonStandardKeyShouldSetValue() throws DynConfigException {
        OPCHardwareAddressImpl expected = new OPCHardwareAddressImpl("x");
        expected.setNamespace(5);
        HardwareAddress actual = createHardwareAddressFrom("itemName=x&setNamespace=5");
        assertEquals(expected, actual);
    }

    HardwareAddress createHardwareAddressFrom(String queries) throws DynConfigException {
        URI uri = URI.create("opc.tcp://host/path?"+ queries);
        strategy = new OpcUaConfigStrategy(uri);
        return this.strategy.prepareTagConfigurations().getAddress().getHardwareAddress();
    }
}