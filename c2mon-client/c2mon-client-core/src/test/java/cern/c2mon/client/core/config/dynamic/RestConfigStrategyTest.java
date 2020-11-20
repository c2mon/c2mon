package cern.c2mon.client.core.config.dynamic;

import cern.c2mon.client.core.config.dynamic.strategy.ITagConfigStrategy;
import cern.c2mon.client.core.config.dynamic.strategy.RestConfigStrategy;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.address.impl.HardwareAddressImpl;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RestConfigStrategyTest {

    ITagConfigStrategy strategy;

    @Test
    void createStrategyWithoutUrlShouldThrowError() {
        URI uri = URI.create("http://host/path?mode=GET");
        assertThrows(DynConfigException.class, () -> new RestConfigStrategy(uri));
    }

    @Test
    void createStrategyWithoutModeShouldThrowError() {
        URI uri = URI.create("http://host/path?url=xxx");
        assertThrows(DynConfigException.class, () -> new RestConfigStrategy(uri));
    }

    @Test
    void invalidModeShouldThrowError() {
        URI uri = URI.create("http://host/path?url=xxx&mode=---&getFrequency=5&postFrequency=3&jsonPathExpression=aa");
        assertThrows(DynConfigException.class, () -> new RestConfigStrategy(uri));
    }

    @Test
    void prepareTagConfigurationsShouldIncludeAllPropertiesInKeys() throws DynConfigException {
        DataTagAddress expected = new DataTagAddress(new HardwareAddressImpl());
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("url", "xxx");
        parameters.put("mode", "POST");
        parameters.put("getFrequency", "5");
        parameters.put("postFrequency", "3");
        parameters.put("jsonPathExpression", "aa");
        expected.setAddressParameters(parameters);

        URI uri = URI.create("http://host/path?url=xxx&mode=POST&getFrequency=5&postFrequency=3&jsonPathExpression=aa");
        strategy = new RestConfigStrategy(uri);
        DataTagAddress actual = this.strategy.prepareDataTagConfigurations().getAddress();

        assertEquals(expected, actual);
    }
}
