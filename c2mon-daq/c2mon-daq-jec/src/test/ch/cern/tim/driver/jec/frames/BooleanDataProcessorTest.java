package ch.cern.tim.driver.jec.frames;

import org.junit.Before;
import org.junit.Test;

import static org.easymock.classextension.EasyMock.*;

import cern.tim.driver.common.EquipmentLogger;
import cern.tim.driver.common.IEquipmentMessageSender;
import cern.tim.shared.common.ConfigurationException;
import cern.tim.shared.common.datatag.DataTagAddress;
import cern.tim.shared.common.datatag.address.HardwareAddress;
import cern.tim.shared.common.datatag.address.PLCHardwareAddress;
import cern.tim.shared.daq.datatag.SourceDataQuality;
import cern.tim.shared.daq.datatag.SourceDataTag;
import ch.cern.tim.driver.jec.JECMessageHandler;
import ch.cern.tim.driver.jec.PLCObjectFactory;
import ch.cern.tim.driver.jec.address.BooleanJECAdressSpace;
import ch.cern.tim.driver.jec.config.PLCConfiguration;
import ch.cern.tim.jec.JECIndexOutOfRangeException;
import ch.cern.tim.jec.JECPFrames;
import ch.cern.tim.shared.datatag.address.impl.PLCHardwareAddressImpl;

public class BooleanDataProcessorTest {

    private BooleanDataProcessor<BooleanJECAdressSpace> booleanDataProcessor;
    private PLCObjectFactory plcFactory;
    private IEquipmentMessageSender equipmentMessageSender;
    private SourceDataTag sourceDataTag;
    
    @Before
    public void setUp() throws InstantiationException, IllegalAccessException, ClassNotFoundException, ConfigurationException {
        EquipmentLogger equipmentLogger = new EquipmentLogger("asd", "asd", "asd");
        PLCConfiguration plcConfiguration = new PLCConfiguration();
        plcConfiguration.setProtocol("TestPLCDriver");
        plcFactory = new PLCObjectFactory(plcConfiguration);
        equipmentMessageSender = createMock(IEquipmentMessageSender.class);
        booleanDataProcessor = new BooleanDataProcessor<BooleanJECAdressSpace>(1, new BooleanJECAdressSpace(), plcFactory, equipmentMessageSender, equipmentLogger);
        sourceDataTag = new SourceDataTag(1L, "asd", false);
        HardwareAddress hardwareAddress = new PLCHardwareAddressImpl(PLCHardwareAddress.STRUCT_BOOLEAN, 0, 5, 0, 100, 1000, "TEST001");
        DataTagAddress dataTagAddress = new DataTagAddress(hardwareAddress);
        sourceDataTag.setAddress(dataTagAddress);
        sourceDataTag.update(true);
        booleanDataProcessor.addSourceDataTag(sourceDataTag);
        booleanDataProcessor.initArrays();
        booleanDataProcessor.setInitialValuesSent(0, true); //o.w. all values are sent when processing the first frame
    }
    
    @Test
    public void testDetectChanges() throws JECIndexOutOfRangeException {
        JECPFrames jecpFrames = plcFactory.getSendFrame((byte) 1);
        jecpFrames.AddJECData((short) 0x0020, 0); // change in bit 5
        jecpFrames.SetDataStartNumber((short) 0);
        jecpFrames.SetDataOffset((short) 1);
        
        equipmentMessageSender.sendTagFiltered(sourceDataTag, true, jecpFrames.GetJECCurrTimeMilliseconds());
        expectLastCall().andReturn(true);
        
        replay(equipmentMessageSender);
        booleanDataProcessor.processJECPFrame(jecpFrames);
        verify(equipmentMessageSender);
    }
    
    @Test
    public void testSendTag() {
        equipmentMessageSender.sendTagFiltered(eq(sourceDataTag), eq(false), geq(System.currentTimeMillis()));
        expectLastCall().andReturn(true);
        
        replay(equipmentMessageSender);
        booleanDataProcessor.sendTag(0, 5);
        sourceDataTag.invalidate(new SourceDataQuality(SourceDataQuality.DATA_UNAVAILABLE, JECMessageHandler.HIERARCHICAL_INVALIDATION_MESSAGE));
        booleanDataProcessor.sendTag(0, 5);
        verify(equipmentMessageSender);
    }
    
    @Test
    public void testRevalidateTag() {
        equipmentMessageSender.sendTagFiltered(eq(sourceDataTag), eq(false), geq(System.currentTimeMillis()));
        expectLastCall().andReturn(true);
        equipmentMessageSender.sendTagFiltered(eq(sourceDataTag), eq(false), geq(System.currentTimeMillis()));
        expectLastCall().andReturn(true);
        
        replay(equipmentMessageSender);
        booleanDataProcessor.revalidateTag(0, 5);
        sourceDataTag.getCurrentValue().setQuality(new SourceDataQuality(SourceDataQuality.DATA_UNAVAILABLE, JECMessageHandler.HIERARCHICAL_INVALIDATION_MESSAGE));
        booleanDataProcessor.revalidateTag(0, 5);
        verify(equipmentMessageSender);
    }
}
