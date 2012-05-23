package ch.cern.tim.driver.jec.frames;

import org.junit.Before;
import org.junit.Test;

import static org.easymock.classextension.EasyMock.*;

import cern.tim.driver.common.EquipmentLogger;
import cern.tim.driver.common.IEquipmentMessageSender;
import cern.tim.shared.common.ConfigurationException;
import cern.tim.shared.common.datatag.DataTagAddress;
import cern.tim.shared.common.datatag.address.PLCHardwareAddress;
import cern.tim.shared.daq.datatag.SourceDataQuality;
import cern.tim.shared.daq.datatag.SourceDataTag;
import ch.cern.tim.driver.jec.JECMessageHandler;
import ch.cern.tim.driver.jec.PLCObjectFactory;
import ch.cern.tim.driver.jec.address.AnalogJECAddressSpace;
import ch.cern.tim.driver.jec.config.PLCConfiguration;
import ch.cern.tim.driver.jec.tools.JECConversionHelper;
import ch.cern.tim.jec.JECIndexOutOfRangeException;
import ch.cern.tim.jec.JECPFrames;
import ch.cern.tim.shared.datatag.address.impl.PLCHardwareAddressImpl;

public class AnalogDataProcessorTest {

    private AnalogDataProcessor<AnalogJECAddressSpace> analogDataProcessor;
    private PLCObjectFactory plcFactory;
    private IEquipmentMessageSender equipmentMessageSender;
    private SourceDataTag sourceDataTag;
    private PLCHardwareAddressImpl hardwareAddress;
    
    @Before
    public void setUp() throws InstantiationException, IllegalAccessException, ClassNotFoundException, ConfigurationException {
        EquipmentLogger equipmentLogger = new EquipmentLogger("asd", "asd", "asd");
        PLCConfiguration plcConfiguration = new PLCConfiguration();
        plcConfiguration.setProtocol("TestPLCDriver");
        plcFactory = new PLCObjectFactory(plcConfiguration);
        equipmentMessageSender = createMock(IEquipmentMessageSender.class);
        analogDataProcessor = new AnalogDataProcessor<AnalogJECAddressSpace>(1, new AnalogJECAddressSpace(), plcFactory, false, equipmentMessageSender, equipmentLogger);
        sourceDataTag = new SourceDataTag(1L, "asd", false);
        hardwareAddress = new PLCHardwareAddressImpl(PLCHardwareAddress.STRUCT_ANALOG, 0, 5, 0, 100, 1000, "TEST001");
        DataTagAddress dataTagAddress = new DataTagAddress(hardwareAddress);
        sourceDataTag.setAddress(dataTagAddress);
        sourceDataTag.update(10);
        sourceDataTag.setDataType("Integer");
        analogDataProcessor.addSourceDataTag(sourceDataTag);
        analogDataProcessor.initArrays();
        analogDataProcessor.setInitialValuesSent(true);
    }
    
    @Test
    public void testDetectChanges() throws JECIndexOutOfRangeException {
        JECPFrames jecpFrames = plcFactory.getSendFrame((byte) 1);
        jecpFrames.AddJECData(JECConversionHelper.convertJavaToPLCValue(1.0f, hardwareAddress), 0); // change in bit 5
        jecpFrames.SetDataStartNumber((short) 0);
        jecpFrames.SetDataOffset((short) 1);
        
        equipmentMessageSender.sendTagFiltered(sourceDataTag, 1, jecpFrames.GetJECCurrTimeMilliseconds());
        expectLastCall().andReturn(true);
        
        replay(equipmentMessageSender);
        analogDataProcessor.processJECPFrame(jecpFrames);
        verify(equipmentMessageSender);
    }
    
    @Test
    public void testSendTag() {
        equipmentMessageSender.sendTagFiltered(eq(sourceDataTag), eq(0), geq(System.currentTimeMillis()));
        expectLastCall().andReturn(true);
        
        replay(equipmentMessageSender);
        analogDataProcessor.sendTag(0, 5);
        sourceDataTag.invalidate(new SourceDataQuality(SourceDataQuality.DATA_UNAVAILABLE, JECMessageHandler.HIERARCHICAL_INVALIDATION_MESSAGE));
        analogDataProcessor.sendTag(0, 5);
        verify(equipmentMessageSender);
    }
    
    @Test
    public void testRevalidateTag() {
        equipmentMessageSender.sendTagFiltered(eq(sourceDataTag), eq(0), geq(System.currentTimeMillis()));
        expectLastCall().andReturn(true);
        equipmentMessageSender.sendTagFiltered(eq(sourceDataTag), eq(0), geq(System.currentTimeMillis()));
        expectLastCall().andReturn(true);
        
        replay(equipmentMessageSender);
        analogDataProcessor.revalidateTag(0, 5);
        sourceDataTag.getCurrentValue().setQuality(new SourceDataQuality(SourceDataQuality.DATA_UNAVAILABLE, JECMessageHandler.HIERARCHICAL_INVALIDATION_MESSAGE));
        analogDataProcessor.revalidateTag(0, 5);
        verify(equipmentMessageSender);
    }
}
