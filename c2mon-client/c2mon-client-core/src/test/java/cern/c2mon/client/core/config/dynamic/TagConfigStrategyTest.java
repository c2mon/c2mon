package cern.c2mon.client.core.config.dynamic;

import cern.c2mon.client.core.config.dynamic.query.QueryObj;
import cern.c2mon.client.core.config.dynamic.strategy.DipConfigStrategy;
import cern.c2mon.client.core.config.dynamic.strategy.ITagConfigStrategy;
import cern.c2mon.client.core.config.dynamic.strategy.OpcUaConfigStrategy;
import cern.c2mon.client.core.config.dynamic.strategy.RestConfigStrategy;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.tag.CommandTag;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TagConfigStrategyTest {

    ITagConfigStrategy strategy;
    C2monClientDynConfigProperties.ProcessEquipmentURIMapping m;

    @BeforeEach
    public void setUp() {
        strategy = null;
        m = new C2monClientDynConfigProperties.ProcessEquipmentURIMapping();
        m.setEquipmentName("name");
    }

    @Test
    void createConfigStrategyForBadUriShouldThrowError() throws DynConfigException {
        assertThrows(DynConfigException.class, () -> ITagConfigStrategy.of(URI.create("host?publicationName=a")));
    }

    @Test
    void prepareEquipmentConfigurationShouldContainRelevantFields() throws DynConfigException {
        Equipment expected = Equipment.create("name", "cern.c2mon.daq.dip.DIPMessageHandler")
                .description("default").address("dip://host/path").build();
        m.setEquipmentDescription("default");
        strategy = ITagConfigStrategy.of(URI.create("dip://host/path?publicationName=x1"));
        Equipment actual = strategy.prepareEquipmentConfiguration(m);
        assertEquals(expected, actual);
    }

    @Test
    void prepareEquipmentConfigurationWithoutDescriptionShouldPass() throws DynConfigException {
        Equipment expected = Equipment.create("name", "cern.c2mon.daq.dip.DIPMessageHandler")
                .address("dip://host/path").build();
        strategy = ITagConfigStrategy.of(URI.create("dip://host/path?publicationName=x1"));
        Equipment actual = strategy.prepareEquipmentConfiguration(m);
        assertEquals(expected, actual);
    }

    @Test
    void opcEquipmentShouldHaveProtocolSpecificMessageHandler() throws DynConfigException {
        strategy = ITagConfigStrategy.of(URI.create("opc.tcp://host/path?itemName=1"));
        Equipment actual = strategy.prepareEquipmentConfiguration(m);
        assertEquals("cern.c2mon.daq.opcua.OPCUAMessageHandler", actual.getHandlerClass());
    }

    @Test
    void dipEquipmentShouldHaveProtocolSpecificMessageHandler() throws DynConfigException {
        strategy = ITagConfigStrategy.of(URI.create("dip://host/path?publicationName=a"));
        Equipment actual = strategy.prepareEquipmentConfiguration(m);
        assertEquals("cern.c2mon.daq.dip.DIPMessageHandler", actual.getHandlerClass());
    }

    @Test
    void restEquipmentShouldHaveProtocolSpecificMessageHandler() throws DynConfigException {
        strategy = ITagConfigStrategy.of(URI.create("http://host/path?url=bla&mode=GET"));
        Equipment actual = strategy.prepareEquipmentConfiguration(m);
        assertEquals("cern.c2mon.daq.rest.RestMessageHandler", actual.getHandlerClass());
    }

    @Test
    void toTagConfigurationWithoutTagNameShouldResortToDefault() throws DynConfigException {
        URI uri = URI.create("dip://host/path?publicationName=1");
        strategy = ITagConfigStrategy.of(uri);
        DataTag dt = strategy.prepareDataTagConfigurations();
        assertEquals(uri.toASCIIString(), dt.getName());
    }

    @Test
    void toTagConfigurationWithTagNameShouldHaveName() throws DynConfigException {
        DataTag tag = getDataTag("publicationName=1&tagName=1");
        assertEquals("1", tag.getName());
    }

    @Test
    void toTagConfigurationWithoutTagDescriptionShouldResortToDefault() throws DynConfigException {
        DataTag tag = getDataTag("publicationName=1&tagName=1");
        assertEquals("dynamically configured tag", tag.getDescription());
    }

    @Test
    void toTagConfigurationWithNonStandardAddressParameterShouldBeSet() throws DynConfigException {
        DataTag tag = getDataTag("publicationName=1&tagName=1&setTimeToLive=67");
        assertEquals(67, tag.getAddress().getTimeToLive());
    }

    @Test
    void toTagConfigurationWithDataTypeShouldSetType() throws DynConfigException {
        DataTag tag = getDataTag("publicationName=1&tagName=1&dataType=" + Exception.class.getName());
        assertEquals(Exception.class.getName(), tag.getDataType());
    }

    @Test
    void toTagConfigurationWithNonStandardTagParameterShouldBeSet() throws DynConfigException {
        DataTag tag = getDataTag("publicationName=1&tagName=1&minValue=67");
        assertEquals(67, tag.getMinValue());
    }

    @Test
    void toCommandTagConfigurationWithDataTypeShouldSetType() throws DynConfigException {
        CommandTag tag = getCommandTag("publicationName=1&tagName=1&dataType=" + Exception.class.getName());
        assertEquals(Exception.class.getName(), tag.getDataType());
    }

    @Test
    void toCommandTagConfigurationWithNonStandardTagParameterShouldBeSet() throws DynConfigException {
        CommandTag tag = getCommandTag("publicationName=1&tagName=1&minValue=67&setClientTimeout=5000");
        assertEquals(5000, tag.getClientTimeout());
    }

    @Test
    void strategyShouldMatchIfQueryObjectMatches() throws DynConfigException {
        for (String regex : Arrays.asList("noMatch", ".dip*", "publicationName=1", ".://*")) {
            assertQueryAndStrategyMatch(regex);
        }
    }

    CommandTag getCommandTag(String queries) throws DynConfigException {
        URI uri = URI.create("dip://host/path?tagType=COMMAND&" + queries);
        strategy = ITagConfigStrategy.of(uri);
        return strategy.prepareCommandTagConfigurations();
    }

    DataTag getDataTag(String queries) throws DynConfigException {
        URI uri = URI.create("dip://host/path?" + queries);
        strategy = ITagConfigStrategy.of(uri);
        return strategy.prepareDataTagConfigurations();
    }

    void assertQueryAndStrategyMatch(String regex) throws DynConfigException {
        URI uri = URI.create("dip://host/path?publicationName=x1");
        QueryObj queryObj = new QueryObj(uri, Collections.emptyList());
        strategy = ITagConfigStrategy.of(uri);
        assertEquals(queryObj.matches(regex), strategy.matches(regex));
    }
}