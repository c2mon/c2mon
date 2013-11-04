package cern.c2mon.daq.opcua.connection.common.impl;

import static org.easymock.EasyMock.*;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.daq.opcua.connection.common.IOPCEndpoint;
import cern.c2mon.daq.opcua.connection.common.impl.AliveWriter;
import cern.c2mon.daq.common.EquipmentLogger;
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
        for (int k=0; k < 3; k++) {
          for (int i=0; i < Byte.MAX_VALUE; i++) {
            endpoint.write(hardwareAddress, Integer.valueOf(i));
          }
        }
        replay(endpoint);
        for (int k=0; k < 3; k++) {
          for (int i=0; i < Byte.MAX_VALUE; i++) {
            aliveWriter.run();
          }
        }
        verify(endpoint);
    }

}
