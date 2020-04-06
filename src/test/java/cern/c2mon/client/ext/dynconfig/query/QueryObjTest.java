package cern.c2mon.client.ext.dynconfig.query;

import cern.c2mon.client.ext.dynconfig.DynConfigException;
import cern.c2mon.client.ext.dynconfig.config.Protocols;
import cern.c2mon.client.ext.dynconfig.factories.ProtocolSpecificFactory;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QueryObjTest {


    @Test
    void testDefaultOpcUaProperties() throws DynConfigException {
        URI opcUa = URI.create("opc.tcp://host:500/path?itemName=x1&namespace=1");

        OpcUaQueryObj queryObj = (OpcUaQueryObj) ProtocolSpecificFactory.of(opcUa).createQueryObj();

        assertEquals(Protocols.PROTOCOL_OPCUA, queryObj.getProtocol());
        assertEquals(1, queryObj.getNamespace());
        assertEquals("opc.tcp://host:500/path", queryObj.getUri());
        assertEquals("x1", queryObj.getItemName());

        assertEquals(Object.class, queryObj.getDataType());
        assertEquals(queryObj.getItemName(), queryObj.getTagName());
        assertEquals("autoconfigured Tag", queryObj.getTagDescription());
    }
    @Test
    void testDefaultDipProperties() throws DynConfigException {
        URI dip = URI.create("dip://host:500/path?itemName=x1");

        DipQueryObj queryObj = (DipQueryObj) ProtocolSpecificFactory.of(dip).createQueryObj();

        assertEquals(Protocols.PROTOCOL_DIP, queryObj.getProtocol());
        assertEquals("dip://host:500/path", queryObj.getUri());
        assertEquals("x1", queryObj.getItemName());

        assertEquals(Object.class, queryObj.getDataType());
        assertEquals(queryObj.getItemName(), queryObj.getTagName());
        assertEquals("autoconfigured Tag", queryObj.getTagDescription());
    }
}