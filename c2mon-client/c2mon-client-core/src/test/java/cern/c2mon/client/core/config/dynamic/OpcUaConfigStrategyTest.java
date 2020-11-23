package cern.c2mon.client.core.config.dynamic;

import cern.c2mon.client.core.config.dynamic.strategy.ITagConfigStrategy;
import cern.c2mon.client.core.config.dynamic.strategy.OpcUaConfigStrategy;
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
        assertThrows(DynConfigException.class, () -> ITagConfigStrategy.of(uri));
    }

    @Test
    void prepareDataTagConfigurationsShouldReturnProperAddressClass() throws DynConfigException {
        HardwareAddress actual = createDataTagHardwareAddressFrom("itemName=x&commandPulseLength=2");
        assertTrue(actual instanceof OPCHardwareAddress);
    }

    @Test
    void prepareDataTagConfigurationsWithPulseShouldIgnorePulse() throws DynConfigException {
        OPCHardwareAddressImpl expected = new OPCHardwareAddressImpl("x");
        HardwareAddress actual = createDataTagHardwareAddressFrom("itemName=x&commandPulseLength=2");
        assertEquals(expected, actual);
    }

    @Test
    void prepareDataTagConfigurationsWithNonStandardKeyShouldSetValue() throws DynConfigException {
        OPCHardwareAddressImpl expected = new OPCHardwareAddressImpl("x");
        expected.setNamespace(5);
        HardwareAddress actual = createDataTagHardwareAddressFrom("itemName=x&setNamespace=5");
        assertEquals(expected, actual);
    }

    @Test
    void prepareCommandTagConfigurationsShouldReturnProperAddressClass() throws DynConfigException {
        HardwareAddress actual = createCommandTagHardwareAddressFrom("itemName=x&commandPulseLength=2");
        assertTrue(actual instanceof OPCHardwareAddress);
    }

    @Test
    void prepareCommandTagConfigurationsWithPulseSetPulse() throws DynConfigException {
        OPCHardwareAddressImpl expected = new OPCHardwareAddressImpl("x", 2);
        HardwareAddress actual = createCommandTagHardwareAddressFrom("itemName=x&commandPulseLength=2");
        assertEquals(expected, actual);
    }

    @Test
    void prepareCommandTagConfigurationsWithNonStandardKeyShouldSetValue() throws DynConfigException {
        OPCHardwareAddressImpl expected = new OPCHardwareAddressImpl("x");
        expected.setNamespace(5);
        HardwareAddress actual = createCommandTagHardwareAddressFrom("itemName=x&setNamespace=5");
        assertEquals(expected, actual);
    }

    HardwareAddress createDataTagHardwareAddressFrom(String queries) throws DynConfigException {
        URI uri = URI.create("opc.tcp://host/path?" + queries);
        strategy = ITagConfigStrategy.of(uri);
        return this.strategy.prepareDataTagConfigurations().getAddress().getHardwareAddress();
    }

    HardwareAddress createCommandTagHardwareAddressFrom(String queries) throws DynConfigException {
        URI uri = URI.create("opc.tcp://host/path?tagType=COMMAND&" + queries);
        strategy = ITagConfigStrategy.of(uri);
        return this.strategy.prepareCommandTagConfigurations().getHardwareAddress();
    }
}