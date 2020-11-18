package cern.c2mon.client.ext.dynconfig.strategy;

import cern.c2mon.client.ext.dynconfig.DynConfigException;
import cern.c2mon.shared.common.datatag.address.DIPHardwareAddress;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.common.datatag.address.impl.DIPHardwareAddressImpl;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class DipConfigStrategyTest {

    @Test
    void createStrategyWithoutPublicationNameShouldThrowError() {
        URI uri = URI.create("dip://host/path");
        assertThrows(DynConfigException.class, () -> new DipConfigStrategy(uri));
    }

    @Test
    void prepareTagConfigurationsShouldReturnProperAddressClass() throws DynConfigException {
        HardwareAddress actual = createHardwareAddressFrom("publicationName=x1");
        assertTrue(actual instanceof DIPHardwareAddress);
    }


    @Test
    void prepareTagConfigurationsWithFieldNameAndIndexShouldSetThem() throws DynConfigException {
        DIPHardwareAddressImpl expected = new DIPHardwareAddressImpl("x1", "b", 2);
        HardwareAddress actual = createHardwareAddressFrom("publicationName=x1&fieldName=b&fieldIndex=2");
        assertEquals(expected, actual);
    }

    static HardwareAddress createHardwareAddressFrom(String queries) throws DynConfigException {
        URI uri = URI.create("dip://host/path?"+ queries);
        ITagConfigStrategy strategy = new DipConfigStrategy(uri);
        return strategy.prepareDataTagConfigurations().getAddress().getHardwareAddress();
    }
}