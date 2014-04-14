package cern.c2mon.daq.common.conf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;

import cern.c2mon.daq.common.conf.core.EquipmentConfiguration;
import cern.c2mon.daq.common.conf.core.EquipmentConfigurationFactory;
import cern.c2mon.daq.common.conf.core.ProcessConfiguration;
import cern.c2mon.daq.common.conf.core.ProcessConfigurationLoader;
import cern.c2mon.daq.tools.processexceptions.ConfRejectedTypeException;
import cern.c2mon.daq.tools.processexceptions.ConfUnknownTypeException;
import cern.c2mon.shared.common.datatag.address.impl.PLCHardwareAddressImpl;
import cern.c2mon.shared.daq.datatag.SourceDataTag;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:resources/daq-core-service.xml"})
public class ProcessConfigurationLoaderTest {
    
    @Autowired
    private ProcessConfigurationLoader processConfigurationLoader;
    
    private static final String PROCESS_CONFIGURATION_XML = "ProcessConfiguration.xml";
    
    private static final String PROCESS_CONFIGURATION_UNKNOWN_TYPE_XML = "UnknownTypeProcessConfiguration.xml";
    
    private static final String PROCESS_CONFIGURATION_REJECTED_XML = "RejectedProcessConfiguration.xml";
    
    private static final Long PROCESS_PIK = 12345L;
    
    private static final String PROCESS_NAME = "P_NACHO";
    
    private String processHostName;
    
    @Value("${c2mon.jms.daq.queue.trunk}") 
    private String jmsDaqQueueTrunk;
    
    @Before
    public void setUp() {
//        processConfigurationLoader = new ProcessConfigurationLoader();
        processConfigurationLoader.setEquipmentConfigurationFactory(EquipmentConfigurationFactory.getInstance());
        
        try {
          this.processHostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
          this.processHostName = "NOHOST";
        }
    }
    
    @Test
    public void testCreateProcessConfiguration() 
            throws ConfUnknownTypeException, ConfRejectedTypeException {
        ProcessConfiguration processConfiguration = getProcessConfiguration(PROCESS_CONFIGURATION_XML);
        
        assertEquals(4092L, processConfiguration.getProcessID().longValue());
        assertEquals(this.jmsDaqQueueTrunk + ".command."+this.processHostName+"."+PROCESS_NAME+"."+PROCESS_PIK, processConfiguration.getJmsDaqCommandQueue());
        assertEquals(100730, processConfiguration.getAliveTagID());
        assertEquals(60000, processConfiguration.getAliveInterval());
        assertEquals(100, processConfiguration.getMaxMessageSize());
        assertEquals(1000, processConfiguration.getMaxMessageDelay());
        
        Map<Long, EquipmentConfiguration> equipmentMap = 
            processConfiguration.getEquipmentConfigurations();
        assertEquals(2, equipmentMap.values().size());
        
        EquipmentConfiguration equipmentConfiguration1 = equipmentMap.get(1L);
        assertEquals("cern.c2mon.daq.testhandler.TestMessageHandler", 
                equipmentConfiguration1.getHandlerClassName());
        assertEquals(47014L, equipmentConfiguration1.getCommFaultTagId());
        assertFalse(equipmentConfiguration1.getCommFaultTagValue());
        assertEquals(47321L, equipmentConfiguration1.getAliveTagId());
        assertEquals(120000L, equipmentConfiguration1.getAliveTagInterval());
        assertEquals("interval=100;eventProb=0.8;inRangeProb=1.0;outDeadBandProb=0.0;outDeadBandProb=0.0;switchProb=0.5;startIn=0.01;aliveInterval=30000", 
                equipmentConfiguration1.getAddress());
        
        Map<Long, Boolean> subEq1 = equipmentConfiguration1.getSubEqCommFaultValues();
        assertEquals(2, subEq1.values().size());
        assertTrue(subEq1.get(1L));
        assertFalse(subEq1.get(2L));
        
        Map<Long, SourceDataTag> sourceDataTagMap1 = equipmentConfiguration1.getDataTags();
        assertEquals(2, sourceDataTagMap1.size());
        SourceDataTag sourceDataTag1 = sourceDataTagMap1.get(1L);
        assertEquals("CP.PRE.AIRH4STP887:DEFAUT_PROCESSEUR", sourceDataTag1.getName());
        assertFalse(sourceDataTag1.isControlTag());
        assertEquals("Boolean", sourceDataTag1.getDataType());
        assertEquals(9999999, sourceDataTag1.getAddress().getTimeToLive());
        assertEquals(7, sourceDataTag1.getAddress().getPriority());
        assertTrue(sourceDataTag1.getAddress().isGuaranteedDelivery());
        
        PLCHardwareAddressImpl hardwareAddress = 
            (PLCHardwareAddressImpl)sourceDataTag1.getHardwareAddress();
        assertEquals(5, hardwareAddress.getBlockType());
        assertEquals(0, hardwareAddress.getWordId());
        assertEquals(1, hardwareAddress.getBitId());
        assertTrue(0.0 - hardwareAddress.getPhysicalMinVal() < 0.0000000001);
        assertTrue(0.0 - hardwareAddress.getPhysicMaxVal() < 0.0000000001);
        assertEquals(1, hardwareAddress.getResolutionFactor());
        assertEquals(0, hardwareAddress.getCommandPulseLength());
        assertEquals("INT999", hardwareAddress.getNativeAddress());
        
    }
    
    @Test
    public void testConfigUnknownException() throws ConfRejectedTypeException {
        try {
            getProcessConfiguration(PROCESS_CONFIGURATION_UNKNOWN_TYPE_XML);
            fail("No ConfUnknownException thrown.");
        } catch (ConfUnknownTypeException e) {
//            e.printStackTrace();
        }
        
    }
    
    @Test
    public void testConfigRejectedException() throws ConfUnknownTypeException {
        try {
            getProcessConfiguration(PROCESS_CONFIGURATION_REJECTED_XML);
            fail("No ConfRejectedTypeException thrown.");
        } catch (ConfRejectedTypeException e) {
//            e.printStackTrace();
        }
    }
    
    private ProcessConfiguration getProcessConfiguration(String name) throws ConfUnknownTypeException, ConfRejectedTypeException {
        String path = ProcessConfigurationLoaderTest.class.getResource(name).getPath();
        Document pconfDocument = processConfigurationLoader.fromFiletoDOC(path);
        ProcessConfiguration processConfiguration = processConfigurationLoader.createProcessConfiguration(PROCESS_NAME, PROCESS_PIK, pconfDocument, true);
        return processConfiguration;
    }
}
