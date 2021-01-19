package cern.c2mon.client.core.configuration.dynamic;

import cern.c2mon.client.core.configuration.dynamic.query.QueryKey;
import cern.c2mon.client.core.configuration.dynamic.query.QueryObj;
import cern.c2mon.client.core.configuration.dynamic.strategy.TagConfigStrategy;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.address.impl.HardwareAddressImpl;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QueryObjTest {

    final URI uri = URI.create("opc.tcp://host:500/path?tagName=1");
    QueryObj queryObj;

    @Test
    void createQueryObjWithMissingKeyShouldThrowException() {
        List<QueryKey<?>> key = Collections.singletonList(new QueryKey<Integer>("missing", null, true));
        assertThrows(DynConfigException.class, () -> new QueryObj(uri, key));
    }

    @Test
    void createQueryObjWithValidKeyShouldHaveNoInfluence() throws DynConfigException {
        QueryObj expected = new QueryObj(uri, Collections.emptyList());
        List<QueryKey<?>> key = Collections.singletonList(new QueryKey<Integer>("tagName", null, true));
        queryObj = new QueryObj(uri, key);
        assertEquals(expected, queryObj);
    }

    @Test
    void tagNameShouldHaveADefault() throws DynConfigException {
        URI localUri = URI.create("opc.tcp://host:500/path");
        queryObj = new QueryObj(localUri, Collections.emptyList());
        assertEquals(localUri.toASCIIString(), queryObj.get(TagConfigStrategy.TAG_NAME).get(0));
    }

    @Test
    void getUriWithoutParamsShouldReturnUri() throws DynConfigException {
        queryObj = new QueryObj(uri, Collections.emptyList());
        assertEquals("opc.tcp://host:500/path", queryObj.getUriWithoutParams());
    }

    @Test
    void getUriWithoutParamsShouldReturnUriWithoutParams() throws DynConfigException {
        queryObj = new QueryObj(URI.create("opc.tcp://host:500/path"), Collections.emptyList());
        assertEquals("opc.tcp://host:500/path", queryObj.getUriWithoutParams());
    }

    @Test
    void mandatoryKeyForClassWithoutTargetShouldThrowError() throws DynConfigException {
        QueryKey<String> mode = new QueryKey<>("mode", "GET", true);
        mode.setVerifier(s -> s.equalsIgnoreCase("GET") || s.equalsIgnoreCase("POST"));

        URI uri = URI.create("opc.tcp://host:500/path?mode=GET");
        QueryObj queryObj = new QueryObj(uri, Collections.singletonList(mode));
        DataTag.CreateBuilder builder = DataTag
                .create("a", Object.class, new DataTagAddress(new HardwareAddressImpl()));
        assertThrows(DynConfigException.class, () -> queryObj.applyQueryPropertiesTo(builder));

    }

    @Test
    void mandatoryKeyForClassWithTargetShouldPassWork() throws DynConfigException {
        QueryKey<String> mode = new QueryKey<>("mode", "GET", true);
        mode.setVerifier(s -> s.equalsIgnoreCase("GET") || s.equalsIgnoreCase("POST"));
        mode.setTargetClass(DataTagAddress.class);

        URI uri = URI.create("opc.tcp://host:500/path?mode=GET");
        QueryObj queryObj = new QueryObj(uri, Collections.singletonList(mode));
        DataTag.CreateBuilder builder = DataTag
                .create("a", Object.class, new DataTagAddress(new HardwareAddressImpl()));

        queryObj.applyQueryPropertiesTo(builder);
    }
}