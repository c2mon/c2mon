package cern.c2mon.daq.opcua.connection.common.impl;


import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.daq.common.conf.core.RunOptions;
import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.common.logger.EquipmentLoggerFactory;
import cern.c2mon.daq.opcua.connection.common.IOPCEndpoint;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.address.OPCHardwareAddress;
import cern.c2mon.shared.daq.datatag.ISourceDataTag;
import cern.c2mon.shared.daq.datatag.SourceDataTag;
import cern.c2mon.shared.common.datatag.address.impl.OPCHardwareAddressImpl;

public class AliveWriterTest {
    
    private OPCHardwareAddress hardwareAddress;
    private ISourceDataTag targetTag;
    private AliveWriter aliveWriter;
    
    private EquipmentLogger equipmentLogger;
    
    /**
     * Mocks
     */
    private EquipmentLoggerFactory equipmentLoggerFactoryMock; 
    private IOPCEndpoint endpointMock;
        
    
    
    @Before
    public void setUp() throws ConfigurationException {
        this.endpointMock = EasyMock.createMock(IOPCEndpoint.class);
        this.hardwareAddress = new OPCHardwareAddressImpl("s");
        DataTagAddress address = new DataTagAddress(hardwareAddress);
        this.targetTag =new SourceDataTag(1L, "asd", true, (short) 0, "Integer", address);
        
        this.equipmentLogger = new EquipmentLogger("Test Logger", "Test Logger", "AliveWriter");
        this.equipmentLoggerFactoryMock = EasyMock.createMock(EquipmentLoggerFactory.class);
    }
    
    @Test
    public void testRun() {
        
        EasyMock.expect(this.equipmentLoggerFactoryMock.getEquipmentLogger(AliveWriter.class)).andReturn(this.equipmentLogger).times(1);
        
        for (int k=0; k < 3; k++) {
            for (int i=0; i < Byte.MAX_VALUE; i++) {
              this.endpointMock.write(this.hardwareAddress, Integer.valueOf(i));
            }
          }
        
        EasyMock.replay(this.endpointMock, this.equipmentLoggerFactoryMock);
       
        this.aliveWriter = new AliveWriter(this.endpointMock, 1000L, this.targetTag, this.equipmentLoggerFactoryMock);
        
        
        for (int k=0; k < 3; k++) {
          for (int i=0; i < Byte.MAX_VALUE; i++) {
            this.aliveWriter.run();
          }
        }
        
        EasyMock.verify(this.endpointMock);
    }

}
