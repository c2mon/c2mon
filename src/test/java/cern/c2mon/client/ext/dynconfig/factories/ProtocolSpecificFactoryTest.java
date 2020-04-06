package cern.c2mon.client.ext.dynconfig.factories;

import cern.c2mon.client.ext.dynconfig.DynConfigException;
import cern.c2mon.client.ext.dynconfig.factories.DipFactory;
import cern.c2mon.client.ext.dynconfig.factories.OpcUaFactory;
import cern.c2mon.client.ext.dynconfig.factories.ProtocolSpecificFactory;
import cern.c2mon.client.ext.dynconfig.query.DipQueryObj;
import cern.c2mon.client.ext.dynconfig.query.IQueryObj;
import cern.c2mon.client.ext.dynconfig.query.OpcUaQueryObj;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class ProtocolSpecificFactoryTest {

    @Test
    void validDipUriShouldReturnDynConfigQueryObj() throws DynConfigException {
        URI uri = URI.create("dip://dip/path?itemName=x1");
        IQueryObj queryObj = ProtocolSpecificFactory.of(uri).createQueryObj();
        assertTrue(queryObj instanceof DipQueryObj);
    }


    @Test
    void validOpcUaUriShouldReturnAppropriateQueryObj() throws DynConfigException {
        URI uri = URI.create("opc.tcp://host:500/path?itemName=x1&namespace=1");
        IQueryObj queryObj = ProtocolSpecificFactory.of(uri).createQueryObj();
        assertTrue(queryObj instanceof OpcUaQueryObj);
    }

    @Test
    void opcUaUriMissingNamespaceShouldThrowAppropriateError() {
        URI uri = URI.create("opc.tcp://host:500/path?itemName=x1");
        DynConfigException e = assertThrows(DynConfigException.class,
                () -> new OpcUaFactory(uri).createQueryObj());
        assertTrue(e.getMessage().contains("namespace"));
    }

    @Test
    void opcUaUriMissingItemNameShouldThrowAppropriateError() {
        URI uri = URI.create("opc.tcp://host:500/path?namespace=1");
        DynConfigException e = assertThrows(DynConfigException.class,
                () -> new OpcUaFactory(uri).createQueryObj());
        assertTrue(e.getMessage().contains("itemName"));
    }

    @Test
    void uriWithInvalidPropertiesShouldIgnoreThem() throws DynConfigException {
        URI uri = URI.create("dip://dip/path?itemName=x1?x=xxx");
        IQueryObj queryObj = new DipFactory(uri).createQueryObj();
        assertTrue(queryObj instanceof DipQueryObj);
    }

    @Test
    void uriWithoutSchemeShouldThrowException() {
        URI uri = URI.create("dip/path?itemName=x1");
        assertThrows(DynConfigException.class,
                () -> new OpcUaFactory(uri).createQueryObj());
    }

    @Test
    void opcUaStringShouldReturnQueryObjWithGivenProperties() throws DynConfigException {
        URI opcUa = URI.create("opc.tcp://host:500/path?itemName=x1&namespace=1");
        OpcUaQueryObj expected = new OpcUaQueryObj(opcUa);
        OpcUaQueryObj queryObj = (OpcUaQueryObj) ProtocolSpecificFactory.of(opcUa).createQueryObj();
        assertEquals(expected, queryObj);
    }

    @Test
    void invalidStringShouldThrowError() {
        URI invalid = URI.create("xx://xxxx");
        assertThrows(DynConfigException.class, () -> ProtocolSpecificFactory.of(invalid));
    }
}