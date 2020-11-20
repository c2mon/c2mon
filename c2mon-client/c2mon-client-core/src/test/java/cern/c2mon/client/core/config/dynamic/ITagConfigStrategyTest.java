package cern.c2mon.client.core.config.dynamic;

import cern.c2mon.client.core.config.dynamic.strategy.DipConfigStrategy;
import cern.c2mon.client.core.config.dynamic.strategy.ITagConfigStrategy;
import cern.c2mon.client.core.config.dynamic.strategy.OpcUaConfigStrategy;
import cern.c2mon.client.core.config.dynamic.strategy.RestConfigStrategy;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ITagConfigStrategyTest {

    @Test
    void nonSupportedUriShouldThrowException() {
        URI uri = URI.create("abc://host/path");
        assertThrows(DynConfigException.class, () -> ITagConfigStrategy.of(uri));
    }

    @Test
    void opctcpUriShouldReturnOpcUaConfigStrategy() throws DynConfigException {
        URI uri = URI.create("opc.tcp://host/path?itemName=1");
        ITagConfigStrategy strategy = ITagConfigStrategy.of(uri);
        assertTrue(strategy instanceof OpcUaConfigStrategy);
    }

    @Test
    void httpUriShouldReturnRestConfigStrategy() throws DynConfigException {
        URI uri = URI.create("http://host/path?mode=GET&url=a");
        ITagConfigStrategy strategy = ITagConfigStrategy.of(uri);
        assertTrue(strategy instanceof RestConfigStrategy);
    }

    @Test
    void httpsUriShouldReturnRestConfigStrategy() throws DynConfigException {
        URI uri = URI.create("https://host/path?mode=GET&url=a");
        ITagConfigStrategy strategy = ITagConfigStrategy.of(uri);
        assertTrue(strategy instanceof RestConfigStrategy);
    }

    @Test
    void dipUriShouldReturnDipConfigStrategy() throws DynConfigException {
        URI uri = URI.create("dip://host/path?publicationName=a");
        ITagConfigStrategy strategy = ITagConfigStrategy.of(uri);
        assertTrue(strategy instanceof DipConfigStrategy);
    }

    @Test
    void capitalizationShouldBeIgnored() throws DynConfigException {
        URI uri = URI.create("DIP://host/path?publicationName=a");
        ITagConfigStrategy strategy = ITagConfigStrategy.of(uri);
        assertTrue(strategy instanceof DipConfigStrategy);
    }
}