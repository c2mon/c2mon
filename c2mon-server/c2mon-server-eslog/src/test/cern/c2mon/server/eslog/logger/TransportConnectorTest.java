package cern.c2mon.server.eslog.logger;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.UnknownHostException;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Test the entire functionality of the node.
 * @author Alban Marguet.
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/server/eslog/config/server-eslog.xml"})
public class TransportConnectorTest {
    DataUtils dataUtils;
    TransportConnector connector;

    @Before
    public void setup() {
        dataUtils = new DataUtils();
        connector = new TransportConnector(dataUtils);
    }

    @Test
    public void testInit() {
        connector.init();
        assertEquals(connector.getClient(), dataUtils.getClient());
    }

//    @Test(expected =  UnknownHostException.class)
//    public void testUnknownHostException() {
//    }

//    @Test
//    public void testInitializeIndexes() {
//        Set<String> result = dataUtils.initializeIndexes();
//        verify(connector).getIndices();
//        assertEquals(indices, result);
//    }

//    @Test
//    public void testInitializeAliases() {
//        Set<String> result = dataUtils.initializeAliases();
//        verify(connector).getAliases();
//        assertEquals(aliases, result);
//    }

//    @Test
//    public void testInitializeTypes() {
//        Set<String> result = dataUtils.initializeTypes();
//        verify(connector).getTypes();
//        assertEquals(types, result);
//    }
}
