package cern.c2mon.driver.opcua.connection.common.impl;

import static org.easymock.EasyMock.*;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.driver.opcua.connection.common.IOPCEndpoint;
import cern.c2mon.driver.opcua.connection.common.impl.AliveWriter;
import cern.tim.driver.common.EquipmentLogger;
import cern.tim.shared.common.ConfigurationException;
import cern.tim.shared.common.datatag.DataTagAddress;
import cern.tim.shared.common.datatag.address.HardwareAddress;
import cern.tim.shared.common.datatag.address.OPCHardwareAddress;
import cern.tim.shared.daq.datatag.ISourceDataTag;
import cern.tim.shared.daq.datatag.SourceDataTag;
import ch.cern.tim.shared.datatag.address.impl.OPCHardwareAddressImpl;

public class AliveWriterTest {
    
    private IOPCEndpoint endpoint = createMock(IOPCEndpoint.class);
    private OPCHardwareAddress hardwareAddress;
    private ISourceDataTag targetTag;
    private AliveWriter aliveWriter;
        
    
    
    @Before
    public void setUp() throws ConfigurationException {
        hardwareAddress = new OPCHardwareAddressImpl("s");
        DataTagAddress address = new DataTagAddress(hardwareAddress);
        targetTag =
            new SourceDataTag(1L, "asd", true, (short) 0, "Integer", address);
        aliveWriter = new AliveWriter(endpoint, 1000L, targetTag, new EquipmentLogger("asd", "asd", "asd"));
    }
    @Test
    public void testRun() {
        endpoint.write(hardwareAddress, Integer.valueOf(0));
        endpoint.write(hardwareAddress, Integer.valueOf(1));
        endpoint.write(hardwareAddress, Integer.valueOf(2));
        replay(endpoint);
        aliveWriter.run();
        aliveWriter.run();
        aliveWriter.run();
        verify(endpoint);
    }

}
